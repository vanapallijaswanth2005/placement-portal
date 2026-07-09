package com.example.placementportal.controller;

import com.example.placementportal.entity.JobApplication;
import com.example.placementportal.entity.ApplicationStatus;
import com.example.placementportal.entity.Student;
import com.example.placementportal.entity.Job;
import com.example.placementportal.entity.Recruiter;
import com.example.placementportal.service.JobApplicationService;
import com.example.placementportal.service.StudentService;
import com.example.placementportal.service.EmailService;
import com.example.placementportal.service.JobService;
import com.example.placementportal.service.RecruiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/apply")
public class ApplicationController {

    private final JobApplicationService service;
    private final StudentService studentService;
    private final RecruiterService recruiterService;
    private final EmailService emailService;
    private final com.example.placementportal.service.SseNotificationService sseNotificationService;
    private final com.example.placementportal.service.CsvExportService csvExportService;

    @Autowired
    private JobService jobService;

    public ApplicationController(JobApplicationService service, 
                                 StudentService studentService,
                                 RecruiterService recruiterService,
                                 EmailService emailService,
                                 com.example.placementportal.service.SseNotificationService sseNotificationService,
                                 com.example.placementportal.service.CsvExportService csvExportService) {
        this.service = service;
        this.studentService = studentService;
        this.recruiterService = recruiterService;
        this.emailService = emailService;
        this.sseNotificationService = sseNotificationService;
        this.csvExportService = csvExportService;
    }

    // 🎓 STUDENT: Apply for job
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    public JobApplication applyJob(@RequestParam Long jobId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentService.getStudentByUsername(username);
        if (student == null) {
            throw new RuntimeException("Please create your student profile first before applying!");
        }

        Job job = jobService.getJobById(jobId);

        if (service.hasAlreadyApplied(student.getId(), jobId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already applied for this job");
        }

        JobApplication application = new JobApplication();
        application.setStudent(student);
        application.setJob(job);
        application.setStatus(ApplicationStatus.APPLIED);

        JobApplication saved = service.save(application);

        String recruiterEmail = job.getRecruiter() != null ? job.getRecruiter().getEmail() : null;
        String jobTitle = job.getTitle();
        String jobCompany = job.getCompany();
        String studentName = student.getName();

        // Send email notification to recruiter asynchronously
        try {
            if (recruiterEmail != null) {
                emailService.sendApplicationStatusUpdate(
                    recruiterEmail,
                    "Recruiter",
                    jobTitle,
                    jobCompany,
                    "New application from " + studentName,
                    null
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return saved;
    }

    // 🎓 STUDENT: Get own applications
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my")
    public List<JobApplication> getMyApplications() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentService.getStudentByUsername(username);
        if (student == null) {
            return java.util.Collections.emptyList();
        }
        return service.getByStudentId(student.getId());
    }

    // 🔍 RECRUITER: Get application by ID
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/{id:\\d+}")
    public JobApplication getApplicationById(@PathVariable Long id) {
        Recruiter recruiter = getCurrentRecruiter();
        return service.getByIdForRecruiter(id, recruiter.getId());
    }

    // 🔍 RECRUITER: Get applications for a specific job
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/job/{jobId}")
    public List<JobApplication> getApplicationsByJob(@PathVariable Long jobId) {
        Recruiter recruiter = getCurrentRecruiter();
        return service.getByJobIdForRecruiter(jobId, recruiter.getId());
    }

    // 📊 RECRUITER or ADMIN: View all applications
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @GetMapping("/all")
    public org.springframework.data.domain.Page<JobApplication> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
        return service.getAll(pageable);
    }

    // 📋 RECRUITER: View applications for their posted jobs
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/recruiter/my")
    public org.springframework.data.domain.Page<JobApplication> getMyRecruiterApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Recruiter recruiter = recruiterService.getRecruiterByUsername(username);
        if (recruiter == null) {
            return org.springframework.data.domain.Page.empty();
        }
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
        return service.getByRecruiterId(recruiter.getId(), pageable);
    }

    // 📋 RECRUITER: Update application status
    @PreAuthorize("hasRole('RECRUITER')")
    @PutMapping("/{id}/status")
    @org.springframework.transaction.annotation.Transactional
    public JobApplication updateStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime interviewDate) {

        Recruiter recruiter = getCurrentRecruiter();
        JobApplication updatedApp = service.updateStatusForRecruiter(id, status, recruiter.getId(), interviewDate);
        Student student = updatedApp.getStudent();
        Job job = updatedApp.getJob();
        String studentEmail = student != null ? studentService.getStudentUserEmail(student.getId()) : null;
        String studentName = student != null ? student.getName() : null;

        // Send status update email to student asynchronously
        try {
            if (studentEmail != null && job != null) {
                    emailService.sendApplicationStatusUpdate(
                        studentEmail,
                        studentName,
                        job.getTitle(),
                        job.getCompany(),
                        status.name(),
                        interviewDate
                    );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Trigger in-app notification
        if (student != null && student.getUser() != null) {
            try {
                String msg = "Status updated to " + status.name() + " for " + job.getTitle() + " at " + job.getCompany();
                sseNotificationService.sendNotification(student.getUser().getUsername(), msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return updatedApp;
    }


    // 📊 RECRUITER or ADMIN: Export applicants for a specific job to CSV
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @GetMapping("/job/{jobId}/export")
    public void exportApplicantsForJob(@PathVariable Long jobId, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        List<JobApplication> applications;
        
        // If it's a recruiter, verify ownership
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RECRUITER"))) {
            Recruiter recruiter = getCurrentRecruiter();
            applications = service.getByJobIdForRecruiter(jobId, recruiter.getId());
        } else {
            // Admin can export for any job
            applications = service.getByJobId(jobId);
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"applicants_job_" + jobId + ".csv\"");
        
        csvExportService.exportApplicationsToCsv(response.getWriter(), applications);
    }

    private Recruiter getCurrentRecruiter() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Recruiter recruiter = recruiterService.getRecruiterByUsername(username);
        if (recruiter == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please complete your recruiter profile first");
        }
        return recruiter;
    }
}

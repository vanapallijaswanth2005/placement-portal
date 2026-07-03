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

    @Autowired
    private JobApplicationService service;

    @Autowired
    private StudentService studentService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JobService jobService;

    @Autowired
    private RecruiterService recruiterService;

    // 🎓 STUDENT: Apply for job
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
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
        new Thread(() -> {
            try {
                if (recruiterEmail != null) {
                    emailService.sendApplicationStatusUpdate(
                        recruiterEmail,
                        "Recruiter",
                        jobTitle,
                        jobCompany,
                        "New application from " + studentName
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

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

    // 🔍 RECRUITER: Get all applications for my jobs
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/recruiter/my")
    public List<JobApplication> getMyRecruiterApplications() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Recruiter recruiter = recruiterService.getRecruiterByUsername(username);
        if (recruiter == null) {
            return java.util.Collections.emptyList();
        }
        return service.getByRecruiterId(recruiter.getId());
    }

    // 📋 RECRUITER: Update application status
    @PreAuthorize("hasRole('RECRUITER')")
    @PutMapping("/{id}/status")
    public JobApplication updateStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status) {

        Recruiter recruiter = getCurrentRecruiter();
        JobApplication updatedApp = service.updateStatusForRecruiter(id, status, recruiter.getId());
        Student student = updatedApp.getStudent();
        Job job = updatedApp.getJob();
        String studentEmail = student != null ? studentService.getStudentUserEmail(student.getId()) : null;
        String studentName = student != null ? student.getName() : null;

        // Send status update email to student asynchronously
        new Thread(() -> {
            try {
                if (studentEmail != null && job != null) {
                        emailService.sendApplicationStatusUpdate(
                            studentEmail,
                            studentName,
                            job.getTitle(),
                            job.getCompany(),
                            status.name()
                        );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return updatedApp;
    }

    // 📊 RECRUITER or ADMIN: View all applications
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @GetMapping("/all")
    public List<JobApplication> getAllApplications() {
        return service.getAll();
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

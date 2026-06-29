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
import com.example.placementportal.repository.UserRepository;
import com.example.placementportal.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Autowired
    private UserRepository userRepository;

    // 🎓 STUDENT: Apply for job
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
    public JobApplication applyJob(@RequestParam Long jobId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentService.getStudentByName(username);
        if (student == null) {
            throw new RuntimeException("Please create your student profile first before applying!");
        }

        Job job = jobService.getJobById(jobId);

        JobApplication application = new JobApplication();
        application.setStudent(student);
        application.setJob(job);
        application.setStatus(ApplicationStatus.APPLIED);

        JobApplication saved = service.save(application);

        // Send email notification to recruiter asynchronously
        new Thread(() -> {
            try {
                Recruiter recruiter = job.getRecruiter();
                if (recruiter != null && recruiter.getEmail() != null) {
                    emailService.sendApplicationStatusUpdate(
                        recruiter.getEmail(),
                        "Recruiter",
                        job.getTitle(),
                        job.getCompany(),
                        "New application from " + student.getName()
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
        Student student = studentService.getStudentByName(username);
        if (student == null) {
            return java.util.Collections.emptyList();
        }
        return service.getByStudentId(student.getId());
    }

    // 🔍 RECRUITER: Get application by ID
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/{id:\\d+}")
    public JobApplication getApplicationById(@PathVariable Long id) {
        return service.getById(id);
    }

    // 🔍 RECRUITER: Get applications for a specific job
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/job/{jobId}")
    public List<JobApplication> getApplicationsByJob(@PathVariable Long jobId) {
        return service.getByJobId(jobId);
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

        JobApplication app = service.getById(id);
        app.setStatus(status);
        JobApplication updatedApp = service.save(app);

        // Send status update email to student asynchronously
        new Thread(() -> {
            try {
                Student student = app.getStudent();
                if (student != null) {
                    User user = userRepository.findByUsername(student.getName()).orElse(null);
                    Job job = app.getJob();

                    if (user != null && job != null) {
                        emailService.sendApplicationStatusUpdate(
                            user.getEmail(),
                            student.getName(),
                            job.getTitle(),
                            job.getCompany(),
                            status.name()
                        );
                    }
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
}
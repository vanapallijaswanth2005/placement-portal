package com.example.placementportal.controller;

import com.example.placementportal.entity.ApplicationStatus;
import com.example.placementportal.entity.JobApplication;
import com.example.placementportal.entity.Recruiter;
import com.example.placementportal.entity.Student;
import com.example.placementportal.service.JobApplicationService;
import com.example.placementportal.service.JobService;
import com.example.placementportal.service.RecruiterService;
import com.example.placementportal.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private JobApplicationService jobApplicationService;

    @Autowired
    private JobService jobService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private RecruiterService recruiterService;

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student")
    public Map<String, Object> getStudentDashboard() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentService.getStudentByUsername(username);

        Map<String, Object> stats = new HashMap<>();
        if (student == null) {
            stats.put("error", "Student profile not found");
            return stats;
        }

        List<JobApplication> applications = jobApplicationService.getByStudentId(student.getId());

        stats.put("totalApplications", applications.size());
        stats.put("statusCounts", applications.stream()
                .collect(Collectors.groupingBy(app -> app.getStatus().name(), Collectors.counting())));
        
        return stats;
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/recruiter")
    public Map<String, Object> getRecruiterDashboard() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Recruiter recruiter = recruiterService.getRecruiterByUsername(username);

        Map<String, Object> stats = new HashMap<>();
        if (recruiter == null) {
            stats.put("error", "Recruiter profile not found");
            return stats;
        }

        List<JobApplication> applications = jobApplicationService.getByRecruiterId(recruiter.getId());
        long activeJobs = jobService.getJobsByRecruiter(recruiter.getId()).size();

        stats.put("totalActiveJobs", activeJobs);
        stats.put("totalApplicationsReceived", applications.size());
        stats.put("applicationStatusCounts", applications.stream()
                .collect(Collectors.groupingBy(app -> app.getStatus().name(), Collectors.counting())));
                
        return stats;
    }
}

package com.example.placementportal.controller;

import com.example.placementportal.entity.Job;
import com.example.placementportal.entity.Recruiter;
import com.example.placementportal.service.JobService;
import com.example.placementportal.service.RecruiterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private RecruiterService recruiterService;

    // ✅ PUBLIC (any logged-in user can view jobs)
    @GetMapping
    public org.springframework.data.domain.Page<Job> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return jobService.getAllJobs(pageable);
    }

    // 🔍 Search with pagination and sorting
    @GetMapping("/search")
    public Page<Job> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return jobService.searchJobs(title, company, location, skills, minSalary, maxSalary, pageable);
    }

    @GetMapping("/{id:\\d+}")
    public Job getJobById(@PathVariable Long id) {
        return jobService.getJobById(id);
    }

    // 🔒 RECRUITER: Get my own jobs
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/my")
    public org.springframework.data.domain.Page<Job> getMyJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Recruiter recruiter = getCurrentRecruiter();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return jobService.getJobsByRecruiter(recruiter.getId(), pageable);
    }

    // 🔒 ONLY RECRUITER can create job (linked to their profile)
    @PreAuthorize("hasRole('RECRUITER')")
    @PostMapping
    public Job createJob(@Valid @RequestBody Job job) {
        Recruiter recruiter = getApprovedRecruiter();
        return jobService.saveJob(job, recruiter);
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @PutMapping("/{id}")
    public Job updateJob(
            @PathVariable Long id,
            @Valid @RequestBody Job job) {
        Recruiter recruiter = getApprovedRecruiter();
        return jobService.updateJob(id, job, recruiter);
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @DeleteMapping("/{id}")
    public void deleteJob(@PathVariable Long id) {
        Recruiter recruiter = getApprovedRecruiter();
        jobService.deleteJob(id, recruiter);
    }

    private Recruiter getCurrentRecruiter() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Recruiter recruiter = recruiterService.getRecruiterByUsername(username);
        if (recruiter == null) {
            throw new RuntimeException("Please create your recruiter profile first");
        }
        return recruiter;
    }

    private Recruiter getApprovedRecruiter() {
        Recruiter recruiter = getCurrentRecruiter();
        recruiterService.assertRecruiterApproved(recruiter);
        return recruiter;
    }
}
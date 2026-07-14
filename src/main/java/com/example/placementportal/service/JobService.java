package com.example.placementportal.service;

import com.example.placementportal.entity.Job;
import com.example.placementportal.entity.Recruiter;
import com.example.placementportal.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    @Autowired
    private JobRepository repo;

    @Cacheable("jobs")
    public List<Job> getAllJobs() {
        return repo.findAll();
    }

    @Cacheable("jobs")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<Job> getAllJobs(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Job getJobById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
    }

    public List<Job> getJobsByRecruiter(Long recruiterId) {
        return repo.findByRecruiterId(recruiterId);
    }

    public Page<Job> getJobsByRecruiter(Long recruiterId, Pageable pageable) {
        return repo.findByRecruiterId(recruiterId, pageable);
    }

    @CacheEvict(value = "jobs", allEntries = true)
    public Job saveJob(Job job, Recruiter recruiter) {
        if (job.getLastDate() != null && job.getLastDate().isBefore(java.time.LocalDate.now())) {
            throw new RuntimeException("Job deadline cannot be set to a past date");
        }
        job.setRecruiter(recruiter);
        return repo.save(job);
    }

    @CacheEvict(value = "jobs", allEntries = true)
    public Job updateJob(Long id, Job updatedJob, Recruiter recruiter) {
        Job existingJob = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

        // Ownership check
        if (existingJob.getRecruiter() == null || !existingJob.getRecruiter().getId().equals(recruiter.getId())) {
            throw new RuntimeException("You can only edit your own jobs");
        }

        // Validate that if they are changing the deadline, it's not set to the past
        if (updatedJob.getLastDate() != null && updatedJob.getLastDate().isBefore(java.time.LocalDate.now())) {
            throw new RuntimeException("Job deadline cannot be set to a past date");
        }

        existingJob.setTitle(updatedJob.getTitle());
        existingJob.setCompany(updatedJob.getCompany());
        existingJob.setSalary(updatedJob.getSalary());
        existingJob.setDescription(updatedJob.getDescription());
        existingJob.setLocation(updatedJob.getLocation());
        existingJob.setExperience(updatedJob.getExperience());
        existingJob.setJobType(updatedJob.getJobType());
        existingJob.setLastDate(updatedJob.getLastDate());
        existingJob.setSkills(updatedJob.getSkills());
        existingJob.setEligibilityCriteria(updatedJob.getEligibilityCriteria());

        return repo.save(existingJob);
    }

    @CacheEvict(value = "jobs", allEntries = true)
    public void deleteJob(Long id, Recruiter recruiter) {
        Job job = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

        // Ownership check
        if (job.getRecruiter() == null || !job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new RuntimeException("You can only delete your own jobs");
        }

        repo.delete(job);
    }

    // Admin can delete any job
    @CacheEvict(value = "jobs", allEntries = true)
    public void deleteJobAdmin(Long id) {
        Job job = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
        repo.delete(job);
    }

    @Cacheable("jobs")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<Job> searchJobs(String title, String company, String location,
                                 String skills, Double minSalary, Double maxSalary,
                                 Pageable pageable) {
        return repo.searchJobs(title, company, location, skills, minSalary, maxSalary, pageable);
    }
}
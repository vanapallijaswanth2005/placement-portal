package com.example.placementportal.service;

import com.example.placementportal.entity.Job;
import com.example.placementportal.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {

    @Autowired
    private JobRepository repo;

    public List<Job> getAllJobs() {
        return repo.findAll();
    }

    public Job getJobById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
    }

    public Job saveJob(Job job) {
        return repo.save(job);
    }

    public Job updateJob(Long id, Job updatedJob) {

        Job existingJob = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

        existingJob.setTitle(updatedJob.getTitle());
        existingJob.setCompany(updatedJob.getCompany());
        existingJob.setSalary(updatedJob.getSalary());

        return repo.save(existingJob);
    }

    public void deleteJob(Long id) {

        Job job = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

        repo.delete(job);
    }
}
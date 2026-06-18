package com.example.placementportal.service;

import com.example.placementportal.entity.JobApplication;
import com.example.placementportal.repository.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobApplicationService {



    @Autowired
    private JobApplicationRepository repo;

    public JobApplication applyJob(JobApplication application) {
        application.setStatus("Applied");
        return repo.save(application);
    }
}
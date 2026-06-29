package com.example.placementportal.service;

import com.example.placementportal.entity.JobApplication;
import com.example.placementportal.entity.ApplicationStatus;
import com.example.placementportal.repository.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobApplicationService {

    @Autowired
    private JobApplicationRepository repo;

    // 🔍 Get application by id
    public JobApplication getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Application not found with id: " + id));
    }

    // 💾 Save application
    public JobApplication save(JobApplication application) {
        return repo.save(application);
    }

    // 📊 Get all applications
    public List<JobApplication> getAll() {
        return repo.findAll();
    }

    // 🎓 Get applications by student ID
    public List<JobApplication> getByStudentId(Long studentId) {
        return repo.findByStudentId(studentId);
    }

    // 🔍 Get applications by job ID
    public List<JobApplication> getByJobId(Long jobId) {
        return repo.findByJobId(jobId);
    }

    // 🔍 Get applications for all jobs owned by a recruiter
    public List<JobApplication> getByRecruiterId(Long recruiterId) {
        return repo.findByJobRecruiterId(recruiterId);
    }

    // 📊 Count by status
    public long countByStatus(ApplicationStatus status) {
        return repo.countByStatus(status);
    }
}
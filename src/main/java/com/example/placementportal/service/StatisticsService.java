package com.example.placementportal.service;

import com.example.placementportal.entity.ApplicationStatus;
import com.example.placementportal.repository.JobApplicationRepository;
import com.example.placementportal.repository.JobRepository;
import com.example.placementportal.repository.StudentRepository;
import com.example.placementportal.repository.RecruiterRepository;
import com.example.placementportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsService {

    @Autowired
    private JobRepository jobRepo;

    @Autowired
    private StudentRepository studentRepo;

    @Autowired
    private RecruiterRepository recruiterRepo;

    @Autowired
    private JobApplicationRepository applicationRepo;

    @Autowired
    private UserRepository userRepo;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalJobs", jobRepo.count());
        stats.put("totalStudents", studentRepo.count());
        stats.put("totalRecruiters", recruiterRepo.count());
        stats.put("totalApplications", applicationRepo.count());
        stats.put("totalUsers", userRepo.count());
        stats.put("applied", applicationRepo.countByStatus(ApplicationStatus.APPLIED));
        stats.put("underReview", applicationRepo.countByStatus(ApplicationStatus.UNDER_REVIEW));
        stats.put("interview", applicationRepo.countByStatus(ApplicationStatus.INTERVIEW));
        stats.put("selected", applicationRepo.countByStatus(ApplicationStatus.SELECTED));
        stats.put("rejected", applicationRepo.countByStatus(ApplicationStatus.REJECTED));
        return stats;
    }
}

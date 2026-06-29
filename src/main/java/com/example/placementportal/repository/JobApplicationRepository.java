package com.example.placementportal.repository;

import com.example.placementportal.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByStudentId(Long studentId);
    List<JobApplication> findByJobId(Long jobId);
    List<JobApplication> findByJobRecruiterId(Long recruiterId);
    boolean existsByStudentIdAndJobId(Long studentId, Long jobId);
    long countByStatus(com.example.placementportal.entity.ApplicationStatus status);
}
package com.example.placementportal.service;

import com.example.placementportal.entity.JobApplication;
import com.example.placementportal.entity.ApplicationStatus;
import com.example.placementportal.repository.JobApplicationRepository;
import com.example.placementportal.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class JobApplicationService {

    @Autowired
    private JobApplicationRepository repo;

    @Autowired
    private JobRepository jobRepository;

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

    public org.springframework.data.domain.Page<JobApplication> getAll(org.springframework.data.domain.Pageable pageable) {
        return repo.findAll(pageable);
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

    public org.springframework.data.domain.Page<JobApplication> getByRecruiterId(Long recruiterId, org.springframework.data.domain.Pageable pageable) {
        return repo.findByJobRecruiterId(recruiterId, pageable);
    }

    // 📊 Count by status
    public long countByStatus(ApplicationStatus status) {
        return repo.countByStatus(status);
    }

    public boolean hasAlreadyApplied(Long studentId, Long jobId) {
        return repo.existsByStudentIdAndJobId(studentId, jobId);
    }

    public JobApplication getByIdForRecruiter(Long id, Long recruiterId) {
        if (!repo.existsByIdAndJobRecruiterId(id, recruiterId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only access applications for your own jobs");
        }
        return getById(id);
    }

    public List<JobApplication> getByJobIdForRecruiter(Long jobId, Long recruiterId) {
        if (!jobRepository.existsByIdAndRecruiterId(jobId, recruiterId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only access applications for your own jobs");
        }
        return getByJobId(jobId);
    }

    public JobApplication updateStatusForRecruiter(Long id, ApplicationStatus status, Long recruiterId, java.time.LocalDateTime interviewDate) {
        JobApplication app = getByIdForRecruiter(id, recruiterId);
        app.setStatus(status);
        if (status == ApplicationStatus.INTERVIEW) {
            app.setInterviewDate(interviewDate);
        } else {
            app.setInterviewDate(null); // Clear it if status is not interview
        }
        return repo.save(app);
    }
}
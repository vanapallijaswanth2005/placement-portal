package com.example.placementportal.controller;

import com.example.placementportal.dto.AdminRecruiterResponse;
import com.example.placementportal.dto.AdminUserResponse;
import com.example.placementportal.entity.User;
import com.example.placementportal.repository.UserRepository;
import com.example.placementportal.service.JobService;
import com.example.placementportal.service.RecruiterService;
import com.example.placementportal.service.StudentService;
import com.example.placementportal.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobService jobService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private RecruiterService recruiterService;

    @GetMapping("/stats")
    public Map<String, Object> getStatistics() {
        return statisticsService.getDashboardStats();
    }

    @GetMapping("/users")
    public org.springframework.data.domain.Page<AdminUserResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return userRepository.findAll(pageable)
                .map(user -> new AdminUserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole()));
    }

    @GetMapping("/recruiters")
    public org.springframework.data.domain.Page<AdminRecruiterResponse> getAllRecruiters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return recruiterService.getAllRecruitersForAdmin(pageable);
    }

    @PutMapping("/recruiters/{id}/approve")
    public AdminRecruiterResponse approveRecruiter(@PathVariable Long id) {
        recruiterService.setApprovalStatus(id, true);
        return findRecruiterResponse(id);
    }

    @PutMapping("/recruiters/{id}/reject")
    public AdminRecruiterResponse rejectRecruiter(@PathVariable Long id) {
        recruiterService.setApprovalStatus(id, false);
        return findRecruiterResponse(id);
    }

    private AdminRecruiterResponse findRecruiterResponse(Long id) {
        return recruiterService.getAllRecruitersForAdmin().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Recruiter not found"));
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    @DeleteMapping("/jobs/{id}")
    public void deleteJob(@PathVariable Long id) {
        jobService.deleteJobAdmin(id);
    }

    @DeleteMapping("/students/{id}")
    public void deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
    }
}

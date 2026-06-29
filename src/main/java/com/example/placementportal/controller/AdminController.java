package com.example.placementportal.controller;

import com.example.placementportal.entity.User;
import com.example.placementportal.repository.UserRepository;
import com.example.placementportal.service.JobService;
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

    // 📊 Dashboard statistics
    @GetMapping("/stats")
    public Map<String, Object> getStatistics() {
        return statisticsService.getDashboardStats();
    }

    // 👥 Get all users
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 🗑️ Delete a user
    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    // 🗑️ Admin can delete any job
    @DeleteMapping("/jobs/{id}")
    public void deleteJob(@PathVariable Long id) {
        jobService.deleteJobAdmin(id);
    }

    // 🗑️ Admin can delete any student
    @DeleteMapping("/students/{id}")
    public void deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
    }
}

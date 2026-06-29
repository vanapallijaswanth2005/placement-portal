package com.example.placementportal.controller;

import com.example.placementportal.entity.Recruiter;
import com.example.placementportal.service.RecruiterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recruiters")
public class RecruiterController {

    @Autowired
    private RecruiterService recruiterService;

    // 🔒 RECRUITER: Get my profile
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/me")
    public Recruiter getMyProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return recruiterService.getRecruiterByUsername(username);
    }

    // 🔒 RECRUITER: Create or update my profile
    @PreAuthorize("hasRole('RECRUITER')")
    @PostMapping("/me")
    public Recruiter saveMyProfile(@Valid @RequestBody Recruiter recruiter) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return recruiterService.saveOrUpdateRecruiterForUser(username, recruiter);
    }
}

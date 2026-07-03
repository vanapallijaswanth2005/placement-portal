package com.example.placementportal.controller;

import com.example.placementportal.dto.RecruiterProfileResponse;
import com.example.placementportal.entity.Recruiter;
import com.example.placementportal.service.RecruiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/recruiters")
public class RecruiterController {

    @Autowired
    private RecruiterService recruiterService;

    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/me")
    public RecruiterProfileResponse getMyProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Recruiter recruiter = recruiterService.getRecruiterByUsername(username);
        if (recruiter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recruiter profile not found");
        }

        return new RecruiterProfileResponse(
                recruiter.getId(),
                recruiter.getRecruiterName(),
                recruiter.getCompanyName(),
                recruiter.getEmail(),
                recruiter.isApproved()
        );
    }
}

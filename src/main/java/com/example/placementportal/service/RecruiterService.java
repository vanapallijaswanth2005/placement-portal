package com.example.placementportal.service;

import com.example.placementportal.entity.Recruiter;
import com.example.placementportal.entity.User;
import com.example.placementportal.repository.RecruiterRepository;
import com.example.placementportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecruiterService {

    @Autowired
    private RecruiterRepository recruiterRepo;

    @Autowired
    private UserRepository userRepo;

    public Recruiter getRecruiterByUsername(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return recruiterRepo.findByUserId(user.getId()).orElse(null);
    }

    public Recruiter saveOrUpdateRecruiterForUser(String username, Recruiter recruiter) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Recruiter existing = recruiterRepo.findByUserId(user.getId()).orElse(null);
        if (existing == null) {
            existing = new Recruiter();
            existing.setUser(user);
        }
        existing.setRecruiterName(recruiter.getRecruiterName());
        existing.setCompanyName(recruiter.getCompanyName());
        existing.setDesignation(recruiter.getDesignation());
        existing.setEmail(recruiter.getEmail());
        existing.setPhone(recruiter.getPhone());
        return recruiterRepo.save(existing);
    }

    public Recruiter getRecruiterById(Long id) {
        return recruiterRepo.findById(id).orElse(null);
    }
}

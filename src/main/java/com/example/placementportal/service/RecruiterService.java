package com.example.placementportal.service;

import com.example.placementportal.dto.AdminRecruiterResponse;
import com.example.placementportal.entity.Recruiter;
import com.example.placementportal.entity.User;
import com.example.placementportal.repository.RecruiterRepository;
import com.example.placementportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

    @Transactional(readOnly = true)
    public List<AdminRecruiterResponse> getAllRecruitersForAdmin() {
        return recruiterRepo.findAll().stream()
                .map(this::toAdminResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AdminRecruiterResponse> getAllRecruitersForAdmin(org.springframework.data.domain.Pageable pageable) {
        return recruiterRepo.findAll(pageable)
                .map(this::toAdminResponse);
    }

    public Recruiter setApprovalStatus(Long recruiterId, boolean approved) {
        Recruiter recruiter = recruiterRepo.findById(recruiterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recruiter not found"));
        recruiter.setApproved(approved);
        return recruiterRepo.save(recruiter);
    }

    public void assertRecruiterApproved(Recruiter recruiter) {
        if (recruiter == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please complete your recruiter profile first");
        }
        if (!recruiter.isApproved()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your recruiter account is pending admin approval");
        }
    }

    private AdminRecruiterResponse toAdminResponse(Recruiter recruiter) {
        User user = recruiter.getUser();
        return new AdminRecruiterResponse(
                recruiter.getId(),
                user != null ? user.getUsername() : "unknown",
                recruiter.getRecruiterName(),
                recruiter.getCompanyName(),
                recruiter.getEmail(),
                recruiter.getPhone(),
                recruiter.isApproved()
        );
    }
}

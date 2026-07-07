package com.example.placementportal.service;

import com.example.placementportal.dto.LoginRequest;
import com.example.placementportal.dto.RegisterRequest;
import com.example.placementportal.entity.Recruiter;
import com.example.placementportal.entity.Role;
import com.example.placementportal.entity.Student;
import com.example.placementportal.entity.User;
import com.example.placementportal.repository.RecruiterRepository;
import com.example.placementportal.repository.StudentRepository;
import com.example.placementportal.repository.UserRepository;
import com.example.placementportal.entity.PasswordResetToken;
import com.example.placementportal.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.example.placementportal.repository.PasswordResetTokenRepository passwordResetTokenRepository;

    @org.springframework.transaction.annotation.Transactional
    public String register(RegisterRequest request) {
        if (request.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin accounts cannot be created via public registration");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        User user = new User();

        user.setUsername(request.getUsername());

        user.setPassword(
                encoder.encode(request.getPassword())
        );

        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == Role.RECRUITER) {
            Recruiter recruiter = new Recruiter();
            recruiter.setUser(savedUser);
            recruiter.setRecruiterName(savedUser.getUsername());
            recruiter.setCompanyName("Not specified");
            recruiter.setEmail(savedUser.getEmail());
            recruiterRepository.save(recruiter);
        } else if (savedUser.getRole() == Role.STUDENT) {
            Student student = new Student();
            student.setUser(savedUser);
            student.setName(savedUser.getUsername());
            student.setEmail(savedUser.getEmail());
            student.setSkills("Not specified");
            studentRepository.save(student);
        }

        // Send welcome email asynchronously
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername(), user.getRole().name());

        return "User Registered Successfully";
    }

    public String login(LoginRequest request) {

        User user = userRepository
                .findFirstByUsername(request.getUsername())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (encoder.matches(
                request.getPassword(),
                user.getPassword())) {

            return jwtUtil.generateToken(
                    user.getUsername(),
                    user.getRole().name()
            );
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    @org.springframework.transaction.annotation.Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // Do not throw an error to prevent email enumeration attacks
            return;
        }

        // Delete any existing tokens for this user
        passwordResetTokenRepository.deleteByUser(user);

        // Generate new token
        String token = java.util.UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(resetToken);

        // Send email
        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), token);
    }

    @org.springframework.transaction.annotation.Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token"));

        if (resetToken.isExpired() || resetToken.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token has expired or already been used");
        }

        User user = resetToken.getUser();
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}

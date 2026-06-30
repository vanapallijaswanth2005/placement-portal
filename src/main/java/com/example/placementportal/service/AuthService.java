package com.example.placementportal.service;

import com.example.placementportal.dto.LoginRequest;
import com.example.placementportal.dto.RegisterRequest;
import com.example.placementportal.entity.User;
import com.example.placementportal.repository.UserRepository;
import com.example.placementportal.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    public String register(RegisterRequest request) {

        User user = new User();

        user.setUsername(request.getUsername());

        user.setPassword(
                encoder.encode(request.getPassword())
        );

        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        userRepository.save(user);

        // Send welcome email asynchronously via @Async
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername(), user.getRole().name());

        return "User Registered Successfully";
    }

    public String login(LoginRequest request) {

        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new com.example.placementportal.exception.UserNotFoundException("User Not Found"));

        if (encoder.matches(
                request.getPassword(),
                user.getPassword())) {

            return jwtUtil.generateToken(
                    user.getUsername(),
                    user.getRole().name()
            );
        }

        throw new com.example.placementportal.exception.AuthenticationFailedException(
                "Invalid Username or Password");
    }
}
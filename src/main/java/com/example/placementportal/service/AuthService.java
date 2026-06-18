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

    public String register(RegisterRequest request) {

        User user = new User();

        user.setUsername(request.getUsername());

        user.setPassword(
                encoder.encode(request.getPassword())
        );

        user.setRole(request.getRole());

        userRepository.save(user);

        return "User Registered Successfully";
    }

    public String login(LoginRequest request) {

        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("User Not Found"));

        if (encoder.matches(
                request.getPassword(),
                user.getPassword())) {

            return JwtUtil.generateToken(
                    user.getUsername()
            );
        }

        throw new RuntimeException(
                "Invalid Username or Password");
    }
}
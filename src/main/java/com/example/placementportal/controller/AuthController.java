package com.example.placementportal.controller;

import com.example.placementportal.dto.LoginRequest;
import com.example.placementportal.dto.RegisterRequest;
import com.example.placementportal.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {
        authService.requestPasswordReset(email);
        return "If an account with that email exists, a reset link has been sent.";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        return "Password successfully reset.";
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return "Email verified successfully! You can now log in.";
    }

    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        return "Logged out successfully.";
    }

    @PostMapping("/verify-mfa")
    public String verifyMfa(@RequestParam String username, @RequestParam String otp) {
        return authService.verifyMfa(username, otp);
    }
}
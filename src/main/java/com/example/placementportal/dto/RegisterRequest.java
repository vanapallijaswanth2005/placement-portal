package com.example.placementportal.dto;

import com.example.placementportal.entity.Role;

public class RegisterRequest {

    private String username;
    private String password;
    private String email;
    private Role role;   // ✅ must be enum

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
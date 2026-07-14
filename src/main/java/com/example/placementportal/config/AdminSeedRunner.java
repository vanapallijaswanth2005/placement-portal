package com.example.placementportal.config;

import com.example.placementportal.entity.Role;
import com.example.placementportal.entity.User;
import com.example.placementportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeedRunner implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${app.seed-admin.enabled:false}")
    private boolean seedEnabled;

    @Value("${app.seed-admin.username:admin}")
    private String adminUsername;

    @Value("${app.seed-admin.password:}")
    private String adminPassword;

    @Value("${app.seed-admin.email:admin@placement.local}")
    private String adminEmail;

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled || adminPassword == null || adminPassword.isBlank()) {
            return;
        }

        User existing = userRepository.findFirstByUsername(adminUsername).orElse(null);
        if (existing != null) {
            // Always update password and email in case they changed in config
            existing.setPassword(passwordEncoder.encode(adminPassword));
            existing.setEmail(adminEmail);
            existing.setRole(Role.ADMIN);
            existing.setEmailVerified(true);
            userRepository.save(existing);
            return;
        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setEmail(adminEmail);
        admin.setRole(Role.ADMIN);
        admin.setEmailVerified(true);
        userRepository.save(admin);
    }
}

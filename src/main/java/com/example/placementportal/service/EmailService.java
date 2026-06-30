package com.example.placementportal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @org.springframework.scheduling.annotation.Async
    public void sendWelcomeEmail(String to, String username, String role) {
        if (to == null || to.isEmpty()) {
            log.warn("Cannot send welcome email, address is empty for user: {}", username);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Welcome to CareerLink, " + username + "!");
            
            String text = "Hi " + username + ",\n\n" +
                          "Welcome to CareerLink, the Modern Career Hub!\n" +
                          "You have successfully registered as a " + role + ".\n\n" +
                          "Get ready to take your career to the next level!\n\n" +
                          "Best Regards,\n" +
                          "CareerLink Team";
                          
            message.setText(text);
            mailSender.send(message);
            log.info("Welcome email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", to, e.getMessage());
        }
    }

    @org.springframework.scheduling.annotation.Async
    public void sendApplicationStatusUpdate(String to, String studentName, String jobTitle, String company, String newStatus) {
        if (to == null || to.isEmpty()) {
            log.warn("Cannot send status update email, address is empty for student: {}", studentName);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Application Status Update: " + jobTitle + " at " + company);
            
            String text = "Hi " + studentName + ",\n\n" +
                          "There has been an update regarding your application for " + jobTitle + " at " + company + ".\n" +
                          "Your new application status is: " + newStatus + "\n\n" +
                          "Log in to CareerLink to view more details.\n\n" +
                          "Best Regards,\n" +
                          "CareerLink Team";
                          
            message.setText(text);
            mailSender.send(message);
            log.info("Application status update email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send application status update to {}: {}", to, e.getMessage());
        }
    }
}

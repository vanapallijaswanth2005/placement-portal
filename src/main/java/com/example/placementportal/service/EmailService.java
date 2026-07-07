package com.example.placementportal.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine emailTemplateEngine;

    public EmailService(JavaMailSender mailSender,
                        @Qualifier("emailTemplateEngine") TemplateEngine emailTemplateEngine) {
        this.mailSender = mailSender;
        this.emailTemplateEngine = emailTemplateEngine;
    }

    @org.springframework.scheduling.annotation.Async
    public void sendWelcomeEmail(String to, String username, String role) {
        if (to == null || to.isEmpty()) {
            log.warn("Cannot send welcome email, address is empty for user: {}", username);
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("role", role);

            String htmlBody = emailTemplateEngine.process("welcome", context);

            sendHtmlEmail(to, "Welcome to CareerLink, " + username + "!", htmlBody);
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
            Context context = new Context();
            context.setVariable("studentName", studentName);
            context.setVariable("jobTitle", jobTitle);
            context.setVariable("company", company);
            context.setVariable("status", newStatus);

            // Set status-specific colors and message
            setStatusStyling(context, newStatus);

            String htmlBody = emailTemplateEngine.process("status-update", context);

            sendHtmlEmail(to, "Application Status Update: " + jobTitle + " at " + company, htmlBody);
            log.info("Application status update email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send application status update to {}: {}", to, e.getMessage());
        }
    }

    private void setStatusStyling(Context context, String status) {
        switch (status.toUpperCase()) {
            case "APPLIED" -> {
                context.setVariable("statusColor", "#f59e0b");
                context.setVariable("statusBgColor", "rgba(245, 158, 11, 0.08)");
                context.setVariable("statusBorderColor", "rgba(245, 158, 11, 0.25)");
                context.setVariable("statusMessage", "Your application has been received! The recruiter will review your profile soon.");
            }
            case "UNDER_REVIEW" -> {
                context.setVariable("statusColor", "#3b82f6");
                context.setVariable("statusBgColor", "rgba(59, 130, 246, 0.08)");
                context.setVariable("statusBorderColor", "rgba(59, 130, 246, 0.25)");
                context.setVariable("statusMessage", "Great news! Your application is currently under review by the hiring team.");
            }
            case "INTERVIEW" -> {
                context.setVariable("statusColor", "#a855f7");
                context.setVariable("statusBgColor", "rgba(168, 85, 247, 0.08)");
                context.setVariable("statusBorderColor", "rgba(168, 85, 247, 0.25)");
                context.setVariable("statusMessage", "Congratulations! You've been shortlisted for an interview. Check your email for further instructions.");
            }
            case "SELECTED" -> {
                context.setVariable("statusColor", "#10b981");
                context.setVariable("statusBgColor", "rgba(16, 185, 129, 0.08)");
                context.setVariable("statusBorderColor", "rgba(16, 185, 129, 0.25)");
                context.setVariable("statusMessage", "🎉 Congratulations! You have been SELECTED for this position! The recruiter will contact you with offer details.");
            }
            case "REJECTED" -> {
                context.setVariable("statusColor", "#ef4444");
                context.setVariable("statusBgColor", "rgba(239, 68, 68, 0.08)");
                context.setVariable("statusBorderColor", "rgba(239, 68, 68, 0.25)");
                context.setVariable("statusMessage", "Unfortunately, your application was not selected this time. Don't give up — keep applying to other opportunities!");
            }
            default -> {
                context.setVariable("statusColor", "#a0a0ba");
                context.setVariable("statusBgColor", "rgba(160, 160, 186, 0.08)");
                context.setVariable("statusBorderColor", "rgba(160, 160, 186, 0.25)");
                context.setVariable("statusMessage", "Your application status has been updated. Log in to CareerLink for details.");
            }
        }
    }

    @org.springframework.scheduling.annotation.Async
    public void sendPasswordResetEmail(String to, String username, String token) {
        if (to == null || to.isEmpty()) {
            log.warn("Cannot send password reset email, address is empty for user: {}", username);
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("username", username);
            
            // Generate the link back to the frontend with the token
            String resetLink = "http://localhost:8080?resetToken=" + token;
            context.setVariable("resetLink", resetLink);

            String htmlBody = emailTemplateEngine.process("password-reset", context);

            sendHtmlEmail(to, "CareerLink Password Reset Request", htmlBody);
            log.info("Password reset email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setTo(to);
        helper.setReplyTo("noreply@careerlink.com");
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true = isHtml
        mailSender.send(mimeMessage);
    }
}

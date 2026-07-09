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
public class EmailService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmailService.class);

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
    public void sendApplicationStatusUpdate(String to, String studentName, String jobTitle, String company, String newStatus, java.time.LocalDateTime interviewDate) {
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
            
            if (interviewDate != null && newStatus.equalsIgnoreCase("INTERVIEW")) {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a");
                context.setVariable("statusMessage", "Congratulations! You've been shortlisted for an interview scheduled on " + interviewDate.format(formatter) + ". A calendar invite is attached.");
            }

            String htmlBody = emailTemplateEngine.process("status-update", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setReplyTo("noreply@careerlink.com");
            helper.setSubject("Application Status Update: " + jobTitle + " at " + company);
            helper.setText(htmlBody, true); // true = isHtml
            
            if (interviewDate != null && newStatus.equalsIgnoreCase("INTERVIEW")) {
                String icsContent = generateIcsContent(interviewDate, company, jobTitle);
                helper.addAttachment("interview.ics", new org.springframework.core.io.ByteArrayResource(icsContent.getBytes("UTF-8")));
            }
            
            mailSender.send(mimeMessage);
            log.info("Application status update email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send application status update to {}: {}", to, e.getMessage());
        }
    }
    
    private String generateIcsContent(java.time.LocalDateTime interviewDate, String company, String jobTitle) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        String start = interviewDate.format(formatter);
        String end = interviewDate.plusHours(1).format(formatter);
        
        return "BEGIN:VCALENDAR\n" +
               "VERSION:2.0\n" +
               "PRODID:-//CareerLink//Placement Portal//EN\n" +
               "CALSCALE:GREGORIAN\n" +
               "METHOD:REQUEST\n" +
               "BEGIN:VEVENT\n" +
               "UID:" + java.util.UUID.randomUUID().toString() + "\n" +
               "DTSTAMP:" + java.time.LocalDateTime.now().format(formatter) + "Z\n" +
               "DTSTART:" + start + "\n" +
               "DTEND:" + end + "\n" +
               "SUMMARY:Interview with " + company + " for " + jobTitle + "\n" +
               "DESCRIPTION:Congratulations on your interview selection! Please join on time.\n" +
               "STATUS:CONFIRMED\n" +
               "SEQUENCE:0\n" +
               "BEGIN:VALARM\n" +
               "TRIGGER:-PT15M\n" +
               "DESCRIPTION:Reminder\n" +
               "ACTION:DISPLAY\n" +
               "END:VALARM\n" +
               "END:VEVENT\n" +
               "END:VCALENDAR";
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

    @org.springframework.scheduling.annotation.Async
    public void sendVerificationEmail(String to, String username, String token) {
        if (to == null || to.isEmpty()) {
            log.warn("Cannot send verification email, address is empty for user: {}", username);
            return;
        }

        try {
            String verificationLink = "http://localhost:8080/auth/verify?token=" + token;
            String htmlBody = "<h2>Welcome to CareerLink, " + username + "!</h2>"
                    + "<p>Please verify your email address to activate your account.</p>"
                    + "<p><a href='" + verificationLink + "'>Click here to verify</a></p>";

            sendHtmlEmail(to, "Verify Your CareerLink Account", htmlBody);
            log.info("Verification email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", to, e.getMessage());
        }
    }

    @org.springframework.scheduling.annotation.Async
    public void sendMfaEmail(String to, String username, String otp) {
        if (to == null || to.isEmpty()) {
            log.warn("Cannot send MFA email, address is empty for user: {}", username);
            return;
        }

        try {
            String htmlBody = "<h2>CareerLink Login Verification</h2>"
                    + "<p>Hi " + username + ",</p>"
                    + "<p>Your One-Time Password (OTP) is: <strong>" + otp + "</strong></p>"
                    + "<p>Please enter this code to complete your login. It will expire in 5 minutes.</p>";

            sendHtmlEmail(to, "Your Login OTP", htmlBody);
            log.info("MFA email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send MFA email to {}: {}", to, e.getMessage());
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

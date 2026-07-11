package com.example.placementportal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.placementportal.dto.LoginRequest;
import com.example.placementportal.dto.RegisterRequest;
import com.example.placementportal.entity.Recruiter;
import com.example.placementportal.entity.Role;
import com.example.placementportal.entity.Student;
import com.example.placementportal.entity.User;
import com.example.placementportal.repository.RecruiterRepository;
import com.example.placementportal.repository.StudentRepository;
import com.example.placementportal.repository.UserRepository;
import com.example.placementportal.repository.VerificationTokenRepository;
import com.example.placementportal.repository.RevokedTokenRepository;
import com.example.placementportal.repository.MfaTokenRepository;
import com.example.placementportal.entity.PasswordResetToken;
import com.example.placementportal.entity.VerificationToken;
import com.example.placementportal.entity.MfaToken;
import com.example.placementportal.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

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

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Autowired
    private MfaTokenRepository mfaTokenRepository;

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
        user.setEmailVerified(true); // Auto-verify for dev environment

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

        // Generate verification token and send verification email
        String token = java.util.UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), token);

        return "User Registered Successfully. Please check your email to verify your account.";
    }

    public String login(LoginRequest request) {

        User user = userRepository
                .findFirstByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Failed login attempt: user {} not found", request.getUsername());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
                });

        if (!user.isAccountNonLocked()) {
            if (user.getLockTime() != null && user.getLockTime().plusMinutes(15).isBefore(java.time.LocalDateTime.now())) {
                user.setAccountNonLocked(true);
                user.setFailedAttemptCount(0);
                user.setLockTime(null);
                userRepository.save(user);
                log.info("Account unlocked for user: {}", user.getUsername());
            } else {
                log.warn("Login attempt for locked account: {}", user.getUsername());
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is locked. Try again later.");
            }
        }

        if (!user.isEmailVerified()) {
            log.warn("Login attempt for unverified email: {}", user.getUsername());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please verify your email before logging in.");
        }

        if (encoder.matches(
                request.getPassword(),
                user.getPassword())) {

            if (user.getFailedAttemptCount() > 0) {
                user.setFailedAttemptCount(0);
                userRepository.save(user);
            }

            if (user.isMfaEnabled()) {
                log.info("MFA required for user: {}", user.getUsername());
                mfaTokenRepository.deleteByUser(user);
                
                String otp = String.format("%06d", new java.util.Random().nextInt(999999));
                MfaToken mfaToken = new MfaToken(otp, user);
                mfaTokenRepository.save(mfaToken);
                emailService.sendMfaEmail(user.getEmail(), user.getUsername(), otp);
                
                return "MFA_REQUIRED";
            }

            log.info("Successful login for user: {}", user.getUsername());
            return jwtUtil.generateToken(
                    user.getUsername(),
                    user.getRole().name()
            );
        }

        int newFailCount = user.getFailedAttemptCount() + 1;
        user.setFailedAttemptCount(newFailCount);
        if (newFailCount >= 5) {
            user.setAccountNonLocked(false);
            user.setLockTime(java.time.LocalDateTime.now());
            log.warn("Account locked for user: {} due to too many failed attempts", user.getUsername());
        }
        userRepository.save(user);

        log.warn("Failed login attempt: invalid password for user {}", user.getUsername());
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

    @org.springframework.transaction.annotation.Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification token"));

        if (verificationToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationTokenRepository.deleteByUser(user);
        log.info("Email verified for user: {}", user.getUsername());
    }

    @org.springframework.transaction.annotation.Transactional
    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            if (!revokedTokenRepository.existsByToken(token)) {
                com.example.placementportal.entity.RevokedToken revokedToken = new com.example.placementportal.entity.RevokedToken(token);
                revokedTokenRepository.save(revokedToken);
                log.info("Token blacklisted successfully");
            }
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public String verifyMfa(String username, String otp) {
        User user = userRepository.findFirstByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username"));

        MfaToken mfaToken = mfaTokenRepository.findByOtpAndUser(otp, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP"));

        if (mfaToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            mfaTokenRepository.delete(mfaToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OTP has expired");
        }

        mfaTokenRepository.deleteByUser(user);
        
        log.info("MFA verified successfully for user: {}", user.getUsername());
        return jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name()
        );
    }
}

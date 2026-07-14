package com.example.placementportal.repository;

import com.example.placementportal.entity.MfaToken;
import com.example.placementportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MfaTokenRepository extends JpaRepository<MfaToken, Long> {
    Optional<MfaToken> findByOtpAndUser(String otp, User user);
    void deleteByUser(User user);
}

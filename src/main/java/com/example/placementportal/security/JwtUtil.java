package com.example.placementportal.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    private static final SecretKey SECRET_KEY =
            Keys.hmacShaKeyFor(
                    "placementportaljwtsecretkey123456placementportal"
                            .getBytes()
            );

    // 🔥 UPDATED: now includes ROLE
    public static String generateToken(String username, String role) {

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)   // 👈 ADD ROLE HERE
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String extractUsername(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 🔥 NEW METHOD: extract role from token
    public static String extractRole(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    public static boolean validateToken(String token) {

        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
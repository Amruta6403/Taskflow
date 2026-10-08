package com.taskflow.security;

import com.taskflow.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    // =====================================================
    // SECRET KEY
    // =====================================================

    private static final String SECRET_KEY =
            "TaskFlowSuperSecretKeyForJwtAuthentication2026Secure";

    private static final long EXPIRATION_TIME =
            1000L * 60 * 60 * 24;

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }

    // =====================================================
    // GENERATE TOKEN
    // =====================================================

    public String generateToken(User user) {

        return Jwts.builder()

                .subject(user.getEmail())

                .claim(
                        "userId",
                        user.getId()
                )

                .claim(
                        "role",
                        user.getRole().name()
                )

                .claim(
                        "employeeType",
                        user.getEmployeeType() != null
                                ? user.getEmployeeType().name()
                                : null
                )

                .issuedAt(new Date())

                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + EXPIRATION_TIME
                        )
                )

                .signWith(getSigningKey())

                .compact();
    }

    // =====================================================
    // EXTRACT USERNAME / EMAIL
    // =====================================================

    public String extractUsername(String token) {

        return extractAllClaims(token)
                .getSubject();
    }

    // =====================================================
    // EXTRACT CLAIMS
    // =====================================================

    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // =====================================================
    // VALIDATE TOKEN
    // =====================================================

    public boolean isTokenValid(
            String token,
            User user) {

        try {

            String username =
                    extractUsername(token);

            return username.equals(
                    user.getEmail()
            )
                    && !isTokenExpired(token);

        } catch (Exception e) {

            return false;
        }
    }

    // =====================================================
    // CHECK EXPIRATION
    // =====================================================

    private boolean isTokenExpired(
            String token) {

        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }
}
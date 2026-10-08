package com.taskflow.service.impl;

import com.taskflow.dto.request.LoginRequest;
import com.taskflow.dto.response.AuthResponse;
import com.taskflow.entity.User;
import com.taskflow.exception.UserNotFoundException;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtService;
import com.taskflow.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;

    // =====================================================
    // LOGIN
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        // -------------------------------------------------
        // FIND USER
        // -------------------------------------------------

        User user =
                userRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User not found with email: "
                                                + request.getEmail()
                                )
                        );

        // -------------------------------------------------
        // CHECK ACTIVE STATUS
        // -------------------------------------------------

        if (!Boolean.TRUE.equals(
                user.getActive()
        )) {

            throw new org.springframework.security.access
                    .AccessDeniedException(
                            "User account is inactive"
                    );
        }

        // -------------------------------------------------
        // AUTHENTICATE
        // -------------------------------------------------

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        // -------------------------------------------------
        // GENERATE JWT
        // -------------------------------------------------

        String token =
                jwtService.generateToken(user);

        // -------------------------------------------------
        // BUILD RESPONSE
        // -------------------------------------------------

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .active(Boolean.TRUE.equals(
                        user.getActive()
                ))
                .build();
    }
}
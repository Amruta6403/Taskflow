package com.taskflow.security;

import com.taskflow.entity.User;
import com.taskflow.exception.UserNotFoundException;
import com.taskflow.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        // No authentication
        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new UserNotFoundException(
                    "Authenticated user not found"
            );
        }

        // =====================================================
        // GET USER FROM SECURITY CONTEXT
        // =====================================================

        Object principal =
                authentication.getPrincipal();

        // JWT filter stores User directly
        if (principal instanceof User user) {

            return user;
        }

        // =====================================================
        // FALLBACK: GET USER USING EMAIL
        // =====================================================

        String email =
                authentication.getName();

        if (email == null
                || email.isBlank()) {

            throw new UserNotFoundException(
                    "Authenticated user not found"
            );
        }

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }
}
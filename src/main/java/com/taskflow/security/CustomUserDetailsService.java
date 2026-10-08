package com.taskflow.security;

import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    // =====================================================
    // LOAD USER BY EMAIL
    // =====================================================

    @Override
    public UserDetails loadUserByUsername(
            String email)
            throws UsernameNotFoundException {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: "
                                        + email
                        )
                );

        // =================================================
        // ROLE
        // =================================================

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(
                        "ROLE_" +
                                user.getRole().name()
                );

        // =================================================
        // SPRING SECURITY USER
        // =================================================

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(authority))
                .disabled(
                        !Boolean.TRUE.equals(
                                user.getActive()
                        )
                )
                .build();
    }
}
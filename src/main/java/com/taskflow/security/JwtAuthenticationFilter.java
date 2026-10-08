package com.taskflow.security;

import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // =================================================
        // GET AUTHORIZATION HEADER
        // =================================================

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // =================================================
        // EXTRACT TOKEN
        // =================================================

        String token =
                authorizationHeader.substring(7);

        try {

            // =================================================
            // EXTRACT EMAIL / USERNAME
            // =================================================

            String email =
                    jwtService.extractUsername(token);

            // =================================================
            // CHECK EXISTING AUTHENTICATION
            // =================================================

            if (email != null
                    && SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                User user =
                        userRepository
                                .findByEmail(email)
                                .orElse(null);

                // =========================================
                // USER EXISTS + ACTIVE
                // =========================================

                if (user != null
                        && Boolean.TRUE.equals(
                                user.getActive())
                        && jwtService.isTokenValid(
                                token,
                                user)) {

                    List<SimpleGrantedAuthority> authorities =
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_"
                                                    + user.getRole()
                                                    .name()
                                    )
                            );

                    UsernamePasswordAuthenticationToken
                            authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    authorities
                            );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(
                                    authentication
                            );
                }
            }

        } catch (Exception ignored) {

            /*
             * Invalid/expired JWT.
             *
             * Do not authenticate the request.
             * Spring Security will handle protected
             * endpoints as unauthorized.
             */
        }

        // =================================================
        // CONTINUE FILTER CHAIN
        // =================================================

        filterChain.doFilter(
                request,
                response
        );
    }
}
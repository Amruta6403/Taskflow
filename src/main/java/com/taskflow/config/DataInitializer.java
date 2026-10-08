package com.taskflow.config;

import com.taskflow.entity.Role;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        createAdminIfNotExists();
    }

    private void createAdminIfNotExists() {

        String adminEmail =
                "admin@taskflow.com";

        if (userRepository.findByEmail(adminEmail)
                .isPresent()) {

            return;
        }

        User admin = User.builder()
                .name("TaskFlow Admin")
                .email(adminEmail)
                .password(
                        passwordEncoder.encode(
                                "Admin@123"
                        )
                )
                .role(Role.ADMIN)
                .active(true)
                .build();

        userRepository.save(admin);

        System.out.println(
                "================================================"
        );

        System.out.println(
                "TaskFlow Admin account created"
        );

        System.out.println(
                "Email    : admin@taskflow.com"
        );

        System.out.println(
                "Password : Admin@123"
        );

        System.out.println(
                "================================================"
        );
    }
}
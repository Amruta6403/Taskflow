package com.taskflow.service.impl;

import com.taskflow.dto.request.CreateUserRequest;
import com.taskflow.dto.request.UpdateUserRequest;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.EmployeeType;
import com.taskflow.entity.Role;
import com.taskflow.entity.User;
import com.taskflow.exception.UserNotFoundException;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.AdminUserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl
        implements AdminUserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    // =====================================================
    // CREATE USER
    // =====================================================

    @Override
    public UserResponse createUser(
            CreateUserRequest request) {

        // -------------------------------------------------
        // EMAIL DUPLICATE CHECK
        // -------------------------------------------------

        if (userRepository.existsByEmail(
                request.getEmail())) {

            throw new IllegalArgumentException(
                    "Email already exists"
            );
        }

        // -------------------------------------------------
        // VALIDATE ROLE DATA
        // -------------------------------------------------

        validateUserRoleData(
                request.getRole(),
                request.getEmployeeType(),
                request.getManagerId()
        );

        // -------------------------------------------------
        // FIND MANAGER
        // -------------------------------------------------

        User manager = null;

        if (request.getRole() == Role.EMPLOYEE) {

            manager = findManager(
                    request.getManagerId()
            );
        }

        // -------------------------------------------------
        // CREATE USER
        // -------------------------------------------------

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim())
                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .role(request.getRole())
                .employeeType(
                        request.getRole() == Role.EMPLOYEE
                                ? request.getEmployeeType()
                                : null
                )
                .manager(manager)
                .active(true)
                .build();

        User savedUser =
                userRepository.save(user);

        return toResponse(savedUser);
    }

    // =====================================================
    // GET ALL USERS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // GET USER BY ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

        User user =
                findUser(id);

        return toResponse(user);
    }

    // =====================================================
    // UPDATE USER
    // ADMIN ONLY
    // =====================================================

    @Override
    public UserResponse updateUser(
            Long id,
            UpdateUserRequest request) {

        // -------------------------------------------------
        // FIND USER
        // -------------------------------------------------

        User user =
                findUser(id);

        // -------------------------------------------------
        // EMAIL DUPLICATE CHECK
        // -------------------------------------------------

        if (!user.getEmail()
                .equalsIgnoreCase(
                        request.getEmail())) {

            if (userRepository.existsByEmail(
                    request.getEmail())) {

                throw new IllegalArgumentException(
                        "Email already exists"
                );
            }
        }

        // -------------------------------------------------
        // VALIDATE ROLE DATA
        // -------------------------------------------------

        validateUserRoleData(
                request.getRole(),
                request.getEmployeeType(),
                request.getManagerId()
        );

        // -------------------------------------------------
        // FIND MANAGER
        // -------------------------------------------------

        User manager = null;

        if (request.getRole() == Role.EMPLOYEE) {

            // Employee cannot be own manager

            if (id.equals(
                    request.getManagerId())) {

                throw new IllegalArgumentException(
                        "User cannot be their own manager"
                );
            }

            manager =
                    findManager(
                            request.getManagerId()
                    );
        }

        // -------------------------------------------------
        // UPDATE BASIC INFORMATION
        // -------------------------------------------------

        user.setName(
                request.getName().trim()
        );

        user.setEmail(
                request.getEmail().trim()
        );

        user.setRole(
                request.getRole()
        );

        // -------------------------------------------------
        // EMPLOYEE TYPE
        // -------------------------------------------------

        if (request.getRole()
                == Role.EMPLOYEE) {

            user.setEmployeeType(
                    request.getEmployeeType()
            );

        } else {

            user.setEmployeeType(null);
        }

        // -------------------------------------------------
        // MANAGER
        // -------------------------------------------------

        user.setManager(manager);

        // -------------------------------------------------
        // PASSWORD
        // -------------------------------------------------

        /*
         * Password is optional during update.
         *
         * Blank password:
         * keep existing password.
         *
         * New password:
         * encode and replace old password.
         */

        if (request.getPassword() != null
                && !request.getPassword()
                        .trim()
                        .isEmpty()) {

            user.setPassword(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );
        }

        // -------------------------------------------------
        // SAVE
        // -------------------------------------------------

        User updatedUser =
                userRepository.save(user);

        return toResponse(updatedUser);
    }

    // =====================================================
    // CHANGE USER STATUS
    // =====================================================

    @Override
    public UserResponse changeUserStatus(
            Long id,
            boolean active) {

        User user =
                findUser(id);

        user.setActive(active);

        User updatedUser =
                userRepository.save(user);

        return toResponse(updatedUser);
    }

    // =====================================================
    // DELETE USER
    // =====================================================

    @Override
    public void deleteUser(Long id) {

        User user =
                findUser(id);

        // -------------------------------------------------
        // PREVENT DELETE ADMIN
        // -------------------------------------------------

        if (user.getRole() == Role.ADMIN) {

            throw new IllegalArgumentException(
                    "Admin users cannot be deleted"
            );
        }

        // -------------------------------------------------
        // MANAGER CHECK
        // -------------------------------------------------

        if (user.getRole() == Role.MANAGER) {

            boolean hasEmployees =
                    userRepository
                            .existsByManagerId(id);

            if (hasEmployees) {

                throw new IllegalArgumentException(
                        "Cannot delete manager. "
                                + "Reassign employees first."
                );
            }
        }

        userRepository.delete(user);
    }

    // =====================================================
    // FIND USER
    // =====================================================

    private User findUser(Long id) {

        return userRepository
                .findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: "
                                        + id
                        )
                );
    }

    // =====================================================
    // FIND MANAGER
    // =====================================================

    private User findManager(Long managerId) {

        if (managerId == null) {

            throw new IllegalArgumentException(
                    "Manager is required for employees"
            );
        }

        User manager =
                userRepository
                        .findById(managerId)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "Manager not found with id: "
                                                + managerId
                                )
                        );

        if (manager.getRole()
                != Role.MANAGER) {

            throw new IllegalArgumentException(
                    "Selected user is not a manager"
            );
        }

        if (!Boolean.TRUE.equals(
                manager.getActive())) {

            throw new IllegalArgumentException(
                    "Selected manager is inactive"
            );
        }

        return manager;
    }

    // =====================================================
    // VALIDATE ROLE DATA
    // =====================================================

    private void validateUserRoleData(
            Role role,
            EmployeeType employeeType,
            Long managerId) {

        if (role == null) {

            throw new IllegalArgumentException(
                    "Role is required"
            );
        }

        // -------------------------------------------------
        // ADMIN
        // -------------------------------------------------

        if (role == Role.ADMIN) {

            if (employeeType != null) {

                throw new IllegalArgumentException(
                        "Admin cannot have an employee type"
                );
            }

            if (managerId != null) {

                throw new IllegalArgumentException(
                        "Admin cannot have a manager"
                );
            }

            return;
        }

        // -------------------------------------------------
        // MANAGER
        // -------------------------------------------------

        if (role == Role.MANAGER) {

            if (employeeType != null) {

                throw new IllegalArgumentException(
                        "Manager cannot have an employee type"
                );
            }

            if (managerId != null) {

                throw new IllegalArgumentException(
                        "Manager cannot have a manager"
                );
            }

            return;
        }

        // -------------------------------------------------
        // EMPLOYEE
        // -------------------------------------------------

        if (role == Role.EMPLOYEE) {

            if (employeeType == null) {

                throw new IllegalArgumentException(
                        "Employee type is required for employees"
                );
            }

            if (managerId == null) {

                throw new IllegalArgumentException(
                        "Manager is required for employees"
                );
            }

            return;
        }

        throw new IllegalArgumentException(
                "Invalid role"
        );
    }

    // =====================================================
    // ENTITY -> RESPONSE
    // =====================================================

    private UserResponse toResponse(
            User user) {

        Long managerId = null;

        String managerName = null;

        if (user.getManager() != null) {

            managerId =
                    user.getManager().getId();

            managerName =
                    user.getManager().getName();
        }

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .employeeType(
                        user.getEmployeeType()
                )
                .active(user.getActive())
                .managerId(managerId)
                .managerName(managerName)
                .build();
    }
}
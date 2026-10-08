package com.taskflow.controller;

import com.taskflow.dto.request.UserRequest;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.Role;
import com.taskflow.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // =====================================================
    // CREATE USER
    // ADMIN ONLY
    // =====================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest request) {

        UserResponse response =
                userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =====================================================
    // GET ALL USERS
    // ADMIN ONLY
    // =====================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }

    // =====================================================
    // GET USER BY ID
    // ADMIN + MANAGER
    // =====================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    // =====================================================
    // GET USERS BY ROLE
    // ADMIN ONLY
    // =====================================================

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsersByRole(
            @PathVariable Role role) {

        return ResponseEntity.ok(
                userService.getUsersByRole(role)
        );
    }

    // =====================================================
    // GET MANAGER'S EMPLOYEES
    // MANAGER ONLY
    // =====================================================

    @GetMapping("/my-employees")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserResponse>> getMyEmployees() {

        return ResponseEntity.ok(
                userService.getMyEmployees()
        );
    }

    // =====================================================
    // UPDATE USER
    // ADMIN ONLY
    // =====================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {

        return ResponseEntity.ok(
                userService.updateUser(
                        id,
                        request
                )
        );
    }

    // =====================================================
    // ASSIGN EMPLOYEE TO MANAGER
    // ADMIN ONLY
    // =====================================================

    @PutMapping("/{employeeId}/manager/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> assignManager(
            @PathVariable Long employeeId,
            @PathVariable Long managerId) {

        return ResponseEntity.ok(
                userService.assignEmployeeToManager(
                        employeeId,
                        managerId
                )
        );
    }

    // =====================================================
    // ACTIVATE USER
    // ADMIN ONLY
    // =====================================================

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> activateUser(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.activateUser(id)
        );
    }

    // =====================================================
    // DEACTIVATE USER
    // ADMIN ONLY
    // =====================================================

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> deactivateUser(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.deactivateUser(id)
        );
    }

    // =====================================================
    // DELETE USER
    // ADMIN ONLY
    // =====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}
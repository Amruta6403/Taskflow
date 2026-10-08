package com.taskflow.controller;

import com.taskflow.dto.request.CreateUserRequest;
import com.taskflow.dto.request.UpdateUserRequest;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.service.AdminUserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // =====================================================
    // CREATE USER
    // =====================================================

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        UserResponse response =
                adminUserService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =====================================================
    // GET ALL USERS
    // =====================================================

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        return ResponseEntity.ok(
                adminUserService.getAllUsers()
        );
    }

    // =====================================================
    // GET USER BY ID
    // =====================================================

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                adminUserService.getUserById(id)
        );
    }

    // =====================================================
    // UPDATE USER
    // =====================================================

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        return ResponseEntity.ok(
                adminUserService.updateUser(
                        id,
                        request
                )
        );
    }

    // =====================================================
    // ACTIVATE / DEACTIVATE USER
    // =====================================================

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> changeUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {

        return ResponseEntity.ok(
                adminUserService.changeUserStatus(
                        id,
                        active
                )
        );
    }

    // =====================================================
    // DELETE USER
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id) {

        adminUserService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}
package com.taskflow.controller;

import com.taskflow.dto.response.AdminDashboardResponse;
import com.taskflow.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    // =====================================================
    // GET ADMIN DASHBOARD
    // =====================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {

        AdminDashboardResponse response =
                adminDashboardService.getDashboard();

        return ResponseEntity.ok(response);
    }
}
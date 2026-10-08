package com.taskflow.controller;

import com.taskflow.dto.response.ManagerDashboardResponse;
import com.taskflow.service.ManagerDashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/dashboard")
@RequiredArgsConstructor
public class ManagerDashboardController {

    private final ManagerDashboardService managerDashboardService;

    // =====================================================
    // GET MANAGER DASHBOARD
    // =====================================================

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ManagerDashboardResponse> getDashboard() {

        ManagerDashboardResponse response =
                managerDashboardService.getDashboard();

        return ResponseEntity.ok(response);
    }
}
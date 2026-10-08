package com.taskflow.controller;

import com.taskflow.dto.response.EmployeeDashboardResponse;
import com.taskflow.service.EmployeeDashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee/dashboard")
@RequiredArgsConstructor
public class EmployeeDashboardController {

    private final EmployeeDashboardService employeeDashboardService;

    // =====================================================
    // GET EMPLOYEE DASHBOARD
    // =====================================================

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<EmployeeDashboardResponse> getDashboard() {

        EmployeeDashboardResponse response =
                employeeDashboardService.getDashboard();

        return ResponseEntity.ok(response);
    }
}
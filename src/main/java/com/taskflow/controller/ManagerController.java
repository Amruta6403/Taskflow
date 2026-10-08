package com.taskflow.controller;

import com.taskflow.dto.response.UserResponse;
import com.taskflow.service.ManagerService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/managers")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    // =====================================================
    // GET MY EMPLOYEES
    // =====================================================

    @GetMapping("/my-employees")
    public ResponseEntity<List<UserResponse>>
    getMyEmployees() {

        return ResponseEntity.ok(
                managerService.getMyEmployees()
        );
    }

    // =====================================================
    // ASSIGN EMPLOYEE
    // =====================================================

    @PutMapping("/{managerId}/employees/{employeeId}")
    public ResponseEntity<Void>
    assignEmployeeToManager(

            @PathVariable Long managerId,

            @PathVariable Long employeeId) {

        managerService.assignEmployeeToManager(
                employeeId,
                managerId
        );

        return ResponseEntity.ok().build();
    }

    // =====================================================
    // REMOVE EMPLOYEE
    // =====================================================

    @DeleteMapping("/employees/{employeeId}")
    public ResponseEntity<Void>
    removeEmployeeFromManager(

            @PathVariable Long employeeId) {

        managerService.removeEmployeeFromManager(
                employeeId
        );

        return ResponseEntity.noContent().build();
    }
}
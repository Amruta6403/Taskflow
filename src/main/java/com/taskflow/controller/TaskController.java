package com.taskflow.controller;

import com.taskflow.dto.request.TaskRequest;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;
import com.taskflow.service.TaskService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // =====================================================
    // CREATE TASK
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request) {

        TaskResponse response =
                taskService.createTask(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =====================================================
    // GET TASK BY ID
    // =====================================================

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id) {

        TaskResponse response =
                taskService.getTaskById(id);

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // SEARCH / FILTER / PAGINATION / SORTING
    // =====================================================

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TaskResponse>> searchTasks(

            @RequestParam(
                    required = false
            )
            String keyword,

            @RequestParam(
                    required = false
            )
            TaskStatus status,

            @RequestParam(
                    required = false
            )
            TaskPriority priority,

            @RequestParam(
                    required = false
            )
            Long assignedToId,

            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size,

            @RequestParam(
                    defaultValue = "createdAt"
            )
            String sortBy,

            @RequestParam(
                    defaultValue = "desc"
            )
            String sortDir) {

        Page<TaskResponse> response =
                taskService.searchTasks(
                        keyword,
                        status,
                        priority,
                        assignedToId,
                        page,
                        size,
                        sortBy,
                        sortDir
                );

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // UPDATE TASK
    // =====================================================

    /*
     * Only ADMIN and MANAGER can perform the
     * complete task update.
     *
     * Employees use /{id}/status instead.
     */

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {

        TaskResponse response =
                taskService.updateTask(
                        id,
                        request
                );

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // UPDATE TASK STATUS
    // =====================================================

    /*
     * ADMIN
     * MANAGER
     * EMPLOYEE
     *
     * can call this endpoint.
     *
     * Service layer verifies whether the user
     * actually owns / manages the task.
     */

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status) {

        TaskResponse response =
                taskService.updateTaskStatus(
                        id,
                        status
                );

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // DELETE TASK
    // =====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id) {

        taskService.deleteTask(id);

        return ResponseEntity.noContent().build();
    }
}
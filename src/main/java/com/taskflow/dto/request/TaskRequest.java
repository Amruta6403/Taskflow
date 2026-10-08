package com.taskflow.dto.request;

import com.taskflow.entity.TaskPriority;
import com.taskflow.entity.TaskStatus;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    // =====================================================
    // TASK TITLE
    // =====================================================

    @NotBlank(message = "Task title is required")
    @Size(
            min = 3,
            max = 150,
            message = "Task title must be between 3 and 150 characters"
    )
    private String title;

    // =====================================================
    // DESCRIPTION
    // =====================================================

    @Size(
            max = 2000,
            message = "Task description cannot exceed 2000 characters"
    )
    private String description;

    // =====================================================
    // STATUS
    // =====================================================

    private TaskStatus status;

    // =====================================================
    // PRIORITY
    // =====================================================

    private TaskPriority priority;

    // =====================================================
    // DUE DATE
    // =====================================================

    @FutureOrPresent(
            message = "Due date cannot be in the past"
    )
    private LocalDateTime dueDate;

    // =====================================================
    // ASSIGNED USER
    // =====================================================

    @NotNull(message = "Assigned user is required")
    private Long assignedToId;
}
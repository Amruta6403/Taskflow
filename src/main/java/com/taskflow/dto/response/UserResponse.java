package com.taskflow.dto.response;

import com.taskflow.entity.EmployeeType;
import com.taskflow.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;

    private String name;

    private String email;

    private Role role;

    private EmployeeType employeeType;

    private Boolean active;

    private Long managerId;

    private String managerName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // =====================================================
    // TASK / WORK PERFORMANCE
    // =====================================================

    private Long totalTasks;

    private Long completedTasks;

    private Long inProgressTasks;

    private Long pendingTasks;

    private Long overdueTasks;

    private Integer completionPercentage;
}
package com.taskflow.dto.request;

import com.taskflow.entity.EmployeeType;
import com.taskflow.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    // =====================================================
    // NAME
    // =====================================================

    @NotBlank(message = "Name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Name must be between 2 and 100 characters"
    )
    private String name;

    // =====================================================
    // EMAIL
    // =====================================================

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;

    // =====================================================
    // PASSWORD
    // =====================================================
    // Optional during update.
    // null or blank = keep existing password.
    // If provided, minimum 6 characters.

    @Size(
            min = 6,
            max = 100,
            message = "Password must be between 6 and 100 characters"
    )
    private String password;

    // =====================================================
    // ROLE
    // =====================================================

    @NotNull(message = "Role is required")
    private Role role;

    // =====================================================
    // EMPLOYEE TYPE
    // =====================================================

    private EmployeeType employeeType;

    // =====================================================
    // MANAGER
    // =====================================================
    // Required when role = EMPLOYEE.

    private Long managerId;
}
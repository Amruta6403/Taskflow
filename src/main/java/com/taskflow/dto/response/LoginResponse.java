package com.taskflow.dto.response;

import com.taskflow.entity.EmployeeType;
import com.taskflow.entity.Role;

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
public class LoginResponse {

    private Long userId;

    private String name;

    private String email;

    private Role role;

    private EmployeeType employeeType;

    private String token;

    private String message;
}
package com.taskflow.service;

import com.taskflow.dto.response.UserResponse;

import java.util.List;

public interface ManagerService {

    List<UserResponse> getMyEmployees();

    void assignEmployeeToManager(
            Long employeeId,
            Long managerId
    );

    void removeEmployeeFromManager(
            Long employeeId
    );
}
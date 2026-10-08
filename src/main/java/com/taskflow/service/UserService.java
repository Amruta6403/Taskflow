
package com.taskflow.service;

import com.taskflow.dto.request.UserRequest;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.Role;

import java.util.List;

public interface UserService {

    // =====================================================
    // CREATE
    // =====================================================

    UserResponse createUser(UserRequest request);

    // =====================================================
    // READ
    // =====================================================

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    List<UserResponse> getUsersByRole(Role role);

    List<UserResponse> getMyEmployees();

    // =====================================================
    // UPDATE
    // ADMIN ONLY
    // =====================================================

    UserResponse updateUser(
            Long id,
            UserRequest request
    );

    // =====================================================
    // MANAGER ASSIGNMENT
    // =====================================================

    UserResponse assignEmployeeToManager(
            Long employeeId,
            Long managerId
    );

    // =====================================================
    // ACCOUNT STATUS
    // =====================================================

    UserResponse activateUser(Long id);

    UserResponse deactivateUser(Long id);

    // =====================================================
    // DELETE
    // =====================================================

    void deleteUser(Long id);
}


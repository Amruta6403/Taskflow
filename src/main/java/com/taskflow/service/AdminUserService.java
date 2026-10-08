package com.taskflow.service;

import com.taskflow.dto.request.CreateUserRequest;
import com.taskflow.dto.request.UpdateUserRequest;
import com.taskflow.dto.response.UserResponse;

import java.util.List;

public interface AdminUserService {

    UserResponse createUser(CreateUserRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse updateUser(
            Long id,
            UpdateUserRequest request
    );

    UserResponse changeUserStatus(
            Long id,
            boolean active
    );

    void deleteUser(Long id);
}
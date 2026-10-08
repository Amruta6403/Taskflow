package com.taskflow.service;

import com.taskflow.dto.request.LoginRequest;
import com.taskflow.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);
}
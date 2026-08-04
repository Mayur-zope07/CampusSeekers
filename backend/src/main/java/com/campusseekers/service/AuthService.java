package com.campusseekers.service;

import com.campusseekers.dto.ApiResponse;
import com.campusseekers.dto.CurrentUserResponse;
import com.campusseekers.dto.LoginRequest;
import com.campusseekers.dto.LoginResponse;
import com.campusseekers.dto.RegisterRequest;

public interface AuthService {
    ApiResponse<Void> register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    CurrentUserResponse getCurrentUser();
}

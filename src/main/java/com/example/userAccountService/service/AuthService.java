package com.example.userAccountService.service;

import com.example.userAccountService.dto.ApiResponse;
import com.example.userAccountService.dto.AuthResponse;
import com.example.userAccountService.dto.LoginRequest;
import com.example.userAccountService.dto.RegistrationRequest;

public interface AuthService {

    ApiResponse<AuthResponse> registerUser(RegistrationRequest registrationRequest);

    ApiResponse<AuthResponse> loginUser(LoginRequest loginRequest);
}

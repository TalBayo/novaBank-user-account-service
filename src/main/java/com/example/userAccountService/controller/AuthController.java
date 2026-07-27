package com.example.userAccountService.controller;


import com.example.userAccountService.dto.ApiResponse;
import com.example.userAccountService.dto.AuthResponse;
import com.example.userAccountService.dto.LoginRequest;
import com.example.userAccountService.dto.RegistrationRequest;
import com.example.userAccountService.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody @Valid RegistrationRequest registrationRequest) {
        return ResponseEntity.ok(authService.registerUser(registrationRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.loginUser(loginRequest));
    }

    @GetMapping("/hello")
    public ResponseEntity<ApiResponse<?>> hello() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Success", "Welcome To Nova Bank 🏦"));
    }
}

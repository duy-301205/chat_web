package com.example.chatWeb.controller;

import com.example.chatWeb.dto.request.LoginRequest;
import com.example.chatWeb.dto.request.RefreshTokenRequest;
import com.example.chatWeb.dto.request.RegisterRequest;
import com.example.chatWeb.dto.response.ApiResponse;
import com.example.chatWeb.dto.response.LoginResponse;
import com.example.chatWeb.dto.response.AuthResponse;
import com.example.chatWeb.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.<AuthResponse>builder()
                .data(authService.register(request))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.<LoginResponse>builder()
                .data(authService.login(request))
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ApiResponse.<LoginResponse>builder()
                .data(authService.refreshToken(request))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request);

        return ApiResponse.<Void>builder()
                .message("Successfully logged out")
                .build();
    }
}

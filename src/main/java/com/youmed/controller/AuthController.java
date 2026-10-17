package com.youmed.controller;

import com.youmed.dto.request.LoginRequest;
import com.youmed.dto.request.RegisterRequest;
import com.youmed.dto.response.LoginResponse;
import com.youmed.dto.response.RegisterResponse;
import com.youmed.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/verify-otp")
    public LoginResponse verifyOtp(@Valid @RequestBody com.youmed.dto.request.VerifyOtpRequest request) {
        return authService.verifyOtp(request);
    }

    @PostMapping("/forgot-password")
    public org.springframework.http.ResponseEntity<java.util.Map<String, String>> forgotPassword(@Valid @RequestBody com.youmed.dto.request.ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return org.springframework.http.ResponseEntity.ok(java.util.Collections.singletonMap("message", "Email khôi phục mật khẩu đã được gửi"));
    }

    @PostMapping("/reset-password")
    public org.springframework.http.ResponseEntity<java.util.Map<String, String>> resetPassword(@Valid @RequestBody com.youmed.dto.request.ResetPasswordRequest request) {
        authService.resetPassword(request);
        return org.springframework.http.ResponseEntity.ok(java.util.Collections.singletonMap("message", "Mật khẩu đã được đặt lại thành công"));
    }
}
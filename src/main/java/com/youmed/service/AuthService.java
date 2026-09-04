package com.youmed.service;

import com.youmed.dto.request.LoginRequest;
import com.youmed.dto.request.RegisterRequest;
import com.youmed.dto.response.LoginResponse;
import com.youmed.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse verifyOtp(com.youmed.dto.request.VerifyOtpRequest request);
    
    void forgotPassword(com.youmed.dto.request.ForgotPasswordRequest request);
    
    void resetPassword(com.youmed.dto.request.ResetPasswordRequest request);
}
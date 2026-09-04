package com.youmed.service;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otp);
    void sendForgotPasswordEmail(String toEmail, String otp);
}

package com.youmed.service.impl;

import com.youmed.dto.request.LoginRequest;
import com.youmed.dto.request.RegisterRequest;
import com.youmed.dto.response.LoginResponse;
import com.youmed.dto.response.RegisterResponse;
import com.youmed.entity.User;
import com.youmed.enums.Role;
import com.youmed.repository.UserRepository;
import com.youmed.service.AuthService;
import com.youmed.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.youmed.exception.DuplicateResourceException;

import com.youmed.entity.OtpEntity;
import com.youmed.repository.OtpRepository;
import com.youmed.service.EmailService;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpRepository otpRepository;
    private final EmailService emailService;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        // Generate OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        
        OtpEntity otpEntity = otpRepository.findByEmail(request.getEmail()).orElse(new OtpEntity());
        otpEntity.setEmail(request.getEmail());
        otpEntity.setFullName(request.getFullName());
        otpEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        otpEntity.setOtp(otp);
        otpEntity.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otpEntity);

        // Send Email
        emailService.sendOtpEmail(request.getEmail(), otp);

        return RegisterResponse.builder()
                .id(0L) // Dummy ID since user is not created yet
                .fullName(otpEntity.getFullName())
                .email(otpEntity.getEmail())
                .role(Role.PATIENT.name())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .build();
    }

    @Override
    public LoginResponse verifyOtp(com.youmed.dto.request.VerifyOtpRequest request) {
        OtpEntity otpEntity = otpRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new com.youmed.exception.ResourceNotFoundException("OTP not found for this email"));

        if (otpEntity.isExpired()) {
            throw new IllegalArgumentException("OTP has expired");
        }

        if (!otpEntity.getOtp().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        // Check if user already exists (just in case)
        if (userRepository.existsByEmail(otpEntity.getEmail())) {
            throw new DuplicateResourceException("User already exists");
        }

        // Create User now
        User user = User.builder()
                .fullName(otpEntity.getFullName())
                .email(otpEntity.getEmail())
                .password(otpEntity.getPassword())
                .role(Role.PATIENT)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        otpRepository.delete(otpEntity);

        // Generate token and login automatically
        String token = jwtService.generateToken(savedUser);

        return LoginResponse.builder()
                .token(token)
                .role(savedUser.getRole().name())
                .fullName(savedUser.getFullName())
                .build();
    }

    @Override
    public void forgotPassword(com.youmed.dto.request.ForgotPasswordRequest request) {
        if (!userRepository.existsByEmail(request.getEmail())) {
            throw new com.youmed.exception.ResourceNotFoundException("Email không tồn tại trong hệ thống");
        }

        // Generate OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        
        OtpEntity otpEntity = otpRepository.findByEmail(request.getEmail()).orElse(new OtpEntity());
        otpEntity.setEmail(request.getEmail());
        otpEntity.setOtp(otp);
        otpEntity.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otpEntity);

        // Send Email
        emailService.sendForgotPasswordEmail(request.getEmail(), otp);
    }

    @Override
    public void resetPassword(com.youmed.dto.request.ResetPasswordRequest request) {
        OtpEntity otpEntity = otpRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new com.youmed.exception.ResourceNotFoundException("Không tìm thấy OTP cho email này"));

        if (otpEntity.isExpired()) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn");
        }

        if (!otpEntity.getOtp().equals(request.getOtp())) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new com.youmed.exception.ResourceNotFoundException("Không tìm thấy người dùng"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpRepository.delete(otpEntity);
    }
}
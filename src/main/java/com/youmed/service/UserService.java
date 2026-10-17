package com.youmed.service;

import com.youmed.dto.request.UpdateUserRequest;
import com.youmed.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    org.springframework.data.domain.Page<com.youmed.dto.response.UserResponse> getAllUsers(String keyword, String role, int page, int size, String sortBy, String sortDir);

    com.youmed.dto.response.UserResponse getUserById(Long id);

    com.youmed.dto.response.UserResponse getPatientById(Long id);

    com.youmed.dto.response.UserResponse adminUpdateUserStatus(Long id, com.youmed.dto.request.AdminUpdateUserRequest request, String adminEmail);

    com.youmed.dto.response.UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    com.youmed.dto.response.UserResponse getCurrentUser(String email);

    com.youmed.dto.response.UserResponse updateCurrentUser(String email, UpdateUserRequest request);

    void changePassword(String email, com.youmed.dto.request.ChangePasswordRequest request);
}
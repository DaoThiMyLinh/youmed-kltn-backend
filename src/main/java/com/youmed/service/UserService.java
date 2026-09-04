package com.youmed.service;

import com.youmed.dto.request.UpdateUserRequest;
import com.youmed.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    User updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    com.youmed.dto.response.UserResponse getCurrentUser(String email);

    com.youmed.dto.response.UserResponse updateCurrentUser(String email, UpdateUserRequest request);

    void changePassword(String email, com.youmed.dto.request.ChangePasswordRequest request);
}
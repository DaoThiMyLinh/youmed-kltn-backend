package com.youmed.controller;

import com.youmed.dto.request.UpdateUserRequest;
import com.youmed.entity.User;
import com.youmed.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.youmed.dto.response.UserResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id).orElse(null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUser(@PathVariable Long id,
                           @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "Delete user successfully";
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse me(Authentication authentication) {
        return userService.getCurrentUser(authentication.getName());
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateMe(Authentication authentication, @RequestBody UpdateUserRequest request) {
        return userService.updateCurrentUser(authentication.getName(), request);
    }

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public org.springframework.http.ResponseEntity<java.util.Map<String, String>> changePassword(Authentication authentication, @RequestBody com.youmed.dto.request.ChangePasswordRequest request) {
        userService.changePassword(authentication.getName(), request);
        return org.springframework.http.ResponseEntity.ok(java.util.Collections.singletonMap("message", "Đổi mật khẩu thành công"));
    }
}
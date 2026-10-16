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
    public org.springframework.data.domain.Page<UserResponse> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        return userService.getAllUsers(keyword, role, page, size, sortBy, sortDir);
    }

    @GetMapping("/patients")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public org.springframework.data.domain.Page<UserResponse> getPatients(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        return userService.getAllUsers(keyword, "PATIENT", page, size, sortBy, sortDir);
    }

    @GetMapping("/patients/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public UserResponse getPatientById(@PathVariable Long id) {
        return userService.getPatientById(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(@jakarta.validation.Valid @RequestBody com.youmed.dto.request.AdminCreateUserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse adminUpdateUserStatus(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody com.youmed.dto.request.AdminUpdateUserRequest request,
            Authentication authentication
    ) {
        return userService.adminUpdateUserStatus(id, request, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(@PathVariable Long id,
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
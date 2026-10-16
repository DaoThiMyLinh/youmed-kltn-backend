package com.youmed.service.impl;

import com.youmed.dto.request.UpdateUserRequest;
import com.youmed.entity.User;
import com.youmed.repository.UserRepository;
import com.youmed.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final com.youmed.repository.DoctorRepository doctorRepository;
    private final com.youmed.repository.SpecialtyRepository specialtyRepository;

    @Value("${app.user.default-password:123456}")
    private String defaultPassword;

    @Override
    public org.springframework.data.domain.Page<com.youmed.dto.response.UserResponse> getAllUsers(String keyword, String role, int page, int size, String sortBy, String sortDir) {
        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) 
            ? org.springframework.data.domain.Sort.by(sortBy).ascending() 
            : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        
        com.youmed.enums.Role roleEnum = null;
        if (role != null && !role.trim().isEmpty()) {
            try {
                roleEnum = com.youmed.enums.Role.valueOf(role.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid role
            }
        }
        
        String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        
        return userRepository.searchUsers(kw, roleEnum, pageable).map(this::mapToResponse);
    }

    @Override
    public com.youmed.dto.response.UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.youmed.exception.ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }
    
    @Override
    public com.youmed.dto.response.UserResponse getPatientById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.youmed.exception.ResourceNotFoundException("Patient not found"));
        if (user.getRole() != com.youmed.enums.Role.PATIENT) {
            throw new com.youmed.exception.BadRequestException("User is not a patient");
        }
        return mapToResponse(user);
    }
    
    @Override
    public com.youmed.dto.response.UserResponse adminUpdateUserStatus(Long id, com.youmed.dto.request.AdminUpdateUserRequest request, String adminEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.youmed.exception.ResourceNotFoundException("User not found"));
                
        if (user.getEmail().equalsIgnoreCase(adminEmail)) {
            if (request.getEnabled() == false) {
                throw new com.youmed.exception.BadRequestException("Admin cannot lock their own account");
            }
            if (request.getRole() != com.youmed.enums.Role.ADMIN) {
                throw new com.youmed.exception.BadRequestException("Admin cannot downgrade their own role");
            }
        }

        com.youmed.enums.Role oldRole = user.getRole();
        com.youmed.enums.Role newRole = request.getRole();

        if (newRole != oldRole) {
            if (newRole == com.youmed.enums.Role.DEPARTMENT_MANAGER) {
                com.youmed.entity.Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);
                if (doctor == null) {
                    throw new com.youmed.exception.BadRequestException("Người dùng chưa có hồ sơ bác sĩ để bổ nhiệm làm Trưởng khoa.");
                }
                if (doctor.getSpecialty() == null) {
                    throw new com.youmed.exception.BadRequestException("Bác sĩ phải thuộc một chuyên khoa trước khi được bổ nhiệm làm Trưởng khoa.");
                }

                com.youmed.entity.Specialty specialty = doctor.getSpecialty();
                if (specialty.getManager() != null && !specialty.getManager().getId().equals(user.getId())) {
                    throw new com.youmed.exception.DuplicateResourceException("Chuyên khoa " + specialty.getName() + " đã có Trưởng khoa (" + specialty.getManager().getFullName() + "). Mỗi chuyên khoa chỉ được có 1 Trưởng khoa.");
                }
                specialty.setManager(user);
                specialtyRepository.save(specialty);
            } else if (oldRole == com.youmed.enums.Role.DEPARTMENT_MANAGER) {
                specialtyRepository.findByManagerId(user.getId()).ifPresent(s -> {
                    s.setManager(null);
                    specialtyRepository.save(s);
                });
            }

            if (newRole == com.youmed.enums.Role.DOCTOR) {
                com.youmed.entity.Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);
                if (doctor == null) {
                    com.youmed.entity.Specialty specialty = specialtyRepository.findByManagerId(user.getId()).orElse(null);
                    if (specialty == null) {
                        specialty = specialtyRepository.findAll().stream().findFirst().orElse(null);
                    }
                    doctor = com.youmed.entity.Doctor.builder()
                            .user(user)
                            .specialty(specialty)
                            .consultationFee(200000.0)
                            .biography("Bác sĩ chuyên khoa tại Bệnh viện YouMed")
                            .active(request.getEnabled() != null ? request.getEnabled() : true)
                            .build();
                    doctorRepository.save(doctor);
                } else {
                    doctor.setActive(request.getEnabled() != null ? request.getEnabled() : true);
                    doctorRepository.save(doctor);
                }
            } else if (newRole != com.youmed.enums.Role.DEPARTMENT_MANAGER) {
                doctorRepository.findByUserId(user.getId()).ifPresent(d -> {
                    d.setActive(false);
                    doctorRepository.save(d);
                });
            }
        }
        
        user.setRole(newRole);
        user.setEnabled(request.getEnabled());
        User savedUser = userRepository.save(user);

        doctorRepository.findByUserId(savedUser.getId()).ifPresent(d -> {
            if (request.getEnabled() != null && !request.getEnabled().equals(d.getActive())) {
                d.setActive(request.getEnabled());
                doctorRepository.save(d);
            }
        });

        return mapToResponse(savedUser);
    }

    @Override
    public com.youmed.dto.response.UserResponse updateUser(Long id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());

        return mapToResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    @Override
    public com.youmed.dto.response.UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(user);
    }

    @Override
    public com.youmed.dto.response.UserResponse updateCurrentUser(String email, UpdateUserRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public void changePassword(String email, com.youmed.dto.request.ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.youmed.exception.ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public com.youmed.dto.response.UserResponse createUser(com.youmed.dto.request.AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new com.youmed.exception.DuplicateResourceException("Email đã tồn tại trong hệ thống");
        }

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (userRepository.existsByPhone(request.getPhone().trim())) {
                throw new com.youmed.exception.DuplicateResourceException("Số điện thoại đã tồn tại trong hệ thống");
            }
        }

        String rawPassword = (request.getPassword() != null && !request.getPassword().trim().isEmpty())
                ? request.getPassword().trim()
                : defaultPassword;

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(rawPassword))
                .phone(request.getPhone() != null && !request.getPhone().trim().isEmpty() ? request.getPhone().trim() : null)
                .role(request.getRole())
                .enabled(true)
                .build();

        return mapToResponse(userRepository.save(user));
    }

    private com.youmed.dto.response.UserResponse mapToResponse(User user) {
        return com.youmed.dto.response.UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .role(user.getRole().name())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
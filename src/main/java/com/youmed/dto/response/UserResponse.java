package com.youmed.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String gender;
    private LocalDate dateOfBirth;
    private String role;
}

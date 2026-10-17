package com.youmed.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorCreateRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private Long specialtyId;

    private Double consultationFee;
    @NotBlank(message = "Phone is required")
    private String phone;

    @Email(message = "Email is invalid")
    @NotBlank(message = "Email is required")
    private String email;

    private String biography;
}
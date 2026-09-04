package com.youmed.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoctorUpdateRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private Long specialtyId;

    private Double consultationFee;
    @NotBlank(message = "Phone is required")
    private String phone;

    private String biography;

    private Boolean active;
}
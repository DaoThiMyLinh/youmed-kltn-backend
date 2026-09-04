package com.youmed.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DoctorResponse {

    private Long id;

    private String fullName;

    private SpecialtyResponse specialty;

    private Double consultationFee;
    private String phone;

    private String email;

    private String biography;

    private Boolean active;

    private LocalDateTime createdAt;
}
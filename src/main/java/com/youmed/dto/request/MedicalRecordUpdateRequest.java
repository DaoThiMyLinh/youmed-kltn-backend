package com.youmed.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MedicalRecordUpdateRequest {

    @NotBlank(message = "Diagnosis is required")
    private String diagnosis;

    private String symptoms;

    private String notes;

}

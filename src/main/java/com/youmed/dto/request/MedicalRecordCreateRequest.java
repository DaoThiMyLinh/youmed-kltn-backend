package com.youmed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MedicalRecordCreateRequest {

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @NotBlank(message = "Diagnosis is required")
    private String diagnosis;

    private String symptoms;

    private String notes;

}

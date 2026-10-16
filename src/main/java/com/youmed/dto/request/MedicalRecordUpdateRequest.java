package com.youmed.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MedicalRecordUpdateRequest {

    @NotBlank(message = "Diagnosis is required")
    private String diagnosis;

    private String symptoms;

    private String notes;

    private java.time.LocalDate followUpDate;
    
    private String referralTarget;
    
    private String referralServices;
    
    private String referralReason;
    
    private String referralNotes;

}

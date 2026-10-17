package com.youmed.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MedicalRecordResponse {

    private Long id;
    
    private Long appointmentId;
    
    private String patientName;
    
    private String doctorName;
    
    private String diagnosis;
    
    private String symptoms;
    
    private String notes;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    private java.time.LocalDate followUpDate;
    
    private String referralTarget;
    
    private String referralServices;
    
    private String referralReason;
    
    private String referralNotes;

}

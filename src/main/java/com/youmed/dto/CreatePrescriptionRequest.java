package com.youmed.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePrescriptionRequest {
    @NotNull(message = "Medical Record ID is required")
    private Long medicalRecordId;
    
    private String note;
    
    @Valid
    private List<CreatePrescriptionItemRequest> items;
}

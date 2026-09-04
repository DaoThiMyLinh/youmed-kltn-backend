package com.youmed.dto;

import jakarta.validation.Valid;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePrescriptionRequest {
    private String note;
    
    @Valid
    private List<CreatePrescriptionItemRequest> items;
}

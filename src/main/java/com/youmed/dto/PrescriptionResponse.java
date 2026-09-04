package com.youmed.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionResponse {
    private Long id;
    private Long medicalRecordId;
    private LocalDate prescribedDate;
    private String note;
    private List<PrescriptionItemResponse> items;
}

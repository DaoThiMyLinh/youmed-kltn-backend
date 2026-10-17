package com.youmed.dto.response;

import com.youmed.dto.PrescriptionResponse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientHistoryResponse {
    private AppointmentResponse appointment;
    private MedicalRecordResponse medicalRecord;
    private PrescriptionResponse prescription;
}

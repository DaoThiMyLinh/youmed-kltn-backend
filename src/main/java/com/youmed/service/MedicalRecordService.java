package com.youmed.service;

import com.youmed.dto.request.MedicalRecordCreateRequest;
import com.youmed.dto.request.MedicalRecordUpdateRequest;
import com.youmed.dto.response.MedicalRecordResponse;
import org.springframework.data.domain.Page;

public interface MedicalRecordService {
    Page<MedicalRecordResponse> getAllMedicalRecords(Long doctorId, Long patientId, int page, int size, String sortBy, String sortDir);

    MedicalRecordResponse createMedicalRecord(MedicalRecordCreateRequest request);

    MedicalRecordResponse updateMedicalRecord(Long id, MedicalRecordUpdateRequest request);

    MedicalRecordResponse getMedicalRecordById(Long id);

    MedicalRecordResponse getMedicalRecordByAppointmentId(Long appointmentId);

}

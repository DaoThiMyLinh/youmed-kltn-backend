package com.youmed.service;

import com.youmed.dto.CreatePrescriptionRequest;
import com.youmed.dto.PrescriptionResponse;
import com.youmed.dto.UpdatePrescriptionRequest;
import org.springframework.data.domain.Page;

public interface PrescriptionService {
    Page<PrescriptionResponse> getAllPrescriptions(int page, int size, String sortBy, String sortDir);
    PrescriptionResponse createPrescription(CreatePrescriptionRequest request);
    PrescriptionResponse updatePrescription(Long id, UpdatePrescriptionRequest request);
    PrescriptionResponse getPrescriptionById(Long id);
    PrescriptionResponse getPrescriptionByMedicalRecordId(Long medicalRecordId);
    void deletePrescription(Long id);
}

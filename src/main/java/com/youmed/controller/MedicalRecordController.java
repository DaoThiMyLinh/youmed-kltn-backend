package com.youmed.controller;

import com.youmed.dto.request.MedicalRecordCreateRequest;
import com.youmed.dto.request.MedicalRecordUpdateRequest;
import com.youmed.dto.response.MedicalRecordResponse;
import com.youmed.service.MedicalRecordService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
@Tag(name = "Medical Record", description = "Medical Record management APIs")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Page<MedicalRecordResponse>> getAllMedicalRecords(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        return ResponseEntity.ok(medicalRecordService.getAllMedicalRecords(doctorId, patientId, page, size, sortBy, sortDir));
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecordResponse> createMedicalRecord(
            @Valid @RequestBody MedicalRecordCreateRequest request
    ) {
        return ResponseEntity.ok(medicalRecordService.createMedicalRecord(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecordResponse> updateMedicalRecord(
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordUpdateRequest request
    ) {
        return ResponseEntity.ok(medicalRecordService.updateMedicalRecord(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<MedicalRecordResponse> getMedicalRecordById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(medicalRecordService.getMedicalRecordById(id));
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<MedicalRecordResponse> getMedicalRecordByAppointmentId(
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.ok(medicalRecordService.getMedicalRecordByAppointmentId(appointmentId));
    }

}

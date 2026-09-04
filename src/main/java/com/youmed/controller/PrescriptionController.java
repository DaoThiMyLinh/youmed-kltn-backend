package com.youmed.controller;

import com.youmed.dto.CreatePrescriptionRequest;
import com.youmed.dto.PrescriptionResponse;
import com.youmed.dto.UpdatePrescriptionRequest;
import com.youmed.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Prescription", description = "Prescription management APIs")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<Page<PrescriptionResponse>> getAllPrescriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        return ResponseEntity.ok(prescriptionService.getAllPrescriptions(page, size, sortBy, sortDir));
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponse> createPrescription(
            @Valid @RequestBody CreatePrescriptionRequest request) {
        return new ResponseEntity<>(prescriptionService.createPrescription(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponse> updatePrescription(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePrescriptionRequest request) {
        return ResponseEntity.ok(prescriptionService.updatePrescription(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<PrescriptionResponse> getPrescriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionById(id));
    }

    @GetMapping("/medical-record/{medicalRecordId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<PrescriptionResponse> getPrescriptionByMedicalRecordId(
            @PathVariable Long medicalRecordId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionByMedicalRecordId(medicalRecordId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deletePrescription(@PathVariable Long id) {
        prescriptionService.deletePrescription(id);
        return ResponseEntity.noContent().build();
    }
}

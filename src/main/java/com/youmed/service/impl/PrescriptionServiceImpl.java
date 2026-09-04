package com.youmed.service.impl;

import com.youmed.dto.CreatePrescriptionItemRequest;
import com.youmed.dto.CreatePrescriptionRequest;
import com.youmed.dto.PrescriptionItemResponse;
import com.youmed.dto.PrescriptionResponse;
import com.youmed.dto.UpdatePrescriptionRequest;
import com.youmed.entity.Doctor;
import com.youmed.entity.MedicalRecord;
import com.youmed.entity.Prescription;
import com.youmed.entity.PrescriptionItem;
import com.youmed.entity.User;
import com.youmed.exception.BadRequestException;
import com.youmed.exception.ResourceNotFoundException;
import com.youmed.repository.DoctorRepository;
import com.youmed.repository.MedicalRecordRepository;
import com.youmed.repository.PrescriptionRepository;
import com.youmed.repository.UserRepository;
import com.youmed.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> getAllPrescriptions(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return prescriptionRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public PrescriptionResponse createPrescription(CreatePrescriptionRequest request) {
        Doctor currentDoctor = getCurrentDoctor();

        MedicalRecord medicalRecord = medicalRecordRepository.findById(request.getMedicalRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));

        if (!medicalRecord.getAppointment().getDoctor().getId().equals(currentDoctor.getId())) {
            throw new BadRequestException("You are not allowed to create a prescription for this medical record");
        }

        if (prescriptionRepository.existsByMedicalRecordId(medicalRecord.getId())) {
            throw new BadRequestException("Prescription already exists for this medical record");
        }

        Prescription prescription = Prescription.builder()
                .medicalRecord(medicalRecord)
                .prescribedDate(LocalDate.now())
                .note(request.getNote())
                .items(new ArrayList<>())
                .build();

        if (request.getItems() != null) {
            for (CreatePrescriptionItemRequest itemRequest : request.getItems()) {
                PrescriptionItem item = PrescriptionItem.builder()
                        .medicineName(itemRequest.getMedicineName())
                        .dosage(itemRequest.getDosage())
                        .frequency(itemRequest.getFrequency())
                        .duration(itemRequest.getDuration())
                        .instruction(itemRequest.getInstruction())
                        .build();
                prescription.addItem(item);
            }
        }

        return mapToResponse(prescriptionRepository.save(prescription));
    }

    @Override
    @Transactional
    public PrescriptionResponse updatePrescription(Long id, UpdatePrescriptionRequest request) {
        Doctor currentDoctor = getCurrentDoctor();

        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));

        if (!prescription.getMedicalRecord().getAppointment().getDoctor().getId().equals(currentDoctor.getId())) {
            throw new BadRequestException("You are not allowed to update this prescription");
        }

        if (request.getNote() != null) {
            prescription.setNote(request.getNote());
        }

        if (request.getItems() != null) {
            // Clear existing items
            prescription.getItems().clear();

            for (CreatePrescriptionItemRequest itemRequest : request.getItems()) {
                PrescriptionItem item = PrescriptionItem.builder()
                        .medicineName(itemRequest.getMedicineName())
                        .dosage(itemRequest.getDosage())
                        .frequency(itemRequest.getFrequency())
                        .duration(itemRequest.getDuration())
                        .instruction(itemRequest.getInstruction())
                        .build();
                prescription.addItem(item);
            }
        }

        return mapToResponse(prescriptionRepository.save(prescription));
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));
        checkPrescriptionAccess(prescription);
        return mapToResponse(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionByMedicalRecordId(Long medicalRecordId) {
        Prescription prescription = prescriptionRepository.findByMedicalRecordId(medicalRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found for this medical record"));
        checkPrescriptionAccess(prescription);
        return mapToResponse(prescription);
    }

    private void checkPrescriptionAccess(Prescription prescription) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        boolean isPatient = prescription.getMedicalRecord().getAppointment().getPatient().getId().equals(currentUser.getId());
        
        boolean isDoctor = false;
        if (doctorRepository.findByUserId(currentUser.getId()).isPresent()) {
            Doctor doc = doctorRepository.findByUserId(currentUser.getId()).get();
            isDoctor = prescription.getMedicalRecord().getAppointment().getDoctor().getId().equals(doc.getId());
        }
        
        if (!isPatient && !isDoctor) {
            throw new BadRequestException("You are not allowed to view this prescription");
        }
    }

    @Override
    @Transactional
    public void deletePrescription(Long id) {
        Doctor currentDoctor = getCurrentDoctor();

        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));

        if (!prescription.getMedicalRecord().getAppointment().getDoctor().getId().equals(currentDoctor.getId())) {
            throw new BadRequestException("You are not allowed to delete this prescription");
        }

        prescriptionRepository.delete(prescription);
    }

    private Doctor getCurrentDoctor() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for this user"));
    }

    private PrescriptionResponse mapToResponse(Prescription prescription) {
        List<PrescriptionItemResponse> itemResponses = prescription.getItems().stream()
                .map(item -> PrescriptionItemResponse.builder()
                        .id(item.getId())
                        .medicineName(item.getMedicineName())
                        .dosage(item.getDosage())
                        .frequency(item.getFrequency())
                        .duration(item.getDuration())
                        .instruction(item.getInstruction())
                        .build())
                .collect(Collectors.toList());

        return PrescriptionResponse.builder()
                .id(prescription.getId())
                .medicalRecordId(prescription.getMedicalRecord().getId())
                .prescribedDate(prescription.getPrescribedDate())
                .note(prescription.getNote())
                .items(itemResponses)
                .build();
    }
}

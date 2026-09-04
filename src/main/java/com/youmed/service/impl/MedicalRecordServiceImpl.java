package com.youmed.service.impl;

import com.youmed.dto.request.MedicalRecordCreateRequest;
import com.youmed.dto.request.MedicalRecordUpdateRequest;
import com.youmed.dto.response.MedicalRecordResponse;
import com.youmed.entity.Appointment;
import com.youmed.entity.Doctor;
import com.youmed.entity.MedicalRecord;
import com.youmed.entity.User;
import com.youmed.exception.BadRequestException;
import com.youmed.exception.ResourceNotFoundException;
import com.youmed.repository.AppointmentRepository;
import com.youmed.repository.DoctorRepository;
import com.youmed.repository.MedicalRecordRepository;
import com.youmed.repository.UserRepository;
import com.youmed.service.MedicalRecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public Page<MedicalRecordResponse> getAllMedicalRecords(Long doctorId, Long patientId, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        if (doctorId != null && patientId != null) {
            return medicalRecordRepository.findByAppointmentDoctorIdAndAppointmentPatientId(doctorId, patientId, pageable).map(this::mapToResponse);
        } else if (doctorId != null) {
            return medicalRecordRepository.findByAppointmentDoctorId(doctorId, pageable).map(this::mapToResponse);
        } else if (patientId != null) {
            return medicalRecordRepository.findByAppointmentPatientId(patientId, pageable).map(this::mapToResponse);
        }
        
        return medicalRecordRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public MedicalRecordResponse createMedicalRecord(MedicalRecordCreateRequest request) {
        
        Doctor currentDoctor = getCurrentDoctor();
        
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
                
        if (!appointment.getDoctor().getId().equals(currentDoctor.getId())) {
            throw new BadRequestException("You are not allowed to create a medical record for this appointment");
        }

        if (medicalRecordRepository.existsByAppointmentId(appointment.getId())) {
            throw new BadRequestException("Medical record already exists for this appointment");
        }

        MedicalRecord record = MedicalRecord.builder()
                .appointment(appointment)
                .diagnosis(request.getDiagnosis())
                .symptoms(request.getSymptoms())
                .notes(request.getNotes())
                .build();

        return mapToResponse(medicalRecordRepository.save(record));
    }

    @Override
    public MedicalRecordResponse updateMedicalRecord(Long id, MedicalRecordUpdateRequest request) {
        
        Doctor currentDoctor = getCurrentDoctor();

        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));
                
        if (!record.getAppointment().getDoctor().getId().equals(currentDoctor.getId())) {
            throw new BadRequestException("You are not allowed to update this medical record");
        }

        if (request.getDiagnosis() != null) {
            record.setDiagnosis(request.getDiagnosis());
        }
        if (request.getSymptoms() != null) {
            record.setSymptoms(request.getSymptoms());
        }
        if (request.getNotes() != null) {
            record.setNotes(request.getNotes());
        }

        return mapToResponse(medicalRecordRepository.save(record));
    }

    @Override
    public MedicalRecordResponse getMedicalRecordById(Long id) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));
        checkRecordAccess(record);
        return mapToResponse(record);
    }

    @Override
    public MedicalRecordResponse getMedicalRecordByAppointmentId(Long appointmentId) {
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found for this appointment"));
        checkRecordAccess(record);
        return mapToResponse(record);
    }
    
    private void checkRecordAccess(MedicalRecord record) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        boolean isPatient = record.getAppointment().getPatient().getId().equals(currentUser.getId());
        
        boolean isDoctor = false;
        if (doctorRepository.findByUserId(currentUser.getId()).isPresent()) {
            Doctor doc = doctorRepository.findByUserId(currentUser.getId()).get();
            isDoctor = record.getAppointment().getDoctor().getId().equals(doc.getId());
        }
        
        if (!isPatient && !isDoctor) {
            throw new BadRequestException("You are not allowed to view this medical record");
        }
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

    private MedicalRecordResponse mapToResponse(MedicalRecord record) {
        return MedicalRecordResponse.builder()
                .id(record.getId())
                .appointmentId(record.getAppointment().getId())
                .patientName(record.getAppointment().getPatient().getFullName())
                .doctorName(record.getAppointment().getDoctor().getUser().getFullName())
                .diagnosis(record.getDiagnosis())
                .symptoms(record.getSymptoms())
                .notes(record.getNotes())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}

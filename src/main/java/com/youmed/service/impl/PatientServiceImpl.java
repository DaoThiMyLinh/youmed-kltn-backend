package com.youmed.service.impl;

import com.youmed.dto.PrescriptionItemResponse;
import com.youmed.dto.PrescriptionResponse;
import com.youmed.dto.response.AppointmentResponse;
import com.youmed.dto.response.MedicalRecordResponse;
import com.youmed.dto.response.PatientHistoryResponse;
import com.youmed.entity.Appointment;
import com.youmed.entity.MedicalRecord;
import com.youmed.entity.Prescription;
import com.youmed.entity.User;
import com.youmed.exception.ResourceNotFoundException;
import com.youmed.repository.AppointmentRepository;
import com.youmed.repository.MedicalRecordRepository;
import com.youmed.repository.PrescriptionRepository;
import com.youmed.repository.UserRepository;
import com.youmed.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.youmed.enums.Role;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<PatientHistoryResponse> getPatientHistory(int page, int size, String sortBy, String sortDir) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId(), pageable);

        return appointments.map(this::mapToHistoryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> searchPatients(String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findByRole(Role.PATIENT, pageable);
        }

        return userRepository.findByRoleAndFullNameContainingIgnoreCase(Role.PATIENT, keyword.trim(), pageable);
    }

    private PatientHistoryResponse mapToHistoryResponse(Appointment appointment) {
        // Map Appointment
        AppointmentResponse appointmentResponse = AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getFullName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getUser().getFullName())
                .specialization(appointment.getDoctor().getSpecialty() != null ? appointment.getDoctor().getSpecialty().getName() : null)
                .appointmentTime(appointment.getAppointmentTime())
                .reason(appointment.getReason())
                .status(appointment.getStatus())
                .createdAt(appointment.getCreatedAt())
                .build();

        MedicalRecordResponse medicalRecordResponse = null;
        PrescriptionResponse prescriptionResponse = null;

        // Try to get Medical Record
        var medicalRecordOpt = medicalRecordRepository.findByAppointmentId(appointment.getId());
        if (medicalRecordOpt.isPresent()) {
            MedicalRecord record = medicalRecordOpt.get();
            medicalRecordResponse = MedicalRecordResponse.builder()
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

            // Try to get Prescription
            var prescriptionOpt = prescriptionRepository.findByMedicalRecordId(record.getId());
            if (prescriptionOpt.isPresent()) {
                Prescription prescription = prescriptionOpt.get();
                prescriptionResponse = PrescriptionResponse.builder()
                        .id(prescription.getId())
                        .medicalRecordId(prescription.getMedicalRecord().getId())
                        .prescribedDate(prescription.getPrescribedDate())
                        .note(prescription.getNote())
                        .items(prescription.getItems().stream()
                                .map(item -> PrescriptionItemResponse.builder()
                                        .id(item.getId())
                                        .medicineName(item.getMedicineName())
                                        .dosage(item.getDosage())
                                        .frequency(item.getFrequency())
                                        .duration(item.getDuration())
                                        .instruction(item.getInstruction())
                                        .build())
                                .collect(Collectors.toList()))
                        .build();
            }
        }

        return PatientHistoryResponse.builder()
                .appointment(appointmentResponse)
                .medicalRecord(medicalRecordResponse)
                .prescription(prescriptionResponse)
                .build();
    }
}

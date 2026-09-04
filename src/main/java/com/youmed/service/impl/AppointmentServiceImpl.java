package com.youmed.service.impl;

import com.youmed.dto.request.CreateAppointmentRequest;
import com.youmed.dto.response.AppointmentResponse;
import com.youmed.entity.Appointment;
import com.youmed.entity.Doctor;
import com.youmed.entity.User;
import com.youmed.entity.TimeSlot;
import com.youmed.enums.AppointmentStatus;
import com.youmed.exception.BadRequestException;
import com.youmed.exception.DuplicateResourceException;
import com.youmed.exception.ResourceNotFoundException;
import com.youmed.repository.AppointmentRepository;
import com.youmed.repository.DoctorRepository;
import com.youmed.repository.TimeSlotRepository;
import com.youmed.repository.UserRepository;
import com.youmed.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;

    private final UserRepository userRepository;

    private final DoctorRepository doctorRepository;

    private final TimeSlotRepository timeSlotRepository;

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void cleanupOldCancelledAppointments() {
        try {
            jdbcTemplate.execute("UPDATE appointments SET time_slot_id = NULL WHERE status = 'CANCELLED' AND time_slot_id IS NOT NULL");
            System.out.println("Cleaned up time_slot_id for CANCELLED appointments.");
        } catch (Exception e) {
            System.err.println("Failed to clean up cancelled appointments: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found"));

        if (Boolean.TRUE.equals(timeSlot.getBooked())) {
            throw new DuplicateResourceException("Slot already booked");
        }

        if (timeSlot.getStartDateTime().isBefore(LocalDateTime.now().plusMinutes(30))) {
            throw new BadRequestException("Appointment must be booked at least 30 minutes in advance");
        }

        Doctor doctor = timeSlot.getDoctorSchedule().getDoctor();

        if (!Boolean.TRUE.equals(timeSlot.getActive())) {
            throw new BadRequestException("Time slot is inactive");
        }

        if (!Boolean.TRUE.equals(timeSlot.getDoctorSchedule().getActive())) {
            throw new BadRequestException("Doctor schedule is inactive");
        }

        if (!Boolean.TRUE.equals(doctor.getActive())) {
            throw new BadRequestException("Doctor is inactive");
        }

        if (appointmentRepository.existsByPatientIdAndTimeSlotStartDateTimeAndStatusNot(
                patient.getId(),
                timeSlot.getStartDateTime(),
                AppointmentStatus.CANCELLED)) {
            throw new BadRequestException("You already have an appointment at this time");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .timeSlot(timeSlot)
                .appointmentTime(timeSlot.getStartDateTime())
                .reason(request.getReason())
                .status(AppointmentStatus.PENDING)
                .build();

        appointment = appointmentRepository.save(appointment);

        timeSlot.setBooked(true);
        timeSlot.setAppointment(appointment);
        timeSlotRepository.save(timeSlot);

        return mapToResponse(appointment);
    }

    @Override
    public Page<AppointmentResponse> getMyAppointments(String status, int page, int size, String sortBy, String sortDir) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (status == null || status.trim().isEmpty()) {
            return appointmentRepository
                    .findByPatientId(patient.getId(), pageable)
                    .map(this::mapToResponse);
        }

        try {
            AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.trim().toUpperCase());
            return appointmentRepository
                    .findByPatientIdAndStatus(patient.getId(), appointmentStatus, pageable)
                    .map(this::mapToResponse);
        } catch (IllegalArgumentException e) {
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<AppointmentResponse> getDoctorAppointments(
            String status,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found for this user"));

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (status == null || status.trim().isEmpty()) {
            return appointmentRepository
                    .findByDoctorId(doctor.getId(), pageable)
                    .map(this::mapToResponse);
        }

        try {
            AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.trim().toUpperCase());
            return appointmentRepository
                    .findByDoctorIdAndStatus(doctor.getId(), appointmentStatus, pageable)
                    .map(this::mapToResponse);
        } catch (IllegalArgumentException e) {
            return Page.empty(pageable);
        }
    }

    @Override
    @Transactional
    public AppointmentResponse updateStatus(
            Long appointmentId,
            String status
    ) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found for this user"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new BadRequestException("You are not allowed to update this appointment");
        }

        AppointmentStatus oldStatus = appointment.getStatus();
        AppointmentStatus newStatus = AppointmentStatus.valueOf(status);
        appointment.setStatus(newStatus);

        appointmentRepository.save(appointment);
        
        if (newStatus == AppointmentStatus.CANCELLED) {
            if (oldStatus != AppointmentStatus.COMPLETED 
                && oldStatus != AppointmentStatus.CHECKED_IN 
                && oldStatus != AppointmentStatus.IN_PROGRESS) {
                TimeSlot timeSlot = appointment.getTimeSlot();
                if (timeSlot != null) {
                    timeSlot.setBooked(false);
                    timeSlot.setAppointment(null);
                    timeSlotRepository.save(timeSlot);
                }
                appointment.setTimeSlot(null);
                appointmentRepository.save(appointment);
            }
        }

        return mapToResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new BadRequestException("You are not allowed to cancel this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new BadRequestException("You can only cancel PENDING appointments");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        TimeSlot timeSlot = appointment.getTimeSlot();
        if (timeSlot != null) {
            timeSlot.setBooked(false);
            timeSlot.setAppointment(null);
            timeSlotRepository.save(timeSlot);
            
            appointment.setTimeSlot(null);
            appointmentRepository.save(appointment);
        }

        return mapToResponse(appointment);
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {

        return AppointmentResponse.builder()
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
    }
}
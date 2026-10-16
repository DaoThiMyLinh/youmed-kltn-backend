package com.youmed.service.impl;

import com.youmed.dto.request.RejectScheduleRequest;
import com.youmed.dto.response.DoctorResponse;
import com.youmed.dto.response.DoctorScheduleResponse;
import com.youmed.dto.response.SpecialtyResponse;
import com.youmed.entity.Doctor;
import com.youmed.entity.DoctorSchedule;
import com.youmed.entity.Specialty;
import com.youmed.entity.User;
import com.youmed.enums.Role;
import com.youmed.enums.ScheduleStatus;
import com.youmed.exception.BadRequestException;
import com.youmed.exception.ResourceNotFoundException;
import com.youmed.repository.DoctorRepository;
import com.youmed.repository.DoctorScheduleRepository;
import com.youmed.repository.SpecialtyRepository;
import com.youmed.repository.UserRepository;
import com.youmed.service.DepartmentManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentManagerServiceImpl implements DepartmentManagerService {

    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;

    private User getCurrentManager() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.DEPARTMENT_MANAGER) {
            throw new AccessDeniedException("User is not a department manager");
        }
        return user;
    }

    private Specialty getManagedSpecialty() {
        User manager = getCurrentManager();
        return specialtyRepository.findByManagerId(manager.getId())
                .orElseThrow(() -> new AccessDeniedException("Manager is not assigned to any specialty"));
    }

    @Override
    public List<DoctorResponse> getManagedDoctors() {
        Specialty specialty = getManagedSpecialty();
        return doctorRepository.findBySpecialtyId(specialty.getId()).stream()
                .map(this::mapToDoctorResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorScheduleResponse> getManagedSchedules() {
        Specialty specialty = getManagedSpecialty();
        return doctorScheduleRepository.findByDoctor_Specialty_Id(specialty.getId()).stream()
                .map(this::mapToScheduleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DoctorScheduleResponse approveSchedule(Long scheduleId) {
        Specialty specialty = getManagedSpecialty();

        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        if (schedule.getDoctor().getSpecialty() == null || !schedule.getDoctor().getSpecialty().getId().equals(specialty.getId())) {
            throw new AccessDeniedException("Cannot approve schedule for a doctor outside your specialty");
        }

        if (schedule.getStatus() != ScheduleStatus.PENDING) {
            throw new BadRequestException("Schedule is not in PENDING state");
        }

        schedule.setStatus(ScheduleStatus.APPROVED);
        schedule.setRejectionReason(null);
        return mapToScheduleResponse(doctorScheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public DoctorScheduleResponse rejectSchedule(Long scheduleId, RejectScheduleRequest request) {
        Specialty specialty = getManagedSpecialty();

        DoctorSchedule schedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));

        if (schedule.getDoctor().getSpecialty() == null || !schedule.getDoctor().getSpecialty().getId().equals(specialty.getId())) {
            throw new AccessDeniedException("Cannot reject schedule for a doctor outside your specialty");
        }

        if (schedule.getStatus() != ScheduleStatus.PENDING) {
            throw new BadRequestException("Schedule is not in PENDING state");
        }

        schedule.setStatus(ScheduleStatus.REJECTED);
        schedule.setRejectionReason(request.getReason().trim());
        return mapToScheduleResponse(doctorScheduleRepository.save(schedule));
    }

    private DoctorResponse mapToDoctorResponse(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .fullName(doctor.getUser().getFullName())
                .specialty(doctor.getSpecialty() != null ? SpecialtyResponse.builder()
                        .id(doctor.getSpecialty().getId())
                        .name(doctor.getSpecialty().getName())
                        .build() : null)
                .consultationFee(doctor.getConsultationFee())
                .phone(doctor.getUser().getPhone())
                .email(doctor.getUser().getEmail())
                .biography(doctor.getBiography())
                .active(doctor.getActive())
                .createdAt(doctor.getCreatedAt())
                .build();
    }

    private DoctorScheduleResponse mapToScheduleResponse(DoctorSchedule schedule) {
        return DoctorScheduleResponse.builder()
                .id(schedule.getId())
                .doctorId(schedule.getDoctor().getId())
                .doctorName(schedule.getDoctor().getUser().getFullName())
                .workingDate(schedule.getWorkingDate())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .active(schedule.getActive())
                .status(schedule.getStatus())
                .rejectionReason(schedule.getRejectionReason())
                .build();
    }
}

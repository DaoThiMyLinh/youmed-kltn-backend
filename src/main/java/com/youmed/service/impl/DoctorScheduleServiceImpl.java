package com.youmed.service.impl;

import com.youmed.dto.request.DoctorScheduleRequest;
import com.youmed.dto.response.DoctorScheduleResponse;
import com.youmed.dto.response.TimeSlotResponse;
import com.youmed.entity.Doctor;
import com.youmed.entity.DoctorSchedule;
import com.youmed.entity.TimeSlot;
import com.youmed.exception.DuplicateResourceException;
import com.youmed.exception.ResourceNotFoundException;
import com.youmed.repository.DoctorRepository;
import com.youmed.repository.DoctorScheduleRepository;
import com.youmed.repository.TimeSlotRepository;
import com.youmed.service.DoctorScheduleService;
import com.youmed.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DoctorRepository doctorRepository;
    private final TimeSlotService timeSlotService;
    private final TimeSlotRepository timeSlotRepository;

    @Override
    @Transactional
    public DoctorScheduleResponse createSchedule(DoctorScheduleRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (doctorScheduleRepository.findByDoctorIdAndWorkingDate(request.getDoctorId(), request.getWorkingDate()).isPresent()) {
            throw new DuplicateResourceException("Schedule already exists for this doctor on the given date");
        }

        DoctorSchedule schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .workingDate(request.getWorkingDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .active(true)
                .build();

        schedule = doctorScheduleRepository.save(schedule);
        timeSlotService.generateTimeSlots(schedule);

        return mapToResponse(schedule);
    }

    @Override
    public List<DoctorScheduleResponse> getSchedulesByDoctor(Long doctorId) {
        return doctorScheduleRepository.findByDoctorId(doctorId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id) {
        DoctorSchedule schedule = doctorScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
        
        timeSlotService.deleteSlotsBySchedule(id);
        doctorScheduleRepository.delete(schedule);
    }

    @Override
    public List<TimeSlotResponse> getSlotsByDoctorAndDate(Long doctorId, LocalDate date) {
        return timeSlotRepository.findAvailableSlots(doctorId, date).stream()
                .map(this::mapToSlotResponse)
                .collect(Collectors.toList());
    }

    private DoctorScheduleResponse mapToResponse(DoctorSchedule schedule) {
        return DoctorScheduleResponse.builder()
                .id(schedule.getId())
                .doctorId(schedule.getDoctor().getId())
                .doctorName(schedule.getDoctor().getUser().getFullName())
                .workingDate(schedule.getWorkingDate())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .active(schedule.getActive())
                .build();
    }

    private TimeSlotResponse mapToSlotResponse(TimeSlot slot) {
        return TimeSlotResponse.builder()
                .id(slot.getId())
                .doctorScheduleId(slot.getDoctorSchedule().getId())
                .startDateTime(slot.getStartDateTime())
                .endDateTime(slot.getEndDateTime())
                .booked(slot.getBooked())
                .appointmentId(slot.getAppointment() != null ? slot.getAppointment().getId() : null)
                .build();
    }
}

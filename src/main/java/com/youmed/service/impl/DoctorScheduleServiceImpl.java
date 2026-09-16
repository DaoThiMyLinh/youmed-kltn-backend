package com.youmed.service.impl;

import com.youmed.dto.request.DoctorScheduleRequest;
import com.youmed.dto.request.DoctorScheduleRangeRequest;
import com.youmed.dto.response.DoctorScheduleResponse;
import com.youmed.dto.response.DoctorScheduleRangeResponse;
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
import java.time.LocalTime;
import java.util.ArrayList;
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
        LocalTime minStartTime = LocalTime.of(6, 30);
        LocalTime maxEndTime = LocalTime.of(16, 30);

        if (request.getStartTime().isBefore(minStartTime)) {
            throw new IllegalArgumentException("Start time cannot be before 06:30");
        }
        if (request.getEndTime().isAfter(maxEndTime)) {
            throw new IllegalArgumentException("End time cannot be after 16:30");
        }
        if (request.getStartTime().getMinute() != 0 && request.getStartTime().getMinute() != 30) {
            throw new IllegalArgumentException("Start time minute must be 00 or 30");
        }
        if (request.getEndTime().getMinute() != 0 && request.getEndTime().getMinute() != 30) {
            throw new IllegalArgumentException("End time minute must be 00 or 30");
        }
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
    @Transactional
    public DoctorScheduleRangeResponse createScheduleRange(DoctorScheduleRangeRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot create schedule for past dates");
        }

        LocalTime minStartTime = LocalTime.of(6, 30);
        LocalTime maxEndTime = LocalTime.of(16, 30);

        if (request.getStartTime().isBefore(minStartTime)) {
            throw new IllegalArgumentException("Start time cannot be before 06:30");
        }
        if (request.getEndTime().isAfter(maxEndTime)) {
            throw new IllegalArgumentException("End time cannot be after 16:30");
        }
        if (request.getStartTime().getMinute() != 0 && request.getStartTime().getMinute() != 30) {
            throw new IllegalArgumentException("Start time minute must be 00 or 30");
        }
        if (request.getEndTime().getMinute() != 0 && request.getEndTime().getMinute() != 30) {
            throw new IllegalArgumentException("End time minute must be 00 or 30");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        List<LocalDate> createdDates = new ArrayList<>();
        List<LocalDate> skippedDates = new ArrayList<>();

        LocalDate currentDate = request.getStartDate();
        while (!currentDate.isAfter(request.getEndDate())) {
            if (doctorScheduleRepository.findByDoctorIdAndWorkingDate(request.getDoctorId(), currentDate).isPresent()) {
                skippedDates.add(currentDate);
            } else {
                DoctorSchedule schedule = DoctorSchedule.builder()
                        .doctor(doctor)
                        .workingDate(currentDate)
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .active(true)
                        .build();
                schedule = doctorScheduleRepository.save(schedule);
                timeSlotService.generateTimeSlots(schedule);
                createdDates.add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }

        String message = createdDates.isEmpty() 
            ? "Không có ngày mới nào được tạo" 
            : "Tạo lịch làm việc hoàn tất";

        return DoctorScheduleRangeResponse.builder()
                .message(message)
                .createdDates(createdDates)
                .skippedDates(skippedDates)
                .build();
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

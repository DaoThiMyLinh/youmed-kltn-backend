package com.youmed.controller;

import com.youmed.dto.request.DoctorScheduleRequest;
import com.youmed.dto.response.DoctorScheduleResponse;
import com.youmed.dto.response.TimeSlotResponse;
import com.youmed.service.DoctorScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;

    @PostMapping("/doctor-schedules")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public DoctorScheduleResponse createSchedule(@Valid @RequestBody DoctorScheduleRequest request) {
        return doctorScheduleService.createSchedule(request);
    }

    @GetMapping("/doctor-schedules/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public List<DoctorScheduleResponse> getSchedulesByDoctor(@PathVariable Long doctorId) {
        return doctorScheduleService.getSchedulesByDoctor(doctorId);
    }

    @DeleteMapping("/doctor-schedules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public void deleteSchedule(@PathVariable Long id) {
        doctorScheduleService.deleteSchedule(id);
    }

    @GetMapping("/doctors/{doctorId}/slots")
    public List<TimeSlotResponse> getSlotsByDoctorAndDate(
            @PathVariable Long doctorId,
            @RequestParam LocalDate date) {
        return doctorScheduleService.getSlotsByDoctorAndDate(doctorId, date);
    }
}

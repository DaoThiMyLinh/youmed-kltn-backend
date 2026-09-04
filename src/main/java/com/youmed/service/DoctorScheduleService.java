package com.youmed.service;

import com.youmed.dto.request.DoctorScheduleRequest;
import com.youmed.dto.response.DoctorScheduleResponse;
import com.youmed.dto.response.TimeSlotResponse;

import java.time.LocalDate;
import java.util.List;

public interface DoctorScheduleService {
    DoctorScheduleResponse createSchedule(DoctorScheduleRequest request);
    List<DoctorScheduleResponse> getSchedulesByDoctor(Long doctorId);
    void deleteSchedule(Long id);
    List<TimeSlotResponse> getSlotsByDoctorAndDate(Long doctorId, LocalDate date);
}

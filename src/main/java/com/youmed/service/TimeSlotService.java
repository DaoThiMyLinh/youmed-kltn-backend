package com.youmed.service;

import com.youmed.entity.DoctorSchedule;
import com.youmed.entity.TimeSlot;

import java.util.List;

public interface TimeSlotService {
    List<TimeSlot> generateTimeSlots(DoctorSchedule schedule);
    void deleteSlotsBySchedule(Long scheduleId);
}

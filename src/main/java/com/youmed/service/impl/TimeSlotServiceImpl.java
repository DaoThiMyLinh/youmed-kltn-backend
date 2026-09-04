package com.youmed.service.impl;

import com.youmed.entity.DoctorSchedule;
import com.youmed.entity.TimeSlot;
import com.youmed.repository.TimeSlotRepository;
import com.youmed.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    @Override
    @Transactional
    public List<TimeSlot> generateTimeSlots(DoctorSchedule schedule) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime currentStartTime = schedule.getStartTime();
        LocalTime endTime = schedule.getEndTime();

        while (currentStartTime.isBefore(endTime)) {
            LocalTime currentEndTime = currentStartTime.plusMinutes(30);

            // Break if the generated slot would exceed the schedule's end time
            if (currentEndTime.isAfter(endTime)) {
                break;
            }

            LocalDateTime startDateTime = LocalDateTime.of(schedule.getWorkingDate(), currentStartTime);
            LocalDateTime endDateTime = LocalDateTime.of(schedule.getWorkingDate(), currentEndTime);

            TimeSlot slot = TimeSlot.builder()
                    .doctorSchedule(schedule)
                    .startDateTime(startDateTime)
                    .endDateTime(endDateTime)
                    .booked(false)
                    .build();

            slots.add(slot);
            currentStartTime = currentEndTime;
        }

        return timeSlotRepository.saveAll(slots);
    }

    @Override
    @Transactional
    public void deleteSlotsBySchedule(Long scheduleId) {
        List<TimeSlot> slots = timeSlotRepository.findByDoctorScheduleId(scheduleId);
        boolean hasBookedSlots = slots.stream().anyMatch(TimeSlot::getBooked);
        
        if (hasBookedSlots) {
            throw new IllegalStateException("Cannot delete schedule with booked slots");
        }
        
        timeSlotRepository.deleteAll(slots);
    }
}

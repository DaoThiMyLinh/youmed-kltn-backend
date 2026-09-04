package com.youmed.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TimeSlotResponse {
    private Long id;
    private Long doctorScheduleId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Boolean booked;
    private Long appointmentId;
}

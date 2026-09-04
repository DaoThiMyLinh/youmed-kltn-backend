package com.youmed.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class DoctorScheduleResponse {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private LocalDate workingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean active;
}

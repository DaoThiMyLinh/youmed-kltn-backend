package com.youmed.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class DoctorScheduleRangeResponse {
    private String message;
    private List<LocalDate> createdDates;
    private List<LocalDate> skippedDates;
}

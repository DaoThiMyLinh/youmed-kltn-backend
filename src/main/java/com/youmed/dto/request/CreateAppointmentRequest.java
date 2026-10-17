package com.youmed.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateAppointmentRequest {

    @NotNull(message = "Time slot id is required")
    private Long timeSlotId;

    @NotBlank(message = "Reason is required")
    private String reason;

}
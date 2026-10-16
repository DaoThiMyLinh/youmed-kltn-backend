package com.youmed.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectScheduleRequest {
    @NotBlank(message = "Reason cannot be blank")
    private String reason;
}

package com.youmed.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpecialtyRequest {

    @NotBlank(message = "Specialty name is required")
    private String name;

    private String description;

    private String imageUrl;

    private Boolean active;
}

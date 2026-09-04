package com.youmed.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SpecialtyResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Boolean active;
}

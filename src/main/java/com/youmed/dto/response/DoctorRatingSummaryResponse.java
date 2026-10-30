package com.youmed.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRatingSummaryResponse {

    private Long doctorId;

    private Double averageRating;

    private Long totalReviews;

    private Map<Integer, Long> ratingDistribution;
}

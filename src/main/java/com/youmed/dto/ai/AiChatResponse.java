package com.youmed.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {

    private String reply;

    private String intent; // "SYMPTOM_ADVICE", "FIND_DOCTOR", "SPECIALTY_INFO", "PRICE_INQUIRY", "BOOKING_GUIDE", "GENERAL_INFO"

    private SpecialtySummary suggestedSpecialty;

    private List<DoctorSummary> suggestedDoctors;

    private List<String> quickSuggestions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecialtySummary {
        private Long id;
        private String name;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorSummary {
        private Long id;
        private String fullName;
        private String specialtyName;
        private Double consultationFee;
        private String biography;
        private String targetDate;
        private List<String> availableSlots;
    }
}

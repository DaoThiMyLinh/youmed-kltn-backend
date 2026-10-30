package com.youmed.service;

import com.youmed.entity.Doctor;
import com.youmed.entity.Specialty;

import java.util.List;

public interface GeminiAiService {
    boolean isAvailable();

    String generateMedicalAdvice(String userMessage, Specialty specialty, List<Doctor> doctors);

    ConsultationResult consultMedicalQuery(
            String userMessage,
            Specialty explicitSpecialty,
            List<Specialty> allSpecialties,
            List<Doctor> allDoctors
    );

    record ConsultationResult(
            String specialtyName,
            String advice
    ) {}
}

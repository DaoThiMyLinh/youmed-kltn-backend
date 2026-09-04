package com.youmed.service;

import com.youmed.dto.response.PatientHistoryResponse;
import org.springframework.data.domain.Page;
import com.youmed.entity.User;

public interface PatientService {
    Page<PatientHistoryResponse> getPatientHistory(int page, int size, String sortBy, String sortDir);
    Page<User> searchPatients(String keyword, int page, int size, String sortBy, String sortDir);
}

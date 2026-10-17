package com.youmed.service;

import com.youmed.dto.request.DoctorCreateRequest;
import com.youmed.dto.request.DoctorUpdateRequest;
import com.youmed.dto.response.DoctorResponse;

import java.util.List;
import org.springframework.data.domain.Page;

public interface DoctorService {

    DoctorResponse createDoctor(DoctorCreateRequest request);

    Page<DoctorResponse> getAllDoctors(Boolean active, int page, int size, String sortBy, String sortDir);

    Page<DoctorResponse> searchDoctors(String keyword, int page, int size, String sortBy, String sortDir);

    DoctorResponse getDoctorById(Long id);

    DoctorResponse updateDoctor(Long id, DoctorUpdateRequest request);

    void deleteDoctor(Long id);
}
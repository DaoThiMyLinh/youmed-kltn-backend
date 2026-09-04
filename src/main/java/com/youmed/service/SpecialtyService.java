package com.youmed.service;

import com.youmed.dto.request.SpecialtyRequest;
import com.youmed.dto.response.SpecialtyResponse;
import org.springframework.data.domain.Page;

public interface SpecialtyService {
    SpecialtyResponse createSpecialty(SpecialtyRequest request);
    SpecialtyResponse updateSpecialty(Long id, SpecialtyRequest request);
    SpecialtyResponse getSpecialtyById(Long id);
    Page<SpecialtyResponse> getAllSpecialties(Boolean active, int page, int size, String sortBy, String sortDir);
    Page<SpecialtyResponse> searchSpecialties(String keyword, int page, int size, String sortBy, String sortDir);
    void deleteSpecialty(Long id);
}

package com.youmed.service.impl;

import com.youmed.dto.request.SpecialtyRequest;
import com.youmed.dto.response.SpecialtyResponse;
import com.youmed.entity.Specialty;
import com.youmed.exception.DuplicateResourceException;
import com.youmed.exception.ResourceNotFoundException;
import com.youmed.repository.SpecialtyRepository;
import com.youmed.repository.UserRepository;
import com.youmed.service.SpecialtyService;
import com.youmed.entity.User;
import com.youmed.enums.Role;
import com.youmed.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final UserRepository userRepository;

    @Override
    public SpecialtyResponse createSpecialty(SpecialtyRequest request) {
        String normalizedName = normalizeName(request.getName());
        
        if (specialtyRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateResourceException("Specialty name already exists");
        }

        User manager = null;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            if (manager.getRole() != Role.DEPARTMENT_MANAGER) {
                throw new BadRequestException("User must have DEPARTMENT_MANAGER role");
            }
            if (specialtyRepository.existsByManagerId(manager.getId())) {
                throw new DuplicateResourceException("Manager is already assigned to another specialty");
            }
        }

        Specialty specialty = Specialty.builder()
                .name(normalizedName)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .manager(manager)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        
        return mapToResponse(specialtyRepository.save(specialty));
    }

    @Override
    public SpecialtyResponse updateSpecialty(Long id, SpecialtyRequest request) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));
        
        String normalizedName = normalizeName(request.getName());
        
        if (specialtyRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new DuplicateResourceException("Specialty name already exists");
        }
        
        specialty.setName(normalizedName);
        specialty.setDescription(request.getDescription());
        specialty.setImageUrl(request.getImageUrl());
        
        if (request.getActive() != null) {
            specialty.setActive(request.getActive());
        }

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            if (manager.getRole() != Role.DEPARTMENT_MANAGER) {
                throw new BadRequestException("User must have DEPARTMENT_MANAGER role");
            }
            if (specialtyRepository.existsByManagerIdAndIdNot(manager.getId(), id)) {
                throw new DuplicateResourceException("Manager is already assigned to another specialty");
            }
            specialty.setManager(manager);
        } else {
            specialty.setManager(null);
        }
        
        return mapToResponse(specialtyRepository.save(specialty));
    }

    @Override
    public SpecialtyResponse getSpecialtyById(Long id) {
        return specialtyRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));
    }

    @Override
    public Page<SpecialtyResponse> getAllSpecialties(Boolean active, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        if (active != null) {
            return specialtyRepository.findByActive(active, pageable).map(this::mapToResponse);
        }
        return specialtyRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public Page<SpecialtyResponse> searchSpecialties(String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return specialtyRepository.findAll(pageable).map(this::mapToResponse);
        }
        return specialtyRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable).map(this::mapToResponse);
    }

    @Override
    public void deleteSpecialty(Long id) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));
        specialtyRepository.delete(specialty);
    }

    private SpecialtyResponse mapToResponse(Specialty specialty) {
        return SpecialtyResponse.builder()
                .id(specialty.getId())
                .name(specialty.getName())
                .description(specialty.getDescription())
                .imageUrl(specialty.getImageUrl())
                .active(specialty.getActive())
                .managerId(specialty.getManager() != null ? specialty.getManager().getId() : null)
                .managerName(specialty.getManager() != null ? specialty.getManager().getFullName() : null)
                .build();
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }
        String[] words = name.trim().split("\\s+");
        StringBuilder normalized = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                normalized.append(Character.toUpperCase(word.charAt(0)))
                          .append(word.substring(1).toLowerCase())
                          .append(" ");
            }
        }
        return normalized.toString().trim();
    }
}

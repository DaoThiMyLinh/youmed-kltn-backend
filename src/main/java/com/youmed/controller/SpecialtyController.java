package com.youmed.controller;

import com.youmed.dto.request.SpecialtyRequest;
import com.youmed.dto.response.SpecialtyResponse;
import com.youmed.service.SpecialtyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/specialties")
@RequiredArgsConstructor
@Tag(name = "Specialty", description = "Specialty management APIs")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @GetMapping
    public Page<SpecialtyResponse> getAllSpecialties(
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        return specialtyService.getAllSpecialties(active, page, size, sortBy, sortDir);
    }

    @GetMapping("/search")
    public Page<SpecialtyResponse> searchSpecialties(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        return specialtyService.searchSpecialties(keyword, page, size, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public SpecialtyResponse getSpecialtyById(@PathVariable Long id) {
        return specialtyService.getSpecialtyById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public SpecialtyResponse createSpecialty(@Valid @RequestBody SpecialtyRequest request) {
        return specialtyService.createSpecialty(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SpecialtyResponse updateSpecialty(@PathVariable Long id, @Valid @RequestBody SpecialtyRequest request) {
        return specialtyService.updateSpecialty(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteSpecialty(@PathVariable Long id) {
        specialtyService.deleteSpecialty(id);
        return "Specialty deleted successfully";
    }
}

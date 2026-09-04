package com.youmed.controller;

import com.youmed.dto.request.DoctorCreateRequest;
import com.youmed.dto.request.DoctorUpdateRequest;
import com.youmed.dto.response.DoctorResponse;
import com.youmed.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor", description = "Doctor management APIs")
public class DoctorController {

    private final DoctorService doctorService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorResponse createDoctor(
            @Valid @RequestBody DoctorCreateRequest request
    ){
        return doctorService.createDoctor(request);
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT')")
    public Page<DoctorResponse> getAllDoctors(
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ){

        return doctorService.getAllDoctors(active, page, size, sortBy, sortDir);
    }


    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT')")
    public Page<DoctorResponse> searchDoctors(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ){

        return doctorService.searchDoctors(keyword, page, size, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT')")
    public DoctorResponse getDoctorById(
            @PathVariable Long id
    ){

        return doctorService.getDoctorById(id);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorResponse updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorUpdateRequest request
    ){

        return doctorService.updateDoctor(id, request);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteDoctor(
            @PathVariable Long id
    ){

        doctorService.deleteDoctor(id);

        return "Doctor deleted successfully";
    }
}
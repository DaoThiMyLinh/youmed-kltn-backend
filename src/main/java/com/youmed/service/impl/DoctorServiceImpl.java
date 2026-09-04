package com.youmed.service.impl;

import com.youmed.dto.request.DoctorCreateRequest;
import com.youmed.dto.request.DoctorUpdateRequest;
import com.youmed.dto.response.DoctorResponse;
import com.youmed.entity.Doctor;
import com.youmed.repository.DoctorRepository;
import com.youmed.repository.SpecialtyRepository;
import com.youmed.entity.Specialty;
import com.youmed.dto.response.SpecialtyResponse;
import com.youmed.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.youmed.repository.UserRepository;
import com.youmed.entity.User;
import com.youmed.enums.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.youmed.exception.ResourceNotFoundException;
import com.youmed.exception.DuplicateResourceException;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public DoctorResponse createDoctor(DoctorCreateRequest request) {

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user != null) {
            if (user.getRole() == Role.DOCTOR) {
                throw new DuplicateResourceException("Doctor email already exists");
            }
            user.setRole(Role.DOCTOR);
            user.setFullName(request.getFullName());
            user.setPhone(request.getPhone());
        } else {
            user = User.builder()
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .password(passwordEncoder.encode("bs123"))
                    .role(Role.DOCTOR)
                    .build();
        }
        user = userRepository.save(user);

        Specialty specialty = null;
        if (request.getSpecialtyId() != null) {
            specialty = specialtyRepository.findById(request.getSpecialtyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));
        }

        Doctor doctor = Doctor.builder()
                .user(user)
                .specialty(specialty)
                .consultationFee(request.getConsultationFee())
                .biography(request.getBiography())
                .active(true)
                .build();

        return mapToResponse(
                doctorRepository.save(doctor)
        );
    }


    @Override
    public Page<DoctorResponse> getAllDoctors(Boolean active, int page, int size, String sortBy, String sortDir) {
        
        if ("fullName".equals(sortBy)) {
            sortBy = "user.fullName";
        } else if ("email".equals(sortBy)) {
            sortBy = "user.email";
        } else if ("phone".equals(sortBy)) {
            sortBy = "user.phone";
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (active == null) {
            return doctorRepository.findAll(pageable).map(this::mapToResponse);
        }
        
        return doctorRepository.findByActive(active, pageable).map(this::mapToResponse);
    }


    @Override
    public Page<DoctorResponse> searchDoctors(String keyword, int page, int size, String sortBy, String sortDir) {
        if ("fullName".equals(sortBy)) {
            sortBy = "user.fullName";
        } else if ("email".equals(sortBy)) {
            sortBy = "user.email";
        } else if ("phone".equals(sortBy)) {
            sortBy = "user.phone";
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (keyword == null || keyword.trim().isEmpty()) {
            return doctorRepository.findAll(pageable).map(this::mapToResponse);
        }

        return doctorRepository.findByUserFullNameContainingIgnoreCaseOrSpecialtyNameContainingIgnoreCase(keyword.trim(), keyword.trim(), pageable)
                .map(this::mapToResponse);
    }

    @Override
    public DoctorResponse getDoctorById(Long id) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor not found")
                );

        return mapToResponse(doctor);
    }


    @Override
    public DoctorResponse updateDoctor(Long id, DoctorUpdateRequest request) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor not found")
                );


        User user = doctor.getUser();
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        userRepository.save(user);

        if (request.getSpecialtyId() != null) {
            Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));
            doctor.setSpecialty(specialty);
        }
        
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setBiography(request.getBiography());

        if (request.getActive() != null) {
            doctor.setActive(request.getActive());
        }


        return mapToResponse(
                doctorRepository.save(doctor)
        );
    }


    @Override
    public void deleteDoctor(Long id) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor not found")
                );

        doctorRepository.delete(doctor);
    }



    private DoctorResponse mapToResponse(Doctor doctor){

        SpecialtyResponse specialtyResponse = null;
        if (doctor.getSpecialty() != null) {
            specialtyResponse = SpecialtyResponse.builder()
                    .id(doctor.getSpecialty().getId())
                    .name(doctor.getSpecialty().getName())
                    .description(doctor.getSpecialty().getDescription())
                    .imageUrl(doctor.getSpecialty().getImageUrl())
                    .active(doctor.getSpecialty().getActive())
                    .build();
        }

        return DoctorResponse.builder()
                .id(doctor.getId())
                .fullName(doctor.getUser().getFullName())
                .specialty(specialtyResponse)
                .consultationFee(doctor.getConsultationFee())
                .phone(doctor.getUser().getPhone())
                .email(doctor.getUser().getEmail())
                .biography(doctor.getBiography())
                .active(doctor.getActive())
                .createdAt(doctor.getCreatedAt())
                .build();
    }
}
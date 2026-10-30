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

import com.youmed.repository.DoctorReviewRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorReviewRepository doctorReviewRepository;

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


    private record RatingStats(Double avgRating, Long totalReviews) {}

    private Map<Long, RatingStats> getRatingMap() {
        Map<Long, RatingStats> map = new HashMap<>();
        if (doctorReviewRepository == null) return map;
        List<Object[]> rows = doctorReviewRepository.getAllDoctorsRatingSummary();
        if (rows != null) {
            for (Object[] row : rows) {
                Long docId = (Long) row[0];
                Double avg = (Double) row[1];
                Long count = (Long) row[2];
                double rounded = 0.0;
                if (avg != null && count != null && count > 0) {
                    rounded = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue();
                }
                map.put(docId, new RatingStats(rounded, count != null ? count : 0L));
            }
        }
        return map;
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

        Map<Long, RatingStats> ratingMap = getRatingMap();

        if (active == null) {
            return doctorRepository.findAllDoctorsWithCorrectRole(pageable)
                    .map(doctor -> mapToResponse(doctor, ratingMap));
        }
        
        return doctorRepository.findByActiveWithCorrectRole(active, pageable)
                .map(doctor -> mapToResponse(doctor, ratingMap));
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

        Map<Long, RatingStats> ratingMap = getRatingMap();

        if (keyword == null || keyword.trim().isEmpty()) {
            return doctorRepository.findAllDoctorsWithCorrectRole(pageable)
                    .map(doctor -> mapToResponse(doctor, ratingMap));
        }

        return doctorRepository.searchDoctorsWithCorrectRole(keyword.trim(), pageable)
                .map(doctor -> mapToResponse(doctor, ratingMap));
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

        if (request.getSpecialtyId() != null) {
            Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));
            doctor.setSpecialty(specialty);
        }
        
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setBiography(request.getBiography());

        if (request.getActive() != null) {
            doctor.setActive(request.getActive());
            user.setEnabled(request.getActive());
        }

        userRepository.save(user);


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



    private DoctorResponse mapToResponse(Doctor doctor) {
        return mapToResponse(doctor, null);
    }

    private DoctorResponse mapToResponse(Doctor doctor, Map<Long, RatingStats> ratingMap) {

        SpecialtyResponse specialtyResponse = null;
        if (doctor.getSpecialty() != null) {
            specialtyResponse = SpecialtyResponse.builder()
                    .id(doctor.getSpecialty().getId())
                    .name(doctor.getSpecialty().getName())
                    .description(doctor.getSpecialty().getDescription())
                    .active(doctor.getSpecialty().getActive())
                    .imageUrl(doctor.getSpecialty().getImageUrl())
                    .build();
        }

        double avgRating = 0.0;
        long totalReviews = 0L;

        if (ratingMap != null && ratingMap.containsKey(doctor.getId())) {
            RatingStats stats = ratingMap.get(doctor.getId());
            avgRating = stats.avgRating() != null ? stats.avgRating() : 0.0;
            totalReviews = stats.totalReviews() != null ? stats.totalReviews() : 0L;
        } else if (doctorReviewRepository != null) {
            Double dbAvg = doctorReviewRepository.getAverageRatingByDoctorId(doctor.getId());
            Long dbCount = doctorReviewRepository.countByDoctorId(doctor.getId());
            if (dbAvg != null && dbCount != null && dbCount > 0) {
                avgRating = BigDecimal.valueOf(dbAvg).setScale(1, RoundingMode.HALF_UP).doubleValue();
                totalReviews = dbCount;
            }
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
                .imageUrl(doctor.getImageUrl())
                .createdAt(doctor.getCreatedAt())
                .averageRating(avgRating)
                .totalReviews(totalReviews)
                .build();
    }
}
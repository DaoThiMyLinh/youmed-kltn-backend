package com.youmed.service.impl;

import com.youmed.dto.request.CreateDoctorReviewRequest;
import com.youmed.dto.response.DoctorRatingSummaryResponse;
import com.youmed.dto.response.DoctorReviewResponse;
import com.youmed.entity.Appointment;
import com.youmed.entity.Doctor;
import com.youmed.entity.DoctorReview;
import com.youmed.entity.User;
import com.youmed.enums.AppointmentStatus;
import com.youmed.enums.Role;
import com.youmed.exception.BadRequestException;
import com.youmed.exception.DuplicateResourceException;
import com.youmed.exception.ResourceNotFoundException;
import com.youmed.repository.AppointmentRepository;
import com.youmed.repository.DoctorRepository;
import com.youmed.repository.DoctorReviewRepository;
import com.youmed.repository.UserRepository;
import com.youmed.service.DoctorReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorReviewServiceImpl implements DoctorReviewService {

    private final DoctorReviewRepository doctorReviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DoctorReviewResponse createReview(CreateDoctorReviewRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        if (currentUser.getRole() != Role.PATIENT) {
            throw new BadRequestException("Chỉ bệnh nhân (PATIENT) mới có quyền tạo đánh giá");
        }

        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Số sao đánh giá phải từ 1 đến 5");
        }

        if (request.getComment() != null && request.getComment().trim().length() > 1000) {
            throw new BadRequestException("Bình luận không được vượt quá 1000 ký tự");
        }

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch khám với ID: " + request.getAppointmentId()));

        if (!appointment.getPatient().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Bạn chỉ có thể đánh giá lịch khám của chính mình");
        }

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Chỉ có thể đánh giá các lịch khám đã hoàn thành (COMPLETED)");
        }

        if (doctorReviewRepository.existsByAppointmentId(appointment.getId())) {
            throw new DuplicateResourceException("Lịch khám này đã được đánh giá trước đó");
        }

        Doctor doctor = appointment.getDoctor();
        if (doctor == null) {
            throw new ResourceNotFoundException("Bác sĩ không tồn tại cho lịch khám này");
        }

        String sanitizedComment = (request.getComment() != null && !request.getComment().trim().isEmpty())
                ? request.getComment().trim()
                : null;

        DoctorReview review = DoctorReview.builder()
                .appointment(appointment)
                .patient(currentUser)
                .doctor(doctor)
                .rating(request.getRating())
                .comment(sanitizedComment)
                .build();

        try {
            review = doctorReviewRepository.save(review);
        } catch (DataIntegrityViolationException ex) {
            log.error("Duplicate review attempted for appointment {}", appointment.getId(), ex);
            throw new DuplicateResourceException("Lịch khám này đã được đánh giá trước đó");
        }

        return mapToResponse(review, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorReviewResponse> getReviewsByDoctorId(Long doctorId, int page, int size) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Không tìm thấy bác sĩ với ID: " + doctorId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DoctorReview> reviews = doctorReviewRepository.findByDoctorId(doctorId, pageable);

        return reviews.map(r -> mapToResponse(r, true));
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorRatingSummaryResponse getDoctorRatingSummary(Long doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Không tìm thấy bác sĩ với ID: " + doctorId);
        }

        Double avgRating = doctorReviewRepository.getAverageRatingByDoctorId(doctorId);
        Long totalReviews = doctorReviewRepository.countByDoctorId(doctorId);

        double roundedAvg = 0.0;
        if (avgRating != null && totalReviews != null && totalReviews > 0) {
            roundedAvg = BigDecimal.valueOf(avgRating)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }

        List<Object[]> distResults = doctorReviewRepository.getRatingDistributionByDoctorId(doctorId);
        if (distResults != null) {
            for (Object[] row : distResults) {
                Integer star = (Integer) row[0];
                Long count = (Long) row[1];
                if (star != null && star >= 1 && star <= 5) {
                    distribution.put(star, count);
                }
            }
        }

        return DoctorRatingSummaryResponse.builder()
                .doctorId(doctorId)
                .averageRating(roundedAvg)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
                .ratingDistribution(distribution)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorReviewResponse getReviewByAppointmentId(Long appointmentId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch khám với ID: " + appointmentId));

        boolean isPatient = currentUser.getRole() == Role.PATIENT;
        boolean isOwnerPatient = isPatient && appointment.getPatient().getId().equals(currentUser.getId());
        boolean isDoctor = currentUser.getRole() == Role.DOCTOR && appointment.getDoctor().getUser().getId().equals(currentUser.getId());
        boolean isAdminOrManager = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.DEPARTMENT_MANAGER || currentUser.getRole() == Role.RECEPTIONIST;

        if (!isOwnerPatient && !isDoctor && !isAdminOrManager) {
            throw new BadRequestException("Bạn không có quyền xem đánh giá của lịch khám này");
        }

        DoctorReview review = doctorReviewRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Chưa có đánh giá nào cho lịch khám này"));

        return mapToResponse(review, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorReviewResponse> getMyReviews(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return doctorReviewRepository.findByPatientId(currentUser.getId(), pageable)
                .map(r -> mapToResponse(r, false));
    }

    private DoctorReviewResponse mapToResponse(DoctorReview review, boolean maskPatient) {
        String patientName = review.getPatient().getFullName();
        if (maskPatient) {
            patientName = maskPatientName(patientName);
        }

        String doctorName = review.getDoctor().getUser() != null
                ? review.getDoctor().getUser().getFullName()
                : "Bác sĩ";

        return DoctorReviewResponse.builder()
                .id(review.getId())
                .appointmentId(review.getAppointment().getId())
                .doctorId(review.getDoctor().getId())
                .doctorName(doctorName)
                .patientId(review.getPatient().getId())
                .patientName(patientName)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private String maskPatientName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Bệnh nhân ẩn danh";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1) + "***";
        }
        // Example: "Nguyễn Văn Nam" -> "Nguyễn V***"
        StringBuilder masked = new StringBuilder(parts[0]);
        masked.append(" ");
        String second = parts[1];
        masked.append(second.substring(0, 1)).append("***");
        return masked.toString();
    }
}

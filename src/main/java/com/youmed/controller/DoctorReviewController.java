package com.youmed.controller;

import com.youmed.dto.request.CreateDoctorReviewRequest;
import com.youmed.dto.response.DoctorRatingSummaryResponse;
import com.youmed.dto.response.DoctorReviewResponse;
import com.youmed.service.DoctorReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Doctor Reviews", description = "APIs for doctor reviews and ratings")
public class DoctorReviewController {

    private final DoctorReviewService doctorReviewService;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Tạo đánh giá bác sĩ sau khi hoàn thành buổi khám")
    public ResponseEntity<DoctorReviewResponse> createReview(
            @Valid @RequestBody CreateDoctorReviewRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(doctorReviewService.createReview(request));
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy đánh giá của một lịch khám cụ thể")
    public ResponseEntity<DoctorReviewResponse> getReviewByAppointmentId(
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.ok(doctorReviewService.getReviewByAppointmentId(appointmentId));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Lấy danh sách đánh giá của bệnh nhân hiện tại")
    public ResponseEntity<Page<DoctorReviewResponse>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(doctorReviewService.getMyReviews(page, size));
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Lấy danh sách đánh giá công khai của bác sĩ (có phân trang)")
    public ResponseEntity<Page<DoctorReviewResponse>> getReviewsByDoctorId(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(doctorReviewService.getReviewsByDoctorId(doctorId, page, size));
    }

    @GetMapping("/doctor/{doctorId}/summary")
    @Operation(summary = "Lấy điểm trung bình, tổng lượt đánh giá và phân bố sao của bác sĩ")
    public ResponseEntity<DoctorRatingSummaryResponse> getDoctorRatingSummary(
            @PathVariable Long doctorId
    ) {
        return ResponseEntity.ok(doctorReviewService.getDoctorRatingSummary(doctorId));
    }
}

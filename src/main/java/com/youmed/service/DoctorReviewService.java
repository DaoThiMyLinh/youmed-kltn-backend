package com.youmed.service;

import com.youmed.dto.request.CreateDoctorReviewRequest;
import com.youmed.dto.response.DoctorRatingSummaryResponse;
import com.youmed.dto.response.DoctorReviewResponse;
import org.springframework.data.domain.Page;

public interface DoctorReviewService {

    DoctorReviewResponse createReview(CreateDoctorReviewRequest request);

    Page<DoctorReviewResponse> getReviewsByDoctorId(Long doctorId, int page, int size);

    DoctorRatingSummaryResponse getDoctorRatingSummary(Long doctorId);

    DoctorReviewResponse getReviewByAppointmentId(Long appointmentId);

    Page<DoctorReviewResponse> getMyReviews(int page, int size);
}

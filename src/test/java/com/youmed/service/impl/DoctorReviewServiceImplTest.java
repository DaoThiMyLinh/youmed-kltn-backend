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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorReviewServiceImplTest {

    @Mock
    private DoctorReviewRepository doctorReviewRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DoctorReviewServiceImpl doctorReviewService;

    private User patientUser;
    private User otherPatientUser;
    private User doctorUser;
    private Doctor doctor;
    private Appointment completedAppointment;
    private CreateDoctorReviewRequest validRequest;

    @BeforeEach
    void setUp() {
        patientUser = User.builder()
                .id(1L)
                .email("patient@gmail.com")
                .fullName("Nguyễn Văn A")
                .role(Role.PATIENT)
                .build();

        otherPatientUser = User.builder()
                .id(2L)
                .email("other@gmail.com")
                .fullName("Trần Thị B")
                .role(Role.PATIENT)
                .build();

        doctorUser = User.builder()
                .id(10L)
                .email("doctor@youmed.com")
                .fullName("BSCKII Lê Văn C")
                .role(Role.DOCTOR)
                .build();

        doctor = Doctor.builder()
                .id(100L)
                .user(doctorUser)
                .active(true)
                .build();

        completedAppointment = Appointment.builder()
                .id(50L)
                .patient(patientUser)
                .doctor(doctor)
                .status(AppointmentStatus.COMPLETED)
                .build();

        validRequest = CreateDoctorReviewRequest.builder()
                .appointmentId(50L)
                .rating(5)
                .comment("Bác sĩ rất tận tâm và chuyên môn cao")
                .build();
    }

    private void mockSecurityUser(User user) {
        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(user.getEmail());
        lenient().when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void createReview_Success() {
        mockSecurityUser(patientUser);
        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(completedAppointment));
        when(doctorReviewRepository.existsByAppointmentId(50L)).thenReturn(false);

        DoctorReview savedReview = DoctorReview.builder()
                .id(1L)
                .appointment(completedAppointment)
                .patient(patientUser)
                .doctor(doctor)
                .rating(5)
                .comment(validRequest.getComment())
                .build();

        when(doctorReviewRepository.save(any(DoctorReview.class))).thenReturn(savedReview);

        DoctorReviewResponse response = doctorReviewService.createReview(validRequest);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals(validRequest.getComment(), response.getComment());
        assertEquals(patientUser.getFullName(), response.getPatientName());
        verify(doctorReviewRepository, times(1)).save(any(DoctorReview.class));
    }

    @Test
    void createReview_ThrowsException_WhenNonPatientRole() {
        User receptionistUser = User.builder()
                .id(3L)
                .email("receptionist@youmed.com")
                .role(Role.RECEPTIONIST)
                .build();
        mockSecurityUser(receptionistUser);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                doctorReviewService.createReview(validRequest));

        assertTrue(ex.getMessage().contains("Chỉ bệnh nhân (PATIENT) mới có quyền tạo đánh giá"));
        verify(doctorReviewRepository, never()).save(any());
    }

    @Test
    void createReview_ThrowsException_WhenRatingOutOfRange() {
        mockSecurityUser(patientUser);
        validRequest.setRating(6);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                doctorReviewService.createReview(validRequest));

        assertTrue(ex.getMessage().contains("Số sao đánh giá phải từ 1 đến 5"));
        verify(doctorReviewRepository, never()).save(any());
    }

    @Test
    void createReview_ThrowsException_WhenAppointmentNotBelongToPatient() {
        mockSecurityUser(otherPatientUser);
        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(completedAppointment));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                doctorReviewService.createReview(validRequest));

        assertTrue(ex.getMessage().contains("Bạn chỉ có thể đánh giá lịch khám của chính mình"));
        verify(doctorReviewRepository, never()).save(any());
    }

    @Test
    void createReview_ThrowsException_WhenAppointmentNotCompleted() {
        mockSecurityUser(patientUser);
        completedAppointment.setStatus(AppointmentStatus.IN_PROGRESS);
        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(completedAppointment));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                doctorReviewService.createReview(validRequest));

        assertTrue(ex.getMessage().contains("Chỉ có thể đánh giá các lịch khám đã hoàn thành"));
        verify(doctorReviewRepository, never()).save(any());
    }

    @Test
    void createReview_ThrowsException_WhenAppointmentAlreadyReviewed() {
        mockSecurityUser(patientUser);
        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(completedAppointment));
        when(doctorReviewRepository.existsByAppointmentId(50L)).thenReturn(true);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () ->
                doctorReviewService.createReview(validRequest));

        assertTrue(ex.getMessage().contains("Lịch khám này đã được đánh giá trước đó"));
        verify(doctorReviewRepository, never()).save(any());
    }

    @Test
    void getDoctorRatingSummary_CalculatesAccurately() {
        when(doctorRepository.existsById(100L)).thenReturn(true);
        when(doctorReviewRepository.getAverageRatingByDoctorId(100L)).thenReturn(4.66666);
        when(doctorReviewRepository.countByDoctorId(100L)).thenReturn(15L);

        List<Object[]> distributionList = new ArrayList<>();
        distributionList.add(new Object[]{5, 10L});
        distributionList.add(new Object[]{4, 4L});
        distributionList.add(new Object[]{3, 1L});
        when(doctorReviewRepository.getRatingDistributionByDoctorId(100L)).thenReturn(distributionList);

        DoctorRatingSummaryResponse summary = doctorReviewService.getDoctorRatingSummary(100L);

        assertNotNull(summary);
        assertEquals(4.7, summary.getAverageRating());
        assertEquals(15L, summary.getTotalReviews());
        assertEquals(10L, summary.getRatingDistribution().get(5));
        assertEquals(4L, summary.getRatingDistribution().get(4));
        assertEquals(1L, summary.getRatingDistribution().get(3));
        assertEquals(0L, summary.getRatingDistribution().get(2));
        assertEquals(0L, summary.getRatingDistribution().get(1));
    }

    @Test
    void getReviewsByDoctorId_MasksPatientNameCorrectly() {
        when(doctorRepository.existsById(100L)).thenReturn(true);

        DoctorReview review = DoctorReview.builder()
                .id(1L)
                .appointment(completedAppointment)
                .patient(patientUser) // "Nguyễn Văn A"
                .doctor(doctor)
                .rating(5)
                .comment("Rất tốt")
                .build();

        Page<DoctorReview> reviewPage = new PageImpl<>(List.of(review));
        when(doctorReviewRepository.findByDoctorId(eq(100L), any(Pageable.class))).thenReturn(reviewPage);

        Page<DoctorReviewResponse> result = doctorReviewService.getReviewsByDoctorId(100L, 0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals("Nguyễn V***", result.getContent().get(0).getPatientName());
    }
}

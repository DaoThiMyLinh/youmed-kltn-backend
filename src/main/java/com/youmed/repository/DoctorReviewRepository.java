package com.youmed.repository;

import com.youmed.entity.DoctorReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorReviewRepository extends JpaRepository<DoctorReview, Long> {

    Optional<DoctorReview> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);

    Page<DoctorReview> findByDoctorId(Long doctorId, Pageable pageable);

    Page<DoctorReview> findByPatientId(Long patientId, Pageable pageable);

    Optional<DoctorReview> findByAppointmentIdAndPatientId(Long appointmentId, Long patientId);

    @Query("SELECT AVG(r.rating) FROM DoctorReview r WHERE r.doctor.id = :doctorId")
    Double getAverageRatingByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT COUNT(r) FROM DoctorReview r WHERE r.doctor.id = :doctorId")
    Long countByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT r.rating, COUNT(r) FROM DoctorReview r WHERE r.doctor.id = :doctorId GROUP BY r.rating")
    List<Object[]> getRatingDistributionByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT r.doctor.id, AVG(r.rating), COUNT(r) FROM DoctorReview r GROUP BY r.doctor.id")
    List<Object[]> getAllDoctorsRatingSummary();
}

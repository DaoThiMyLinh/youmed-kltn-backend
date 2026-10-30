package com.youmed.repository;

import com.youmed.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.youmed.enums.AppointmentStatus;
import java.time.LocalDateTime;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    Page<Appointment> findByPatientId(
            Long patientId,
            Pageable pageable
    );

    Page<Appointment> findByPatientIdAndStatus(
            Long patientId,
            AppointmentStatus status,
            Pageable pageable
    );

    Page<Appointment> findByDoctorId(
            Long doctorId,
            Pageable pageable
    );

    Page<Appointment> findByDoctorIdAndStatus(
            Long doctorId,
            AppointmentStatus status,
            Pageable pageable
    );

    boolean existsByDoctorIdAndTimeSlotStartDateTime(
            Long doctorId,
            LocalDateTime startDateTime
    );

    boolean existsByPatientIdAndTimeSlotStartDateTimeAndStatusNot(
            Long patientId,
            LocalDateTime startDateTime,
            AppointmentStatus status
    );

    Page<Appointment> findByStatus(
            AppointmentStatus status,
            Pageable pageable
    );

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Appointment a WHERE " +
            "(:keyword = '' OR LOWER(a.patient.fullName) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) " +
            "OR a.patient.phone LIKE CONCAT('%', CAST(:keyword AS string), '%') " +
            "OR LOWER(a.doctor.user.fullName) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) " +
            "OR LOWER(a.doctor.specialty.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) " +
            "OR CAST(a.id AS string) LIKE CONCAT('%', CAST(:keyword AS string), '%')) " +
            "AND (:status = '' OR CAST(a.status AS string) = :status) " +
            "AND (CAST(:startDate AS java.time.LocalDateTime) IS NULL OR a.appointmentTime >= :startDate) " +
            "AND (CAST(:endDate AS java.time.LocalDateTime) IS NULL OR a.appointmentTime <= :endDate)")
    Page<Appointment> searchAppointments(
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            @org.springframework.data.repository.query.Param("status") String status,
            @org.springframework.data.repository.query.Param("startDate") LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") LocalDateTime endDate,
            Pageable pageable);

}
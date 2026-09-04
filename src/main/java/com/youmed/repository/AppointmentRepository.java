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

}
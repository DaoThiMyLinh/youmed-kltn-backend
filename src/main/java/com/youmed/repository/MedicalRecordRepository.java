package com.youmed.repository;

import com.youmed.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);

    boolean existsByAppointmentId(Long appointmentId);

    Page<MedicalRecord> findByAppointmentDoctorId(Long doctorId, Pageable pageable);

    Page<MedicalRecord> findByAppointmentPatientId(Long patientId, Pageable pageable);

    Page<MedicalRecord> findByAppointmentDoctorIdAndAppointmentPatientId(Long doctorId, Long patientId, Pageable pageable);
}

package com.youmed.repository;

import com.youmed.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByMedicalRecordId(Long medicalRecordId);
    boolean existsByMedicalRecordId(Long medicalRecordId);
}

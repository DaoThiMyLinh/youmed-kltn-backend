package com.youmed.repository;

import com.youmed.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    
    Page<Doctor> findByUserFullNameContainingIgnoreCaseOrSpecialtyNameContainingIgnoreCase(String fullName, String specialtyName, Pageable pageable);
    
    Page<Doctor> findByActive(Boolean active, Pageable pageable);
}
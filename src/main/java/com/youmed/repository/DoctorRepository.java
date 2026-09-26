package com.youmed.repository;

import com.youmed.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    
    Page<Doctor> findByUserFullNameContainingIgnoreCaseOrSpecialtyNameContainingIgnoreCase(String fullName, String specialtyName, Pageable pageable);
    
    Page<Doctor> findByActive(Boolean active, Pageable pageable);
    
    java.util.List<Doctor> findBySpecialtyId(Long specialtyId);
    
    @Query("SELECT d FROM Doctor d WHERE d.user.role IN ('DOCTOR', 'DEPARTMENT_MANAGER')")
    Page<Doctor> findAllDoctorsWithCorrectRole(Pageable pageable);

    @Query("SELECT d FROM Doctor d WHERE d.user.role IN ('DOCTOR', 'DEPARTMENT_MANAGER') AND d.active = :active")
    Page<Doctor> findByActiveWithCorrectRole(@Param("active") Boolean active, Pageable pageable);

    @Query("SELECT d FROM Doctor d WHERE d.user.role IN ('DOCTOR', 'DEPARTMENT_MANAGER') AND (LOWER(d.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.specialty.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Doctor> searchDoctorsWithCorrectRole(@Param("keyword") String keyword, Pageable pageable);
}
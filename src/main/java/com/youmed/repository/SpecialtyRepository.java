package com.youmed.repository;

import com.youmed.entity.Specialty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
    Page<Specialty> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Specialty> findByActive(Boolean active, Pageable pageable);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}

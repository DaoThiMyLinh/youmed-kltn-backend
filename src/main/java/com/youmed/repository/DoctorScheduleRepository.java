package com.youmed.repository;

import com.youmed.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {
    Optional<DoctorSchedule> findByDoctorIdAndWorkingDate(Long doctorId, LocalDate workingDate);
    List<DoctorSchedule> findByDoctorId(Long doctorId);
}

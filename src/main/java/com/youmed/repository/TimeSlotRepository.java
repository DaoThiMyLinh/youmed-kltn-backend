package com.youmed.repository;

import com.youmed.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByDoctorScheduleId(Long scheduleId);

    List<TimeSlot> findByBookedFalse();

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.doctorSchedule.doctor.id = :doctorId AND ts.doctorSchedule.workingDate = :date ORDER BY ts.startDateTime ASC")
    List<TimeSlot> findAvailableSlots(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
}

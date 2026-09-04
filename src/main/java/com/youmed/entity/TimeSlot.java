package com.youmed.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "time_slots", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"doctor_schedule_id", "start_date_time"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_schedule_id", nullable = false)
    private DoctorSchedule doctorSchedule;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column(nullable = false)
    @Builder.Default
    private Boolean booked = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Version
    private Long version;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;
}

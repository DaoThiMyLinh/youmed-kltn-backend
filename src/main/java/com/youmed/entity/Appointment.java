package com.youmed.entity;


import com.youmed.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment extends BaseEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "patient_id",
            nullable = false
    )
    private User patient;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "doctor_id",
            nullable = false
    )
    private Doctor doctor;



    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "time_slot_id",
            unique = true
    )
    private TimeSlot timeSlot;

    @Column(nullable = false)
    private LocalDateTime appointmentTime;



    @Column(length = 500)
    private String reason;



    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

}
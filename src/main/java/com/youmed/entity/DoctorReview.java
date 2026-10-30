package com.youmed.entity;

import jakarta.persistence.*;
import lombok.*;

@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(
        name = "doctor_reviews",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_doctor_reviews_appointment", columnNames = {"appointment_id"})
        },
        indexes = {
                @Index(name = "idx_doctor_reviews_doctor_id", columnList = "doctor_id"),
                @Index(name = "idx_doctor_reviews_patient_id", columnList = "patient_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "appointment_id",
            nullable = false,
            unique = true
    )
    private Appointment appointment;

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

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT", length = 1000)
    private String comment;
}

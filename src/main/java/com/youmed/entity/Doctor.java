package com.youmed.entity;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor extends BaseEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id")
    private Specialty specialty;

    @Column(name = "consultation_fee")
    private Double consultationFee;
    @Column(columnDefinition = "TEXT")
    private String biography;


    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

}
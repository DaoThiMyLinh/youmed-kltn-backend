package com.youmed.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "symptoms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Symptom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}

package com.youmed.entity;

import com.youmed.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String fullName;


    @Column(nullable = false, unique = true)
    private String email;


    @Column(nullable = false, length = 255)
    private String password;


    @Column(unique = true)
    private String phone;

    @Column
    private String address;

    @Column
    private String gender;

    @Column
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;


    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @OneToOne(mappedBy = "user")
    private Doctor doctor;

}
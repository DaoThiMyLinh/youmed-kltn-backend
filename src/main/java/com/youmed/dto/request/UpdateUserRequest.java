package com.youmed.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateUserRequest {

    private String fullName;

    private String phone;

    private String address;

    private String gender;

    private LocalDate dateOfBirth;
}
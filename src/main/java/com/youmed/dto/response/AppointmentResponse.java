package com.youmed.dto.response;


import com.youmed.enums.AppointmentStatus;
import lombok.Builder;
import lombok.Data;


import java.time.LocalDateTime;


@Data
@Builder
public class AppointmentResponse {


    private Long id;



    private Long patientId;


    private String patientName;
    
    private String patientPhone;
    
    private String patientGender;



    private Long doctorId;


    private String doctorName;



    private String specialization;



    private LocalDateTime appointmentTime;



    private String reason;



    private AppointmentStatus status;



    private LocalDateTime createdAt;
    private Boolean hasReviewed;
    private Integer reviewRating;
    private Long reviewId;

}
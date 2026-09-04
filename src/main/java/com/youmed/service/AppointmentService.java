package com.youmed.service;

import com.youmed.dto.request.CreateAppointmentRequest;
import com.youmed.dto.response.AppointmentResponse;
import org.springframework.data.domain.Page;

public interface AppointmentService {

    AppointmentResponse createAppointment(
            CreateAppointmentRequest request
    );

    Page<AppointmentResponse> getMyAppointments(
            String status,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    Page<AppointmentResponse> getDoctorAppointments(
            String status,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    AppointmentResponse updateStatus(
            Long appointmentId,
            String status
    );

    AppointmentResponse cancelAppointment(Long appointmentId);

}
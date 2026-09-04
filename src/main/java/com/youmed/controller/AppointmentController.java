package com.youmed.controller;

import com.youmed.dto.request.CreateAppointmentRequest;
import com.youmed.dto.response.AppointmentResponse;
import com.youmed.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointment", description = "Appointment management APIs")
public class AppointmentController {

    private final AppointmentService appointmentService;

    // PATIENT tạo lịch khám
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request
    ) {

        return ResponseEntity.ok(
                appointmentService.createAppointment(request)
        );
    }

    // PATIENT xem lịch của mình
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Page<AppointmentResponse>> getMyAppointments(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {

        return ResponseEntity.ok(
                appointmentService.getMyAppointments(status, page, size, sortBy, sortDir)
        );
    }

    // DOCTOR xem lịch khám của mình
    @GetMapping("/doctor/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Page<AppointmentResponse>> getDoctorAppointments(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {

        return ResponseEntity.ok(
                appointmentService.getDoctorAppointments(
                        status,
                        page,
                        size,
                        sortBy,
                        sortDir
                )
        );
    }

    // DOCTOR cập nhật trạng thái lịch khám
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentResponse> updateStatus(

            @PathVariable Long id,

            @RequestParam String status

    ) {

        return ResponseEntity.ok(
                appointmentService.updateStatus(
                        id,
                        status
                )
        );
    }

    // PATIENT hủy lịch hẹn
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                appointmentService.cancelAppointment(id)
        );
    }

}
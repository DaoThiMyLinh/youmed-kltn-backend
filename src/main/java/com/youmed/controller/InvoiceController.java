package com.youmed.controller;

import com.youmed.entity.Invoice;
import com.youmed.enums.InvoiceType;
import com.youmed.service.InvoiceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Data
    public static class CreateInvoiceRequest {
        private Long patientId;
        private Long appointmentId;
        private BigDecimal amount;
        private InvoiceType type;
        private String description;
    }

    // RECEPTIONIST tạo hóa đơn thu phí
    @PostMapping
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<Invoice> createInvoice(@RequestBody CreateInvoiceRequest request) {
        return ResponseEntity.ok(invoiceService.createInvoice(
                request.getPatientId(),
                request.getAppointmentId(),
                request.getAmount(),
                request.getType(),
                request.getDescription()
        ));
    }

    // RECEPTIONIST/PATIENT xem hóa đơn của bệnh nhân
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<List<Invoice>> getPatientInvoices(@PathVariable Long patientId) {
        return ResponseEntity.ok(invoiceService.getPatientInvoices(patientId));
    }

    // RECEPTIONIST xem hóa đơn của 1 ca khám
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<List<Invoice>> getAppointmentInvoices(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(invoiceService.getAppointmentInvoices(appointmentId));
    }

    // RECEPTIONIST thanh toán hóa đơn
    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<Invoice> payInvoice(@PathVariable Long id, @RequestParam(required = false, defaultValue = "CASH") String paymentMethod) {
        return ResponseEntity.ok(invoiceService.payInvoice(id, paymentMethod));
    }

    // RECEPTIONIST xem tất cả hóa đơn (có phân trang và tìm kiếm)
    @GetMapping
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public ResponseEntity<org.springframework.data.domain.Page<Invoice>> getAllInvoices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(keyword, status, page, size, sortBy, sortDir));
    }
}

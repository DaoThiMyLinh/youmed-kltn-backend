package com.youmed.service;

import com.youmed.entity.Invoice;
import com.youmed.enums.InvoiceType;

import java.math.BigDecimal;
import java.util.List;

public interface InvoiceService {

    Invoice createInvoice(Long patientId, Long appointmentId, BigDecimal amount, InvoiceType type, String description);

    Invoice payInvoice(Long invoiceId, String paymentMethod);

    List<Invoice> getPatientInvoices(Long patientId);

    List<Invoice> getAppointmentInvoices(Long appointmentId);

    org.springframework.data.domain.Page<Invoice> getAllInvoices(String keyword, String status, int page, int size, String sortBy, String sortDir);
}

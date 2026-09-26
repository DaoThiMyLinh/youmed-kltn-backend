package com.youmed.service.impl;

import com.youmed.entity.Appointment;
import com.youmed.entity.Invoice;
import com.youmed.entity.User;
import com.youmed.enums.InvoiceStatus;
import com.youmed.enums.InvoiceType;
import com.youmed.exception.BadRequestException;
import com.youmed.exception.ResourceNotFoundException;
import com.youmed.repository.AppointmentRepository;
import com.youmed.repository.InvoiceRepository;
import com.youmed.repository.UserRepository;
import com.youmed.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional
    public Invoice createInvoice(Long patientId, Long appointmentId, BigDecimal amount, InvoiceType type, String description) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Appointment appointment = null;
        if (appointmentId != null) {
            appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        }

        Invoice invoice = Invoice.builder()
                .patient(patient)
                .appointment(appointment)
                .amount(amount)
                .type(type)
                .description(description)
                .status(InvoiceStatus.PENDING)
                .build();

        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public Invoice payInvoice(Long invoiceId, String paymentMethod) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("Invoice is already paid");
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaymentDate(LocalDateTime.now());
        invoice.setPaymentMethod(paymentMethod);
        
        return invoiceRepository.save(invoice);
    }

    @Override
    public List<Invoice> getPatientInvoices(Long patientId) {
        return invoiceRepository.findByPatientId(patientId);
    }

    @Override
    public List<Invoice> getAppointmentInvoices(Long appointmentId) {
        return invoiceRepository.findByAppointmentId(appointmentId);
    }

    @Override
    public org.springframework.data.domain.Page<Invoice> getAllInvoices(String keyword, String status, int page, int size, String sortBy, String sortDir) {
        String safeKeyword = (keyword == null) ? "" : keyword;
        String safeStatus = (status == null) ? "" : status;
        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase(org.springframework.data.domain.Sort.Direction.ASC.name()) ? org.springframework.data.domain.Sort.by(sortBy).ascending() : org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return invoiceRepository.searchInvoices(safeKeyword, safeStatus, pageable);
    }
}

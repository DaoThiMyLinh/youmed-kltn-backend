package com.youmed.repository;

import com.youmed.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByPatientId(Long patientId);
    List<Invoice> findByAppointmentId(Long appointmentId);

    @org.springframework.data.jpa.repository.Query("SELECT i FROM Invoice i WHERE " +
            "(:keyword = '' OR LOWER(i.patient.fullName) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR i.patient.phone LIKE CONCAT('%', CAST(:keyword AS string), '%')) " +
            "AND (:status = '' OR CAST(i.status AS string) = :status)")
    org.springframework.data.domain.Page<Invoice> searchInvoices(
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            @org.springframework.data.repository.query.Param("status") String status,
            org.springframework.data.domain.Pageable pageable);
}

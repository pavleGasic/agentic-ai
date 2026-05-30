package com.master.invoicemanagementserver.repository;

import com.master.invoicemanagementserver.entity.Invoice;
import com.master.invoicemanagementserver.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByStatus(InvoiceStatus status);
    boolean existsByInvoiceId(String invoiceId);
}

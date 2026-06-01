package com.master.invoicemanagementserver.repository;

import com.master.invoicemanagementserver.entity.Invoice;
import com.master.invoicemanagementserver.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByStatus(InvoiceStatus status);
    boolean existsByInvoiceId(String invoiceId);
    List<Invoice> findByBatchUploadId(String batchUploadId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.batchUploadId = :batchUploadId AND i.vendor.vendorCode = :vendorCode AND i.status = 'PROCESSED'")
    BigDecimal sumProcessedAmountByBatchAndVendor(@Param("batchUploadId") String batchUploadId, @Param("vendorCode") String vendorCode);

    @Query("SELECT DISTINCT i.vendor.vendorCode FROM Invoice i WHERE i.batchUploadId = :batchUploadId AND i.status = 'PROCESSED' AND i.vendor IS NOT NULL")
    List<String> findDistinctVendorCodesByBatchUploadId(@Param("batchUploadId") String batchUploadId);
}

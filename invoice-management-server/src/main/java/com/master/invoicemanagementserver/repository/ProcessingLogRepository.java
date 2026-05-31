package com.master.invoicemanagementserver.repository;

import com.master.invoicemanagementserver.entity.ProcessingLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessingLogRepository extends JpaRepository<ProcessingLog, Long> {
    List<ProcessingLog> findByInvoiceIdOrderByTimestampAsc(String invoiceId);
    List<ProcessingLog> findByBatchUploadIdOrderByTimestampAsc(String batchUploadId);
}

package com.master.invoicemanagementserver.repository;

import com.master.invoicemanagementserver.entity.BatchUpload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchUploadRepository extends JpaRepository<BatchUpload, Long> {
}

package com.master.invoicemanagementserver.repository;

import com.master.invoicemanagementserver.entity.BatchUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BatchUploadRepository extends JpaRepository<BatchUpload, UUID> {
}

package com.master.invoicemanagementserver.repository;

import com.master.invoicemanagementserver.entity.VendorEarning;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorEarningRepository extends JpaRepository<VendorEarning, Long> {
    List<VendorEarning> findByBatchUploadId(String batchUploadId);
    void deleteByBatchUploadId(String batchUploadId);
}

package com.master.invoicemanagementserver.repository;

import com.master.invoicemanagementserver.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByVendorCode(String vendorCode);
    boolean existsByVendorCode(String vendorCode);
}

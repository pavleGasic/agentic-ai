package com.master.invoicemanagementserver.dto;

import com.master.invoicemanagementserver.entity.VendorEarning;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VendorEarningDTO(
        String vendorCode,
        String vendorName,
        BigDecimal baseAmount,
        BigDecimal bonusRate,
        BigDecimal totalAmount,
        LocalDateTime calculatedAt
) {
    public static VendorEarningDTO from(VendorEarning e) {
        return new VendorEarningDTO(
                e.getVendorCode(),
                e.getVendorName(),
                e.getBaseAmount(),
                e.getBonusRate(),
                e.getTotalAmount(),
                e.getCalculatedAt()
        );
    }
}

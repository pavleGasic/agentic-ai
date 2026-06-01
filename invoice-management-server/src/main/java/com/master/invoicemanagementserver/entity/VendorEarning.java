package com.master.invoicemanagementserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_earnings")
@Getter
@Setter
@NoArgsConstructor
public class VendorEarning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String batchUploadId;

    @Column(nullable = false)
    private String vendorCode;

    @Column(nullable = false)
    private String vendorName;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal baseAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal bonusRate;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime calculatedAt = LocalDateTime.now();
}

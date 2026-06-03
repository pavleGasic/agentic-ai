package com.master.invoicemanagementserver.service;

import com.master.invoicemanagementserver.dto.VendorEarningDTO;
import com.master.invoicemanagementserver.entity.VendorEarning;
import com.master.invoicemanagementserver.exception.InvoiceValidationException;
import com.master.invoicemanagementserver.repository.BatchUploadRepository;
import com.master.invoicemanagementserver.repository.InvoiceRepository;
import com.master.invoicemanagementserver.repository.VendorEarningRepository;
import com.master.invoicemanagementserver.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class EarningsCalculationService {

    private static final BigDecimal THRESHOLD_LOW  = new BigDecimal("5000");
    private static final BigDecimal THRESHOLD_HIGH = new BigDecimal("8000");
    private static final BigDecimal RATE_LOW       = new BigDecimal("0.10");
    private static final BigDecimal RATE_HIGH      = new BigDecimal("0.25");

    private final BatchUploadRepository batchUploadRepository;
    private final VendorRepository vendorRepository;
    private final InvoiceRepository invoiceRepository;
    private final VendorEarningRepository earningRepository;
    private final LoggingService loggingService;

    public EarningsCalculationService(BatchUploadRepository batchUploadRepository,
                                      VendorRepository vendorRepository,
                                      InvoiceRepository invoiceRepository,
                                      VendorEarningRepository earningRepository,
                                      LoggingService loggingService) {
        this.batchUploadRepository = batchUploadRepository;
        this.vendorRepository = vendorRepository;
        this.invoiceRepository = invoiceRepository;
        this.earningRepository = earningRepository;
        this.loggingService = loggingService;
    }

    @Transactional
    public List<VendorEarningDTO> calculate(UUID batchId) {
        var batchUploadId = batchId.toString();
        var businessContext = "batchUploadId:" + batchUploadId;

        if (!batchUploadRepository.existsById(batchId)) {
            loggingService.error("EarningsCalculationService", businessContext, "Calculation aborted — batch not found: " + batchUploadId);
            throw new InvoiceValidationException("Batch not found: " + batchUploadId);
        }

        var vendorCodes = invoiceRepository.findDistinctVendorCodesByBatchUploadId(batchUploadId);
        if (vendorCodes.isEmpty()) {
            loggingService.warn("EarningsCalculationService", businessContext, "No processed invoices found for batch, skipping calculation");
            return List.of();
        }

        loggingService.info("EarningsCalculationService", businessContext,
                "Earnings calculation started — overwriting previous results, vendors: " + vendorCodes);

        earningRepository.deleteByBatchUploadId(batchUploadId);

        var results = vendorCodes.stream()
                .map(vendorCode -> calculateForVendor(batchUploadId, vendorCode))
                .toList();

        var totalBase  = results.stream().map(VendorEarningDTO::baseAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalFinal = results.stream().map(VendorEarningDTO::totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        loggingService.info("EarningsCalculationService", businessContext,
                "Earnings calculation completed — vendors: " + results.size()
                + ", totalBase: " + totalBase + ", totalFinal: " + totalFinal);

        return results;
    }

    private VendorEarningDTO calculateForVendor(String batchUploadId, String vendorCode) {
        var businessContext = "batchUploadId:" + batchUploadId + ", vendor:" + vendorCode;

        var vendor = vendorRepository.findByVendorCode(vendorCode).orElseThrow(
                () -> new InvoiceValidationException("Vendor not found: " + vendorCode));

        var baseAmount = invoiceRepository.sumProcessedAmountByBatchAndVendor(batchUploadId, vendorCode);
        var bonusRate  = resolveBonusRate(baseAmount);
        var bonus      = baseAmount.multiply(bonusRate).setScale(4, RoundingMode.HALF_UP);
        var total      = baseAmount.add(bonus).setScale(4, RoundingMode.HALF_UP);

        loggingService.info("EarningsCalculationService", businessContext,
                "vendor:" + vendorCode + ", base:" + baseAmount + ", bonusRate:" + bonusRate.toPlainString() + ", total:" + total);

        var earning = new VendorEarning();
        earning.setBatchUploadId(batchUploadId);
        earning.setVendorCode(vendor.getVendorCode());
        earning.setVendorName(vendor.getName());
        earning.setBaseAmount(baseAmount);
        earning.setBonusRate(bonusRate);
        earning.setTotalAmount(total);
        earningRepository.save(earning);

        return VendorEarningDTO.from(earning);
    }

    private BigDecimal resolveBonusRate(BigDecimal amount) {
        if (amount.compareTo(THRESHOLD_LOW) < 0)   return BigDecimal.ZERO;
        if (amount.compareTo(THRESHOLD_HIGH) <= 0) return RATE_LOW;
        return RATE_HIGH;
    }
}

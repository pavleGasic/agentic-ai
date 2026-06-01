package com.master.invoicemanagementserver.controller;

import com.master.invoicemanagementserver.dto.BatchUploadDTO;
import com.master.invoicemanagementserver.dto.InvoiceDTO;
import com.master.invoicemanagementserver.dto.VendorEarningDTO;
import com.master.invoicemanagementserver.service.EarningsCalculationService;
import com.master.invoicemanagementserver.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/batches")
public class BatchController {

    private final InvoiceService invoiceService;
    private final EarningsCalculationService earningsCalculationService;

    public BatchController(InvoiceService invoiceService, EarningsCalculationService earningsCalculationService) {
        this.invoiceService = invoiceService;
        this.earningsCalculationService = earningsCalculationService;
    }

    @GetMapping
    public ResponseEntity<List<BatchUploadDTO>> getAll() {
        return ResponseEntity.ok(invoiceService.getAllBatches());
    }

    @GetMapping("/{id}/invoices")
    public ResponseEntity<List<InvoiceDTO>> getInvoices(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.getInvoicesByBatch(id));
    }

    @PostMapping("/{id}/earnings")
    public ResponseEntity<List<VendorEarningDTO>> calculateEarnings(@PathVariable UUID id) {
        return ResponseEntity.ok(earningsCalculationService.calculate(id));
    }
}

package com.master.invoicemanagementserver.controller;

import com.master.invoicemanagementserver.dto.BatchUploadDTO;
import com.master.invoicemanagementserver.dto.InvoiceDTO;
import com.master.invoicemanagementserver.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/batches")
public class BatchController {

    private final InvoiceService invoiceService;

    public BatchController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public ResponseEntity<List<BatchUploadDTO>> getAll() {
        return ResponseEntity.ok(invoiceService.getAllBatches());
    }

    @GetMapping("/{id}/invoices")
    public ResponseEntity<List<InvoiceDTO>> getInvoices(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.getInvoicesByBatch(id));
    }
}

package com.master.invoicemanagementserver.controller;

import com.master.invoicemanagementserver.dto.InvoiceDTO;
import com.master.invoicemanagementserver.dto.UploadResponseDTO;
import com.master.invoicemanagementserver.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDTO> upload(@RequestParam("file") MultipartFile file) {
        UploadResponseDTO response = invoiceService.uploadInvoices(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceDTO>> getAll(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(status));
    }

    @GetMapping("/failed")
    public ResponseEntity<List<InvoiceDTO>> getFailed() {
        return ResponseEntity.ok(invoiceService.getFailedInvoices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }
}

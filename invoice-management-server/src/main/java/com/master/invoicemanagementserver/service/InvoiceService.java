package com.master.invoicemanagementserver.service;

import com.master.invoicemanagementserver.dto.BatchUploadDTO;
import com.master.invoicemanagementserver.dto.InvoiceDTO;
import com.master.invoicemanagementserver.dto.InvoiceRowDTO;
import com.master.invoicemanagementserver.dto.UploadResponseDTO;
import com.master.invoicemanagementserver.entity.*;
import com.master.invoicemanagementserver.exception.InvoiceNotFoundException;
import com.master.invoicemanagementserver.exception.InvoiceValidationException;
import com.master.invoicemanagementserver.repository.BatchUploadRepository;
import com.master.invoicemanagementserver.repository.InvoiceRepository;
import com.master.invoicemanagementserver.repository.VendorRepository;
import com.master.invoicemanagementserver.util.CsvParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final VendorRepository vendorRepository;
    private final ProcessingService processingService;
    private final BatchUploadRepository batchUploadRepository;
    private final CsvParser csvParser;
    private final LoggingService loggingService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          VendorRepository vendorRepository,
                          ProcessingService processingService,
                          BatchUploadRepository batchUploadRepository,
                          CsvParser csvParser,
                          LoggingService loggingService) {
        this.invoiceRepository = invoiceRepository;
        this.vendorRepository = vendorRepository;
        this.processingService = processingService;
        this.batchUploadRepository = batchUploadRepository;
        this.csvParser = csvParser;
        this.loggingService = loggingService;
    }

    public UploadResponseDTO uploadInvoices(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvoiceValidationException("Uploaded file is empty");
        }

        var uploadId = UUID.randomUUID();
        var fileName = file.getOriginalFilename();

        var batchUpload = new BatchUpload();
        batchUpload.setId(uploadId);
        batchUpload.setStartDate(LocalDateTime.now());
        batchUpload.setImportFileName(fileName);

        var parseResult = new CsvParser.ParseResult(List.of(), List.of());
        try {
          parseResult = csvParser.parse(file);
        } catch (Exception e){
            loggingService.error(
                "InvoiceService",
                "file:" + fileName,
                "Failed to parse CSV file: " + e.getMessage(),
                e
            );
            throw e;
        }
        String errorMessage = null;

        if (!parseResult.errors().isEmpty()) {
          for (var parseError : parseResult.errors()) {
            loggingService.error(
                    "CsvParser",
                    "file:" + fileName,
                    parseError.message()
            );
          }
          throw new InvoiceValidationException("One or more rows in the CSV file are invalid. Please correct the errors and try again.");
        }

        int successCount = 0;
        int failedCount = 0;

        for (var row : parseResult.validRows()) {
            var businessContext = "file:" + fileName + ", invoice:" + row.getInvoiceId() + ", vendor:" + row.getVendorCode();
            try {
                if (invoiceRepository.existsByInvoiceId(row.getInvoiceId())) {
                    loggingService.error(
                        "InvoiceService",
                        businessContext,
                        "Duplicate invoiceId rejected: " + row.getInvoiceId()
                    );
                    failedCount++;
                    errorMessage = "One or more invoices could not be processed. Please contact support.";
                    continue;
                }

                var invoice = toEntity(row);
                invoice.setBatchUploadId(uploadId.toString());

                var vendor = vendorRepository.findByVendorCode(row.getVendorCode()).orElse(null);
                if (vendor == null) {
                    loggingService.error(
                        "InvoiceService",
                        businessContext,
                        "Invoice rejected — vendor not found: " + row.getVendorCode()
                    );
                    failedCount++;
                    invoice.setStatus(InvoiceStatus.FAILED);
                    invoiceRepository.save(invoice);
                    errorMessage = "One or more invoices could not be processed. Please contact support.";
                    continue;
                }

                invoice.setVendor(vendor);
                invoiceRepository.save(invoice);

                processingService.processInvoiceAsync(batchUpload, invoice);
                successCount++;
            } catch (Exception e) {
                loggingService.error(
                    "InvoiceService",
                    businessContext,
                    "Failed to store invoice: " + row.getInvoiceId(),
                    e
                );
                failedCount++;
                errorMessage = "One or more invoices could not be processed. Please contact support.";
            }
        }

        int total = parseResult.validRows().size() + parseResult.errors().size();
        batchUpload.setEndTime(LocalDateTime.now());
        batchUploadRepository.save(batchUpload);

        return new UploadResponseDTO(uploadId, total, successCount, failedCount, errorMessage);
    }

    public List<InvoiceDTO> getAllInvoices(String statusFilter) {
        List<Invoice> invoices;
        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                InvoiceStatus status = InvoiceStatus.valueOf(statusFilter.toUpperCase());
                invoices = invoiceRepository.findByStatus(status);
            } catch (IllegalArgumentException e) {
                throw new InvoiceValidationException("Invalid status filter: " + statusFilter);
            }
        } else {
            invoices = invoiceRepository.findAll();
        }
        return invoices.stream().map(InvoiceDTO::from).collect(Collectors.toList());
    }

    public InvoiceDTO getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .map(InvoiceDTO::from)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found with id: " + id));
    }

    public List<InvoiceDTO> getFailedInvoices() {
        return invoiceRepository.findByStatus(InvoiceStatus.FAILED)
                .stream().map(InvoiceDTO::from).collect(Collectors.toList());
    }

    public List<BatchUploadDTO> getAllBatches() {
        return batchUploadRepository.findAll().stream()
                .map(BatchUploadDTO::from)
                .collect(Collectors.toList());
    }

    public List<InvoiceDTO> getInvoicesByBatch(UUID batchId) {
        return invoiceRepository.findByBatchUploadId(batchId.toString())
                .stream().map(InvoiceDTO::from).collect(Collectors.toList());
    }

    private Invoice toEntity(InvoiceRowDTO row) {
        var invoice = new Invoice();
        invoice.setInvoiceId(row.getInvoiceId());
        invoice.setCustomerName(row.getCustomerName());
        invoice.setAmount(new BigDecimal(row.getAmount()));
        invoice.setCurrency(row.getCurrency().toUpperCase());
        invoice.setIssueDate(LocalDate.parse(row.getIssueDate()));
        invoice.setStatus(InvoiceStatus.PENDING);
        return invoice;
    }
}

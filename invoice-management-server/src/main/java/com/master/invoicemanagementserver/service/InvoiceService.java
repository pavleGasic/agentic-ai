package com.master.invoicemanagementserver.service;

import com.master.invoicemanagementserver.dto.InvoiceDTO;
import com.master.invoicemanagementserver.dto.BatchUploadDTO;
import com.master.invoicemanagementserver.dto.InvoiceRowDTO;
import com.master.invoicemanagementserver.dto.UploadResponseDTO;
import com.master.invoicemanagementserver.entity.*;
import com.master.invoicemanagementserver.exception.InvoiceNotFoundException;
import com.master.invoicemanagementserver.exception.InvoiceValidationException;
import com.master.invoicemanagementserver.repository.BatchUploadRepository;
import com.master.invoicemanagementserver.repository.InvoiceRepository;
import com.master.invoicemanagementserver.repository.ProcessingLogRepository;
import com.master.invoicemanagementserver.repository.VendorRepository;
import com.master.invoicemanagementserver.util.CsvParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final VendorRepository vendorRepository;
    private final ProcessingService processingService;
    private final BatchUploadRepository batchUploadRepository;
    private final CsvParser csvParser;
    private final ProcessingLogRepository logRepository;


    public InvoiceService(InvoiceRepository invoiceRepository,
                          VendorRepository vendorRepository,
                          ProcessingService processingService,
                          BatchUploadRepository batchUploadRepository,
                          CsvParser csvParser,
                          ProcessingLogRepository logRepository) {
        this.invoiceRepository = invoiceRepository;
        this.vendorRepository = vendorRepository;
        this.processingService = processingService;
        this.batchUploadRepository = batchUploadRepository;
        this.csvParser = csvParser;
        this.logRepository = logRepository;
    }

    public UploadResponseDTO uploadInvoices(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvoiceValidationException("Uploaded file is empty");
        }

        var batchUpload = new BatchUpload();
        var uploadId = UUID.randomUUID();
        var fileName = file.getOriginalFilename();
        batchUpload.setId(uploadId);
        batchUpload.setStartDate(LocalDateTime.now());
        batchUpload.setImportFileName(fileName);
        MDC.put("fileName", fileName);
        log.info("Starting CSV upload: filename={}, size={}", fileName, file.getSize());

        var parseResult = csvParser.parse(file, batchUpload);
        List<String> errors = new ArrayList<>();
        for (String parseError : parseResult.errors()) {
            log.warn("CSV parse error: {}", parseError);
            errors.add("One or more invoices could not be processed. Please contact support.");
        }
        int successCount = 0;

        for (InvoiceRowDTO row : parseResult.validRows()) {
            MDC.put("invoiceId", row.getInvoiceId());
            try {
                if (invoiceRepository.existsByInvoiceId(row.getInvoiceId())) {
                    var errorMessage = "Duplicate invoiceId rejected: " + row.getInvoiceId();
                    errors.add("One or more invoices could not be processed. Please contact support.");
                    log.error(errorMessage);
                    persistLog(row.getInvoiceId(), batchUpload.getId().toString(), errorMessage, null);
                    continue;
                }

                var invoice = toEntity(row);
                invoice.setBatchUploadId(uploadId.toString());

                var vendor = vendorRepository.findByVendorCode(row.getVendorCode()).orElse(null);
                if (vendor == null) {
                    var errorMessage = String.format("Invoice %s rejected — vendor not found: %s", row.getInvoiceId(), row.getVendorCode());
                    errors.add("One or more invoices could not be processed. Please contact support.");
                    log.error("Invoice {} rejected — vendor not found: {}", row.getInvoiceId(), row.getVendorCode());
                    invoice.setStatus(InvoiceStatus.FAILED);
                    persistLog(row.getInvoiceId(), batchUpload.getId().toString(), errorMessage, null);
                    invoiceRepository.save(invoice);
                    continue;
                }

                invoice.setVendor(vendor);
                invoiceRepository.save(invoice);
                log.info("Invoice saved as PENDING: {}", invoice.getInvoiceId());

                processingService.processInvoiceAsync(batchUpload, invoice);
                successCount++;
            } catch (Exception e) {
                errors.add("One or more invoices could not be processed. Please contact support.");
                log.error("Failed to store invoice {}", row.getInvoiceId(), e);
            } finally {
                MDC.remove("invoiceId");
            }
        }

        int total = parseResult.validRows().size() + parseResult.errors().size();
        batchUpload.setImportedInvoices(total - errors.size());
        batchUpload.setErrorInvoices(errors.size());
        batchUpload.setEndTime(LocalDateTime.now());
        batchUploadRepository.save(batchUpload);
        log.info("Upload complete: total={}, accepted={}, errors={}", total, successCount, errors.size());
        return new UploadResponseDTO(uploadId, total, successCount, errors.size(), errors);
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

    public List<BatchUploadDTO> getAllBatches() {
        return batchUploadRepository.findAll().stream()
                .map(BatchUploadDTO::from)
                .collect(Collectors.toList());
    }

    public List<InvoiceDTO> getInvoicesByBatch(UUID batchId) {
        return invoiceRepository.findByBatchUploadId(batchId.toString())
                .stream().map(InvoiceDTO::from).collect(Collectors.toList());
    }

    private void persistLog(String invoiceId, String batchUploadId, String message, String stackTrace) {
        try {
            var entry = new ProcessingLog();
            entry.setInvoiceId(invoiceId);
            entry.setLevel("ERROR");
            entry.setModule("InvoiceService");
            entry.setEndpoint(MDC.get("endpoint") != null ? MDC.get("endpoint") : "/invoices/upload");
            entry.setBatchUploadId(batchUploadId);
            entry.setMessage(message);
            entry.setStackTrace(stackTrace);
            entry.setTimestamp(LocalDateTime.now());
            logRepository.save(entry);
        } catch (Exception e) {
            log.warn("Failed to persist processing log for {}: {}", invoiceId, e.getMessage());
        }
    }
}

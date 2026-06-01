package com.master.invoicemanagementserver.service;

import com.master.invoicemanagementserver.entity.BatchUpload;
import com.master.invoicemanagementserver.entity.Invoice;
import com.master.invoicemanagementserver.entity.InvoiceStatus;
import com.master.invoicemanagementserver.repository.InvoiceRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
public class ProcessingService {

    private static final Random random = new Random();

    private final InvoiceRepository invoiceRepository;
    private final LoggingService loggingService;

    public ProcessingService(InvoiceRepository invoiceRepository,
                             LoggingService loggingService) {
        this.invoiceRepository = invoiceRepository;
        this.loggingService = loggingService;
    }

    @Async
    public void processInvoiceAsync(BatchUpload batchUpload, Invoice invoice) {
        var batchId = batchUpload.getId().toString();
        var businessContext = "file:" + batchUpload.getImportFileName() + ", invoice:" + invoice.getInvoiceId() + ", batchUploadId:" + batchId;

        try {
            long daysOld = ChronoUnit.DAYS.between(LocalDate.now(), invoice.getIssueDate());
            if (daysOld > 730) {
                loggingService.error("ProcessingService", businessContext, "Invoice issue date is too old for processing: " + invoice.getIssueDate());
                throw new IllegalArgumentException(
                        "Invoice issue date is too old for processing: " + invoice.getIssueDate());
            }

            invoice.setStatus(InvoiceStatus.PROCESSED);
            invoice.setProcessedAt(LocalDateTime.now());
            invoiceRepository.save(invoice);

            loggingService.info("ProcessingService", businessContext, "Processing completed successfully");

        } catch (Exception e) {
            handleFailure(invoice, businessContext, "Processing failed: " + e.getMessage(), e);
        }
    }

    private void handleFailure(Invoice invoice, String businessContext, String message, Exception e) {
        loggingService.error("ProcessingService", businessContext, message, e);
        try {
            var fresh = invoiceRepository.findById(invoice.getId()).orElse(invoice);
            fresh.setStatus(InvoiceStatus.FAILED);
            fresh.setErrorMessage(message);
            fresh.setProcessedAt(LocalDateTime.now());
            invoiceRepository.save(fresh);
        } catch (Exception saveEx) {
            loggingService.error("ProcessingService", businessContext, "Could not persist failure status: " + saveEx.getMessage());
        }
    }
}

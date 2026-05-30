package com.master.invoicemanagementserver.service;

import com.master.invoicemanagementserver.config.ProcessingConfig;
import com.master.invoicemanagementserver.entity.Invoice;
import com.master.invoicemanagementserver.entity.InvoiceStatus;
import com.master.invoicemanagementserver.entity.ProcessingLog;
import com.master.invoicemanagementserver.repository.InvoiceRepository;
import com.master.invoicemanagementserver.repository.ProcessingLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;

@Service
public class ProcessingService {

    private static final Logger log = LoggerFactory.getLogger(ProcessingService.class);
    private static final Random random = new Random();

    private final InvoiceRepository invoiceRepository;
    private final ProcessingLogRepository logRepository;
    private final ProcessingConfig processingConfig;

    public ProcessingService(InvoiceRepository invoiceRepository,
                             ProcessingLogRepository logRepository,
                             ProcessingConfig processingConfig) {
        this.invoiceRepository = invoiceRepository;
        this.logRepository = logRepository;
        this.processingConfig = processingConfig;
    }

    @Async
    public void processInvoiceAsync(Invoice invoice) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        MDC.put("invoiceId", invoice.getInvoiceId());
        MDC.put("module", "ProcessingService");
        MDC.put("endpoint", "/invoices/upload");

        persistLog(invoice.getInvoiceId(), "INFO", "ProcessingService", "Processing started for invoice", null, requestId);
        log.info("Processing started for invoice: {}", invoice.getInvoiceId());

        try {
            Thread.sleep(100 + random.nextInt(400));

            long daysOld = ChronoUnit.DAYS.between(LocalDate.now(), invoice.getIssueDate());
            if (daysOld > 730) {
                throw new IllegalArgumentException(
                        "Invoice issue date is too old for processing: " + invoice.getIssueDate());
            }

            if (invoice.getAmount().doubleValue() >= 10000) {
                log.info("High-value invoice detected, applying extended validation: {}", invoice.getInvoiceId());
                Thread.sleep(50 + random.nextInt(100));
            }

            BigDecimal amount = invoice.getAmount();
            if (amount.compareTo(new BigDecimal("1000")) >= 0
                    && amount.compareTo(new BigDecimal("2000")) < 0
                    && random.nextDouble() < processingConfig.getFailureRate()) {
                throw new RuntimeException("Intermittent processing failure for high-value invoice: " + invoice.getInvoiceId());
            }

            invoice.setStatus(InvoiceStatus.PROCESSED);
            invoice.setProcessedAt(LocalDateTime.now());
            invoiceRepository.save(invoice);
            persistLog(invoice.getInvoiceId(), "INFO", "ProcessingService", "Processing completed successfully", null, requestId);
            log.info("Invoice {} status updated to FAILED", invoice.getInvoiceId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            handleFailure(invoice, e, requestId, "Processing interrupted");
        } catch (Exception e) {
            handleFailure(invoice, e, requestId, "Processing failed: " + e.getMessage());
        } finally {
            MDC.remove("invoiceId");
            MDC.remove("module");
            MDC.remove("endpoint");
        }
    }

    private void handleFailure(Invoice invoice, Exception e, String requestId, String message) {
        String stackTrace = stackTraceToString(e);
        log.error("Processing failed for invoice {}: {}", invoice.getInvoiceId(), e.getMessage(), e);
        persistLog(invoice.getInvoiceId(), "ERROR", "ProcessingService", message, stackTrace, requestId);
        try {
            Invoice fresh = invoiceRepository.findById(invoice.getId()).orElse(invoice);
            fresh.setStatus(InvoiceStatus.FAILED);
            fresh.setErrorMessage(message);
            fresh.setProcessedAt(LocalDateTime.now());
            invoiceRepository.save(fresh);
        } catch (Exception saveEx) {
            log.error("Could not persist failure status for invoice {}", invoice.getInvoiceId(), saveEx);
        }
    }

    private void persistLog(String invoiceId, String level, String module, String message, String stackTrace, String requestId) {
        try {
            ProcessingLog entry = new ProcessingLog();
            entry.setInvoiceId(invoiceId);
            entry.setLevel(level);
            entry.setModule(module);
            entry.setEndpoint(MDC.get("endpoint") != null ? MDC.get("endpoint") : "/invoices/upload");
            entry.setRequestId(requestId);
            entry.setMessage(message);
            entry.setStackTrace(stackTrace);
            entry.setTimestamp(LocalDateTime.now());
            logRepository.save(entry);
        } catch (Exception e) {
            log.warn("Failed to persist processing log for {}: {}", invoiceId, e.getMessage());
        }
    }

    private static String stackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}

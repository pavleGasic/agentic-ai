package com.master.invoicemanagementserver.util;

import com.master.invoicemanagementserver.dto.InvoiceRowDTO;
import com.master.invoicemanagementserver.entity.BatchUpload;
import com.master.invoicemanagementserver.entity.Invoice;
import com.master.invoicemanagementserver.entity.ProcessingLog;
import com.master.invoicemanagementserver.exception.InvoiceValidationException;
import com.master.invoicemanagementserver.repository.ProcessingLogRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CsvParser {

    private static final Logger log = LoggerFactory.getLogger(CsvParser.class);
    private static final List<String> REQUIRED_HEADERS =
            List.of("invoice_id", "customer_name", "amount", "currency", "issue_date", "vendor_code");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public record ParseResult(List<InvoiceRowDTO> validRows, List<String> errors) {}

    private final ProcessingLogRepository logRepository;

    public CsvParser(ProcessingLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public ParseResult parse(MultipartFile file, BatchUpload batchUpload) {
        var validRows = new ArrayList<InvoiceRowDTO>();
        var errors = new ArrayList<String>();
        try {
            List<String[]> rows;
            try (var reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                rows = reader.readAll();
            } catch (IOException | CsvException e) {
                throw new InvoiceValidationException("Malformed CSV file: " + e.getMessage());
            }

            if (rows.isEmpty()) {
                throw new InvoiceValidationException("CSV file is empty");
            }

            String[] headers = Arrays.stream(rows.get(0)).map(String::trim).map(String::toLowerCase).toArray(String[]::new);
            for (String required : REQUIRED_HEADERS) {
                boolean found = Arrays.asList(headers).contains(required);
                if (!found) {
                    throw new InvoiceValidationException("Missing required column: " + required);
                }
            }

            int idxInvoiceId = indexOf(headers, "invoice_id");
            int idxCustomerName = indexOf(headers, "customer_name");
            int idxAmount = indexOf(headers, "amount");
            int idxCurrency = indexOf(headers, "currency");
            int idxIssueDate = indexOf(headers, "issue_date");
            int idxVendorCode = indexOf(headers, "vendor_code");

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                int rowNum = i + 1;

                if (row.length <= Math.max(Math.max(idxInvoiceId, idxCustomerName), Math.max(idxAmount, Math.max(idxCurrency, Math.max(idxIssueDate, idxVendorCode))))) {
                    errors.add("Row " + rowNum + ": insufficient columns");
                    continue;
                }

                var invoiceId = row[idxInvoiceId].trim();
                var customerName = row[idxCustomerName].trim();
                var amount = row[idxAmount].trim();
                var currency = row[idxCurrency].strip();
                var issueDate = row[idxIssueDate].trim();
                var vendorCode = row[idxVendorCode].trim();

                var rowErrors = new ArrayList<String>();

                if (invoiceId.isEmpty()) rowErrors.add("invoice_id is empty");
                if (customerName.isEmpty()) rowErrors.add("customer_name is empty");
                if (amount.isEmpty()) rowErrors.add("amount is empty");
                if (vendorCode.isEmpty()) rowErrors.add("vendor_code is empty");

                if (!currency.matches("[A-Z]{3}")) {
                    rowErrors.add("invalid currency '" + currency + "' (must be 3 uppercase letters)");
                }

                if (!isValidDate(issueDate)) {
                    rowErrors.add("invalid date format '" + issueDate + "' (expected yyyy-MM-dd)");
                }

                if (!rowErrors.isEmpty()) {
                    errors.add("Row " + rowNum + " [" + invoiceId + "]: " + String.join("; ", rowErrors));
                } else {
                    validRows.add(new InvoiceRowDTO(invoiceId, customerName, amount, currency, issueDate, vendorCode));
                }
            }

            return new ParseResult(validRows, errors);
        } catch (Exception e){
            handleFailure(new Invoice(), batchUpload.getId().toString(), e, "Processing failed: " + e.getMessage());
        }
        return new ParseResult(validRows, errors);
    }

    private static int indexOf(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(name)) return i;
        }
        return -1;
    }

    private static boolean isValidDate(String value) {
        try {
            LocalDate.parse(value, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void handleFailure(Invoice invoice, String batchUploadId, Exception e, String message) {
        var stackTrace = stackTraceToString(e);
        log.error("Processing failed for invoice {}: {}", invoice.getInvoiceId(), e.getMessage(), e);
        persistLog(invoice.getInvoiceId(), batchUploadId, message, stackTrace);
    }

    private void persistLog(String invoiceId, String batchUploadId, String message, String stackTrace) {
        try {
            var entry = new ProcessingLog();
            entry.setInvoiceId(invoiceId);
            entry.setLevel("ERROR");
            entry.setModule("CSVParser");
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

    private static String stackTraceToString(Throwable t) {
        var sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}

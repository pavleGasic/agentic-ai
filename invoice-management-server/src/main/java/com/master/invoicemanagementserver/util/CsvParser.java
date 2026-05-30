package com.master.invoicemanagementserver.util;

import com.master.invoicemanagementserver.dto.InvoiceRowDTO;
import com.master.invoicemanagementserver.exception.InvoiceValidationException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvParser {

    private static final List<String> REQUIRED_HEADERS =
            List.of("invoice_id", "customer_name", "amount", "currency", "issue_date", "vendor_code");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public record ParseResult(List<InvoiceRowDTO> validRows, List<String> errors) {}

    public static ParseResult parse(MultipartFile file) {
        List<String[]> rows;
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
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

        List<InvoiceRowDTO> validRows = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            int rowNum = i + 1;

            if (row.length <= Math.max(Math.max(idxInvoiceId, idxCustomerName), Math.max(idxAmount, Math.max(idxCurrency, Math.max(idxIssueDate, idxVendorCode))))) {
                errors.add("Row " + rowNum + ": insufficient columns");
                continue;
            }

            String invoiceId = row[idxInvoiceId].trim();
            String customerName = row[idxCustomerName].trim();
            String amount = row[idxAmount].trim();
            String currency = row[idxCurrency].strip();
            String issueDate = row[idxIssueDate].trim();
            String vendorCode = row[idxVendorCode].trim();

            List<String> rowErrors = new ArrayList<>();

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
}

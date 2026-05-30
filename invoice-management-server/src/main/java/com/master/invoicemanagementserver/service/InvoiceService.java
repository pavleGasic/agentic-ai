package com.master.invoicemanagementserver.service;

import com.master.invoicemanagementserver.dto.InvoiceDTO;
import com.master.invoicemanagementserver.dto.InvoiceRowDTO;
import com.master.invoicemanagementserver.dto.UploadResponseDTO;
import com.master.invoicemanagementserver.entity.Invoice;
import com.master.invoicemanagementserver.entity.InvoiceStatus;
import com.master.invoicemanagementserver.entity.Vendor;
import com.master.invoicemanagementserver.exception.InvoiceNotFoundException;
import com.master.invoicemanagementserver.exception.InvoiceValidationException;
import com.master.invoicemanagementserver.repository.InvoiceRepository;
import com.master.invoicemanagementserver.repository.VendorRepository;
import com.master.invoicemanagementserver.util.CsvParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final VendorRepository vendorRepository;
    private final ProcessingService processingService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          VendorRepository vendorRepository,
                          ProcessingService processingService) {
        this.invoiceRepository = invoiceRepository;
        this.vendorRepository = vendorRepository;
        this.processingService = processingService;
    }

    public UploadResponseDTO uploadInvoices(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvoiceValidationException("Uploaded file is empty");
        }

        log.info("Starting CSV upload: filename={}, size={}", file.getOriginalFilename(), file.getSize());

        CsvParser.ParseResult parseResult = CsvParser.parse(file);
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
                    errors.add("One or more invoices could not be processed. Please contact support.");
                    log.warn("Duplicate invoiceId rejected: {}", row.getInvoiceId());
                    continue;
                }

                Vendor vendor = vendorRepository.findByVendorCode(row.getVendorCode()).orElse(null);
                if (vendor == null) {
                    errors.add("One or more invoices could not be processed. Please contact support.");
                    log.warn("Invoice {} rejected — vendor not found: {}", row.getInvoiceId(), row.getVendorCode());
                    continue;
                }

                Invoice invoice = toEntity(row, vendor);
                invoiceRepository.save(invoice);
                log.info("Invoice saved as PENDING: {}", invoice.getInvoiceId());

                processingService.processInvoiceAsync(invoice);
                successCount++;
            } catch (Exception e) {
                errors.add("One or more invoices could not be processed. Please contact support.");
                log.error("Failed to store invoice {}", row.getInvoiceId(), e);
            } finally {
                MDC.remove("invoiceId");
            }
        }

        int total = parseResult.validRows().size() + parseResult.errors().size();
        log.info("Upload complete: total={}, accepted={}, errors={}", total, successCount, errors.size());
        return new UploadResponseDTO(total, successCount, errors.size(), errors);
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

    private Invoice toEntity(InvoiceRowDTO row, Vendor vendor) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(row.getInvoiceId());
        invoice.setCustomerName(row.getCustomerName());
        invoice.setAmount(new BigDecimal(row.getAmount()));
        invoice.setCurrency(row.getCurrency().toUpperCase());
        invoice.setIssueDate(LocalDate.parse(row.getIssueDate()));
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setVendor(vendor);
        return invoice;
    }
}

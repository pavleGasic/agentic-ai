package com.master.invoicemanagementserver.dto;

import com.master.invoicemanagementserver.dto.VendorDTO;
import com.master.invoicemanagementserver.entity.Invoice;
import com.master.invoicemanagementserver.entity.InvoiceStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class InvoiceDTO {
    private Long id;
    private String invoiceId;
    private String customerName;
    private BigDecimal amount;
    private String currency;
    private LocalDate issueDate;
    private InvoiceStatus status;
    private String errorMessage;
    private VendorDTO vendor;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String batchUploadId;

    public static InvoiceDTO from(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.id = invoice.getId();
        dto.invoiceId = invoice.getInvoiceId();
        dto.customerName = invoice.getCustomerName();
        dto.amount = invoice.getAmount();
        dto.currency = invoice.getCurrency();
        dto.issueDate = invoice.getIssueDate();
        dto.status = invoice.getStatus();
        dto.errorMessage = invoice.getErrorMessage();
        dto.vendor = invoice.getVendor() != null ? VendorDTO.from(invoice.getVendor()) : null;
        dto.createdAt = invoice.getCreatedAt();
        dto.processedAt = invoice.getProcessedAt();
        dto.batchUploadId = invoice.getBatchUploadId();
        return dto;
    }
}

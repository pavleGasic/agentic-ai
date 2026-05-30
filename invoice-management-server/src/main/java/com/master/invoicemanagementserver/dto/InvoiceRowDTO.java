package com.master.invoicemanagementserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRowDTO {
    private String invoiceId;
    private String customerName;
    private String amount;
    private String currency;
    private String issueDate;
    private String vendorCode;
}

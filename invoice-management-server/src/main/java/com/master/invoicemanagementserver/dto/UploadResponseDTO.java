package com.master.invoicemanagementserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponseDTO {
    private int totalRows;
    private int successCount;
    private int failedCount;
    private List<String> errors;
}

package com.master.invoicemanagementserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponseDTO {
    private UUID batchId;
    private int totalRows;
    private int successCount;
    private int failedCount;
    private String errorMessage;
}

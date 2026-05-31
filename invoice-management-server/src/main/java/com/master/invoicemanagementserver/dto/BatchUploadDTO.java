package com.master.invoicemanagementserver.dto;

import com.master.invoicemanagementserver.entity.BatchUpload;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class BatchUploadDTO {
    private UUID id;
    private String importFileName;
    private Integer importedInvoices;
    private Integer errorInvoices;
    private LocalDateTime startDate;
    private LocalDateTime endTime;

    public static BatchUploadDTO from(BatchUpload batch) {
        BatchUploadDTO dto = new BatchUploadDTO();
        dto.id = batch.getId();
        dto.importFileName = batch.getImportFileName();
        dto.importedInvoices = batch.getImportedInvoices();
        dto.errorInvoices = batch.getErrorInvoices();
        dto.startDate = batch.getStartDate();
        dto.endTime = batch.getEndTime();
        return dto;
    }
}

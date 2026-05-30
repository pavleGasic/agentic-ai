package com.master.invoicemanagementserver.dto;

import com.master.invoicemanagementserver.entity.ProcessingLog;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ProcessingLogDTO {
    private Long id;
    private String invoiceId;
    private String level;
    private String module;
    private String endpoint;
    private String requestId;
    private String message;
    private String stackTrace;
    private LocalDateTime timestamp;

    public static ProcessingLogDTO from(ProcessingLog log) {
        ProcessingLogDTO dto = new ProcessingLogDTO();
        dto.id = log.getId();
        dto.invoiceId = log.getInvoiceId();
        dto.level = log.getLevel();
        dto.module = log.getModule();
        dto.endpoint = log.getEndpoint();
        dto.requestId = log.getRequestId();
        dto.message = log.getMessage();
        dto.stackTrace = log.getStackTrace();
        dto.timestamp = log.getTimestamp();
        return dto;
    }
}

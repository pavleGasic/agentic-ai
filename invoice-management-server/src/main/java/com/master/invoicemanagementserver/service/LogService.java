package com.master.invoicemanagementserver.service;

import com.master.invoicemanagementserver.dto.ProcessingLogDTO;
import com.master.invoicemanagementserver.repository.ProcessingLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogService {

    private final ProcessingLogRepository logRepository;

    public LogService(ProcessingLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public List<ProcessingLogDTO> getLogsForInvoice(String invoiceId) {
        return logRepository.findByInvoiceIdOrderByTimestampAsc(invoiceId)
                .stream().map(ProcessingLogDTO::from).collect(Collectors.toList());
    }

    public List<ProcessingLogDTO> getLogsForBatch(String batchUploadId) {
        return logRepository.findByBatchUploadIdOrderByTimestampAsc(batchUploadId)
                .stream().map(ProcessingLogDTO::from).collect(Collectors.toList());
    }
}

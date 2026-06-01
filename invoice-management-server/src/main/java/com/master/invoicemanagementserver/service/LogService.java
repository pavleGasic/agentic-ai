package com.master.invoicemanagementserver.service;

import com.master.invoicemanagementserver.dto.ProcessingLogDTO;
import com.master.invoicemanagementserver.repository.ProcessingLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogService {

    private final ProcessingLogRepository logRepository;

    public LogService(ProcessingLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public List<ProcessingLogDTO> getLogs(String keyword, LocalDateTime from, LocalDateTime to, String level) {
        return logRepository.search(
                (keyword != null && !keyword.isBlank()) ? keyword : null,
                from,
                to,
                (level != null && !level.isBlank()) ? level : null
        ).stream().map(ProcessingLogDTO::from).collect(Collectors.toList());
    }
}

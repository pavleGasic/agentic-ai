package com.master.invoicemanagementserver.controller;

import com.master.invoicemanagementserver.dto.ProcessingLogDTO;
import com.master.invoicemanagementserver.service.LogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<List<ProcessingLogDTO>> getLogsForInvoice(@PathVariable String invoiceId) {
        return ResponseEntity.ok(logService.getLogsForInvoice(invoiceId));
    }
}

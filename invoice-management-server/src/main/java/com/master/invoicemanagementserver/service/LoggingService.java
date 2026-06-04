package com.master.invoicemanagementserver.service;

import com.master.invoicemanagementserver.entity.ProcessingLog;
import com.master.invoicemanagementserver.repository.ProcessingLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@Service
public class LoggingService {

    private final ProcessingLogRepository logRepository;
    private LoggingService self;

    public LoggingService(ProcessingLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Autowired
    public void setSelf(@Lazy LoggingService self) {
        this.self = self;
    }

    public void info(String module, String businessContext, String message) {
        self.persist("INFO", module, businessContext, message, null);
    }

    public void warn(String module, String businessContext, String message) {
        self.persist("WARN", module, businessContext, message, null);
    }

    public void error(String module, String businessContext, String message, Throwable cause) {
        String stackTrace = cause != null ? stackTraceToString(cause) : null;
        self.persist("ERROR", module, businessContext, message, stackTrace);
    }

    public void error(String module, String businessContext, String message) {
        self.persist("ERROR", module, businessContext, message, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persist(String level, String module, String businessContext, String message, String stackTrace) {
        var entry = new ProcessingLog();
        entry.setLevel(level);
        entry.setModule(module);
        entry.setBusinessContext(businessContext);
        entry.setMessage(message);
        entry.setStackTrace(stackTrace);
        entry.setTimestamp(LocalDateTime.now());
        logRepository.save(entry);
    }

    private static String stackTraceToString(Throwable t) {
        var sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}

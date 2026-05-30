package com.master.invoicemanagementserver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableAsync
public class ProcessingConfig {

    private static final Logger log = LoggerFactory.getLogger(ProcessingConfig.class);

    @Value("${processing.mode:normal}")
    private String processingMode;

    @Value("${processing.failure-rate:0.2}")
    private double failureRate;

    @PostConstruct
    public void init() {
        if ("broken".equalsIgnoreCase(processingMode)) {
            log.warn("ProcessingConfig: PROCESSING_MODE=broken detected, service may be unstable");
        }
        log.info("ProcessingConfig initialized: mode={}, failureRate={}", processingMode, failureRate);
    }

    public String getProcessingMode() {
        return processingMode;
    }

    public double getFailureRate() {
        return failureRate;
    }
}

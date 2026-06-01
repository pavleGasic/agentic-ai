package com.master.incidentmanagementserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IncidentManagementServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentManagementServerApplication.class, args);
    }
}

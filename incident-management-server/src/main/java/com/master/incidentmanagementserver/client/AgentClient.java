package com.master.incidentmanagementserver.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class AgentClient {

    private static final Logger log = LoggerFactory.getLogger(AgentClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${agent.url}")
    private String agentUrl;

    @Async
    public void triggerIncidentProcessing(Long incidentId, String title, String description) {
        Map<String, Object> body = new HashMap<>();
        body.put("incident_id", String.valueOf(incidentId));
        body.put("title", title);
        body.put("description", description);

        try {
            restTemplate.postForEntity(agentUrl + "/agent/process", body, Void.class);
            log.info("Agent triggered for incident: {}", incidentId);
        } catch (Exception e) {
            log.error("Failed to trigger agent for incident {}: {}", incidentId, e.getMessage());
        }
    }
}

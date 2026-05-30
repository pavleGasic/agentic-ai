package com.master.incidentmanagementserver.dto;

import com.master.incidentmanagementserver.entity.Incident;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDTO {
    private Long id;
    private String title;
    private String description;
    private String createdByUsername;
    private LocalDateTime createdAt;

    public static IncidentDTO from(Incident incident) {
        return new IncidentDTO(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getCreatedBy().getUsername(),
                incident.getCreatedAt()
        );
    }
}

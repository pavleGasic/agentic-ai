package com.master.incidentmanagementserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentCreateRequest {
    private String title;
    private String description;
}

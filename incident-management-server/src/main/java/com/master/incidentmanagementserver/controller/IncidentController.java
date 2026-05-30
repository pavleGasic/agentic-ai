package com.master.incidentmanagementserver.controller;

import com.master.incidentmanagementserver.dto.IncidentCreateRequest;
import com.master.incidentmanagementserver.dto.IncidentDTO;
import com.master.incidentmanagementserver.service.IncidentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public ResponseEntity<List<IncidentDTO>> getAll(
            @AuthenticationPrincipal UserDetails principal) {
        String role = extractRole(principal);
        if ("DEVELOPER".equals(role)) {
            return ResponseEntity.ok(incidentService.getAllIncidents());
        }
        return ResponseEntity.ok(incidentService.getIncidentsByUser(principal.getUsername()));
    }

    @PostMapping
    public ResponseEntity<IncidentDTO> create(
            @RequestBody IncidentCreateRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        IncidentDTO created = incidentService.createIncident(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        String role = extractRole(principal);
        return ResponseEntity.ok(incidentService.getIncidentById(id, principal.getUsername(), role));
    }

    private String extractRole(UserDetails principal) {
        return principal.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");
    }
}

package com.master.incidentmanagementserver.service;

import com.master.incidentmanagementserver.dto.IncidentCreateRequest;
import com.master.incidentmanagementserver.dto.IncidentDTO;
import com.master.incidentmanagementserver.entity.Incident;
import com.master.incidentmanagementserver.entity.User;
import com.master.incidentmanagementserver.exception.IncidentNotFoundException;
import com.master.incidentmanagementserver.exception.UnauthorizedException;
import com.master.incidentmanagementserver.repository.IncidentRepository;
import com.master.incidentmanagementserver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IncidentService {

    private static final Logger log = LoggerFactory.getLogger(IncidentService.class);

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    public IncidentService(IncidentRepository incidentRepository,
                           UserRepository userRepository) {
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<IncidentDTO> getAllIncidents() {
        return incidentRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(IncidentDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<IncidentDTO> getIncidentsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IncidentNotFoundException("User not found: " + username));
        return incidentRepository.findByCreatedByOrderByCreatedAtDesc(user)
                .stream().map(IncidentDTO::from).toList();
    }

    @Transactional
    public IncidentDTO createIncident(IncidentCreateRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IncidentNotFoundException("User not found: " + username));
        Incident incident = new Incident();
        incident.setTitle(request.getTitle());
        incident.setDescription(request.getDescription());
        incident.setCreatedBy(user);
        Incident saved = incidentRepository.save(incident);
        log.info("Incident created: id={}, by={}", saved.getId(), username);
        return IncidentDTO.from(saved);
    }

    @Transactional(readOnly = true)
    public IncidentDTO getIncidentById(Long id, String username, String role) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException("Incident not found: " + id));
        if ("CUSTOMER".equals(role) && !incident.getCreatedBy().getUsername().equals(username)) {
            throw new UnauthorizedException("Access denied to incident: " + id);
        }
        return IncidentDTO.from(incident);
    }
}

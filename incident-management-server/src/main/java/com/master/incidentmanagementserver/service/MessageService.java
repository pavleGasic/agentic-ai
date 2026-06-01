package com.master.incidentmanagementserver.service;

import com.master.incidentmanagementserver.dto.MessageCreateRequest;
import com.master.incidentmanagementserver.dto.MessageDTO;
import com.master.incidentmanagementserver.entity.Incident;
import com.master.incidentmanagementserver.entity.Message;
import com.master.incidentmanagementserver.entity.User;
import com.master.incidentmanagementserver.entity.Visibility;
import com.master.incidentmanagementserver.exception.IncidentNotFoundException;
import com.master.incidentmanagementserver.exception.UnauthorizedException;
import com.master.incidentmanagementserver.repository.IncidentRepository;
import com.master.incidentmanagementserver.repository.MessageRepository;
import com.master.incidentmanagementserver.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository,
                          IncidentRepository incidentRepository,
                          UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MessageDTO addMessage(Long incidentId, MessageCreateRequest request,
                                 String username, String role) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IncidentNotFoundException("Incident not found: " + incidentId));

        if ("CUSTOMER".equals(role) && !incident.getCreatedBy().getUsername().equals(username)) {
            throw new UnauthorizedException("Access denied to incident: " + incidentId);
        }

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new IncidentNotFoundException("User not found: " + username));

        Visibility visibility = resolveVisibility(request.getVisibility(), role);

        Message message = new Message();
        message.setContent(request.getContent());
        message.setIncident(incident);
        message.setAuthor(author);
        message.setVisibility(visibility);
        return MessageDTO.from(messageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getMessages(Long incidentId, String username, String role) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IncidentNotFoundException("Incident not found: " + incidentId));

        if ("CUSTOMER".equals(role) && !incident.getCreatedBy().getUsername().equals(username)) {
            throw new UnauthorizedException("Access denied to incident: " + incidentId);
        }

        if ("CUSTOMER".equals(role)) {
            return messageRepository.findByIncidentAndVisibilityOrderByCreatedAtAsc(incident, Visibility.PUBLIC)
                    .stream().map(MessageDTO::from).toList();
        }

        return messageRepository.findByIncidentOrderByCreatedAtAsc(incident)
                .stream().map(MessageDTO::from).toList();
    }

    private Visibility resolveVisibility(Visibility requested, String role) {
        if ("CUSTOMER".equals(role)) {
            return Visibility.PUBLIC;
        }
        return requested != null ? requested : Visibility.PUBLIC;
    }
}

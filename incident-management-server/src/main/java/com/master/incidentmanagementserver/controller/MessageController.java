package com.master.incidentmanagementserver.controller;

import com.master.incidentmanagementserver.dto.MessageCreateRequest;
import com.master.incidentmanagementserver.dto.MessageDTO;
import com.master.incidentmanagementserver.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents/{incidentId}/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ResponseEntity<List<MessageDTO>> getMessages(
            @PathVariable Long incidentId,
            @AuthenticationPrincipal UserDetails principal) {
        String role = extractRole(principal);
        return ResponseEntity.ok(
                messageService.getMessages(incidentId, principal.getUsername(), role));
    }

    @PostMapping
    public ResponseEntity<MessageDTO> addMessage(
            @PathVariable Long incidentId,
            @RequestBody MessageCreateRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        String role = extractRole(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.addMessage(incidentId, request, principal.getUsername(), role));
    }

    private String extractRole(UserDetails principal) {
        return principal.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");
    }
}

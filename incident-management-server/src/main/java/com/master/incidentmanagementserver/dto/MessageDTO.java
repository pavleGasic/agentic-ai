package com.master.incidentmanagementserver.dto;

import com.master.incidentmanagementserver.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private String content;
    private String authorUsername;
    private LocalDateTime createdAt;

    public static MessageDTO from(Message message) {
        return new MessageDTO(
                message.getId(),
                message.getContent(),
                message.getAuthor().getUsername(),
                message.getCreatedAt()
        );
    }
}

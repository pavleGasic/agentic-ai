package com.master.incidentmanagementserver.dto;

import com.master.incidentmanagementserver.entity.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateRequest {
    private String content;
    private Visibility visibility;
}

package com.smartexpense.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String conversationId;
    private String message;
    private String model;
    private List<String> toolsUsed;
    private long latencyMs;
    private LocalDateTime timestamp;
}
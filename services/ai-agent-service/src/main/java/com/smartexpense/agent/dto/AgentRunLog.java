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
public class AgentRunLog {
    private String conversationId;
    private String userMessage;
    private String agentResponse;
    private List<String> toolsInvoked;
    private String model;
    private long latencyMs;
    private int inputTokensEstimate;
    private LocalDateTime timestamp;
    private boolean success;
    private String errorMessage;
}
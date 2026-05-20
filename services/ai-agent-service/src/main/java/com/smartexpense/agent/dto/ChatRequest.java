package com.smartexpense.agent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "Message is required")
    private String message;

    private String conversationId;
}
package com.smartexpense.agent.service;

import com.smartexpense.agent.dto.AgentRunLog;
import com.smartexpense.agent.dto.ChatRequest;
import com.smartexpense.agent.dto.ChatResponse;
import com.smartexpense.agent.tool.TokenHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final ChatClient chatClient;
    private final ObservabilityService observabilityService;

    private static final String SYSTEM_PROMPT_TEMPLATE =
            "You are a helpful personal finance assistant for SmartExpense. " +
            "You help users understand their spending patterns and manage their budget. " +
            "Today's date is %s. " +
            "You have access to tools to fetch real expense data. " +
            "Always use the tools to fetch real data before answering. " +
            "Be concise, friendly, and give actionable insights. " +
            "Format currency as EUR with 2 decimal places. " +
            "When showing breakdowns, use percentages to give context.";

    public ChatResponse chat(ChatRequest request, String token, String userId) {
        String conversationId = request.getConversationId() != null
                ? request.getConversationId()
                : UUID.randomUUID().toString();

        long startTime = System.currentTimeMillis();
        List<String> toolsUsed = new ArrayList<>();
        boolean success = true;
        String errorMessage = null;
        String responseText = null;

        try {
            TokenHolder.setToken(token);

            log.info("Agent request: conversationId={} userId={} message={}",
                    conversationId, userId, request.getMessage());

            String systemPrompt = String.format(
                    SYSTEM_PROMPT_TEMPLATE, LocalDate.now());

            responseText = chatClient.prompt()
                    .system(systemPrompt)
                    .user(request.getMessage())
                    .call()
                    .content();

            log.info("Agent response: conversationId={} length={}",
                    conversationId, responseText != null ? responseText.length() : 0);

        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            responseText = "I'm sorry, I encountered an error. Please try again.";
            log.error("Agent error: conversationId={}", conversationId, e);
        } finally {
            TokenHolder.clear();
        }

        long latencyMs = System.currentTimeMillis() - startTime;

        observabilityService.logAgentRun(AgentRunLog.builder()
                .conversationId(conversationId)
                .userMessage(request.getMessage())
                .agentResponse(responseText)
                .toolsInvoked(toolsUsed)
                .model("llama-3.3-70b-versatile")
                .latencyMs(latencyMs)
                .inputTokensEstimate(request.getMessage().split("\\s+").length)
                .timestamp(LocalDateTime.now())
                .success(success)
                .errorMessage(errorMessage)
                .build());

        return ChatResponse.builder()
                .conversationId(conversationId)
                .message(responseText)
                .model("llama-3.3-70b-versatile")
                .toolsUsed(toolsUsed)
                .latencyMs(latencyMs)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
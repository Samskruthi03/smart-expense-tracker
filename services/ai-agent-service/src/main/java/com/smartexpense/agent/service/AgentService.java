package com.smartexpense.agent.service;

import com.smartexpense.agent.dto.AgentRunLog;
import com.smartexpense.agent.dto.ChatRequest;
import com.smartexpense.agent.dto.ChatResponse;
import com.smartexpense.agent.tool.ExpenseTools;
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
    private final ExpenseTools expenseTools;

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

            String context = buildContext(request.getMessage(), toolsUsed);
            log.info("Context built with tools: {}", toolsUsed);

            String systemPrompt = "You are a helpful personal finance assistant. " +
                    "Today is " + LocalDate.now() + ". " +
                    "Here is the user's real expense data: " + context + " " +
                    "Use this data to answer accurately. " +
                    "Be concise and friendly. Format currency as EUR.";

            responseText = chatClient.prompt()
                    .system(systemPrompt)
                    .user(request.getMessage())
                    .call()
                    .content();

            log.info("Agent response: conversationId={} toolsUsed={} length={}",
                    conversationId, toolsUsed,
                    responseText != null ? responseText.length() : 0);

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

    private String buildContext(String message, List<String> toolsUsed) {
        StringBuilder context = new StringBuilder();
        String lower = message.toLowerCase();
        LocalDate now = LocalDate.now();

        try {
            log.info("Fetching current month summary...");
            String monthly = expenseTools.getCurrentMonthSummary().apply(null);
            context.append("Current month (")
                   .append(now.getMonth()).append(" ")
                   .append(now.getYear()).append("): ")
                   .append(monthly).append(" ");
            toolsUsed.add("getCurrentMonthSummary");
            log.info("Current month data fetched successfully");
        } catch (Exception e) {
            log.warn("Failed to fetch current month: {}", e.getMessage());
        }

        if (lower.contains("recent") || lower.contains("transaction") ||
            lower.contains("expense") || lower.contains("spent on") ||
            lower.contains("last") || lower.contains("purchase")) {
            try {
                log.info("Fetching recent expenses...");
                String expenses = expenseTools.listRecentExpenses()
                        .apply(new ExpenseTools.ListExpensesRequest(10, null));
                context.append("Recent expenses: ").append(expenses).append(" ");
                toolsUsed.add("listRecentExpenses");
            } catch (Exception e) {
                log.warn("Failed to fetch recent expenses: {}", e.getMessage());
            }
        }

        if (lower.contains("year") || lower.contains("annual") ||
            lower.contains("trend") || lower.contains("compare")) {
            try {
                log.info("Fetching yearly summary...");
                String yearly = expenseTools.getYearlySummary()
                        .apply(new ExpenseTools.YearRequest(now.getYear()));
                context.append("Yearly data: ").append(yearly).append(" ");
                toolsUsed.add("getYearlySummary");
            } catch (Exception e) {
                log.warn("Failed to fetch yearly data: {}", e.getMessage());
            }
        }

        return context.length() > 0 ? context.toString() : "No expense data available.";
    }
}
package com.smartexpense.agent.service;

import com.smartexpense.agent.dto.AgentRunLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ObservabilityService {

    public void logAgentRun(AgentRunLog runLog) {
        if (runLog.isSuccess()) {
            log.info("""
                    AGENT_RUN_SUCCESS \
                    conversationId={} \
                    model={} \
                    latencyMs={} \
                    inputTokens={} \
                    toolsInvoked={} \
                    responseLength={}""",
                    runLog.getConversationId(),
                    runLog.getModel(),
                    runLog.getLatencyMs(),
                    runLog.getInputTokensEstimate(),
                    runLog.getToolsInvoked(),
                    runLog.getAgentResponse() != null
                            ? runLog.getAgentResponse().length() : 0);
        } else {
            log.error("""
                    AGENT_RUN_FAILED \
                    conversationId={} \
                    model={} \
                    latencyMs={} \
                    error={}""",
                    runLog.getConversationId(),
                    runLog.getModel(),
                    runLog.getLatencyMs(),
                    runLog.getErrorMessage());
        }
    }
}
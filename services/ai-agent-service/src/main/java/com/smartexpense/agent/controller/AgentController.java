package com.smartexpense.agent.controller;

import com.smartexpense.agent.dto.ChatRequest;
import com.smartexpense.agent.dto.ChatResponse;
import com.smartexpense.agent.service.AgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader("Authorization") String authHeader,
            Authentication auth) {

        String userId = (String) auth.getCredentials();
        return ResponseEntity.ok(
                agentService.chat(request, authHeader, userId));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Agent Service is running");
    }
}
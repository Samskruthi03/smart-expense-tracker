package com.smartexpense.analytics.controller;

import com.smartexpense.analytics.dto.MonthlySummaryResponse;
import com.smartexpense.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @PathVariable int year,
            @PathVariable int month,
            Authentication auth) {
        String userId = (String) auth.getCredentials();
        return ResponseEntity.ok(analyticsService.getMonthlySummary(userId, year, month));
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<List<MonthlySummaryResponse>> getYearlySummary(
            @PathVariable int year,
            Authentication auth) {
        String userId = (String) auth.getCredentials();
        return ResponseEntity.ok(analyticsService.getYearlySummary(userId, year));
    }
}
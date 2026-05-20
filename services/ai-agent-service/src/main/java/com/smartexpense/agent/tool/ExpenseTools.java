package com.smartexpense.agent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.function.Function;

@Slf4j
@Configuration
public class ExpenseTools {

    @Value("${application.expense-service.url}")
    private String expenseServiceUrl;

    @Value("${application.analytics-service.url}")
    private String analyticsServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private HttpEntity<Void> authEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", TokenHolder.getToken());
        return new HttpEntity<>(headers);
    }

    public Function<Void, String> getCurrentMonthSummary() {
        return ignored -> {
            log.info("Tool: getCurrentMonthSummary");
            LocalDate now = LocalDate.now();
            try {
                String url = analyticsServiceUrl +
                        "/api/v1/analytics/monthly/" +
                        now.getYear() + "/" + now.getMonthValue();
                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.GET, authEntity(), String.class);
                return response.getBody();
            } catch (Exception e) {
                log.error("getCurrentMonthSummary failed: {}", e.getMessage());
                return "{\"error\": \"Could not fetch current month data\"}";
            }
        };
    }

    public Function<MonthlyRequest, String> getMonthlySummary() {
        return request -> {
            log.info("Tool: getMonthlySummary year={} month={}",
                    request.year(), request.month());
            try {
                String url = analyticsServiceUrl +
                        "/api/v1/analytics/monthly/" +
                        request.year() + "/" + request.month();
                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.GET, authEntity(), String.class);
                return response.getBody();
            } catch (Exception e) {
                log.error("getMonthlySummary failed: {}", e.getMessage());
                return "{\"error\": \"Could not fetch monthly data\"}";
            }
        };
    }

    public Function<YearRequest, String> getYearlySummary() {
        return request -> {
            log.info("Tool: getYearlySummary year={}", request.year());
            try {
                String url = analyticsServiceUrl +
                        "/api/v1/analytics/yearly/" + request.year();
                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.GET, authEntity(), String.class);
                return response.getBody();
            } catch (Exception e) {
                log.error("getYearlySummary failed: {}", e.getMessage());
                return "{\"error\": \"Could not fetch yearly data\"}";
            }
        };
    }

    public Function<ListExpensesRequest, String> listRecentExpenses() {
        return request -> {
            log.info("Tool: listRecentExpenses pageSize={} category={}",
                    request.pageSize(), request.category());
            try {
                String url = expenseServiceUrl +
                        "/api/v1/expenses?pageSize=" + request.pageSize();
                if (request.category() != null && !request.category().isBlank()) {
                    url += "&category=" + request.category();
                }
                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.GET, authEntity(), String.class);
                return response.getBody();
            } catch (Exception e) {
                log.error("listRecentExpenses failed: {}", e.getMessage());
                return "{\"error\": \"Could not fetch expenses\"}";
            }
        };
    }

    public record MonthlyRequest(int year, int month) {}
    public record YearRequest(int year) {}
    public record ListExpensesRequest(int pageSize, String category) {}
}
package com.smartexpense.agent.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.function.Function;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ExpenseTools {

    @Value("${application.expense-service.url}")
    private String expenseServiceUrl;

    @Value("${application.analytics-service.url}")
    private String analyticsServiceUrl;

    private final RestTemplate restTemplate;

    // Tool 1: Get monthly spending summary
    @Bean
    @Description("Get the spending summary for a specific month and year. " +
                 "Returns total spent and breakdown by category. " +
                 "Use this when user asks about monthly spending or budget.")
    public Function<MonthlyRequest, String> getMonthlySummary() {
        return request -> {
            log.info("Tool called: getMonthlySummary year={} month={}",
                    request.year(), request.month());
            try {
                String url = analyticsServiceUrl +
                        "/api/v1/analytics/monthly/" +
                        request.year() + "/" + request.month();

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", TokenHolder.getToken());
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, String.class);
                return response.getBody();
            } catch (Exception e) {
                log.error("getMonthlySummary failed", e);
                return "{\"error\": \"Could not fetch monthly summary\"}";
            }
        };
    }

    // Tool 2: Get current month summary
    @Bean
    @Description("Get the spending summary for the current month. " +
                 "Use this when user asks about this month's spending, " +
                 "current budget status, or recent expenses.")
    public Function<Void, String> getCurrentMonthSummary() {
        return ignored -> {
            log.info("Tool called: getCurrentMonthSummary");
            LocalDate now = LocalDate.now();
            try {
                String url = analyticsServiceUrl +
                        "/api/v1/analytics/monthly/" +
                        now.getYear() + "/" + now.getMonthValue();

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", TokenHolder.getToken());
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, String.class);
                return response.getBody();
            } catch (Exception e) {
                log.error("getCurrentMonthSummary failed", e);
                return "{\"error\": \"Could not fetch current month summary\"}";
            }
        };
    }

    // Tool 3: Get yearly overview
    @Bean
    @Description("Get the spending overview for an entire year broken down by month. " +
                 "Use this when user asks about yearly trends, annual spending, " +
                 "or wants to compare months.")
    public Function<YearRequest, String> getYearlySummary() {
        return request -> {
            log.info("Tool called: getYearlySummary year={}", request.year());
            try {
                String url = analyticsServiceUrl +
                        "/api/v1/analytics/yearly/" + request.year();

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", TokenHolder.getToken());
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, String.class);
                return response.getBody();
            } catch (Exception e) {
                log.error("getYearlySummary failed", e);
                return "{\"error\": \"Could not fetch yearly summary\"}";
            }
        };
    }

    // Tool 4: List recent expenses
    @Bean
    @Description("List the most recent expenses. " +
                 "Use this when user asks about recent transactions, " +
                 "latest purchases, or wants to see expense details.")
    public Function<ListExpensesRequest, String> listRecentExpenses() {
        return request -> {
            log.info("Tool called: listRecentExpenses pageSize={}", request.pageSize());
            try {
                String url = expenseServiceUrl +
                        "/api/v1/expenses?pageSize=" + request.pageSize();

                if (request.category() != null && !request.category().isEmpty()) {
                    url += "&category=" + request.category();
                }

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", TokenHolder.getToken());
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, String.class);
                return response.getBody();
            } catch (Exception e) {
                log.error("listRecentExpenses failed", e);
                return "{\"error\": \"Could not fetch expenses\"}";
            }
        };
    }

    // Request/Response records for tool schemas
    public record MonthlyRequest(int year, int month) {}
    public record YearRequest(int year) {}
    public record ListExpensesRequest(int pageSize, String category) {}
}
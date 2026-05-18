package com.smartexpense.analytics.service;

import com.smartexpense.analytics.domain.MonthlySummary;
import com.smartexpense.analytics.dto.MonthlySummaryResponse;
import com.smartexpense.analytics.repository.MonthlySummaryRepository;
import com.smartexpense.events.ExpenseCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final MonthlySummaryRepository summaryRepository;

    @Transactional
    public void processExpenseCreated(ExpenseCreatedEvent event) {
        int year = event.getCreatedAt().getYear();
        int month = event.getCreatedAt().getMonthValue();
        UUID userId = UUID.fromString(event.getUserId());

        MonthlySummary summary = summaryRepository
                .findByUserIdAndYearAndMonthAndCategory(
                        userId, year, month, event.getCategory())
                .orElseGet(() -> MonthlySummary.builder()
                        .userId(userId)
                        .year(year)
                        .month(month)
                        .category(event.getCategory())
                        .totalAmount(BigDecimal.ZERO)
                        .expenseCount(0)
                        .build());

        summary.setTotalAmount(summary.getTotalAmount().add(event.getAmount()));
        summary.setExpenseCount(summary.getExpenseCount() + 1);
        summaryRepository.save(summary);

        log.info("Updated monthly summary: userId={} year={} month={} category={} total={}",
                userId, year, month, event.getCategory(), summary.getTotalAmount());
    }

    public MonthlySummaryResponse getMonthlySummary(String userId, int year, int month) {
        List<MonthlySummary> summaries = summaryRepository
                .findByUserIdAndYearAndMonthOrderByTotalAmountDesc(
                        UUID.fromString(userId), year, month);

        BigDecimal total = summaries.stream()
                .map(MonthlySummary::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<MonthlySummaryResponse.CategoryBreakdown> breakdowns = summaries.stream()
                .map(s -> MonthlySummaryResponse.CategoryBreakdown.builder()
                        .category(s.getCategory())
                        .totalAmount(s.getTotalAmount())
                        .expenseCount(s.getExpenseCount())
                        .percentage(total.compareTo(BigDecimal.ZERO) == 0 ? 0 :
                                s.getTotalAmount()
                                        .divide(total, 4, RoundingMode.HALF_UP)
                                        .multiply(BigDecimal.valueOf(100))
                                        .doubleValue())
                        .build())
                .toList();

        return MonthlySummaryResponse.builder()
                .year(year)
                .month(month)
                .totalSpent(total)
                .categories(breakdowns)
                .build();
    }

    public List<MonthlySummaryResponse> getYearlySummary(String userId, int year) {
        return summaryRepository
                .findByUserIdAndYearOrderByMonthAscTotalAmountDesc(
                        UUID.fromString(userId), year)
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(MonthlySummary::getMonth))
                .entrySet().stream()
                .map(entry -> {
                    List<MonthlySummary> monthSummaries = entry.getValue();
                    BigDecimal total = monthSummaries.stream()
                            .map(MonthlySummary::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return MonthlySummaryResponse.builder()
                            .year(year)
                            .month(entry.getKey())
                            .totalSpent(total)
                            .categories(monthSummaries.stream()
                                    .map(s -> MonthlySummaryResponse.CategoryBreakdown.builder()
                                            .category(s.getCategory())
                                            .totalAmount(s.getTotalAmount())
                                            .expenseCount(s.getExpenseCount())
                                            .build())
                                    .toList())
                            .build();
                })
                .sorted(java.util.Comparator.comparingInt(MonthlySummaryResponse::getMonth))
                .toList();
    }
}
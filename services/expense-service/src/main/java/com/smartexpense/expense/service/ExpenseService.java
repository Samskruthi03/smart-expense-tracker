package com.smartexpense.expense.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartexpense.events.ExpenseCreatedEvent;
import com.smartexpense.expense.domain.Expense;

import com.smartexpense.expense.dto.*;
import com.smartexpense.expense.kafka.ExpenseEventProducer;
import com.smartexpense.expense.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseEventProducer eventProducer;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String IDEMPOTENCY_PREFIX = "idempotency:expense:";

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request,
                                         String userId,
                                         String idempotencyKey) {
        // Check idempotency key in Redis
        if (idempotencyKey != null) {
            String redisKey = IDEMPOTENCY_PREFIX + idempotencyKey;
            String cached = redisTemplate.opsForValue().get(redisKey);
            if (cached != null) {
                log.info("Idempotent request detected, returning cached response. key={}", idempotencyKey);
                try {
                    return objectMapper.readValue(cached, ExpenseResponse.class);
                } catch (Exception e) {
                    log.error("Failed to deserialize cached response", e);
                }
            }
        }

        Expense expense = Expense.builder()
                .userId(UUID.fromString(userId))
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .category(request.getCategory())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .build();

        Expense saved = expenseRepository.save(expense);
        ExpenseResponse response = toResponse(saved);

        // Store in Redis for idempotency (24h TTL)
        if (idempotencyKey != null) {
            try {
                String redisKey = IDEMPOTENCY_PREFIX + idempotencyKey;
                redisTemplate.opsForValue().set(
                        redisKey,
                        objectMapper.writeValueAsString(response),
                        24, TimeUnit.HOURS);
            } catch (Exception e) {
                log.error("Failed to cache idempotency response", e);
            }
        }

        // Publish Kafka event
        eventProducer.publishExpenseCreated(ExpenseCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(userId)
                .expenseId(saved.getId().toString())
                .amount(saved.getAmount())
                .category(saved.getCategory().name())
                .description(saved.getDescription())
                .createdAt(saved.getCreatedAt())
                .build());

        log.info("Created expense: id={} userId={}", saved.getId(), userId);
        return response;
    }

    public PagedResponse<ExpenseResponse> listExpenses(String userId,
                                                        String category,
                                                        LocalDate fromDate,
                                                        LocalDate toDate,
                                                        String cursor,
                                                        int pageSize) {
        if (pageSize <= 0 || pageSize > 100) pageSize = DEFAULT_PAGE_SIZE;

        LocalDateTime cursorDateTime = null;
        if (cursor != null) {
            cursorDateTime = LocalDateTime.parse(cursor, DateTimeFormatter.ISO_DATE_TIME);
        }

        final LocalDateTime cursorFinal = cursorDateTime;

        List<Expense> all = expenseRepository
                .findByUserIdOrderByCreatedAtDesc(UUID.fromString(userId));

        List<Expense> filtered = all.stream()
                .filter(e -> category == null ||
                        e.getCategory().name().equalsIgnoreCase(category))
                .filter(e -> fromDate == null ||
                        !e.getExpenseDate().isBefore(fromDate))
                .filter(e -> toDate == null ||
                        !e.getExpenseDate().isAfter(toDate))
                .filter(e -> cursorFinal == null ||
                        e.getCreatedAt().isBefore(cursorFinal))
                .toList();

        boolean hasMore = filtered.size() > pageSize;
        List<Expense> page = hasMore ? filtered.subList(0, pageSize) : filtered;

        String nextCursor = null;
        if (hasMore && !page.isEmpty()) {
            nextCursor = page.get(page.size() - 1)
                    .getCreatedAt()
                    .format(DateTimeFormatter.ISO_DATE_TIME);
        }

        return PagedResponse.<ExpenseResponse>builder()
                .data(page.stream().map(this::toResponse).toList())
                .pageSize(page.size())
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .build();
    }

    public ExpenseResponse getExpense(String expenseId, String userId) {
        Expense expense = expenseRepository
                .findByIdAndUserId(UUID.fromString(expenseId), UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("Expense not found: " + expenseId));
        return toResponse(expense);
    }

    @Transactional
    public ExpenseResponse updateExpense(String expenseId,
                                          UpdateExpenseRequest request,
                                          String userId) {
        Expense expense = expenseRepository
                .findByIdAndUserId(UUID.fromString(expenseId), UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("Expense not found: " + expenseId));

        if (request.getAmount() != null) expense.setAmount(request.getAmount());
        if (request.getCurrency() != null) expense.setCurrency(request.getCurrency());
        if (request.getCategory() != null) expense.setCategory(request.getCategory());
        if (request.getDescription() != null) expense.setDescription(request.getDescription());
        if (request.getExpenseDate() != null) expense.setExpenseDate(request.getExpenseDate());

        Expense updated = expenseRepository.save(expense);
        log.info("Updated expense: id={} userId={}", expenseId, userId);
        return toResponse(updated);
    }

    @Transactional
    public void deleteExpense(String expenseId, String userId) {
        Expense expense = expenseRepository
                .findByIdAndUserId(UUID.fromString(expenseId), UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("Expense not found: " + expenseId));
        expenseRepository.delete(expense);
        log.info("Deleted expense: id={} userId={}", expenseId, userId);
    }

    private ExpenseResponse toResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId().toString())
                .userId(expense.getUserId().toString())
                .amount(expense.getAmount())
                .currency(expense.getCurrency())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .expenseDate(expense.getExpenseDate())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}
package com.smartexpense.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCreatedEvent {
    private String eventId;
    private String userId;
    private String expenseId;
    private BigDecimal amount;
    private String category;
    private String description;
    private LocalDateTime createdAt;
}
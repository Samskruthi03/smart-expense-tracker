package com.smartexpense.expense.dto;

import com.smartexpense.expense.domain.ExpenseCategory;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateExpenseRequest {

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "Invalid amount format")
    private BigDecimal amount;

    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
    private String currency;

    private ExpenseCategory category;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @PastOrPresent(message = "Expense date cannot be in the future")
    private LocalDate expenseDate;
}
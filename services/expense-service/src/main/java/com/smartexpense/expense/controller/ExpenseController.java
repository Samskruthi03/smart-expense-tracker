package com.smartexpense.expense.controller;

import com.smartexpense.expense.dto.*;
import com.smartexpense.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody CreateExpenseRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            Authentication auth) {
        String userId = (String) auth.getCredentials();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(expenseService.createExpense(request, userId, idempotencyKey));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ExpenseResponse>> listExpenses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int pageSize,
            Authentication auth) {
        String userId = (String) auth.getCredentials();
        return ResponseEntity.ok(
                expenseService.listExpenses(userId, category, fromDate, toDate, cursor, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpense(
            @PathVariable String id,
            Authentication auth) {
        String userId = (String) auth.getCredentials();
        return ResponseEntity.ok(expenseService.getExpense(id, userId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable String id,
            @Valid @RequestBody UpdateExpenseRequest request,
            Authentication auth) {
        String userId = (String) auth.getCredentials();
        return ResponseEntity.ok(expenseService.updateExpense(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable String id,
            Authentication auth) {
        String userId = (String) auth.getCredentials();
        expenseService.deleteExpense(id, userId);
        return ResponseEntity.noContent().build();
    }
}
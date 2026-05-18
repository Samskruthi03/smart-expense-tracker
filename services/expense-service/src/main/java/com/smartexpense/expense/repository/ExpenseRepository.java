package com.smartexpense.expense.repository;

import com.smartexpense.expense.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    Optional<Expense> findByIdAndUserId(UUID id, UUID userId);

    List<Expense> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("""
        SELECT SUM(e.amount) FROM Expense e
        WHERE e.userId = :userId
        AND e.expenseDate >= :fromDate
        AND e.expenseDate <= :toDate
        """)
    Optional<BigDecimal> sumByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
package com.smartexpense.analytics.repository;

import com.smartexpense.analytics.domain.MonthlySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonthlySummaryRepository extends JpaRepository<MonthlySummary, UUID> {

    Optional<MonthlySummary> findByUserIdAndYearAndMonthAndCategory(
            UUID userId, int year, int month, String category);

    List<MonthlySummary> findByUserIdAndYearAndMonthOrderByTotalAmountDesc(
            UUID userId, int year, int month);

    List<MonthlySummary> findByUserIdAndYearOrderByMonthAscTotalAmountDesc(
            UUID userId, int year);

    @Query("""
        SELECT SUM(m.totalAmount) FROM MonthlySummary m
        WHERE m.userId = :userId
        AND m.year = :year
        AND m.month = :month
        """)
    Optional<java.math.BigDecimal> sumTotalByUserIdAndYearAndMonth(
            @Param("userId") UUID userId,
            @Param("year") int year,
            @Param("month") int month);
}
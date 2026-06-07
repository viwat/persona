package com.example.persona.pinless.repository;

import com.example.persona.pinless.config.PinlessCounterWarmup;
import com.example.persona.pinless.model.entity.PinlessTxnCounter;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PinlessTxnCounterRepository extends JpaRepository<PinlessTxnCounter, Long> {

    @Query("""
    SELECT
        p.customerNo        AS customerNo,
        p.currency          AS currency,
        SUM(CASE WHEN p.resolvedAt >= :startOfDay THEN 1 ELSE 0 END) AS dailyCount,
        COUNT(p)                                                       AS monthlyCount,
        COALESCE(SUM(CASE WHEN p.resolvedAt >= :startOfDay THEN p.amount ELSE null END), 0) AS dailyAmount,
        COALESCE(SUM(p.amount), 0)                                     AS monthlyAmount
    FROM PinlessTxnCounter p
    WHERE p.authLevelResolved = 'STEP_UP'
      AND p.resolvedAt BETWEEN :startOfMonth AND :now
    GROUP BY p.customerNo, p.currency
    """)
    List<PinlessCounterWarmup.CustomerCounterProjection> findDailyAndMonthlyTotals(
            @Param("startOfDay") Instant startOfDay,
            @Param("startOfMonth") Instant startOfMonth,
            @Param("now") Instant now);

    // ── Reporting queries ─────────────────────────────────────────────────────

    @Query("""
        SELECT COUNT(p) FROM PinlessTxnCounter p
        WHERE p.customerNo = :customerNo
        AND p.authLevelResolved = 'STEP_UP'
        AND p.resolvedAt BETWEEN :from AND :to
    """)
    long countPinlessByCustomer(
            @Param("customerNo") String customerNo, @Param("from") Instant from, @Param("to") Instant to);

    @Query("""
        SELECT COUNT(p) FROM PinlessTxnCounter p
        WHERE p.accountId = :accountId
        AND p.authLevelResolved = 'STEP_UP'
        AND p.resolvedAt BETWEEN :from AND :to
    """)
    long countPinlessByAccount(
            @Param("accountId") String accountId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("""
        SELECT COUNT(p) FROM PinlessTxnCounter p
        WHERE p.channel = :channel
        AND p.authLevelResolved = 'STEP_UP'
        AND p.resolvedAt BETWEEN :from AND :to
    """)
    long countPinlessByChannel(@Param("channel") String channel, @Param("from") Instant from, @Param("to") Instant to);

    @Query("""
        SELECT COUNT(p) FROM PinlessTxnCounter p
        WHERE p.txnType = :txnType
        AND p.authLevelResolved = 'STEP_UP'
        AND p.resolvedAt BETWEEN :from AND :to
    """)
    long countPinlessByTxnType(@Param("txnType") String txnType, @Param("from") Instant from, @Param("to") Instant to);
}

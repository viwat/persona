package com.example.persona.pinless.config;

import com.example.persona.pinless.repository.PinlessTxnCounterRepository;
import com.example.persona.pinless.service.PinlessCounterService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PinlessCounterWarmup implements ApplicationRunner {

    private final PinlessTxnCounterRepository txnCounterRepository;
    private final PinlessCounterService counterService;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        log.info("[PINLESS] Starting Redis counter warmup...");

        Instant startOfDay = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant now = Instant.now();

        List<CustomerCounterProjection> projections =
                txnCounterRepository.findDailyAndMonthlyTotals(startOfDay, startOfMonth, now);

        int warmed = 0;
        for (CustomerCounterProjection p : projections) {
            try {
                // Currency now passed — each currency gets its own isolated Redis keys
                counterService.warmUp(
                        p.getCustomerNo(),
                        p.getCurrency(),
                        p.getDailyCount(),
                        p.getMonthlyCount(),
                        p.getDailyAmount(),
                        p.getMonthlyAmount());
                warmed++;
            } catch (Exception ex) {
                log.warn(
                        "[PINLESS] Warmup skipped for customer={} currency={}: {}",
                        p.getCustomerNo(),
                        p.getCurrency(),
                        ex.getMessage());
            }
        }

        log.info("[PINLESS] Redis counter warmup complete: {} entries seeded", warmed);
    }

    public interface CustomerCounterProjection {
        String getCustomerNo();

        String getCurrency();

        long getDailyCount();

        long getMonthlyCount();

        BigDecimal getDailyAmount();

        BigDecimal getMonthlyAmount();
    }
}

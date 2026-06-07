package com.example.persona.pinless.model.dto;

import com.example.persona.pinless.model.enums.BreachAction;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class PinlessConfigData {

    Long id;
    String customerNo;
    String channel;
    String currency;
    boolean enabled;

    // ── Threshold ─────────────────────────────────────────────────────────────
    boolean thresholdEnabled;
    BigDecimal thresholdAmount;
    BigDecimal systemCap;

    // ── Daily count ───────────────────────────────────────────────────────────
    boolean dailyCountEnabled;
    Integer dailyCountLimit;

    // ── Monthly count ─────────────────────────────────────────────────────────
    boolean monthlyCountEnabled;
    Integer monthlyCountLimit;

    // ── Daily amount ──────────────────────────────────────────────────────────
    boolean dailyAmountEnabled;
    BigDecimal dailyAmountLimit;

    // ── Monthly amount ────────────────────────────────────────────────────────
    boolean monthlyAmountEnabled;
    BigDecimal monthlyAmountLimit;

    // ── Misc ──────────────────────────────────────────────────────────────────
    BreachAction breachAction;
    LocalDate effectiveDate;
    String createdBy;
    Instant createdAt;
    String updatedBy;
    Instant updatedAt;
}

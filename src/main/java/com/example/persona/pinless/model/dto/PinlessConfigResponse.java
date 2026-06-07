package com.example.persona.pinless.model.dto;

import com.example.persona.pinless.model.enums.BreachAction;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinlessConfigResponse {

    private Long id;
    private String customerNo;
    private String channel;
    private String currency;
    private boolean enabled;

    // ── Threshold ─────────────────────────────────────────────────────────────
    // Mobile uses thresholdAmount to render the slider's current position.
    // systemCap is returned so mobile knows the slider's right boundary.

    private boolean thresholdEnabled;
    private BigDecimal thresholdAmount;
    private BigDecimal systemCap; // read-only, from system config — never stored per customer

    // ── Other limit dimensions ────────────────────────────────────────────────

    private boolean dailyCountEnabled;
    private Integer dailyCountLimit;

    private boolean monthlyCountEnabled;
    private Integer monthlyCountLimit;

    private boolean dailyAmountEnabled;
    private BigDecimal dailyAmountLimit;

    private boolean monthlyAmountEnabled;
    private BigDecimal monthlyAmountLimit;

    private BreachAction breachAction;
    private LocalDate effectiveDate;

    private String createdBy;
    private Instant createdAt;
    private String updatedBy;
    private Instant updatedAt;
}

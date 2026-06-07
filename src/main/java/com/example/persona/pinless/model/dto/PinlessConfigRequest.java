package com.example.persona.pinless.model.dto;

import com.example.persona.pinless.model.enums.BreachAction;
import com.example.persona.pinless.validation.ValidPinlessConfig;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
@ValidPinlessConfig
public class PinlessConfigRequest {

    @NotBlank(message = "customerNo is required")
    @Size(max = 50, message = "customerNo must not exceed 50 characters")
    private String customerNo;

    @Pattern(regexp = "MOBAPP|WEB|POS", message = "channel must be one of: MOBAPP, WEB, POS")
    private String channel;

    @Pattern(regexp = "[A-Z]{3}", message = "currency must be a 3-letter ISO code (e.g. USD, KHR)")
    private String currency;

    private Boolean enabled;

    // ── Threshold (maps to UI slider) ─────────────────────────────────────────
    // thresholdEnabled = true  → threshold check is active
    // thresholdAmount          → must be >= $1 and <= system cap ($1,000)
    //                            validated in PinlessConfigValidator against system cap

    private Boolean thresholdEnabled;

    @DecimalMin(value = "1.00", message = "thresholdAmount must be at least 1.00")
    @Digits(integer = 16, fraction = 2, message = "thresholdAmount must have at most 2 decimal places")
    private BigDecimal thresholdAmount;

    // ── Daily count ───────────────────────────────────────────────────────────

    private Boolean dailyCountEnabled;

    @Min(value = 1, message = "dailyCountLimit must be at least 1")
    @Max(value = 9999, message = "dailyCountLimit must not exceed 9999")
    private Integer dailyCountLimit;

    // ── Monthly count ─────────────────────────────────────────────────────────

    private Boolean monthlyCountEnabled;

    @Min(value = 1, message = "monthlyCountLimit must be at least 1")
    @Max(value = 99999, message = "monthlyCountLimit must not exceed 99999")
    private Integer monthlyCountLimit;

    // ── Daily amount ──────────────────────────────────────────────────────────

    private Boolean dailyAmountEnabled;

    @DecimalMin(value = "0.01", message = "dailyAmountLimit must be greater than 0")
    @Digits(integer = 16, fraction = 2, message = "dailyAmountLimit must have at most 2 decimal places")
    private BigDecimal dailyAmountLimit;

    // ── Monthly amount ────────────────────────────────────────────────────────

    private Boolean monthlyAmountEnabled;

    @DecimalMin(value = "0.01", message = "monthlyAmountLimit must be greater than 0")
    @Digits(integer = 16, fraction = 2, message = "monthlyAmountLimit must have at most 2 decimal places")
    private BigDecimal monthlyAmountLimit;

    // ── Misc ──────────────────────────────────────────────────────────────────

    private BreachAction breachAction;

    @Size(max = 255, message = "changeReason must not exceed 255 characters")
    private String changeReason;
}

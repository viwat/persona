package com.example.persona.pinless.validation;

import com.example.persona.pinless.config.PinlessSystemProperties;
import com.example.persona.pinless.model.dto.PinlessConfigRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PinlessConfigValidator implements ConstraintValidator<ValidPinlessConfig, PinlessConfigRequest> {

    private final PinlessSystemProperties systemProperties;

    @Override
    public boolean isValid(PinlessConfigRequest req, ConstraintValidatorContext ctx) {
        if (req == null) return true;

        boolean valid = true;
        ctx.disableDefaultConstraintViolation();

        String currency = req.getCurrency() != null ? req.getCurrency() : "USD";

        // ── Threshold rules ───────────────────────────────────────────────────

        if (req.getThresholdAmount() != null) {
            BigDecimal min = systemProperties.getSystem().getMinThreshold(currency);
            BigDecimal cap = systemProperties.getSystem().getCap(currency);

            if (min != null && req.getThresholdAmount().compareTo(min) < 0) {
                addError(ctx, "thresholdAmount", "thresholdAmount must be at least " + min + " " + currency);
                valid = false;
            }

            if (cap != null && req.getThresholdAmount().compareTo(cap) > 0) {
                addError(
                        ctx,
                        "thresholdAmount",
                        "thresholdAmount must not exceed system cap of " + cap + " " + currency);
                valid = false;
            }
        }

        // Contradictory: thresholdEnabled=false but amount provided
        if (Boolean.FALSE.equals(req.getThresholdEnabled()) && req.getThresholdAmount() != null) {
            addError(ctx, "thresholdEnabled", "thresholdEnabled cannot be false when thresholdAmount is provided");
            valid = false;
        }

        // ── Count limit rules ─────────────────────────────────────────────────

        if (Boolean.TRUE.equals(req.getDailyCountEnabled()) && req.getDailyCountLimit() == null) {
            addError(ctx, "dailyCountLimit", "dailyCountLimit is required when dailyCountEnabled is true");
            valid = false;
        }

        if (Boolean.TRUE.equals(req.getMonthlyCountEnabled()) && req.getMonthlyCountLimit() == null) {
            addError(ctx, "monthlyCountLimit", "monthlyCountLimit is required when monthlyCountEnabled is true");
            valid = false;
        }

        if (Boolean.TRUE.equals(req.getDailyCountEnabled())
                && Boolean.TRUE.equals(req.getMonthlyCountEnabled())
                && req.getDailyCountLimit() != null
                && req.getMonthlyCountLimit() != null
                && req.getDailyCountLimit() > req.getMonthlyCountLimit()) {
            addError(ctx, "dailyCountLimit", "dailyCountLimit must not exceed monthlyCountLimit");
            valid = false;
        }

        // ── Amount limit rules ────────────────────────────────────────────────

        if (Boolean.TRUE.equals(req.getDailyAmountEnabled()) && req.getDailyAmountLimit() == null) {
            addError(ctx, "dailyAmountLimit", "dailyAmountLimit is required when dailyAmountEnabled is true");
            valid = false;
        }

        if (Boolean.TRUE.equals(req.getMonthlyAmountEnabled()) && req.getMonthlyAmountLimit() == null) {
            addError(ctx, "monthlyAmountLimit", "monthlyAmountLimit is required when monthlyAmountEnabled is true");
            valid = false;
        }

        if (Boolean.TRUE.equals(req.getDailyAmountEnabled())
                && Boolean.TRUE.equals(req.getMonthlyAmountEnabled())
                && req.getDailyAmountLimit() != null
                && req.getMonthlyAmountLimit() != null
                && req.getDailyAmountLimit().compareTo(req.getMonthlyAmountLimit()) > 0) {
            addError(ctx, "dailyAmountLimit", "dailyAmountLimit must not exceed monthlyAmountLimit");
            valid = false;
        }

        return valid;
    }

    private void addError(ConstraintValidatorContext ctx, String field, String message) {
        ctx.buildConstraintViolationWithTemplate(message).addPropertyNode(field).addConstraintViolation();
    }
}

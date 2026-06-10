package com.example.persona.pinless.model.dto;

import com.example.persona.pinless.model.enums.BreachReason;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResolveResponse {

    private String authLevel; // STEP_UP | BLOCKED
    private List<String> steps;
    private int stepCount;
    private BreachReason breachReason;
    private String message;

    public static AuthResolveResponse stepUp(List<String> policySteps) {
        return AuthResolveResponse.builder()
                .authLevel("STEP_UP")
                .steps(policySteps)
                .stepCount(policySteps.size())
                .message(
                        policySteps.size() == 1
                                ? "Face ID authentication required"
                                : "Enhanced authentication required")
                .build();
    }

    public static AuthResolveResponse blocked(BreachReason reason) {
        List<String> steps = List.of("PIN");
        return AuthResolveResponse.builder()
                .authLevel("BLOCKED")
                .steps(steps)
                .stepCount(steps.size())
                .breachReason(reason)
                .message(resolveMessage(reason))
                .build();
    }

    private static String resolveMessage(BreachReason reason) {
        return switch (reason) {
            case PINLESS_DISABLED -> "Pinless is not enabled for this customer";
            case CURRENCY_MISMATCH -> "Transaction currency does not match pinless config currency";
            case THRESHOLD_EXCEEDED -> "Transaction amount exceeds threshold";
            case DAILY_COUNT_EXCEEDED -> "Daily pinless transaction count limit reached";
            case MONTHLY_COUNT_EXCEEDED -> "Monthly pinless transaction count limit reached";
            case DAILY_AMOUNT_EXCEEDED -> "Daily pinless amount limit reached";
            case MONTHLY_AMOUNT_EXCEEDED -> "Monthly pinless amount limit reached";
        };
    }
}

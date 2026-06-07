package com.example.persona.transaction.dto;

import com.example.persona.transaction.model.LimitThreshold;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LimitThresholdResponse {
    private Long thresholdSn;
    private String serviceType;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String minAmountType;
    private String maxAmountType;
    private String description;
    private String data;
    private String metadata;

    public static LimitThresholdResponse from(LimitThreshold threshold) {
        return LimitThresholdResponse.builder()
                .thresholdSn(threshold.getId())
                .serviceType(threshold.getServiceType())
                .minAmount(threshold.getMinAmount())
                .maxAmount(threshold.getMaxAmount())
                .minAmountType(threshold.getMinAmountType())
                .maxAmountType(threshold.getMaxAmountType())
                .description(threshold.getDescription())
                .data(threshold.getData())
                .metadata(threshold.getMetadata())
                .build();
    }
}

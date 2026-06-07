package com.example.persona.transaction.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LimitThresholdRequest {
    @JsonProperty("threshold_sn")
    private Long thresholdSn;

    @JsonProperty("service_type")
    private String serviceType;

    @JsonProperty("min_amount")
    private BigDecimal minAmount;

    @JsonProperty("max_amount")
    private BigDecimal maxAmount;

    @JsonProperty("min_amount_type")
    private String minAmountType;

    @JsonProperty("max_amount_type")
    private String maxAmountType;

    @JsonProperty("description")
    private String description;

    @JsonProperty("data")
    private String data;

    @JsonProperty("metadata")
    private String metadata;
}

package com.example.persona.pinless.model.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PinlessSummaryResponse {
    private String customerNo;
    private long totalResolutions; // PIN + PINLESS
    private long pinlessCount;
    private long pinCount;
    private BigDecimal totalPinlessAmount;
    private Instant from;
    private Instant to;
}

package com.example.persona.pinless.model.dto;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class CustomerCounter {
    String customerNo;
    String currency;
    long dailyCount;
    long monthlyCount;
    BigDecimal dailyAmount;
    BigDecimal monthlyAmount;
}

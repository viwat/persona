package com.example.persona.pinless.service;

import com.example.persona.pinless.model.dto.PinlessConfigData;
import com.example.persona.pinless.model.enums.BreachReason;
import java.math.BigDecimal;

public interface PinlessCounterService {
    BreachReason checkAndIncrement(String customerNo, BigDecimal amount, PinlessConfigData config);

    void warmUp(
            String customerNo,
            String currency,
            long dailyCount,
            long monthlyCount,
            BigDecimal dailyAmount,
            BigDecimal monthlyAmount);
}

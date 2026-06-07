package com.example.persona.transaction.dto.request;

import com.example.persona.dto.TransactionBaseRequest;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TxnDetailRequest extends TransactionBaseRequest {
    private BigDecimal amount;
}

package com.example.persona.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionBaseRequest extends CustomerBaseRequest {
    private String transactionId;
    private String referenceId;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal amountinUSD;
    private BigDecimal feeCharged;
    private String title;
    private String imageUrl;
    private String description;
    private String type;
    private String category;
    private String subCategory;
    private String account;
    private BigDecimal balance;
    private String currency;
    private String status;
    private String transactionDate;
    private String transactionTime;
    private String transactionCategory;
    private String transactionSubCategory;

    private String metadata;
    private HashMap<String, String> attributes;
}

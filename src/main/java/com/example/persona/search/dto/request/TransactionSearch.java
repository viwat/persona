package com.example.persona.search.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSearch {
    public String _id;

    @JsonProperty("customer_key")
    private String customerKey;

    @JsonProperty("customer_no")
    private String customerNo;

    @JsonProperty("account_no")
    private String accountNo;

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("entry_type")
    private String entryType;

    @JsonProperty("service_type")
    private String serviceType;

    @JsonProperty("service_sub_type")
    private String serviceSubType;

    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("location")
    private String location;

    @JsonProperty("deep_link")
    private String deepLink;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("amount_in_usd")
    private BigDecimal amountInUsd;

    @JsonProperty("transaction_timestamp")
    private Long transactionTimestamp;

    @JsonProperty("day")
    private String day;

    @JsonProperty("month")
    private String month;

    @JsonProperty("year")
    private String year;

    @JsonProperty("_geo")
    private GeoLocation geoLocation;

    @JsonProperty("metadata")
    private String metadata;

    @JsonProperty("tags")
    private String tags;
}

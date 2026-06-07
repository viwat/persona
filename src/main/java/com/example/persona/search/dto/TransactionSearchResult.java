package com.example.persona.search.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.persona.search.dto.request.GeoLocation;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionSearchResult {
    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("_id")
    private String id;

    @JsonProperty("customer_key")
    private String customerKey;

    @JsonProperty("customer_no")
    private String customerNo;

    @JsonProperty("account_no")
    private String accountNo;

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
    private GeoLocation geo;

    @JsonProperty("metadata")
    private String metadata;

    @JsonProperty("tags")
    private String tags;

    // Map to store any additional/dynamic fields
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        additionalProperties.put(name, value);
    }
}

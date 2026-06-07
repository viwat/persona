package com.example.persona.search.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PeopleSearch {
    public String _id;

    @JsonProperty("customer_key")
    private String customerKey;

    @JsonProperty("customer_no")
    private String customerNo;

    @JsonProperty("account_no")
    private String accountNo;

    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("name")
    private String name;

    @JsonProperty("short_name")
    private String shortName;

    @JsonProperty("profile_url")
    private String profileUrl;

    @JsonProperty("deep_link")
    private String deepLink;

    @JsonProperty("metadata")
    private String metadata;

    @JsonProperty("tags")
    private String tags;
}

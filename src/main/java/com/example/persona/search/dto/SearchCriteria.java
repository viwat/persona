package com.example.persona.search.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    @JsonAlias({"customer_key", "master_account_id"})
    @NotNull(message = "customer_key is required")
    private String customerKey;

    @JsonAlias({"query", "q"})
    @NotNull(message = "query is required")
    private String query; // A keyword typed by the user

    @JsonAlias("customer_no")
    private String customerNo;

    @JsonAlias("account_no")
    private String accountNo;

    @JsonAlias("type")
    private String type; // Property type

    @JsonAlias("location")
    private Double[] location; // Geo Point [lat, lng]

    @JsonAlias("radius")
    private Double radius;

    @JsonAlias("geo_bounds")
    private Double[][] geoBounds; // Bounds on the map [ [lat, lng], [lat, lng] ] (North East coords, South West
    // Coords)
}

package com.example.persona.location.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

/**
 * Extensible enum for location types.
 * New types can be added without breaking existing consumers.
 */
public enum LocationType {
    BRANCH("branch", "Bank Branch"),
    ATM_CRM("atm_crm", "ATM/CRM"),
    AGENT("agent", "Bank Agent"),
    MASTER_AGENT("master_agent", "Bank Master Agent");

    private final String code;
    private final String displayName;

    LocationType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static LocationType fromCode(String code) {
        return Arrays.stream(values())
                .filter(t -> t.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown location type: " + code));
    }
}

// ── PinlessConfigHistoryResponse.java ────────────────────────────────────────
package com.example.persona.pinless.model.dto;

import com.example.persona.pinless.model.enums.ConfigAction;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PinlessConfigHistoryResponse {
    private Long id;
    private ConfigAction action;
    private Map<String, Object> oldValue;
    private Map<String, Object> newValue;
    private String changedBy;
    private Instant changedAt;
    private String changeReason;
}

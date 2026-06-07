package com.example.persona.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HomeScreenMetadata {
    private LocalDateTime lastUpdateTimestamp;
    private boolean hasUpdates;
    private List<String> updatedKeys;
}

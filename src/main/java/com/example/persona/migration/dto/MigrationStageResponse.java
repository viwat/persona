package com.example.persona.migration.dto;

import com.example.persona.migration.model.MigrationStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MigrationStageResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer displayOrder;
    private Boolean isFinal;
    private Boolean isMandatory;

    public static MigrationStageResponse fromEntity(MigrationStage stage) {
        return MigrationStageResponse.builder()
                .id(stage.getId())
                .code(stage.getCode())
                .name(stage.getName())
                .description(stage.getDescription())
                .displayOrder(stage.getDisplayOrder())
                .isFinal(stage.getIsFinal())
                .isMandatory(stage.getIsMandatory())
                .build();
    }
}

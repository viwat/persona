package com.example.persona.misc.dto.request;

import com.example.persona.enums.StatusType;
import com.example.persona.model.MultilingualContent;
import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnumerationModifyRequest implements Serializable {
    private Long parentId;

    private String category;

    private String subCategory;

    private String code;

    private MultilingualContent value;

    private String label;

    private String description;

    private MultilingualContent displayValue;

    private String displayCondition;

    private Integer displayOrder;

    private Boolean isDefault;

    private String metadata;

    private StatusType status;
}

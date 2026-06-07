package com.example.persona.misc.dto.response;

import com.example.persona.enums.StatusType;
import com.example.persona.model.MultilingualContent;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnumerationResponse implements Serializable {

    private Long id;

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

    private String createdBy;
    private LocalDateTime createdDate;
    private String modifiedBy;
    private LocalDateTime modifiedDate;

    private Integer version;
}

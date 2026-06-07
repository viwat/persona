package com.example.persona.migration.model;

import com.example.persona.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Master data for migration stages. Flexible design allows adding new stages by
 * inserting records. Stages are ordered by displayOrder field.
 */
@Entity
@Table(name = "dgtl_migration_stage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MigrationStage extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_final", nullable = false)
    private Boolean isFinal;

    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory;

    @Column(name = "metadata", length = 1000)
    private String metadata;
}

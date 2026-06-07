package com.example.persona.transaction.model;

import com.example.persona.model.BaseModel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dgtl_limit_threshold")
public class LimitThreshold extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "min_amount", nullable = false)
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false)
    private BigDecimal maxAmount;

    @Column(name = "min_amount_type", nullable = false)
    private String minAmountType;

    @Column(name = "max_amount_type", nullable = false)
    private String maxAmountType;

    @Column(name = "description")
    private String description;

    @Column(name = "data")
    private String data;

    @Column(name = "metadata")
    private String metadata;
}

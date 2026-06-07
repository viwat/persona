package com.example.persona.transaction.model;

import com.example.persona.model.BaseModel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dgtl_authentication_threshold")
@Getter
@Setter
public class AuthenticationThreshold extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_method", nullable = false)
    private AuthenticationMethod authenticationMethod;

    @Column(name = "min_amount", nullable = false)
    private BigDecimal minimumAmount;

    @Column(name = "max_amount", nullable = false)
    private BigDecimal maximumAmount;
}

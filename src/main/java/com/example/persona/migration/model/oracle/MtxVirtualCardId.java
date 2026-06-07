package com.example.persona.migration.model.oracle;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MtxVirtualCardId implements Serializable {
    private String masterAccId;
    private String trackingNumber;
    private String cardType;
}

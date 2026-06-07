package com.example.persona.migration.model.oracle;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PortalMerchantNotificationId implements Serializable {

    @Column(name = "MASTER_ACC_ID", length = 20)
    private String masterAccId;

    @Column(name = "NOTIFICATION_ID", length = 100)
    private String notificationId;
}

package com.example.persona.migration.model.oracle;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "PORTAL_MERCHANT_NOTIFICAION")
public class PortalMerchantNotification {

    @EmbeddedId
    private PortalMerchantNotificationId id;

    @Column(name = "NOTIFICATION_STATUS")
    private Integer notificationStatus;

    @Column(name = "CREATED_ON")
    private LocalDateTime createdOn;
}

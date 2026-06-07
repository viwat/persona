package com.example.persona.migration.repository.oracle;

import com.example.persona.migration.model.oracle.PortalMerchantNotification;
import com.example.persona.migration.model.oracle.PortalMerchantNotificationId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortalMerchantNotificationRepository
        extends JpaRepository<PortalMerchantNotification, PortalMerchantNotificationId> {

    List<PortalMerchantNotification> findByIdMasterAccId(String masterAccId);

    PortalMerchantNotification findByIdMasterAccIdAndIdNotificationId(String masterAccId, String notificationId);

    List<PortalMerchantNotification> findByNotificationStatus(Integer notificationStatus);
}

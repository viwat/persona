package com.example.persona.migration.repository.oracle;

import com.example.persona.migration.model.oracle.PortalMasterDevice;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortalMasterDeviceRepository extends JpaRepository<PortalMasterDevice, String> {

    Optional<PortalMasterDevice> findByMasterAccIdAndStatus(String masterAccId, String status);

    Optional<PortalMasterDevice> findByDeviceId(String deviceId);
}

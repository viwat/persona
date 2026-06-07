package com.example.persona.migration.model.oracle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PORTAL_MASTER_DEVICES_ID")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalMasterDevice {

    @Id
    @Column(name = "MASTER_DEVICE_ID", length = 20, nullable = false)
    private String masterDeviceId;

    @Column(name = "MASTER_ACC_ID", length = 50, nullable = false)
    private String masterAccId;

    @Column(name = "DEVICE_ID", length = 100, nullable = false)
    private String deviceId;

    @Column(name = "STATUS", length = 5)
    private String status;

    @Column(name = "CREATED_ON")
    private LocalDateTime createdOn;

    @Column(name = "OS_PLATFORM", length = 16)
    private String osPlatform;

    @Column(name = "OS_VERSION", length = 16)
    private String osVersion;

    @Column(name = "MODIFIED_ON")
    private LocalDateTime modifiedOn;
}

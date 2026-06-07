package com.example.persona.migration.handler;

import com.example.persona.client.CdpClient;
import com.example.persona.dto.CdpKycStatus;
import com.example.persona.dto.response.AccountDetail;
import com.example.persona.migration.constants.MigrationConstants;
import com.example.persona.migration.dto.MigrationResult;
import com.example.persona.migration.model.CustomerMigrationStage;
import com.example.persona.migration.model.oracle.DigiMasterAccountView;
import com.example.persona.migration.model.oracle.PortalMasterDevice;
import com.example.persona.migration.repository.oracle.DigiMasterAccountViewRepository;
import com.example.persona.migration.repository.oracle.PortalMasterDeviceRepository;
import com.example.persona.migration.util.CustomerKeyParser;
import com.example.persona.migration.util.CustomerProfileMigrationFields;
import com.example.persona.profile.dto.request.CustomerAppProfileRequest;
import com.example.persona.profile.service.CustomerAppProfileService;
import com.example.persona.profile.service.CustomerProfileService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Handler for DEVICE migration stage. Migrates device registration data from
 * Oracle to PostgreSQL.
 *
 * <p>Absence of an active device is treated as a soft skip (stage completes
 * with metadata) rather than a failure — customers that never registered a
 * device are a valid state and should not block the overall migration job.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceMigrationHandler implements MigrationStageHandler {

    public static final String STAGE_CODE = "DEVICE";
    private static final String DEVICE_STATUS_ACTIVE = "DA";
    private static final String CHANNEL_CODE = "MOBAPP";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DigiMasterAccountViewRepository digiMasterAccountViewRepository;
    private final PortalMasterDeviceRepository portalMasterDeviceRepository;
    private final CustomerAppProfileService customerAppProfileService;
    private final CustomerProfileService customerProfileService;
    private final CdpClient cdpClient;

    @Override
    public String getStageCode() {
        return STAGE_CODE;
    }

    @Override
    public MigrationResult execute(CustomerMigrationStage customerMigrationStage) {
        String customerKey = customerMigrationStage != null ? customerMigrationStage.getCustomerKey() : null;
        log.info("Starting DEVICE migration for customer: {}", customerKey);

        try {
            // 1. Parse Key (Do this once)
            CustomerKeyParser.ParsedKey parsedKey = CustomerKeyParser.parse(
                    StringUtils.hasText(customerKey) ? customerKey : "", CustomerKeyParser.LoginIdStrategy.ACCOUNT_NO);

            // 2. Fetch Oracle Data (Fail fast if not found)
            DigiMasterAccountView oracleRecord = getDataFromSource(parsedKey)
                    .orElseThrow(() -> new IllegalArgumentException("Oracle data not found for key: " + customerKey));

            // 3. Fetch Device Data
            Optional<PortalMasterDevice> deviceOpt = portalMasterDeviceRepository.findByMasterAccIdAndStatus(
                    oracleRecord.getMasterAccId(), DEVICE_STATUS_ACTIVE);

            // 4. Fetch additional customer data from CDP (optional)
            AccountDetail.AccountDetailData cdpData = fetchCdpData(parsedKey.accountNo());

            mergeCustomerProfileForStage(oracleRecord, parsedKey, cdpData);

            if (deviceOpt.isEmpty()) {
                // Customer never registered a device — nothing to migrate; soft skip.
                log.info(
                        "No active device for customer: {} (MasterAccId: {}) — stage completed with skip",
                        customerKey,
                        oracleRecord.getMasterAccId());
                return MigrationResult.completed("NO_ACTIVE_DEVICE");
            }

            CustomerAppProfileRequest request =
                    buildProfileRequest(oracleRecord, parsedKey, deviceOpt.get(), cdpData, customerKey);
            customerAppProfileService.mergeCustomerAppProfileForMigration(request);

            log.info("DEVICE migration completed for customer: {}", customerKey);
            return MigrationResult.completed();

        } catch (Exception e) {
            log.error("DEVICE migration failed for customer: {}", customerKey, e);
            return MigrationResult.failure("DEVICE_ERROR", e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }

    private Optional<DigiMasterAccountView> getDataFromSource(CustomerKeyParser.ParsedKey key) {
        if (StringUtils.hasText(key.loginId())) {
            Optional<DigiMasterAccountView> result = digiMasterAccountViewRepository.findByLoginIdAndApplicationId(
                    key.loginId(), MigrationConstants.APPLICATION_ID_WINGPAY);
            if (result.isPresent()) {
                return result;
            }
        }

        if (StringUtils.hasText(key.accountNo())) {
            Optional<DigiMasterAccountView> result = digiMasterAccountViewRepository.findByLoginIdAndApplicationId(
                    key.accountNo(), MigrationConstants.APPLICATION_ID_WINGPAY);
            if (result.isPresent()) {
                return result;
            }
        }

        if (StringUtils.hasText(key.masterAccId())) {
            Optional<DigiMasterAccountView> result =
                    digiMasterAccountViewRepository.findByMasterAccId(key.masterAccId());
            if (result.isPresent()) {
                return result;
            }
        }

        return Optional.empty();
    }

    private void mergeCustomerProfileForStage(
            DigiMasterAccountView oracle, CustomerKeyParser.ParsedKey key, AccountDetail.AccountDetailData cdpData) {
        var finalCustomerNo = StringUtils.hasText(key.customerNo()) ? key.customerNo() : oracle.getPartyId();
        if (!StringUtils.hasText(finalCustomerNo)) {
            return;
        }
        var customerName = CustomerProfileMigrationFields.resolveCustomerNameForMigration(cdpData);
        customerProfileService.mergeCustomerProfileForMigration(
                finalCustomerNo, customerName, CdpKycStatus.resolve(cdpData), cdpData);
    }

    private AccountDetail.AccountDetailData fetchCdpData(String accountNo) {
        if (!StringUtils.hasText(accountNo)) {
            return null;
        }
        try {
            AccountDetail.AccountDetailData cdpData = cdpClient.getAccountInfo(accountNo);
            if (cdpData != null) {
                log.debug("Found CDP data for accountNo: {}", accountNo);
            }
            return cdpData;
        } catch (Exception e) {
            log.warn("Failed to fetch CDP data for accountNo: {} - {}", accountNo, e.getMessage());
            return null;
        }
    }

    private CustomerAppProfileRequest buildProfileRequest(
            DigiMasterAccountView oracle,
            CustomerKeyParser.ParsedKey key,
            PortalMasterDevice device,
            AccountDetail.AccountDetailData cdpData,
            String migrationCustomerKey) {
        var finalCustomerNo = StringUtils.hasText(key.customerNo()) ? key.customerNo() : oracle.getPartyId();
        var finalAccountNo = StringUtils.hasText(key.accountNo()) ? key.accountNo() : oracle.getMasterAccId();

        var customerName = CustomerProfileMigrationFields.resolveCustomerNameForMigration(cdpData);

        var builder = CustomerAppProfileRequest.builder()
                // From parsed key
                .customerNo(finalCustomerNo)
                .accountNo(finalAccountNo)
                .customerKey(StringUtils.hasText(migrationCustomerKey) ? migrationCustomerKey.trim() : null)
                .phoneNo(CustomerProfileMigrationFields.resolvePhoneForMigration(
                        oracle != null ? oracle.getPhoneNumber() : null, cdpData))
                .masterAccountNo(oracle != null ? oracle.getMasterAccId() : null)
                .customerName(customerName)
                .kycStatus(CdpKycStatus.resolve(cdpData))
                .channelCode(CHANNEL_CODE)
                // From Oracle PortalMasterDevice
                .deviceId(device != null ? device.getDeviceId() : null);

        // Populate additional fields from CDP if available (all optional)
        if (cdpData != null) {
            builder.gender(cdpData.getSex());
            builder.nationality(cdpData.getNationality());
            builder.dateOfBirth(parseDateOfBirth(cdpData.getDataOfBirth()));
            builder.maritalStatus(cdpData.getMaritalStatus());
            builder.email(cdpData.getEmail());
            builder.placeOfBirth(cdpData.getPlaceOfBirth());
            builder.currentAddress(CustomerProfileMigrationFields.resolveCurrentAddress(cdpData));
            builder.idType(cdpData.getUniqueIdName());
            builder.idNumber(cdpData.getUniqueIdValue());
            builder.profileIdentificationExpired(
                    CustomerProfileMigrationFields.parseUniqueIdExpiration(cdpData.getUniqueIdExp()));
            if (StringUtils.hasText(cdpData.getFirstNameInKhmer())
                    || StringUtils.hasText(cdpData.getLastNameInKhmer())) {
                builder.customerNameKh(CustomerProfileMigrationFields.joinKhmerNames(
                        cdpData.getFirstNameInKhmer(), cdpData.getLastNameInKhmer()));
            }
        }

        return builder.build();
    }

    private LocalDate parseDateOfBirth(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.debug("Could not parse date of birth: {} - {}", dateStr, e.getMessage());
            return null;
        }
    }
}

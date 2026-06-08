package com.example.persona.migration.handler;

import com.example.persona.client.CdpClient;
import com.example.persona.dto.CdpKycStatus;
import com.example.persona.dto.response.AccountDetail;
import com.example.persona.migration.constants.MigrationConstants;
import com.example.persona.migration.dto.MigrationResult;
import com.example.persona.migration.model.CustomerMigrationStage;
import com.example.persona.migration.model.oracle.DigiMasterAccountView;
import com.example.persona.migration.repository.oracle.DigiMasterAccountViewRepository;
import com.example.persona.migration.util.CustomerKeyParser;
import com.example.persona.migration.util.CustomerKeyParser.LoginIdStrategy;
import com.example.persona.migration.util.CustomerKeyParser.ParsedKey;
import com.example.persona.migration.util.CustomerProfileMigrationFields;
import com.example.persona.profile.dto.request.CustomerAppProfileRequest;
import com.example.persona.profile.service.CustomerAppProfileService;
import com.example.persona.profile.service.CustomerProfileService;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Handler for MASTER_ACCOUNT migration stage. Migrates master account data from
 * Oracle to PostgreSQL.
 */
@Slf4j
@Component
public class MasterAccountMigrationHandler extends AbstractOracleMigrationHandler {

    public static final String STAGE_CODE = "MASTER_ACCOUNT";

    private final CustomerAppProfileService customerAppProfileService;
    private final CustomerProfileService customerProfileService;

    public MasterAccountMigrationHandler(
            DigiMasterAccountViewRepository digiMasterAccountViewRepository,
            CdpClient cdpClient,
            CustomerAppProfileService customerAppProfileService,
            CustomerProfileService customerProfileService) {
        super(digiMasterAccountViewRepository, cdpClient);
        this.customerAppProfileService = customerAppProfileService;
        this.customerProfileService = customerProfileService;
    }

    @Override
    public String getStageCode() {
        return STAGE_CODE;
    }

    @Override
    public MigrationResult execute(CustomerMigrationStage customerMigrationStage) {
        String customerKey = customerMigrationStage != null ? customerMigrationStage.getCustomerKey() : null;
        log.info("Starting MASTER_ACCOUNT migration for customer: {}", customerKey);

        try {
            // 1. Parse Customer Key ONCE
            ParsedKey parsedKey = CustomerKeyParser.parse(
                    StringUtils.hasText(customerKey) ? customerKey : "", LoginIdStrategy.ACCOUNT_NO);

            // 2. Fetch master account data from Oracle (read-only)
            Optional<DigiMasterAccountView> oracleData = getDataFromSource(parsedKey);

            if (oracleData.isEmpty()) {
                log.warn("No Oracle data found for customer: {} (Parsed: {})", customerKey, parsedKey);
                return MigrationResult.failure(
                        "ORACLE_DATA_NOT_FOUND", "No master account data found in Oracle for customer: " + customerKey);
            }

            DigiMasterAccountView oracleRecord = oracleData.get();
            log.info(
                    "Found Oracle data for masterAccId: {}, loginId: {}",
                    oracleRecord.getMasterAccId(),
                    oracleRecord.getLoginId());

            // 3. Fetch additional customer data from CDP (optional)
            AccountDetail.AccountDetailData cdpData = fetchCdpData(parsedKey.accountNo());

            // 4. Transform & Save (pin canonical customer_key from migration so version_key matches all stages)
            CustomerAppProfileRequest profileRequest =
                    transformToRequest(oracleRecord, parsedKey, cdpData, customerKey);
            customerAppProfileService.mergeCustomerAppProfileForMigration(profileRequest);
            customerProfileService.mergeCustomerProfileForMigration(
                    profileRequest.getCustomerNo(),
                    profileRequest.getCustomerName(),
                    profileRequest.getKycStatus(),
                    cdpData);

            log.info("Successfully migrated master account data to PostgreSQL for customer: {}", customerKey);
            return MigrationResult.completed();

        } catch (Exception e) {
            log.error("MASTER_ACCOUNT migration failed for customer: {}", customerKey, e);
            return MigrationResult.failure(
                    "MASTER_ACCOUNT_ERROR", e.getMessage() != null ? e.getMessage() : "Unknown error");
        }
    }

    private CustomerAppProfileRequest transformToRequest(
            DigiMasterAccountView oracle,
            ParsedKey key,
            AccountDetail.AccountDetailData cdpData,
            String migrationCustomerKey) {
        var finalCustomerNo = StringUtils.hasText(key.customerNo()) ? key.customerNo() : oracle.getPartyId();
        var finalAccountNo = StringUtils.hasText(key.accountNo()) ? key.accountNo() : oracle.getMasterAccId();

        var customerName = CustomerProfileMigrationFields.resolveCustomerNameForMigration(cdpData);

        var builder = CustomerAppProfileRequest.builder()
                .customerNo(finalCustomerNo)
                .accountNo(finalAccountNo)
                .customerKey(StringUtils.hasText(migrationCustomerKey) ? migrationCustomerKey.trim() : null)
                .phoneNo(CustomerProfileMigrationFields.resolvePhoneForMigration(
                        oracle != null ? oracle.getPhoneNumber() : null, cdpData))
                .masterAccountNo(oracle != null ? oracle.getMasterAccId() : null)
                .customerName(customerName)
                .kycStatus(CdpKycStatus.resolve(cdpData))
                .channelCode(MigrationConstants.CHANNEL_CODE_MOBAPP);

        // Populate additional fields from CDP if available
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
}

package com.example.persona.profile.service;

import com.example.persona.client.CdpClient;
import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.dto.response.AccountDetail;
import com.example.persona.enums.StatusType;
import com.example.persona.exception.BusinessException;
import com.example.persona.profile.dto.request.CustomerAppProfileRequest;
import com.example.persona.profile.dto.response.CustomerAppProfileResponse;
import com.example.persona.profile.model.CustomerAppProfile;
import com.example.persona.profile.repository.CustomerAppProfileRepository;
import com.example.persona.utils.NullableDisplayUtils;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerAppProfileService {

    private final CustomerAppProfileRepository customerAppProfileRepository;
    private final ProfileVersioningService versioningService;
    private final CdpClient cdpClient;

    @Transactional(readOnly = true)
    public CustomerAppProfileResponse getCustomerAppProfile(String customerNo) {
        CustomerBaseRequest request =
                CustomerBaseRequest.builder().customerNo(customerNo).build();
        return getCustomerAppProfile(request);
    }

    /**
     * Get profile by (in order): provided {@code customer_key}; else
     * {@code customer_no + "_" + account_no} when both are set; else the latest active row by
     * {@code customer_no} or {@code account_no} alone ({@code created_date} desc, then {@code id} desc).
     */
    @Transactional(readOnly = true)
    public CustomerAppProfileResponse getCustomerAppProfile(CustomerBaseRequest request) {
        requireLookupIdentifiers(request);
        return findActiveCustomerAppProfile(request)
                .map(this::mapToResponse)
                .orElseThrow(() -> new BusinessException("Customer app profile not found"));
    }

    /**
     * {@link #getCustomerAppProfile(CustomerBaseRequest)}: explicit {@code customer_key},
     * composed {@code customer_no}_{@code account_no}, or latest active by {@code customer_no} / {@code account_no}.
     */
    private Optional<CustomerAppProfile> findActiveCustomerAppProfile(CustomerBaseRequest request) {
        if (request.hasProvidedCustomerKey()) {
            log.debug("Lookup customer app profile by provided customer_key");
            return customerAppProfileRepository.findByCustomerKeyAndStatus(
                    request.getProvidedCustomerKey(), StatusType.ACTIVE);
        }

        boolean hasCustomerNo = StringUtils.hasText(request.getCustomerNo());
        boolean hasAccountNo = StringUtils.hasText(request.getAccountNo());

        if (hasCustomerNo && hasAccountNo) {
            String customerKey = request.getCustomerNo().trim() + "_"
                    + request.getAccountNo().trim();
            log.debug("Lookup customer app profile by composed customer_key {}", customerKey);
            return customerAppProfileRepository.findByCustomerKeyAndStatus(customerKey, StatusType.ACTIVE);
        }

        if (hasCustomerNo) {
            log.debug("Lookup customer app profile by customer_no (latest active)");
            return customerAppProfileRepository.findFirstByCustomerNoAndStatusOrderByCreatedDateDescIdDesc(
                    request.getCustomerNo().trim(), StatusType.ACTIVE);
        }

        log.debug("Lookup customer app profile by account_no (latest active)");
        return customerAppProfileRepository.findFirstByAccountNoAndStatusOrderByCreatedDateDescIdDesc(
                request.getAccountNo().trim(), StatusType.ACTIVE);
    }

    private static boolean hasLookupIdentifiers(CustomerBaseRequest request) {
        return request.hasProvidedCustomerKey()
                || StringUtils.hasText(request.getCustomerNo())
                || StringUtils.hasText(request.getAccountNo());
    }

    private static void requireLookupIdentifiers(CustomerBaseRequest request) {
        if (!hasLookupIdentifiers(request)) {
            throw new BusinessException(
                    "Provide customer_key, or customer_no with account_no, or customer_no or account_no");
        }
    }

    /**
     * Version key for a <em>new</em> customer app profile row; aligned with {@link #findActiveCustomerAppProfile}.
     */
    private static String resolveCustomerAppVersionKey(CustomerBaseRequest request) {
        requireLookupIdentifiers(request);
        if (request.hasProvidedCustomerKey()) {
            return request.getProvidedCustomerKey();
        }
        boolean hasCustomerNo = StringUtils.hasText(request.getCustomerNo());
        boolean hasAccountNo = StringUtils.hasText(request.getAccountNo());
        if (hasCustomerNo && hasAccountNo) {
            return request.getCustomerNo().trim() + "_" + request.getAccountNo().trim();
        }
        if (hasCustomerNo) {
            return request.getCustomerNo().trim();
        }
        return request.getAccountNo().trim();
    }

    @Transactional
    public CustomerAppProfileResponse createOrUpdateCustomerAppProfile(CustomerAppProfileRequest request) {
        log.debug("Creating or updating customer app profile for request: {}", request);

        requireLookupIdentifiers(request);
        CustomerAppProfile oldVersion = findActiveCustomerAppProfile(request).orElse(null);
        String versionKey = oldVersion != null ? oldVersion.getVersionKey() : resolveCustomerAppVersionKey(request);

        // Create new version (copies all fields from old if exists)
        CustomerAppProfile newVersion = new CustomerAppProfile();
        versioningService.createNewVersion(oldVersion, newVersion, versionKey);

        // Update only changed fields (only update if request has non-null values)
        updateCustomerAppProfileFields(newVersion, request);

        // Save old version (DELETED) and new version (ACTIVE)
        if (oldVersion != null) {
            customerAppProfileRepository.save(oldVersion);
        }
        CustomerAppProfile savedProfile = customerAppProfileRepository.save(newVersion);
        log.info(
                "Customer app profile version {} saved successfully for customer: {}",
                savedProfile.getVersion(),
                request.getCustomerNo());

        return mapToResponse(savedProfile);
    }

    /**
     * Creates or updates the active customer app profile <strong>in a single row</strong> for
     * migration pipelines. Unlike {@link #createOrUpdateCustomerAppProfile}, this does not create
     * a new version row or mark the previous row {@code DELETED}; repeated migration stages update
     * the same entity.
     */
    @Transactional
    public CustomerAppProfileResponse mergeCustomerAppProfileForMigration(CustomerAppProfileRequest request) {
        log.debug("Merge (in-place) customer app profile for migration: {}", request.getCustomerNo());

        String versionKey =
                versioningService.generateCustomerVersionKey(request.getCustomerKey(), request.getCustomerNo());

        Optional<CustomerAppProfile> active =
                customerAppProfileRepository.findByVersionKeyAndStatus(versionKey, StatusType.ACTIVE);

        if (active.isEmpty()) {
            CustomerAppProfile newProfile = new CustomerAppProfile();
            versioningService.createNewVersion(null, newProfile, versionKey);
            updateCustomerAppProfileFields(newProfile, request);
            CustomerAppProfile saved = customerAppProfileRepository.save(newProfile);
            log.info("Migration customer app profile created for customer: {}", request.getCustomerNo());
            return mapToResponse(saved);
        }

        CustomerAppProfile profile = active.get();
        updateCustomerAppProfileFields(profile, request);
        CustomerAppProfile saved = customerAppProfileRepository.save(profile);
        log.info("Migration customer app profile updated in place for customer: {}", request.getCustomerNo());
        return mapToResponse(saved);
    }

    @Transactional
    public CustomerAppProfileResponse createCustomerAppProfile(CustomerAppProfileRequest request) {
        log.debug("Creating customer app profile for request: {}", request);

        requireLookupIdentifiers(request);
        findActiveCustomerAppProfile(request).ifPresent(existing -> {
            throw new BusinessException(
                    String.format("Customer app profile already exists for customer: %s", request.getCustomerNo()));
        });

        String versionKey = resolveCustomerAppVersionKey(request);

        // Create new version (no old version for create)
        CustomerAppProfile newVersion = new CustomerAppProfile();
        versioningService.createNewVersion(null, newVersion, versionKey);

        // Set all fields from request (for create, we set all provided fields)
        updateCustomerAppProfileFields(newVersion, request);

        CustomerAppProfile savedProfile = customerAppProfileRepository.save(newVersion);
        log.info("Customer app profile created successfully for customer: {}", request.getCustomerNo());

        return mapToResponse(savedProfile);
    }

    @Transactional
    public CustomerAppProfileResponse updateCustomerAppProfile(CustomerAppProfileRequest request) {
        log.debug("Updating customer app profile for request: {}", request);

        requireLookupIdentifiers(request);
        CustomerAppProfile oldVersion = findActiveCustomerAppProfile(request)
                .orElseThrow(() -> new BusinessException(
                        String.format("Customer app profile not found for customer: %s", request.getCustomerNo())));

        String versionKey = oldVersion.getVersionKey();

        // Create new version (copies all fields from old)
        CustomerAppProfile newVersion = new CustomerAppProfile();
        versioningService.createNewVersion(oldVersion, newVersion, versionKey);

        // Update only changed fields (only update if request has non-null values)
        updateCustomerAppProfileFields(newVersion, request);

        // Save old version (DELETED) and new version (ACTIVE)
        customerAppProfileRepository.save(oldVersion);
        CustomerAppProfile savedProfile = customerAppProfileRepository.save(newVersion);
        log.info(
                "Customer app profile version {} updated successfully for customer: {}",
                savedProfile.getVersion(),
                request.getCustomerNo());

        return mapToResponse(savedProfile);
    }

    @Transactional
    public CustomerAppProfileResponse updatePinLimit(CustomerBaseRequest request, java.math.BigDecimal pinLimitAmount) {
        log.debug("Updating PIN limit for customer app profile lookup: {}", request);

        requireLookupIdentifiers(request);
        CustomerAppProfile oldVersion = findActiveCustomerAppProfile(request)
                .orElseThrow(() -> new BusinessException("Customer app profile not found"));

        // Create new version
        CustomerAppProfile newVersion = new CustomerAppProfile();
        versioningService.createNewVersion(oldVersion, newVersion, oldVersion.getVersionKey());

        // Update only PIN limit
        newVersion.setPinLimitAmount(pinLimitAmount);

        // Save versions
        customerAppProfileRepository.save(oldVersion);
        CustomerAppProfile savedProfile = customerAppProfileRepository.save(newVersion);

        return mapToResponse(savedProfile);
    }

    @Transactional
    public CustomerAppProfileResponse updateProfileIdentificationExpired(
            CustomerBaseRequest request, LocalDateTime expiredDate) {
        log.debug("Updating profile identification expired date for customer app profile lookup: {}", request);

        requireLookupIdentifiers(request);
        CustomerAppProfile oldVersion = findActiveCustomerAppProfile(request)
                .orElseThrow(() -> new BusinessException("Customer app profile not found"));

        // Create new version
        CustomerAppProfile newVersion = new CustomerAppProfile();
        versioningService.createNewVersion(oldVersion, newVersion, oldVersion.getVersionKey());

        // Update only expiry date
        newVersion.setProfileIdentificationExpired(expiredDate);

        // Save versions
        customerAppProfileRepository.save(oldVersion);
        CustomerAppProfile savedProfile = customerAppProfileRepository.save(newVersion);

        return mapToResponse(savedProfile);
    }

    /**
     * Updates only the fields that are provided (non-null) in the request.
     * Preserves existing values for fields that are not in the request.
     */
    private void updateCustomerAppProfileFields(CustomerAppProfile profile, CustomerAppProfileRequest request) {
        // Base customer fields
        setIfPresent(request.getCustomerKey(), profile::setCustomerKey);
        setIfPresent(request.getCustomerNo(), profile::setCustomerNo);
        setIfPresent(request.getAccountNo(), profile::setAccountNo);
        setIfPresent(request.getPhoneNo(), profile::setPhoneNo);
        setIfPresent(request.getMasterAccountNo(), profile::setMasterAccountNo);
        setIfPresent(request.getChannelCode(), profile::setChannelCode);
        setIfPresent(request.getCustomerName(), profile::setCustomerName);

        // Personal information fields
        setIfPresent(request.getCustomerNameKh(), profile::setCustomerNameKh);
        setIfPresent(request.getGender(), profile::setGender);
        setIfPresent(request.getMaritalStatus(), profile::setMaritalStatus);
        setIfPresent(request.getDateOfBirth(), profile::setDateOfBirth);
        setIfPresent(request.getPlaceOfBirth(), profile::setPlaceOfBirth);
        setIfPresent(request.getNationality(), profile::setNationality);
        setIfPresent(request.getEmail(), profile::setEmail);
        setIfPresent(request.getCurrentAddress(), profile::setCurrentAddress);

        // Registration/KYC fields
        setIfPresent(request.getIdType(), profile::setIdType);
        setIfPresent(request.getIdNumber(), profile::setIdNumber);
        setIfPresent(request.getKycStatus(), profile::setKycStatus);

        // Customer app profile specific fields
        setIfPresent(request.getPinLimitAmount(), profile::setPinLimitAmount);
        setIfPresent(request.getProfileIdentificationExpired(), profile::setProfileIdentificationExpired);

        // customer_app_id is set (required field)
        if (profile.getCustomerAppId() == null) {
            setIfPresent(request.getCustomerNo(), profile::setCustomerAppId);
        }
    }

    private <T> void setIfPresent(T value, Consumer<T> setter) {
        Optional.ofNullable(value).ifPresent(setter);
    }

    private CustomerAppProfileResponse mapToResponse(CustomerAppProfile profile) {
        String customerName = resolveDisplayCustomerNameForResponse(profile);
        String customerNameKh = profile.getCustomerNameKh();
        if (customerNameKh == null || customerNameKh.isBlank()) {
            customerNameKh = customerName;
        }

        return CustomerAppProfileResponse.builder()
                .customerNo(NullableDisplayUtils.stringOrNa(profile.getCustomerNo()))
                .phoneNo(NullableDisplayUtils.stringOrNa(profile.getPhoneNo()))
                .kycStatus(NullableDisplayUtils.stringOrNa(profile.getKycStatus()))
                .customerName(NullableDisplayUtils.stringOrNa(customerName))
                .customerNameKh(NullableDisplayUtils.stringOrNa(customerNameKh))
                .gender(NullableDisplayUtils.stringOrNa(profile.getGender()))
                .maritalStatus(NullableDisplayUtils.stringOrNa(profile.getMaritalStatus()))
                .dateOfBirth(profile.getDateOfBirth())
                .placeOfBirth(NullableDisplayUtils.stringOrNa(profile.getPlaceOfBirth()))
                .nationality(NullableDisplayUtils.stringOrNa(profile.getNationality()))
                .email(NullableDisplayUtils.stringOrNa(profile.getEmail()))
                .currentAddress(NullableDisplayUtils.stringOrNa(profile.getCurrentAddress()))
                .idType(NullableDisplayUtils.stringOrNa(profile.getIdType()))
                .idNumber(NullableDisplayUtils.stringOrNa(profile.getIdNumber()))
                .profileIdentificationExpired(profile.getProfileIdentificationExpired())
                .pinLimitAmount(profile.getPinLimitAmount())
                .metaData(NullableDisplayUtils.stringOrNa(profile.getMetaData()))
                .build();
    }

    /**
     * When persisted {@code customer_name} is the account or customer number (migration quirk),
     * replace with CDP full name for API output. Does not write back to DB.
     */
    private String resolveDisplayCustomerNameForResponse(CustomerAppProfile profile) {
        String stored = profile.getCustomerName();
        if (!StringUtils.hasText(stored)) {
            return enrichNameFromCdp(profile.getAccountNo(), null);
        }
        String trimmed = stored.trim();
        String accountNo = profile.getAccountNo();
        String customerNo = profile.getCustomerNo();
        boolean looksLikeId = StringUtils.hasText(accountNo) && trimmed.equals(accountNo.trim())
                || StringUtils.hasText(customerNo) && trimmed.equals(customerNo.trim());
        if (!looksLikeId) {
            return stored;
        }
        String fromCdp = enrichNameFromCdp(accountNo, stored);
        return StringUtils.hasText(fromCdp) ? fromCdp : stored;
    }

    private String enrichNameFromCdp(String accountNo, String fallback) {
        if (!StringUtils.hasText(accountNo)) {
            return fallback;
        }
        try {
            AccountDetail.AccountDetailData cdp = cdpClient.getAccountInfo(accountNo.trim());
            if (cdp != null && StringUtils.hasText(cdp.getName())) {
                return cdp.getName().trim();
            }
        } catch (Exception e) {
            log.debug("CDP display name not used for account {}: {}", accountNo, e.getMessage());
        }
        return fallback;
    }
}

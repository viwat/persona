package com.example.persona.profile.service;

import com.example.persona.enums.StatusType;
import com.example.persona.profile.model.AccountAppProfile;
import com.example.persona.profile.model.CardAppProfile;
import com.example.persona.profile.model.CustomerAppProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Service for handling versioning of profile entities. Creates new versions by
 * copying old records and updating only changed fields.
 */
@Slf4j
@Component
public class ProfileVersioningService {

    /**
     * Creates a new version of AccountAppProfile fromEntity the old version. Copies
     * all fields fromEntity old to new, then updates only the changed fields.
     */
    public void createNewVersion(AccountAppProfile oldVersion, AccountAppProfile newVersion, String versionKey) {
        if (oldVersion == null) {
            // First version
            newVersion.setVersionKey(versionKey);
            newVersion.setVersion(1);
            newVersion.setStatus(StatusType.ACTIVE);
            // Generate account hash if customerNo and accountNo are set
            if (newVersion.getCustomerNo() != null && newVersion.getAccountNo() != null) {
                newVersion.setAccountHash(newVersion.generateAccountHash());
            }
            // customer_key in DB column customer_app_id; use customerNo as legacy fallback
            if (newVersion.getCustomerKey() == null && newVersion.getCustomerNo() != null) {
                newVersion.setCustomerKey(newVersion.getCustomerNo());
            }
            log.debug("Creating first AccountAppProfile version for key: {}", versionKey);
            return;
        }

        // Copy all fields fromEntity old version
        copyAccountAppProfileFields(oldVersion, newVersion);

        // Set versioning fields
        newVersion.setVersionKey(versionKey);
        newVersion.setPreviousVersionId(oldVersion.getId());
        newVersion.setVersion(oldVersion.getVersion() != null ? oldVersion.getVersion() + 1 : 1);
        newVersion.setStatus(StatusType.ACTIVE);
        newVersion.setId(null); // Reset ID for new record

        // Mark old version as deleted
        oldVersion.setStatus(StatusType.DELETED);

        log.debug(
                "Created new AccountAppProfile version {} (previous: {}) for key: {}",
                newVersion.getVersion(),
                oldVersion.getId(),
                versionKey);
    }

    /**
     * Creates a new version of CardAppProfile fromEntity the old version.
     */
    public void createNewVersion(CardAppProfile oldVersion, CardAppProfile newVersion, String versionKey) {
        if (oldVersion == null) {
            newVersion.setVersionKey(versionKey);
            newVersion.setVersion(1);
            newVersion.setStatus(StatusType.ACTIVE);
            // Set customer_app_id if not already set (use customerNo as fallback)
            if (newVersion.getCustomerAppId() == null && newVersion.getCustomerNo() != null) {
                newVersion.setCustomerAppId(newVersion.getCustomerNo());
            }
            log.debug("Creating first CardAppProfile version for key: {}", versionKey);
            return;
        }

        copyCardAppProfileFields(oldVersion, newVersion);

        newVersion.setVersionKey(versionKey);
        newVersion.setPreviousVersionId(oldVersion.getId());
        newVersion.setVersion(oldVersion.getVersion() != null ? oldVersion.getVersion() + 1 : 1);
        newVersion.setStatus(StatusType.ACTIVE);
        newVersion.setId(null);

        oldVersion.setStatus(StatusType.DELETED);

        log.debug(
                "Created new CardAppProfile version {} (previous: {}) for key: {}",
                newVersion.getVersion(),
                oldVersion.getId(),
                versionKey);
    }

    /**
     * Creates a new version of CustomerAppProfile fromEntity the old version.
     */
    public void createNewVersion(CustomerAppProfile oldVersion, CustomerAppProfile newVersion, String versionKey) {
        if (oldVersion == null) {
            newVersion.setVersionKey(versionKey);
            newVersion.setVersion(1);
            newVersion.setStatus(StatusType.ACTIVE);
            // Set customer_app_id if not already set (use customerNo as fallback)
            if (newVersion.getCustomerAppId() == null && newVersion.getCustomerNo() != null) {
                newVersion.setCustomerAppId(newVersion.getCustomerNo());
            }
            log.debug("Creating first CustomerAppProfile version for key: {}", versionKey);
            return;
        }

        copyCustomerAppProfileFields(oldVersion, newVersion);

        newVersion.setVersionKey(versionKey);
        newVersion.setPreviousVersionId(oldVersion.getId());
        newVersion.setVersion(oldVersion.getVersion() != null ? oldVersion.getVersion() + 1 : 1);
        newVersion.setStatus(StatusType.ACTIVE);
        newVersion.setId(null);

        oldVersion.setStatus(StatusType.DELETED);

        log.debug(
                "Created new CustomerAppProfile version {} (previous: {}) for key: {}",
                newVersion.getVersion(),
                oldVersion.getId(),
                versionKey);
    }

    /**
     * Copies all fields fromEntity source to target AccountAppProfile.
     */
    private void copyAccountAppProfileFields(AccountAppProfile source, AccountAppProfile target) {
        target.setCustomerKey(source.getCustomerKey());
        target.setCustomerNo(source.getCustomerNo());
        target.setAccountNo(source.getAccountNo());
        target.setAccountHash(source.getAccountHash());
        target.setAccountType(source.getAccountType());
        target.setAccountStatus(source.getAccountStatus());
        target.setAccountName(source.getAccountName());
        target.setAccountHolderName(source.getAccountHolderName());
        target.setClassOfService(source.getClassOfService());
        target.setAccountCategory(source.getAccountCategory());
        target.setCurrency(source.getCurrency());
        target.setBalance(source.getBalance());
        target.setMetaData(source.getMetaData());
        target.setDisplayOrder(source.getDisplayOrder());
        target.setIsPinned(source.getIsPinned());
        target.setIsHidden(source.getIsHidden());
        target.setStopTransactionNotification(source.getStopTransactionNotification());
        target.setIsDefaultPaymentAccount(source.getIsDefaultPaymentAccount());
        target.setAllowTransaction(source.getAllowTransaction());
        target.setAllowCardUsage(source.getAllowCardUsage());
        target.setAccountHolders(source.getAccountHolders());
        target.setAccountServices(source.getAccountServices());
        target.setAccountBadges(source.getAccountBadges());
    }

    /**
     * Copies all fields fromEntity source to target CardAppProfile.
     */
    private void copyCardAppProfileFields(CardAppProfile source, CardAppProfile target) {
        target.setTrackingNumber(source.getTrackingNumber());
        target.setCustomerAppId(source.getCustomerAppId());
        target.setCustomerNo(source.getCustomerNo());
        target.setAccountNo(source.getAccountNo());
        target.setCardType(source.getCardType());
        target.setCardBrand(source.getCardBrand());
        target.setCardName(source.getCardName());
        target.setCardStatus(source.getCardStatus());
        target.setExpiryDate(source.getExpiryDate());
        target.setCardHolderName(source.getCardHolderName());
        target.setCssNumber(source.getCssNumber());
        target.setCreditLimit(source.getCreditLimit());
        target.setAvailableCredit(source.getAvailableCredit());
        target.setTransactionMaxLimit(source.getTransactionMaxLimit());
        target.setDailyLimit(source.getDailyLimit());
        target.setMonthlyLimit(source.getMonthlyLimit());
        target.setDisplayOrder(source.getDisplayOrder());
        target.setIsPinned(source.getIsPinned());
        target.setIsHidden(source.getIsHidden());
        target.setStopTransactionNotification(source.getStopTransactionNotification());
        target.setIsDefaultPaymentCard(source.getIsDefaultPaymentCard());
        target.setAllowTransaction(source.getAllowTransaction());
        target.setAllowOnlineTransaction(source.getAllowOnlineTransaction());
        target.setAllowContactlessTransaction(source.getAllowContactlessTransaction());
        target.setAllowInternationalTransaction(source.getAllowInternationalTransaction());
        target.setCardIcon(source.getCardIcon());
        target.setCardColor(source.getCardColor());
        target.setMetaData(source.getMetaData());
    }

    /**
     * Copies all fields fromEntity source to target CustomerAppProfile.
     */
    private void copyCustomerAppProfileFields(CustomerAppProfile source, CustomerAppProfile target) {
        target.setCustomerAppId(source.getCustomerAppId());
        target.setCustomerKey(source.getCustomerKey());
        target.setCustomerNo(source.getCustomerNo());
        target.setAccountNo(source.getAccountNo());
        target.setPhoneNo(source.getPhoneNo());
        target.setMasterAccountNo(source.getMasterAccountNo());
        target.setChannelCode(source.getChannelCode());
        target.setVariant(source.getVariant());
        target.setCode(source.getCode());
        target.setDeviceId(source.getDeviceId());
        target.setPinLimitAmount(source.getPinLimitAmount());
        target.setProfileIdentificationExpired(source.getProfileIdentificationExpired());
        target.setShowAccountListByCif(source.getShowAccountListByCif());
        target.setMetaData(source.getMetaData());
        // Personal information fields
        target.setCustomerName(source.getCustomerName());
        target.setCustomerNameKh(source.getCustomerNameKh());
        target.setGender(source.getGender());
        target.setMaritalStatus(source.getMaritalStatus());
        target.setDateOfBirth(source.getDateOfBirth());
        target.setPlaceOfBirth(source.getPlaceOfBirth());
        target.setNationality(source.getNationality());
        target.setEmail(source.getEmail());
        target.setCurrentAddress(source.getCurrentAddress());
        // Registration information fields
        target.setIdType(source.getIdType());
        target.setIdNumber(source.getIdNumber());
        target.setKycStatus(source.getKycStatus());
        target.setKycStatusDate(source.getKycStatusDate());
        target.setKycStatusReason(source.getKycStatusReason());
    }

    /**
     * Generates version key for AccountAppProfile
     */
    public String generateAccountVersionKey(String customerNo, String accountNo) {
        return customerNo + "_" + accountNo;
    }

    /**
     * Generates version key for CardAppProfile
     */
    public String generateCardVersionKey(String customerNo, String accountNo, String cardNo) {
        return customerNo + "_" + accountNo + "_" + cardNo;
    }

    /**
     * Generates version key for CustomerAppProfile
     */
    public String generateCustomerVersionKey(String customerKey, String customerNo) {
        return customerKey != null && !customerKey.isEmpty() ? customerKey : customerNo;
    }
}

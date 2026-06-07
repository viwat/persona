package com.example.persona.profile.service;

import com.example.persona.client.CdpClient;
import com.example.persona.dto.response.AccountDetail;
import com.example.persona.enums.StatusType;
import com.example.persona.exception.BusinessException;
import com.example.persona.profile.dto.request.AccountAppManagementRequest;
import com.example.persona.profile.dto.response.AccountAppProfileResponse;
import com.example.persona.profile.model.AccountAppProfile;
import com.example.persona.profile.repository.AccountAppProfileRepository;
import com.example.persona.utils.NullableDisplayUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class AccountAppProfileService {

    private final AccountAppProfileRepository accountAppProfileRepository;
    private final ProfileVersioningService versioningService;
    private final CdpClient cdpClient;

    public AccountAppProfileService(
            AccountAppProfileRepository accountAppProfileRepository,
            ProfileVersioningService versioningService,
            CdpClient cdpClient) {
        this.accountAppProfileRepository = accountAppProfileRepository;
        this.versioningService = versioningService;
        this.cdpClient = cdpClient;
    }

    @Transactional(readOnly = true)
    public AccountAppProfileResponse getAccountProfileByAccountNo(String customerNo, String accountNo) {
        log.debug("Fetching account app profile for customer: {}, account: {}", customerNo, accountNo);

        return accountAppProfileRepository
                .findByCustomerNoAndAccountNoAndStatus(customerNo, accountNo, StatusType.ACTIVE)
                .map(this::mapToResponse)
                .orElseThrow(() -> new BusinessException(
                        "Account app profile not found for customer: " + customerNo + ", account: " + accountNo));
    }

    @Transactional(readOnly = true)
    public AccountAppProfileResponse getAccountProfileByAccountHash(String accountHash) {
        log.debug("Fetching account app profile by account hash: {}", accountHash);

        AccountAppProfile profile = accountAppProfileRepository
                .findByAccountHashAndStatus(accountHash, StatusType.ACTIVE)
                .orElseThrow(
                        () -> new BusinessException("Account app profile not found for account hash: " + accountHash));
        return mapToResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<AccountAppProfileResponse> getVisibleAccountProfiles(String customerNo) {
        log.debug("Fetching visible account app profiles for customer: {}", customerNo);

        return accountAppProfileRepository
                .findByCustomerNoAndIsHiddenFalseAndStatusOrderByDisplayOrderAsc(customerNo, StatusType.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public AccountAppProfileResponse createAccountAppProfile(AccountAppManagementRequest request) {
        log.debug("Creating account app profile for request: {}", request);

        // Validate required fields
        if (request.getCustomerNo() == null || request.getAccountNo() == null) {
            throw new BusinessException("Customer number and account number are required to create account profile");
        }

        // Generate version key
        String versionKey =
                versioningService.generateAccountVersionKey(request.getCustomerNo(), request.getAccountNo());

        // Check if profile already exists
        accountAppProfileRepository
                .findByVersionKeyAndStatus(versionKey, StatusType.ACTIVE)
                .ifPresent(existing -> {
                    throw new BusinessException(String.format(
                            "Account app profile already exists for customer: %s, account: %s",
                            request.getCustomerNo(), request.getAccountNo()));
                });

        // Create new version (no old version for create)
        AccountAppProfile newVersion = new AccountAppProfile();
        versioningService.createNewVersion(null, newVersion, versionKey);

        // Set required fields
        newVersion.setCustomerNo(request.getCustomerNo());
        newVersion.setAccountNo(request.getAccountNo());

        // Populate account details (from request or CDP)
        populateAccountDetails(newVersion, request);

        // Generate account hash (required field)
        newVersion.setAccountHash(newVersion.generateAccountHash());

        // customer_key persisted in column customer_app_id (required)
        if (newVersion.getCustomerKey() == null) {
            newVersion.setCustomerKey(request.getCustomerKey());
        }

        // Set optional fields from request
        if (request.getAccountName() != null) {
            newVersion.setAccountName(request.getAccountName());
        }

        updateOptionalFields(newVersion, request);

        if (request.getIsDefaultPaymentAccount() != null && request.getIsDefaultPaymentAccount()) {
            handleDefaultPaymentAccount(newVersion, request.getCustomerNo());
        }

        AccountAppProfile savedProfile = accountAppProfileRepository.save(newVersion);
        log.info(
                "Account app profile created successfully for customer: {}, account: {}",
                request.getCustomerNo(),
                request.getAccountNo());

        return mapToResponse(savedProfile);
    }

    @Transactional
    public AccountAppProfileResponse createOrUpdateAccountAppProfile(AccountAppManagementRequest request) {
        log.debug("Create or update account app profile for request: {}", request);

        if (request.getCustomerNo() == null || request.getAccountNo() == null) {
            throw new BusinessException("Customer number and account number are required");
        }

        String versionKey =
                versioningService.generateAccountVersionKey(request.getCustomerNo(), request.getAccountNo());

        return accountAppProfileRepository
                .findByVersionKeyAndStatus(versionKey, StatusType.ACTIVE)
                .map(existing -> updateAccountManagement(request))
                .orElseGet(() -> createAccountProfileForCreateOrUpdate(request, versionKey));
    }

    @Transactional
    public AccountAppProfileResponse createAccountProfileForCreateOrUpdate(
            AccountAppManagementRequest request, String versionKey) {
        AccountAppProfile newVersion = new AccountAppProfile();
        versioningService.createNewVersion(null, newVersion, versionKey);

        newVersion.setCustomerNo(request.getCustomerNo());
        newVersion.setAccountNo(request.getAccountNo());
        newVersion.setAccountName(request.getAccountName());

        populateAccountDetails(newVersion, request);

        newVersion.setAccountHash(newVersion.generateAccountHash());
        if (newVersion.getCustomerKey() == null) {
            newVersion.setCustomerKey(request.getCustomerKey());
        }

        updateOptionalFields(newVersion, request);

        if (request.getIsDefaultPaymentAccount() != null && request.getIsDefaultPaymentAccount()) {
            handleDefaultPaymentAccount(newVersion, request.getCustomerNo());
        }

        AccountAppProfile savedProfile = accountAppProfileRepository.save(newVersion);
        log.info(
                "Account app profile created (createOrUpdate) for customer: {}, account: {}",
                request.getCustomerNo(),
                request.getAccountNo());
        return mapToResponse(savedProfile);
    }

    /**
     * Use details from request if available (e.g. from migration), otherwise fetch from CDP.
     */
    private void populateAccountDetails(AccountAppProfile newVersion, AccountAppManagementRequest request) {
        if (StringUtils.hasText(request.getAccountClass())) {
            populateAccountDetailsFromRequest(newVersion, request);
        } else {
            populateAccountDetailsFromCdp(newVersion, request);
        }
    }

    private static void populateAccountDetailsFromRequest(
            AccountAppProfile newVersion, AccountAppManagementRequest request) {
        newVersion.setAccountClass(request.getAccountClass());
        newVersion.setAccountType(request.getAccountType());
        newVersion.setAccountCategory(request.getAccountCategory());
        newVersion.setAccountStatus(request.getAccountStatus());
        newVersion.setClassOfService(request.getClassOfService());
        if (request.getAccountHolderName() != null) {
            newVersion.setAccountHolderName(request.getAccountHolderName());
        }
        if (request.getCurrency() != null) {
            newVersion.setCurrency(request.getCurrency());
        }
        if (request.getBalance() != null) {
            newVersion.setBalance(request.getBalance());
        }
    }

    private void populateAccountDetailsFromCdp(AccountAppProfile newVersion, AccountAppManagementRequest request) {
        AccountDetail.AccountDetailData accountDetail = cdpClient.getAccountInfo(request.getAccountNo());
        if (accountDetail == null) {
            throw new BusinessException(String.format("Account not found: %s", request.getAccountNo()));
        }
        newVersion.setAccountClass(accountDetail.getAccountClass());
        newVersion.setAccountType(accountDetail.getAccountType());
        newVersion.setAccountCategory(resolveAccountCategoryFromCdp(accountDetail));
        newVersion.setAccountStatus(resolveAccountStatusFromCdp(accountDetail));
        newVersion.setClassOfService(accountDetail.getCategoryProfileName());
        newVersion.setAccountHolderName(accountDetail.getName());
        newVersion.setCurrency(accountDetail.getCcy());
        if (StringUtils.hasText(accountDetail.getDomainId())) {
            newVersion.setAccountSegment(accountDetail.getDomainId());
        }
        newVersion.setBalance(pickAccountBalance(accountDetail));

        if (request.getAccountName() == null) {
            newVersion.setAccountName(accountDetail.getName());
        }
    }

    private static String resolveAccountCategoryFromCdp(AccountDetail.AccountDetailData accountDetail) {
        if (StringUtils.hasText(accountDetail.getCustomerCategory())) {
            return accountDetail.getCustomerCategory();
        }
        return accountDetail.getCategoryId();
    }

    private void updateOptionalFields(AccountAppProfile newVersion, AccountAppManagementRequest request) {
        if (request.getDisplayOrder() != null) {
            newVersion.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsPinned() != null) {
            newVersion.setIsPinned(request.getIsPinned());
        }
        if (request.getIsHidden() != null) {
            newVersion.setIsHidden(request.getIsHidden());
        } else {
            // Only set default if not updating an existing one (caller handles logic or
            // checks null)
            // But for create/newVersion, we want defaults
            if (newVersion.getId() == null) {
                newVersion.setIsHidden(false);
            }
        }
        if (request.getStopTransactionNotification() != null) {
            newVersion.setStopTransactionNotification(request.getStopTransactionNotification());
        }
        if (request.getAllowTransaction() != null) {
            newVersion.setAllowTransaction(request.getAllowTransaction());
        } else {
            if (newVersion.getId() == null) {
                newVersion.setAllowTransaction(true);
            }
        }
        if (request.getAllowCardUsage() != null) {
            newVersion.setAllowCardUsage(request.getAllowCardUsage());
        }
        if (request.getAccountHolders() != null) {
            newVersion.setAccountHolders(request.getAccountHolders());
        }
        if (request.getAccountServices() != null) {
            newVersion.setAccountServices(request.getAccountServices());
        }
        if (request.getAccountBadges() != null) {
            newVersion.setAccountBadges(request.getAccountBadges());
        }
    }

    private void handleDefaultPaymentAccount(AccountAppProfile newVersion, String customerNo) {
        List<AccountAppProfile> allAccounts =
                accountAppProfileRepository.findByCustomerNoAndStatusOrderByDisplayOrderAsc(
                        customerNo, StatusType.ACTIVE);
        batchUnsetDefaultPaymentAccount(allAccounts);
        newVersion.setIsDefaultPaymentAccount(true);
    }

    @Transactional
    public AccountAppProfileResponse updateAccountManagement(AccountAppManagementRequest request) {
        log.debug("Updating account management for request: {}", request);

        // Generate version key
        String versionKey =
                versioningService.generateAccountVersionKey(request.getCustomerNo(), request.getAccountNo());

        // Find active version
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByVersionKeyAndStatus(versionKey, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException("Account app profile not found for customer: "
                        + request.getCustomerNo() + ", account: " + request.getAccountNo()));

        // Create new version (copies all fields from old)
        AccountAppProfile newVersion = new AccountAppProfile();
        versioningService.createNewVersion(oldVersion, newVersion, versionKey);

        // Update only changed fields
        if (request.getAccountName() != null) {
            newVersion.setAccountName(request.getAccountName());
        }
        if (request.getDisplayOrder() != null) {
            newVersion.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getIsPinned() != null) {
            newVersion.setIsPinned(request.getIsPinned());
        }
        if (request.getIsHidden() != null) {
            newVersion.setIsHidden(request.getIsHidden());
        }
        if (request.getStopTransactionNotification() != null) {
            newVersion.setStopTransactionNotification(request.getStopTransactionNotification());
        }
        if (request.getAllowTransaction() != null) {
            newVersion.setAllowTransaction(request.getAllowTransaction());
        }
        if (request.getAllowCardUsage() != null) {
            newVersion.setAllowCardUsage(request.getAllowCardUsage());
        }
        if (request.getAccountHolders() != null) {
            newVersion.setAccountHolders(request.getAccountHolders());
        }
        if (request.getAccountServices() != null) {
            newVersion.setAccountServices(request.getAccountServices());
        }
        if (request.getAccountBadges() != null) {
            newVersion.setAccountBadges(request.getAccountBadges());
        }
        if (request.getIsDefaultPaymentAccount() != null) {
            if (request.getIsDefaultPaymentAccount()) {
                List<AccountAppProfile> allAccounts =
                        accountAppProfileRepository.findByCustomerNoAndStatusOrderByDisplayOrderAsc(
                                request.getCustomerNo(), StatusType.ACTIVE);
                List<AccountAppProfile> accountsToUnset = allAccounts.stream()
                        .filter(acc -> !acc.getAccountNo().equals(request.getAccountNo()))
                        .toList();
                batchUnsetDefaultPaymentAccount(accountsToUnset);
            }
            newVersion.setIsDefaultPaymentAccount(request.getIsDefaultPaymentAccount());
        }

        // Save old version (DELETED) and new version (ACTIVE)
        accountAppProfileRepository.save(oldVersion);
        AccountAppProfile savedProfile = accountAppProfileRepository.save(newVersion);
        log.info(
                "Account app profile version {} saved successfully for customer: {}, account: {}",
                savedProfile.getVersion(),
                request.getCustomerNo(),
                request.getAccountNo());

        return mapToResponse(savedProfile);
    }

    @Transactional
    public AccountAppProfileResponse renameAccount(String customerNo, String accountNo, String newAccountName) {
        log.debug(
                "Renaming account for customer: {}, account: {}, new name: {}", customerNo, accountNo, newAccountName);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByVersionKeyAndStatus(
                        versioningService.generateAccountVersionKey(customerNo, accountNo), StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "Account app profile not found for customer: " + customerNo + ", account: " + accountNo));
        return applyVersionedUpdate(oldVersion, v -> v.setAccountName(newAccountName));
    }

    @Transactional
    public AccountAppProfileResponse renameAccountByHash(String accountHash, String newAccountName) {
        log.debug("Renaming account by hash: {}, new name: {}", accountHash, newAccountName);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByAccountHashAndStatus(accountHash, StatusType.ACTIVE)
                .orElseThrow(
                        () -> new BusinessException("Account app profile not found for account hash: " + accountHash));
        return applyVersionedUpdate(oldVersion, v -> v.setAccountName(newAccountName));
    }

    @Transactional
    public AccountAppProfileResponse updateTransactionMaxLimit(
            String customerNo, String accountNo, BigDecimal transactionMaxLimit) {
        log.debug("Updating transaction max limit for customer: {}, account: {}", customerNo, accountNo);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByVersionKeyAndStatus(
                        versioningService.generateAccountVersionKey(customerNo, accountNo), StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "Account app profile not found for customer: " + customerNo + ", account: " + accountNo));
        // AccountAppProfile has no transactionMaxLimit field; versioned update
        // preserves audit trail
        return applyVersionedUpdate(oldVersion, v -> {});
    }

    @Transactional
    public AccountAppProfileResponse updateTransactionMaxLimitByHash(
            String accountHash, BigDecimal transactionMaxLimit) {
        log.debug("Updating transaction max limit by hash: {}", accountHash);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByAccountHashAndStatus(accountHash, StatusType.ACTIVE)
                .orElseThrow(
                        () -> new BusinessException("Account app profile not found for account hash: " + accountHash));
        return applyVersionedUpdate(oldVersion, v -> {});
    }

    @Transactional
    public AccountAppProfileResponse changeAccountOrder(String customerNo, String accountNo, Integer newOrder) {
        log.debug(
                "Changing account order for customer: {}, account: {}, new order: {}", customerNo, accountNo, newOrder);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByVersionKeyAndStatus(
                        versioningService.generateAccountVersionKey(customerNo, accountNo), StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "Account app profile not found for customer: " + customerNo + ", account: " + accountNo));
        return applyVersionedUpdate(oldVersion, v -> v.setDisplayOrder(newOrder));
    }

    @Transactional
    public AccountAppProfileResponse changeAccountOrderByHash(String accountHash, Integer newOrder) {
        log.debug("Changing account order by hash: {}, new order: {}", accountHash, newOrder);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByAccountHashAndStatus(accountHash, StatusType.ACTIVE)
                .orElseThrow(
                        () -> new BusinessException("Account app profile not found for account hash: " + accountHash));
        return applyVersionedUpdate(oldVersion, v -> v.setDisplayOrder(newOrder));
    }

    @Transactional
    public AccountAppProfileResponse pinAccountToTop(String customerNo, String accountNo) {
        log.debug("Pinning account to top for customer: {}, account: {}", customerNo, accountNo);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByVersionKeyAndStatus(
                        versioningService.generateAccountVersionKey(customerNo, accountNo), StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "Account app profile not found for customer: " + customerNo + ", account: " + accountNo));
        return applyVersionedUpdate(oldVersion, v -> {
            v.setIsPinned(true);
            v.setDisplayOrder(0);
        });
    }

    @Transactional
    public AccountAppProfileResponse pinAccountToTopByHash(String accountHash) {
        log.debug("Pinning account to top by hash: {}", accountHash);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByAccountHashAndStatus(accountHash, StatusType.ACTIVE)
                .orElseThrow(
                        () -> new BusinessException("Account app profile not found for account hash: " + accountHash));
        return applyVersionedUpdate(oldVersion, v -> {
            v.setIsPinned(true);
            v.setDisplayOrder(0);
        });
    }

    @Transactional
    public AccountAppProfileResponse hideAccount(String customerNo, String accountNo) {
        log.debug("Hiding account for customer: {}, account: {}", customerNo, accountNo);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByVersionKeyAndStatus(
                        versioningService.generateAccountVersionKey(customerNo, accountNo), StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "Account app profile not found for customer: " + customerNo + ", account: " + accountNo));
        return applyVersionedUpdate(oldVersion, v -> v.setIsHidden(true));
    }

    @Transactional
    public AccountAppProfileResponse hideAccountByHash(String accountHash) {
        log.debug("Hiding account by hash: {}", accountHash);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByAccountHashAndStatus(accountHash, StatusType.ACTIVE)
                .orElseThrow(
                        () -> new BusinessException("Account app profile not found for account hash: " + accountHash));
        return applyVersionedUpdate(oldVersion, v -> v.setIsHidden(true));
    }

    @Transactional
    public AccountAppProfileResponse showAccount(String customerNo, String accountNo) {
        log.debug("Showing account for customer: {}, account: {}", customerNo, accountNo);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByVersionKeyAndStatus(
                        versioningService.generateAccountVersionKey(customerNo, accountNo), StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "Account app profile not found for customer: " + customerNo + ", account: " + accountNo));
        return applyVersionedUpdate(oldVersion, v -> v.setIsHidden(false));
    }

    @Transactional
    public AccountAppProfileResponse showAccountByHash(String accountHash) {
        log.debug("Showing account by hash: {}", accountHash);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByAccountHashAndStatus(accountHash, StatusType.ACTIVE)
                .orElseThrow(
                        () -> new BusinessException("Account app profile not found for account hash: " + accountHash));
        return applyVersionedUpdate(oldVersion, v -> v.setIsHidden(false));
    }

    @Transactional
    public AccountAppProfileResponse stopTransactionNotification(String customerNo, String accountNo) {
        log.debug("Stopping transaction notification for customer: {}, account: {}", customerNo, accountNo);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByVersionKeyAndStatus(
                        versioningService.generateAccountVersionKey(customerNo, accountNo), StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "Account app profile not found for customer: " + customerNo + ", account: " + accountNo));
        return applyVersionedUpdate(oldVersion, v -> v.setStopTransactionNotification(true));
    }

    @Transactional
    public AccountAppProfileResponse stopTransactionNotificationByHash(String accountHash) {
        log.debug("Stopping transaction notification by hash: {}", accountHash);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByAccountHashAndStatus(accountHash, StatusType.ACTIVE)
                .orElseThrow(
                        () -> new BusinessException("Account app profile not found for account hash: " + accountHash));
        return applyVersionedUpdate(oldVersion, v -> v.setStopTransactionNotification(true));
    }

    @Transactional
    public AccountAppProfileResponse enableTransactionNotification(String customerNo, String accountNo) {
        log.debug("Enabling transaction notification for customer: {}, account: {}", customerNo, accountNo);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByVersionKeyAndStatus(
                        versioningService.generateAccountVersionKey(customerNo, accountNo), StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "Account app profile not found for customer: " + customerNo + ", account: " + accountNo));
        return applyVersionedUpdate(oldVersion, v -> v.setStopTransactionNotification(false));
    }

    @Transactional
    public AccountAppProfileResponse enableTransactionNotificationByHash(String accountHash) {
        log.debug("Enabling transaction notification by hash: {}", accountHash);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByAccountHashAndStatus(accountHash, StatusType.ACTIVE)
                .orElseThrow(
                        () -> new BusinessException("Account app profile not found for account hash: " + accountHash));
        return applyVersionedUpdate(oldVersion, v -> v.setStopTransactionNotification(false));
    }

    @Transactional
    public AccountAppProfileResponse setAsDefaultPaymentAccount(String customerNo, String accountNo) {
        log.debug("Setting account as default payment account for customer: {}, account: {}", customerNo, accountNo);

        String versionKey = versioningService.generateAccountVersionKey(customerNo, accountNo);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByVersionKeyAndStatus(versionKey, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "Account app profile not found for customer: " + customerNo + ", account: " + accountNo));

        List<AccountAppProfile> allAccounts =
                accountAppProfileRepository.findByCustomerNoAndStatusOrderByDisplayOrderAsc(
                        customerNo, StatusType.ACTIVE);
        List<AccountAppProfile> accountsToUnset = allAccounts.stream()
                .filter(acc ->
                        !acc.getAccountNo().equals(accountNo) && Boolean.TRUE.equals(acc.getIsDefaultPaymentAccount()))
                .toList();
        batchUnsetDefaultPaymentAccount(accountsToUnset);

        AccountAppProfile newVersion = new AccountAppProfile();
        versioningService.createNewVersion(oldVersion, newVersion, versionKey);
        newVersion.setIsDefaultPaymentAccount(true);

        accountAppProfileRepository.save(oldVersion);
        AccountAppProfile savedProfile = accountAppProfileRepository.save(newVersion);

        return mapToResponse(savedProfile);
    }

    @Transactional
    public AccountAppProfileResponse setAsDefaultPaymentAccountByHash(String accountHash) {
        log.debug("Setting account as default payment account by hash: {}", accountHash);

        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByAccountHashAndStatus(accountHash, StatusType.ACTIVE)
                .orElseThrow(
                        () -> new BusinessException("Account app profile not found for account hash: " + accountHash));

        String customerNo = oldVersion.getCustomerNo();
        String versionKey = oldVersion.getVersionKey();

        List<AccountAppProfile> allAccounts =
                accountAppProfileRepository.findByCustomerNoAndStatusOrderByDisplayOrderAsc(
                        customerNo, StatusType.ACTIVE);
        List<AccountAppProfile> accountsToUnset = allAccounts.stream()
                .filter(acc -> !acc.getAccountHash().equals(accountHash)
                        && Boolean.TRUE.equals(acc.getIsDefaultPaymentAccount()))
                .toList();
        batchUnsetDefaultPaymentAccount(accountsToUnset);

        AccountAppProfile newVersion = new AccountAppProfile();
        versioningService.createNewVersion(oldVersion, newVersion, versionKey);
        newVersion.setIsDefaultPaymentAccount(true);

        accountAppProfileRepository.save(oldVersion);
        AccountAppProfile savedProfile = accountAppProfileRepository.save(newVersion);

        return mapToResponse(savedProfile);
    }

    @Transactional
    public AccountAppProfileResponse updateAllowTransaction(
            String customerNo, String accountNo, Boolean allowTransaction) {
        log.debug(
                "Updating allow transaction for customer: {}, account: {}, allow: {}",
                customerNo,
                accountNo,
                allowTransaction);

        String versionKey = versioningService.generateAccountVersionKey(customerNo, accountNo);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByVersionKeyAndStatus(versionKey, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "Account app profile not found for customer: " + customerNo + ", account: " + accountNo));

        AccountAppProfile newVersion = new AccountAppProfile();
        versioningService.createNewVersion(oldVersion, newVersion, versionKey);
        newVersion.setAllowTransaction(allowTransaction);

        accountAppProfileRepository.save(oldVersion);
        AccountAppProfile savedProfile = accountAppProfileRepository.save(newVersion);

        return mapToResponse(savedProfile);
    }

    @Transactional
    public AccountAppProfileResponse updateAllowCardUsage(String customerNo, String accountNo, Boolean allowCardUsage) {
        log.debug(
                "Updating allow card usage for customer: {}, account: {}, allow: {}",
                customerNo,
                accountNo,
                allowCardUsage);

        String versionKey = versioningService.generateAccountVersionKey(customerNo, accountNo);
        AccountAppProfile oldVersion = accountAppProfileRepository
                .findByVersionKeyAndStatus(versionKey, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException(
                        "Account app profile not found for customer: " + customerNo + ", account: " + accountNo));

        AccountAppProfile newVersion = new AccountAppProfile();
        versioningService.createNewVersion(oldVersion, newVersion, versionKey);
        newVersion.setAllowCardUsage(allowCardUsage);

        accountAppProfileRepository.save(oldVersion);
        AccountAppProfile savedProfile = accountAppProfileRepository.save(newVersion);

        return mapToResponse(savedProfile);
    }

    /**
     * Applies a versioned update: creates new version, applies modifier to new
     * version, saves both. Reduces duplication between by-versionKey and by-hash
     * methods.
     */
    private AccountAppProfileResponse applyVersionedUpdate(
            AccountAppProfile oldVersion, Consumer<AccountAppProfile> applyToNewVersion) {
        String versionKey = oldVersion.getVersionKey();
        AccountAppProfile newVersion = new AccountAppProfile();
        versioningService.createNewVersion(oldVersion, newVersion, versionKey);
        applyToNewVersion.accept(newVersion);
        accountAppProfileRepository.save(oldVersion);
        AccountAppProfile savedProfile = accountAppProfileRepository.save(newVersion);
        return mapToResponse(savedProfile);
    }

    private void batchUnsetDefaultPaymentAccount(List<AccountAppProfile> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return;
        }
        List<AccountAppProfile> toSave = new java.util.ArrayList<>(accounts.size() * 2);
        for (AccountAppProfile acc : accounts) {
            AccountAppProfile unsetNewVersion = new AccountAppProfile();
            versioningService.createNewVersion(acc, unsetNewVersion, acc.getVersionKey());
            unsetNewVersion.setIsDefaultPaymentAccount(false);
            toSave.add(acc);
            toSave.add(unsetNewVersion);
        }
        accountAppProfileRepository.saveAll(toSave);
    }

    private static String resolveAccountStatusFromCdp(AccountDetail.AccountDetailData accountDetail) {
        if (StringUtils.hasText(accountDetail.getResidentStatus())) {
            return accountDetail.getResidentStatus().trim();
        }
        if (accountDetail.isBlocked()) {
            return "BLOCKED";
        }
        return "ACTIVE";
    }

    private static BigDecimal pickAccountBalance(AccountDetail.AccountDetailData accountDetail) {
        if (accountDetail.getCurrentBalance() != null) {
            return accountDetail.getCurrentBalance();
        }
        if (accountDetail.getAvailableBalance() != null) {
            return accountDetail.getAvailableBalance();
        }
        if (accountDetail.getActualBalance() != null) {
            return accountDetail.getActualBalance();
        }
        return BigDecimal.ZERO;
    }

    private AccountAppProfileResponse mapToResponse(AccountAppProfile profile) {
        String accountName = profile.getAccountName();
        if (accountName == null || accountName.isBlank()) {
            accountName = profile.getAccountHolderName();
        }

        return AccountAppProfileResponse.builder()
                .accountNo(NullableDisplayUtils.stringOrNa(profile.getAccountNo()))
                .accountHash(NullableDisplayUtils.stringOrNa(profile.getAccountHash()))
                .accountName(NullableDisplayUtils.stringOrNa(accountName))
                .accountHolderName(NullableDisplayUtils.stringOrNa(profile.getAccountHolderName()))
                .accountCategory(NullableDisplayUtils.stringOrNa(profile.getAccountCategory()))
                .accountType(NullableDisplayUtils.stringOrNa(profile.getAccountType()))
                .accountStatus(NullableDisplayUtils.stringOrNa(profile.getAccountStatus()))
                .classOfService(NullableDisplayUtils.stringOrNa(profile.getClassOfService()))
                .currency(NullableDisplayUtils.stringOrNa(profile.getCurrency()))
                .balance(profile.getBalance())
                .displayOrder(profile.getDisplayOrder())
                .isPinned(profile.getIsPinned())
                .isHidden(profile.getIsHidden())
                .stopTransactionNotification(profile.getStopTransactionNotification())
                .isDefaultPaymentAccount(profile.getIsDefaultPaymentAccount())
                .allowTransaction(profile.getAllowTransaction())
                .allowCardUsage(profile.getAllowCardUsage())
                .accountHolders(profile.getAccountHolders())
                .accountServices(profile.getAccountServices())
                .accountBadges(profile.getAccountBadges())
                .build();
    }
}

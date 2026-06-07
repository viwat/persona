package com.example.persona.controller;

import com.example.persona.cipher.annotation.ZeroTrust;
import com.example.persona.config.annotation.ApiLifecycle;
import com.example.persona.dto.ApiResponse;
import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.profile.dto.request.AccountAppManagementRequest;
import com.example.persona.profile.dto.request.CardAppManagementRequest;
import com.example.persona.profile.dto.request.CardBaseRequest;
import com.example.persona.profile.dto.request.ChangeAccountOrderRequest;
import com.example.persona.profile.dto.request.ChangeCardOrderRequest;
import com.example.persona.profile.dto.request.CustomerAppProfileRequest;
import com.example.persona.profile.dto.request.ProfileRequest;
import com.example.persona.profile.dto.request.RenameAccountRequest;
import com.example.persona.profile.dto.request.RenameCardRequest;
import com.example.persona.profile.dto.request.UpdateCardTransactionLimitRequest;
import com.example.persona.profile.dto.request.UpdatePinLimitRequest;
import com.example.persona.profile.dto.request.UpdateTransactionLimitRequest;
import com.example.persona.profile.dto.response.AccountAppProfileResponse;
import com.example.persona.profile.dto.response.CardAppProfileResponse;
import com.example.persona.profile.dto.response.CustomerAppProfileResponse;
import com.example.persona.profile.dto.response.ProfileResponse;
import com.example.persona.profile.service.AccountAppProfileService;
import com.example.persona.profile.service.CardAppProfileService;
import com.example.persona.profile.service.CustomerAppProfileService;
import com.example.persona.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kh.com.wingbank.cipher.token.annotation.TrackUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@TrackUserContext
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "APIs for managing customer app profile and account profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final CustomerAppProfileService customerAppProfileService;
    private final AccountAppProfileService accountAppProfileService;
    private final CardAppProfileService cardAppProfileService;

    @ZeroTrust
    @GetMapping("/me")
    @ApiLifecycle(since = "1.0.0", deprecated = "2.0.0", removed = "2.0.0")
    @Operation(summary = "Get user profile", description = "Gets the profile information for the authenticated user")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        return ResponseEntity.ok(ApiResponse.success(profileService.getProfile()));
    }

    @ZeroTrust
    @PutMapping("/me/update")
    @Operation(
            summary = "Update user profile",
            description = "Updates the profile information for the authenticated user")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @Valid @RequestBody ProfileRequest profileRequest) {
        return ResponseEntity.ok(ApiResponse.success(profileService.updateProfile(profileRequest)));
    }

    // Customer App Profile Endpoints
    @ZeroTrust
    @PostMapping("/customer-app")
    @Operation(
            summary = "Get customer app profile",
            description =
                    "Gets the customer app profile (PIN limit, identification expiry). Lookup: optional customer_key; "
                            + "else customer_no + account_no; else latest active row by customer_no or account_no (created_date desc).")
    public ResponseEntity<ApiResponse<CustomerAppProfileResponse>> getCustomerAppProfile(
            @Valid @RequestBody CustomerBaseRequest request) {
        CustomerAppProfileResponse profile = customerAppProfileService.getCustomerAppProfile(request);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PostMapping("/customer-app/create")
    @Operation(
            summary = "Create customer app profile",
            description =
                    "Creates a new customer app profile (PIN limit, identification expiry). Include customer_key, or customer_no + account_no, or customer_no / account_no for identity (same rules as GET /customer-app).")
    public ResponseEntity<ApiResponse<CustomerAppProfileResponse>> createCustomerAppProfile(
            @Valid @RequestBody CustomerAppProfileRequest request) {
        CustomerAppProfileResponse profile = customerAppProfileService.createCustomerAppProfile(request);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/customer-app")
    @Operation(
            summary = "Update customer app profile",
            description =
                    "Updates an existing customer app profile. Resolve the row the same way as GET /customer-app (customer_key, customer_no + account_no, or latest by customer_no / account_no).")
    public ResponseEntity<ApiResponse<CustomerAppProfileResponse>> updateCustomerAppProfile(
            @Valid @RequestBody CustomerAppProfileRequest request) {
        CustomerAppProfileResponse profile = customerAppProfileService.updateCustomerAppProfile(request);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/customer-app/pin-limit")
    @Operation(
            summary = "Update PIN limit",
            description =
                    "Updates the PIN limit amount that allows processing without PIN. Body uses the same identifiers as GET /customer-app (customer_key, or customer_no + account_no, etc.).")
    public ResponseEntity<ApiResponse<CustomerAppProfileResponse>> updatePinLimit(
            @Valid @RequestBody UpdatePinLimitRequest request) {
        CustomerAppProfileResponse profile =
                customerAppProfileService.updatePinLimit(request, request.getPinLimitAmount());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PostMapping("/account")
    @Operation(
            summary = "Get visible account profiles",
            description = "Gets visible (non-hidden) account profiles for a customer")
    public ResponseEntity<ApiResponse<List<AccountAppProfileResponse>>> getVisibleAccountProfiles(
            @Valid @RequestBody CustomerBaseRequest request) {
        List<AccountAppProfileResponse> profiles =
                accountAppProfileService.getVisibleAccountProfiles(request.getCustomerNo());
        return ResponseEntity.ok(ApiResponse.success(profiles));
    }

    @ZeroTrust
    @PostMapping("/account/create")
    @Operation(summary = "Create account profile", description = "Creates a new account app profile for a customer")
    public ResponseEntity<ApiResponse<AccountAppProfileResponse>> createAccountProfile(
            @Valid @RequestBody AccountAppManagementRequest request) {
        AccountAppProfileResponse profile = accountAppProfileService.createAccountAppProfile(request);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PostMapping("/account/{accountHash}")
    @Operation(summary = "Get account profile", description = "Gets a specific account profile by account hash")
    public ResponseEntity<ApiResponse<AccountAppProfileResponse>> getAccountProfile(@PathVariable String accountHash) {
        AccountAppProfileResponse profile = accountAppProfileService.getAccountProfileByAccountHash(accountHash);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/account/{accountHash}/rename")
    @Operation(summary = "Rename account", description = "Renames an account")
    public ResponseEntity<ApiResponse<AccountAppProfileResponse>> renameAccount(
            @PathVariable String accountHash, @Valid @RequestBody RenameAccountRequest request) {
        AccountAppProfileResponse profile =
                accountAppProfileService.renameAccountByHash(accountHash, request.getAccountName());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/account/{accountHash}/transaction-limit")
    @Operation(
            summary = "Update transaction max limit",
            description = "Updates the maximum transaction limit for an account")
    public ResponseEntity<ApiResponse<AccountAppProfileResponse>> updateTransactionMaxLimit(
            @PathVariable String accountHash, @Valid @RequestBody UpdateTransactionLimitRequest request) {
        AccountAppProfileResponse profile =
                accountAppProfileService.updateTransactionMaxLimitByHash(accountHash, request.getTransactionMaxLimit());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/account/{accountHash}/order")
    @Operation(summary = "Change account order", description = "Changes the display order of an account")
    public ResponseEntity<ApiResponse<AccountAppProfileResponse>> changeAccountOrder(
            @PathVariable String accountHash, @Valid @RequestBody ChangeAccountOrderRequest request) {
        AccountAppProfileResponse profile =
                accountAppProfileService.changeAccountOrderByHash(accountHash, request.getDisplayOrder());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/account/{accountHash}/pin")
    @Operation(summary = "Pin account to top", description = "Pins an account to the top of the account list")
    public ResponseEntity<ApiResponse<AccountAppProfileResponse>> pinAccountToTop(@PathVariable String accountHash) {
        AccountAppProfileResponse profile = accountAppProfileService.pinAccountToTopByHash(accountHash);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/account/{accountHash}/hide")
    @Operation(summary = "Hide account", description = "Hides an account fromEntity the account list")
    public ResponseEntity<ApiResponse<AccountAppProfileResponse>> hideAccount(@PathVariable String accountHash) {
        AccountAppProfileResponse profile = accountAppProfileService.hideAccountByHash(accountHash);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/account/{accountHash}/show")
    @Operation(summary = "Show account", description = "Shows a previously hidden account")
    public ResponseEntity<ApiResponse<AccountAppProfileResponse>> showAccount(@PathVariable String accountHash) {
        AccountAppProfileResponse profile = accountAppProfileService.showAccountByHash(accountHash);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/account/{accountHash}/stop-notification")
    @Operation(
            summary = "Stop transaction notification",
            description = "Stops transaction notifications for an account")
    public ResponseEntity<ApiResponse<AccountAppProfileResponse>> stopTransactionNotification(
            @PathVariable String accountHash) {
        AccountAppProfileResponse profile = accountAppProfileService.stopTransactionNotificationByHash(accountHash);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/account/{accountHash}/enable-notification")
    @Operation(
            summary = "Enable transaction notification",
            description = "Enables transaction notifications for an account")
    public ResponseEntity<ApiResponse<AccountAppProfileResponse>> enableTransactionNotification(
            @PathVariable String accountHash) {
        AccountAppProfileResponse profile = accountAppProfileService.enableTransactionNotificationByHash(accountHash);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/account/{accountHash}/default-payment")
    @Operation(
            summary = "Set as default payment account",
            description = "Sets an account as the default payment account")
    public ResponseEntity<ApiResponse<AccountAppProfileResponse>> setAsDefaultPaymentAccount(
            @PathVariable String accountHash) {
        AccountAppProfileResponse profile = accountAppProfileService.setAsDefaultPaymentAccountByHash(accountHash);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    // Card App Profile Endpoints
    @ZeroTrust
    @PostMapping("/card")
    @Operation(summary = "Get all card profiles", description = "Gets all card profiles for a customer")
    public ResponseEntity<ApiResponse<List<CardAppProfileResponse>>> getAllCardProfiles(
            @Valid @RequestBody CustomerBaseRequest request) {
        List<CardAppProfileResponse> profiles = cardAppProfileService.getAllCardProfiles(request.getCustomerNo());
        return ResponseEntity.ok(ApiResponse.success(profiles));
    }

    @ZeroTrust
    @PostMapping("/card/visible")
    @Operation(
            summary = "Get visible card profiles",
            description = "Gets visible (non-hidden) card profiles for a customer")
    public ResponseEntity<ApiResponse<List<CardAppProfileResponse>>> getVisibleCardProfiles(
            @Valid @RequestBody CustomerBaseRequest request) {
        List<CardAppProfileResponse> profiles = cardAppProfileService.getVisibleCardProfiles(request.getCustomerNo());
        return ResponseEntity.ok(ApiResponse.success(profiles));
    }

    @ZeroTrust
    @PostMapping("/card/account")
    @Operation(summary = "Get card profiles by account", description = "Gets all card profiles for a specific account")
    public ResponseEntity<ApiResponse<List<CardAppProfileResponse>>> getCardProfilesByAccount(
            @Valid @RequestBody CustomerBaseRequest request) {
        // Note: accountNo should be in the request body
        List<CardAppProfileResponse> profiles =
                cardAppProfileService.getCardProfilesByAccount(request.getCustomerNo(), request.getAccountNo());
        return ResponseEntity.ok(ApiResponse.success(profiles));
    }

    @ZeroTrust
    @PostMapping("/card/create")
    @Operation(summary = "Create card profile", description = "Creates a new card app profile for a customer")
    public ResponseEntity<ApiResponse<CardAppProfileResponse>> createCardProfile(
            @Valid @RequestBody CardAppManagementRequest request) {
        CardAppProfileResponse profile = cardAppProfileService.createCardAppProfile(request);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/card")
    @Operation(
            summary = "Update card management",
            description = "Updates card management settings including limits, display order, and permissions")
    public ResponseEntity<ApiResponse<CardAppProfileResponse>> updateCardManagement(
            @Valid @RequestBody CardAppManagementRequest request) {
        CardAppProfileResponse profile = cardAppProfileService.updateCardManagement(request);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/card/rename")
    @Operation(summary = "Rename card", description = "Renames a card")
    public ResponseEntity<ApiResponse<CardAppProfileResponse>> renameCard(
            @Valid @RequestBody RenameCardRequest request) {
        CardAppProfileResponse profile = cardAppProfileService.renameCard(
                request.getCustomerNo(), request.getAccountNo(), request.getCardNo(), request.getCardName());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/card/transaction-limit")
    @Operation(
            summary = "Update card transaction max limit",
            description = "Updates the maximum transaction limit for a card")
    public ResponseEntity<ApiResponse<CardAppProfileResponse>> updateCardTransactionMaxLimit(
            @Valid @RequestBody UpdateCardTransactionLimitRequest request) {
        CardAppProfileResponse profile = cardAppProfileService.updateTransactionMaxLimit(
                request.getCustomerNo(), request.getAccountNo(), request.getCardNo(), request.getTransactionMaxLimit());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/card/order")
    @Operation(summary = "Change card order", description = "Changes the display order of a card")
    public ResponseEntity<ApiResponse<CardAppProfileResponse>> changeCardOrder(
            @Valid @RequestBody ChangeCardOrderRequest request) {
        // Using updateCardManagement to change display order
        CardAppManagementRequest managementRequest = CardAppManagementRequest.builder()
                .cardNo(request.getCardNo())
                .displayOrder(request.getDisplayOrder())
                .build();
        CardAppProfileResponse profile = cardAppProfileService.updateCardManagement(managementRequest);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/card/hide")
    @Operation(summary = "Hide card", description = "Hides a card fromEntity the card list")
    public ResponseEntity<ApiResponse<CardAppProfileResponse>> hideCard(@Valid @RequestBody CardBaseRequest request) {
        CardAppProfileResponse profile =
                cardAppProfileService.hideCard(request.getCustomerNo(), request.getAccountNo(), request.getCardNo());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/card/show")
    @Operation(summary = "Show card", description = "Shows a previously hidden card")
    public ResponseEntity<ApiResponse<CardAppProfileResponse>> showCard(@Valid @RequestBody CardBaseRequest request) {
        CardAppProfileResponse profile =
                cardAppProfileService.showCard(request.getCustomerNo(), request.getAccountNo(), request.getCardNo());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @ZeroTrust
    @PutMapping("/card/default-payment")
    @Operation(summary = "Set as default payment card", description = "Sets a card as the default payment card")
    public ResponseEntity<ApiResponse<CardAppProfileResponse>> setAsDefaultPaymentCard(
            @Valid @RequestBody CardBaseRequest request) {
        CardAppProfileResponse profile = cardAppProfileService.setAsDefaultPaymentCard(
                request.getCustomerNo(), request.getAccountNo(), request.getCardNo());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}

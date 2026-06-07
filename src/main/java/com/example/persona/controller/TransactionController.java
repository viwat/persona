package com.example.persona.controller;

import com.example.persona.cipher.annotation.ZeroTrust;
import com.example.persona.dto.ApiResponse;
import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.transaction.dto.LimitThresholdResponse;
import com.example.persona.transaction.dto.TxnAuthorizeResponse;
import com.example.persona.transaction.dto.TxnLimitResponse;
import com.example.persona.transaction.dto.request.LimitThresholdRequest;
import com.example.persona.transaction.dto.request.TxnAuthorizationRequest;
import com.example.persona.transaction.dto.request.TxnDetailRequest;
import com.example.persona.transaction.dto.request.TxnLimitRequest;
import com.example.persona.transaction.service.LimitThresholdService;
import com.example.persona.transaction.service.TransactionLimitService;
import com.example.persona.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import kh.com.wingbank.cipher.token.annotation.TrackUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@TrackUserContext
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction API", description = "APIs for managing transactions, transaction details, and authorizations")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionLimitService transactionLimitService;
    private final LimitThresholdService limitThresholdService;

    @ZeroTrust
    @PostMapping("/detail")
    @Operation(
            summary = "Save transaction detail",
            description = "Saves a transaction detail asynchronously and updates daily aggregates")
    public ResponseEntity<ApiResponse<Void>> saveTransactionDetail(@Valid @RequestBody TxnDetailRequest request) {
        CompletableFuture<Void> future = transactionService.saveTransactionDetail(request);
        future.join();
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @ZeroTrust
    @PostMapping("/authorize")
    @Operation(summary = "Authorize transaction", description = "Gets active transaction authorizations for a customer")
    public ResponseEntity<ApiResponse<List<TxnAuthorizeResponse>>> authorizeTransaction(
            @Valid @RequestBody CustomerBaseRequest request) {
        List<TxnAuthorizeResponse> authorizations = transactionService.authorizeTransaction(request);
        return ResponseEntity.ok(ApiResponse.success(authorizations));
    }

    @ZeroTrust
    @PostMapping("/authorization")
    @Operation(
            summary = "Create transaction authorization",
            description = "Creates a new transaction authorization with min/max amounts and authentication method")
    public ResponseEntity<ApiResponse<TxnAuthorizeResponse>> createTransactionAuthorization(
            @Valid @RequestBody TxnAuthorizationRequest request) {
        TxnAuthorizeResponse authorization = transactionService.createTransactionAuthorization(request);
        return ResponseEntity.ok(ApiResponse.success(authorization));
    }

    @ZeroTrust
    @PutMapping("/authorization")
    @Operation(
            summary = "Update transaction authorization",
            description =
                    "Updates an existing transaction authorization with new min/max amounts and authentication method")
    public ResponseEntity<ApiResponse<TxnAuthorizeResponse>> updateTransactionAuthorization(
            @Valid @RequestBody TxnAuthorizationRequest request) {
        TxnAuthorizeResponse authorization = transactionService.updateTransactionAuthorization(request);
        return ResponseEntity.ok(ApiResponse.success(authorization));
    }

    @ZeroTrust
    @DeleteMapping("/authorization")
    @Operation(
            summary = "Delete transaction authorization",
            description = "Deactivates a transaction authorization by setting its status to INACTIVE")
    public ResponseEntity<ApiResponse<Void>> deleteTransactionAuthorization(
            @Valid @RequestBody TxnAuthorizationRequest request) {
        transactionService.deleteTransactionAuthorization(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @ZeroTrust
    @PostMapping("/limit")
    @Operation(
            summary = "Create transaction limit",
            description = "Creates a new transaction limit configurable by account and service type")
    public ResponseEntity<ApiResponse<TxnLimitResponse>> createTransactionLimit(
            @Valid @RequestBody TxnLimitRequest request) {
        TxnLimitResponse limit = transactionLimitService.createTransactionLimit(request);
        return ResponseEntity.ok(ApiResponse.success(limit));
    }

    @ZeroTrust
    @PutMapping("/limit")
    @Operation(
            summary = "Update transaction limit",
            description =
                    "Updates an existing transaction limit. Can find by customerKey+serviceType, accountNo+serviceType, or limitSn")
    public ResponseEntity<ApiResponse<TxnLimitResponse>> updateTransactionLimit(
            @Valid @RequestBody TxnLimitRequest request) {
        TxnLimitResponse limit = transactionLimitService.updateTransactionLimit(request);
        return ResponseEntity.ok(ApiResponse.success(limit));
    }

    @ZeroTrust
    @DeleteMapping("/limit")
    @Operation(
            summary = "Delete transaction limit",
            description = "Deactivates a transaction limit by setting its status to INACTIVE")
    public ResponseEntity<ApiResponse<Void>> deleteTransactionLimit(@Valid @RequestBody TxnLimitRequest request) {
        transactionLimitService.deleteTransactionLimit(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @ZeroTrust
    @PostMapping("/limit/get")
    @Operation(
            summary = "Get transaction limit",
            description = "Gets an active transaction limit by accountNo and serviceType")
    public ResponseEntity<ApiResponse<TxnLimitResponse>> getTransactionLimit(
            @Valid @RequestBody TxnLimitRequest request) {
        TxnLimitResponse limit = transactionLimitService.getTransactionLimit(request);
        return ResponseEntity.ok(ApiResponse.success(limit));
    }

    @ZeroTrust
    @PostMapping("/limits")
    @Operation(
            summary = "Get all transaction limits",
            description =
                    "Gets all active transaction limits. Can query by accountNo, customerKey (customerKey), masterAccountNo, or customerNo. Priority: accountNo > customerKey > masterAccountNo > customerNo")
    public ResponseEntity<ApiResponse<List<TxnLimitResponse>>> getTransactionLimits(
            @Valid @RequestBody TxnLimitRequest request) {
        List<TxnLimitResponse> limits = transactionLimitService.getTransactionLimits(request);
        return ResponseEntity.ok(ApiResponse.success(limits));
    }

    @ZeroTrust
    @PostMapping("/threshold")
    @Operation(
            summary = "Create limit threshold",
            description =
                    "Creates a new system-wide limit threshold that defines the allowed range for transaction limits")
    public ResponseEntity<ApiResponse<LimitThresholdResponse>> createLimitThreshold(
            @Valid @RequestBody LimitThresholdRequest request) {
        LimitThresholdResponse threshold = limitThresholdService.createLimitThreshold(request);
        return ResponseEntity.ok(ApiResponse.success(threshold));
    }

    @ZeroTrust
    @PutMapping("/threshold")
    @Operation(summary = "Update limit threshold", description = "Updates an existing limit threshold by thresholdSn")
    public ResponseEntity<ApiResponse<LimitThresholdResponse>> updateLimitThreshold(
            @Valid @RequestBody LimitThresholdRequest request) {
        LimitThresholdResponse threshold = limitThresholdService.updateLimitThreshold(request);
        return ResponseEntity.ok(ApiResponse.success(threshold));
    }

    @ZeroTrust
    @DeleteMapping("/threshold")
    @Operation(
            summary = "Delete limit threshold",
            description = "Deactivates a limit threshold by setting its status to INACTIVE")
    public ResponseEntity<ApiResponse<Void>> deleteLimitThreshold(@Valid @RequestBody LimitThresholdRequest request) {
        limitThresholdService.deleteLimitThreshold(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @ZeroTrust
    @PostMapping("/threshold/get")
    @Operation(summary = "Get limit threshold", description = "Gets a limit threshold by thresholdSn")
    public ResponseEntity<ApiResponse<LimitThresholdResponse>> getLimitThreshold(
            @Valid @RequestBody LimitThresholdRequest request) {
        LimitThresholdResponse threshold = limitThresholdService.getLimitThreshold(request);
        return ResponseEntity.ok(ApiResponse.success(threshold));
    }

    @ZeroTrust
    @PostMapping("/thresholds")
    @Operation(
            summary = "Get all limit thresholds",
            description = "Gets all active limit thresholds. Can optionally filter by serviceType in request body")
    public ResponseEntity<ApiResponse<List<LimitThresholdResponse>>> getAllLimitThresholds(
            @RequestBody(required = false) LimitThresholdRequest request) {
        List<LimitThresholdResponse> thresholds;
        if (request != null && StringUtils.hasText(request.getServiceType())) {
            thresholds = limitThresholdService.getLimitThresholdsByServiceType(request.getServiceType());
        } else {
            thresholds = limitThresholdService.getAllLimitThresholds();
        }
        return ResponseEntity.ok(ApiResponse.success(thresholds));
    }
}

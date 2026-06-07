package com.example.persona.transaction.service;

import com.example.persona.config.UserContextHolder;
import com.example.persona.dto.CustomerBaseRequest;
import com.example.persona.dto.TransactionBaseRequest;
import com.example.persona.enums.ErrorCode;
import com.example.persona.enums.StatusType;
import com.example.persona.exception.BusinessException;
import com.example.persona.dto.UserContext;
import com.example.persona.search.dto.request.GeoLocation;
import com.example.persona.search.dto.request.TransactionSearch;
import com.example.persona.search.service.TransactionSearchService;
import com.example.persona.transaction.constants.TransactionConstants;
import com.example.persona.transaction.dto.TxnAuthorizeResponse;
import com.example.persona.transaction.dto.request.TxnAuthorizationRequest;
import com.example.persona.transaction.dto.request.TxnDetailRequest;
import com.example.persona.transaction.model.AuthenticationMethod;
import com.example.persona.transaction.model.TransactionAuthorization;
import com.example.persona.transaction.model.TransactionDetail;
import com.example.persona.transaction.repository.TransactionAuthorizationRepository;
import com.example.persona.transaction.repository.TransactionDetailRepository;
import com.example.persona.utils.TraceIdUtils;
import com.example.persona.utils.UniqueIdGeneratorUtil;
import io.micrometer.tracing.Tracer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionAuthorizationRepository authorizationRepository;
    private final TransactionDetailRepository transactionDetailRepository;
    private final CacheManager cacheManager;
    private final TransactionSearchService transactionSearchService;
    private final Tracer tracer;
    private final ObjectProvider<StringRedisTemplate> redisTemplate;
    private final TransactionTemplate transactionTemplate;

    @Async
    public CompletableFuture<Void> saveTransactionDetail(TxnDetailRequest request) {
        UserContext ctx = resolveUserContext();
        LocalDateTime now = LocalDateTime.now();

        TransactionDetail detail = buildTransactionDetail(request, ctx, now);
        transactionTemplate.executeWithoutResult(status -> transactionDetailRepository.save(detail));

        updateDailyAggregate(detail);

        TransactionSearch searchDoc = mapToTransactionSearch(detail, request, ctx);
        try {
            transactionSearchService.addTransaction(searchDoc);
        } catch (Exception e) {
            log.error(
                    "Transaction saved but search indexing failed for transactionId={}", detail.getTransactionId(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    public List<TxnAuthorizeResponse> authorizeTransaction(CustomerBaseRequest request) {
        return authorizationRepository.findActiveByCustomerKey(request.getCustomerKey()).stream()
                .map(TxnAuthorizeResponse::from)
                .toList();
    }

    protected Optional<TransactionAuthorization> getTransactionAuthorization(TransactionBaseRequest request) {
        Optional<TransactionAuthorization> authorization =
                authorizationRepository.findActiveByCustomerKeyAndServiceType(
                        request.getCustomerKey(), request.getTransactionType());

        if (authorization.isEmpty()) {
            log.debug(
                    "No active authorization found for customer: {} and service type: {}",
                    request.getCustomerKey(),
                    request.getTransactionType());
            return Optional.empty();
        }

        TransactionAuthorization auth = authorization.get();
        BigDecimal amount = request.getAmount();
        if (amount == null) {
            log.debug(
                    "No authorization match: amount is null for customer: {} and service type: {}",
                    request.getCustomerKey(),
                    request.getTransactionType());
            return Optional.empty();
        }

        if (amount.compareTo(auth.getMinAmount()) < 0) {
            log.debug(
                    "Amount is less than minimum amount for customer: {} and service type: {}",
                    request.getCustomerKey(),
                    request.getTransactionType());
            return Optional.empty();
        }

        if (amount.compareTo(auth.getMaxAmount()) > 0) {
            log.debug(
                    "Amount is greater than maximum amount for customer: {} and service type: {}",
                    request.getCustomerKey(),
                    request.getTransactionType());
            return Optional.empty();
        }

        return authorization;
    }

    protected AuthenticationMethod getAuthenticationMethod(TransactionBaseRequest request) {
        return getTransactionAuthorization(request)
                .map(auth -> auth.getAuthenticationThreshold().getAuthenticationMethod())
                .orElse(AuthenticationMethod.PIN);
    }

    public TxnAuthorizeResponse createTransactionAuthorization(TxnAuthorizationRequest request) {
        TransactionAuthorization authorization = TransactionAuthorization.builder()
                .customerKey(request.getCustomerKey())
                .accountNo(request.getAccountNo())
                .serviceType(request.getServiceType())
                .minAmount(request.getMinAmount())
                .maxAmount(request.getMaxAmount())
                .maxAmountType(request.getMaxAmountType())
                .status(StatusType.ACTIVE)
                .build();
        return TxnAuthorizeResponse.from(authorizationRepository.save(authorization));
    }

    public TxnAuthorizeResponse updateTransactionAuthorization(TxnAuthorizationRequest request) {
        TransactionAuthorization authorization = authorizationRepository
                .findById(request.getAuthorizationSn())
                .orElseThrow(() -> new BusinessException(
                        "Transaction authorization not found: " + request.getAuthorizationSn(),
                        ErrorCode.RESOURCE_NOT_FOUND));

        authorization.setMinAmount(request.getMinAmount());
        authorization.setMaxAmount(request.getMaxAmount());
        authorization.setMaxAmountType(request.getMaxAmountType());

        return TxnAuthorizeResponse.from(authorizationRepository.save(authorization));
    }

    public void deleteTransactionAuthorization(TxnAuthorizationRequest request) {
        TransactionAuthorization authorization = authorizationRepository
                .findById(request.getAuthorizationSn())
                .orElseThrow(() -> new BusinessException(
                        "Transaction authorization not found: " + request.getAuthorizationSn(),
                        ErrorCode.RESOURCE_NOT_FOUND));

        authorization.setStatus(StatusType.INACTIVE);
        authorizationRepository.save(authorization);
    }

    private UserContext resolveUserContext() {
        UserContext ctx = UserContextHolder.getCurrentContext();
        if (ctx == null) {
            log.warn("UserContext is null in async method, creating minimal context");
            return UserContext.builder().build();
        }
        return ctx;
    }

    private TransactionDetail buildTransactionDetail(TxnDetailRequest request, UserContext ctx, LocalDateTime now) {
        String transactionId = StringUtils.hasText(request.getTransactionId())
                ? request.getTransactionId()
                : UUID.randomUUID().toString();
        String requestId = resolveId(ctx.getRequestId(), MDC.get(TransactionConstants.KEY_REQUEST_ID));
        String sessionId = resolveId(ctx.getSessionId(), MDC.get(TransactionConstants.KEY_SESSION_ID));
        String masterAccount = resolveMasterAccount(request, ctx);

        return TransactionDetail.builder()
                .accountNo(request.getAccountNo())
                .customerNo(request.getCustomerNo())
                .branchCode(TransactionConstants.DEFAULT_BRANCH)
                .terminalId(TransactionConstants.DEFAULT_TERMINAL)
                .phoneNo(request.getPhoneNo())
                .deviceId(
                        StringUtils.hasText(ctx.getDeviceId())
                                ? ctx.getDeviceId()
                                : TransactionConstants.DEFAULT_DEVICE)
                .masterAccountId(masterAccount)
                .masterAccountNo(masterAccount)
                .traceId(TraceIdUtils.getTraceId(tracer))
                .requestId(requestId)
                .sessionId(sessionId)
                .transactionId(transactionId)
                .subTransactionId(UUID.randomUUID().toString())
                .transactionReference(UniqueIdGeneratorUtil.getUniqueKey().trim())
                .amount(request.getAmount())
                .transactionDate(now)
                .title(request.getTitle())
                .serviceType(request.getTransactionType())
                .type(Optional.ofNullable(request.getType()).orElse(request.getTransactionType()))
                .metadata(request.getMetadata())
                .day(String.valueOf(now.getDayOfMonth()))
                .month(now.format(DateTimeFormatter.ofPattern("MM")))
                .year(String.valueOf(now.getYear()))
                .status(StatusType.ACTIVE)
                .build();
    }

    private String resolveId(String primary, String fallback) {
        if (StringUtils.hasText(primary)) {
            return primary;
        }
        if (StringUtils.hasText(fallback)) {
            return fallback;
        }
        return UUID.randomUUID().toString();
    }

    private String resolveMasterAccount(TxnDetailRequest request, UserContext ctx) {
        if (ctx.getDefaultAccount() != null) {
            return ctx.getDefaultAccount();
        }
        if (request.getMasterAccountNo() != null) {
            return request.getMasterAccountNo();
        }
        return request.getAccountNo();
    }

    private TransactionSearch mapToTransactionSearch(
            TransactionDetail detail, TxnDetailRequest request, UserContext ctx) {
        String customerKey = detail.getCustomerNo() + "_" + detail.getAccountNo();
        long transactionTimestamp = detail.getTransactionDate().toEpochSecond(ZoneOffset.UTC);

        GeoLocation geoLocation = null;
        if (ctx != null && ctx.getLatitude() != null && ctx.getLongitude() != null) {
            try {
                geoLocation = new GeoLocation();
                geoLocation.setLatitude(Double.parseDouble(ctx.getLatitude()));
                geoLocation.setLongitude(Double.parseDouble(ctx.getLongitude()));
            } catch (NumberFormatException e) {
                log.warn(
                        "Invalid latitude/longitude format in UserContext: lat={}, lng={}",
                        ctx.getLatitude(),
                        ctx.getLongitude());
            }
        }

        return TransactionSearch.builder()
                ._id(detail.getTransactionId())
                .customerKey(customerKey)
                .customerNo(detail.getCustomerNo())
                .accountNo(detail.getAccountNo())
                .transactionId(detail.getTransactionId())
                .entryType(detail.getType())
                .serviceType(detail.getServiceType())
                .serviceSubType(request.getSubCategory())
                .serviceName(detail.getServiceType())
                .title(detail.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .location(null)
                .deepLink(null)
                .amount(detail.getAmount())
                .currency(request.getCurrency())
                .amountInUsd(request.getAmountinUSD())
                .transactionTimestamp(transactionTimestamp)
                .day(detail.getDay())
                .month(detail.getMonth())
                .year(detail.getYear())
                .geoLocation(geoLocation)
                .metadata(detail.getMetadata())
                .tags(request.getCategory())
                .build();
    }

    private void updateDailyAggregate(TransactionDetail detail) {
        LocalDate today = detail.getTransactionDate().toLocalDate();
        String cacheKey = String.format(
                TransactionConstants.DAILY_AGG_KEY_FORMAT,
                detail.getCustomerNo(),
                detail.getAccountNo(),
                detail.getServiceType(),
                today.toString());

        BigDecimal delta = detail.getAmount() != null ? detail.getAmount() : BigDecimal.ZERO;

        StringRedisTemplate template = redisTemplate.getIfAvailable();
        if (template != null) {
            updateWithRedis(template, cacheKey, delta);
        } else {
            updateWithSpringCache(cacheKey, delta);
        }
    }

    private static final RedisScript<String> INCREMENT_SCRIPT = RedisScript.of("""
        local current = redis.call('GET', KEYS[1])
        local val = current and (tonumber(current) + tonumber(ARGV[1])) or tonumber(ARGV[1])
        redis.call('SETEX', KEYS[1], ARGV[2], tostring(val))
        return tostring(val)
        """, String.class);

    private void updateWithRedis(StringRedisTemplate template, String key, BigDecimal amount) {
        try {
            template.execute(
                    INCREMENT_SCRIPT,
                    Collections.singletonList(key),
                    amount.toPlainString(),
                    String.valueOf(TransactionConstants.CACHE_EXPIRY_SECONDS));
        } catch (Exception e) {
            log.error("Error updating daily aggregate in Redis, falling back to Spring Cache", e);
            updateWithSpringCache(key, amount);
        }
    }

    private void updateWithSpringCache(String key, BigDecimal amount) {
        Cache cache = cacheManager.getCache(TransactionConstants.CACHE_NAME);
        if (cache == null) {
            log.error("Cache '{}' not found", TransactionConstants.CACHE_NAME);
            return;
        }

        Cache.ValueWrapper wrapper = cache.get(key);
        BigDecimal currentTotal = BigDecimal.ZERO;

        if (wrapper != null) {
            Object value = wrapper.get();
            if (value instanceof BigDecimal bigDecimalValue) {
                currentTotal = bigDecimalValue;
            }
        }

        cache.put(key, currentTotal.add(amount));
    }
}

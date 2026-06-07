package com.example.persona.pinless.service.impl;

import com.example.persona.exception.AppException;
import com.example.persona.pinless.config.PinlessSystemProperties;
import com.example.persona.pinless.model.dto.AuthResolveRequest;
import com.example.persona.pinless.model.dto.AuthResolveResponse;
import com.example.persona.pinless.model.dto.PinlessConfigData;
import com.example.persona.pinless.model.entity.PinlessAuthPolicy;
import com.example.persona.pinless.model.entity.PinlessTxnCounter;
import com.example.persona.pinless.model.enums.BreachReason;
import com.example.persona.pinless.repository.PinlessAuthPolicyRepository;
import com.example.persona.pinless.service.PinlessAuditService;
import com.example.persona.pinless.service.PinlessAuthResolverService;
import com.example.persona.pinless.service.PinlessConfigService;
import com.example.persona.pinless.service.PinlessCounterService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class PinlessAuthResolverServiceImpl implements PinlessAuthResolverService {

    private static final long IDEMPOTENCY_TTL_SECONDS = 60L;
    private static final String DEFAULT_TIER = "DEFAULT";

    private final PinlessConfigService configService;
    private final PinlessCounterService counterService;
    private final PinlessAuthPolicyRepository authPolicyRepository;
    private final PinlessAuditService auditService;
    private final PinlessSystemProperties systemProperties;
    private final ObjectProvider<StringRedisTemplate> redisTemplate;
    private final ObjectMapper objectMapper;

    public PinlessAuthResolverServiceImpl(
            PinlessConfigService configService,
            PinlessCounterService counterService,
            PinlessAuthPolicyRepository authPolicyRepository,
            PinlessAuditService auditService,
            PinlessSystemProperties systemProperties,
            ObjectProvider<StringRedisTemplate> redisTemplate,
            ObjectMapper objectMapper) {
        this.configService = configService;
        this.counterService = counterService;
        this.authPolicyRepository = authPolicyRepository;
        this.auditService = auditService;
        this.systemProperties = systemProperties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public AuthResolveResponse resolve(AuthResolveRequest request) {
        if (StringUtils.hasText(request.getIdempotencyKey())) {
            AuthResolveResponse cached = getCachedIdempotentResponse(request.getIdempotencyKey());
            if (cached != null) {
                log.debug(
                        "[PINLESS] Idempotent hit: key={} customer={}",
                        request.getIdempotencyKey(),
                        request.getCustomerNo());
                return cached;
            }
        }

        AuthResolveResponse response = doResolve(request);

        if (StringUtils.hasText(request.getIdempotencyKey()) && "STEP_UP".equals(response.getAuthLevel())) {
            cacheIdempotentResponse(request.getIdempotencyKey(), response);
        }

        return response;
    }

    // ── Core resolution ───────────────────────────────────────────────────────

    private AuthResolveResponse doResolve(AuthResolveRequest request) {
        String customerNo = request.getCustomerNo();
        String reqCurrency = defaultCurrency(request.getCurrency());

        PinlessConfigData config;
        try {
            config = configService.getByCustomerNo(customerNo);
        } catch (AppException ex) {
            log.debug("[PINLESS] No config: customer={}", customerNo);
            return recordOutcome(request, AuthResolveResponse.blocked(BreachReason.PINLESS_DISABLED));
        }

        if (!config.isEnabled()) {
            log.debug("[PINLESS] Disabled: customer={}", customerNo);
            return recordOutcome(request, AuthResolveResponse.blocked(BreachReason.PINLESS_DISABLED));
        }

        String configCurrency = config.getCurrency() != null ? config.getCurrency() : "USD";

        BigDecimal effectiveAmount;
        try {
            effectiveAmount = convertToConfigCurrency(request.getAmount(), reqCurrency, configCurrency);
        } catch (IllegalArgumentException ex) {
            // No exchange rate configured for this currency pair
            log.warn(
                    "[PINLESS] No exchange rate: customer={} from={} to={}: {}",
                    customerNo,
                    reqCurrency,
                    configCurrency,
                    ex.getMessage());
            return recordOutcome(request, AuthResolveResponse.blocked(BreachReason.CURRENCY_MISMATCH));
        }

        if (!reqCurrency.equals(configCurrency)) {
            log.debug(
                    "[PINLESS] Currency converted: customer={} {} {} → {} {}",
                    customerNo,
                    request.getAmount(),
                    reqCurrency,
                    effectiveAmount.setScale(2, RoundingMode.HALF_UP),
                    configCurrency);
        }

        PinlessAuthPolicy policy = authPolicyRepository.findPolicyForTier(DEFAULT_TIER);

        BreachReason breach = counterService.checkAndIncrement(customerNo, effectiveAmount, config);
        if (breach != null) {
            log.debug("[PINLESS] Counter breach: customer={} reason={}", customerNo, breach);
            return recordOutcome(request, AuthResolveResponse.blocked(breach));
        }

        AuthResolveResponse response = resolveSteps(effectiveAmount, config, policy);

        log.info(
                "[PINLESS] Resolved: customer={} reqCurrency={} amount={} effectiveAmount={} configCurrency={} steps={}",
                customerNo,
                reqCurrency,
                request.getAmount(),
                effectiveAmount.setScale(2, RoundingMode.HALF_UP),
                configCurrency,
                response.getSteps());

        return recordOutcome(request, response);
    }

    private BigDecimal convertToConfigCurrency(BigDecimal amount, String fromCurrency, String toCurrency) {
        return systemProperties.getExchange().convert(amount, fromCurrency, toCurrency);
    }

    private AuthResolveResponse resolveSteps(
            BigDecimal effectiveAmount, PinlessConfigData config, PinlessAuthPolicy policy) {
        boolean aboveThreshold = config.isThresholdEnabled()
                && config.getThresholdAmount() != null
                && effectiveAmount.compareTo(config.getThresholdAmount()) > 0;

        List<String> steps = aboveThreshold ? policy.getAboveThresholdSteps() : policy.getBelowThresholdSteps();

        return AuthResolveResponse.stepUp(steps);
    }

    // ── Audit record ──────────────────────────────────────────────────────────

    private AuthResolveResponse recordOutcome(AuthResolveRequest request, AuthResolveResponse response) {
        PinlessTxnCounter pinlessTxnCounter = PinlessTxnCounter.builder()
                .customerNo(request.getCustomerNo())
                .accountId(request.getAccountId())
                .channel(defaultChannel(request.getChannel()))
                .txnType(request.getTxnType())
                .currency(defaultCurrency(request.getCurrency())) // original currency
                .amount(request.getAmount()) // original amount
                .authLevelResolved(response.getAuthLevel())
                .breachReason(
                        response.getBreachReason() != null
                                ? response.getBreachReason().name()
                                : null)
                .resolvedAt(Instant.now())
                .build();

        auditService.saveRecord(pinlessTxnCounter);
        return response;
    }

    // ── Idempotency ───────────────────────────────────────────────────────────

    private AuthResolveResponse getCachedIdempotentResponse(String key) {
        StringRedisTemplate template = redisTemplate.getIfAvailable();
        if (template == null) return null;
        try {
            String cached = template.opsForValue().get(idemKey(key));
            if (cached == null) return null;
            return objectMapper.readValue(cached, AuthResolveResponse.class);
        } catch (Exception ex) {
            log.warn("[PINLESS] Idempotency read failed key={}: {}", key, ex.getMessage());
            return null;
        }
    }

    private void cacheIdempotentResponse(String key, AuthResolveResponse response) {
        redisTemplate.ifAvailable(template -> {
            try {
                String json = objectMapper.writeValueAsString(response);
                template.opsForValue().set(idemKey(key), json, Duration.ofSeconds(IDEMPOTENCY_TTL_SECONDS));
            } catch (Exception ex) {
                log.warn("[PINLESS] Idempotency write failed key={}: {}", key, ex.getMessage());
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String idemKey(String key) {
        return systemProperties.getCache().resolvedPrefix() + "pinless:idem:" + key;
    }

    private String defaultChannel(String channel) {
        return StringUtils.hasText(channel) ? channel : "MOBAPP";
    }

    private String defaultCurrency(String currency) {
        return StringUtils.hasText(currency) ? currency : "USD";
    }
}

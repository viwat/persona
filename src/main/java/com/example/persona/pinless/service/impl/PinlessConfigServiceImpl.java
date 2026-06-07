package com.example.persona.pinless.service.impl;

import com.example.persona.exception.AppException;
import com.example.persona.pinless.config.PinlessCacheConfig;
import com.example.persona.pinless.config.PinlessSystemProperties;
import com.example.persona.pinless.mapper.PinlessConfigMapper;
import com.example.persona.pinless.model.dto.*;
import com.example.persona.pinless.model.entity.PinlessConfig;
import com.example.persona.pinless.model.entity.PinlessConfigHistory;
import com.example.persona.pinless.model.enums.BreachAction;
import com.example.persona.pinless.model.enums.ConfigAction;
import com.example.persona.pinless.repository.PinlessConfigHistoryRepository;
import com.example.persona.pinless.repository.PinlessConfigRepository;
import com.example.persona.pinless.service.PinlessConfigService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PinlessConfigServiceImpl implements PinlessConfigService {

    private final PinlessConfigRepository configRepository;
    private final PinlessConfigHistoryRepository historyRepository;
    private final PinlessConfigMapper mapper;
    private final PinlessSystemProperties systemProperties;
    private final CacheManager pinlessCacheManager;

    public PinlessConfigServiceImpl(
            PinlessConfigRepository configRepository,
            PinlessConfigHistoryRepository historyRepository,
            PinlessConfigMapper mapper,
            PinlessSystemProperties systemProperties,
            @Qualifier(PinlessCacheConfig.CACHE_MANAGER) CacheManager pinlessCacheManager) {
        this.configRepository = configRepository;
        this.historyRepository = historyRepository;
        this.mapper = mapper;
        this.systemProperties = systemProperties;
        this.pinlessCacheManager = pinlessCacheManager;
    }

    // ── Upsert ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = PinlessCacheConfig.CACHE_NAME,
            cacheManager = PinlessCacheConfig.CACHE_MANAGER,
            key = "#request.customerNo")
    public PinlessConfigResponse upsert(PinlessConfigRequest request) {
        Optional<PinlessConfig> existing = configRepository.findByCustomerNo(request.getCustomerNo());
        PinlessConfigData data = existing.isPresent() ? update(existing.get(), request) : create(request);
        return mapper.toResponse(data);
    }

    // ── Get ──────────────────────────────────────────────────────────────────

    @Override
    @Cacheable(
            cacheNames = PinlessCacheConfig.CACHE_NAME,
            cacheManager = PinlessCacheConfig.CACHE_MANAGER,
            key = "#customerNo")
    @Transactional(readOnly = true)
    public PinlessConfigData getByCustomerNo(String customerNo) {
        PinlessConfig config = configRepository
                .findByCustomerNo(customerNo)
                .orElseThrow(() -> new AppException("No pinless config found for customer: " + customerNo));
        return toData(config);
    }

    // ── Toggle ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PinlessConfigResponse toggle(PinlessToggleRequest request) {
        PinlessConfig config = configRepository
                .findByCustomerNo(request.getCustomerNo())
                .orElseThrow(
                        () -> new AppException("No pinless config found for customer: " + request.getCustomerNo()));

        boolean wasEnabled = config.isEnabled();
        boolean nowEnabled = Boolean.TRUE.equals(request.getEnabled());

        if (wasEnabled == nowEnabled) {
            log.debug("[PINLESS] Toggle no-op: customer={} already enabled={}", request.getCustomerNo(), nowEnabled);
            return mapper.toResponse(toData(config));
        }

        Map<String, Object> oldSnapshot = toSnapshot(config);
        config.setEnabled(nowEnabled);
        config.setUpdatedBy(request.getCustomerNo());

        PinlessConfig saved = configRepository.save(config);
        ConfigAction action = nowEnabled ? ConfigAction.ENABLED : ConfigAction.DISABLED;
        log.info("[PINLESS] {} for customer={}", action, saved.getCustomerNo());

        saveHistory(saved, action, oldSnapshot, request.getChangeReason());
        evictCache(request.getCustomerNo());
        return mapper.toResponse(toData(saved));
    }

    // ── History ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<PinlessConfigHistoryResponse> getHistory(String customerNo, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "changedAt"));
        return historyRepository.findByCustomerNo(customerNo, pageRequest).map(mapper::toHistoryResponse);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private PinlessConfigData create(PinlessConfigRequest req) {
        String currency = defaultIfNull(req.getCurrency(), "USD");
        boolean enabled = req.getEnabled() == null || req.getEnabled();

        boolean thresholdEnabled = resolveThresholdEnabled(req.getThresholdEnabled(), req.getThresholdAmount());

        BigDecimal thresholdAmount = req.getThresholdAmount() != null
                ? req.getThresholdAmount()
                : (thresholdEnabled ? systemProperties.getDefaults().getThresholdAmount(currency) : null);

        PinlessConfig config = PinlessConfig.builder()
                .customerNo(req.getCustomerNo())
                .channel(defaultIfNull(req.getChannel(), "MOBILE"))
                .currency(currency)
                .enabled(enabled)
                .thresholdEnabled(thresholdEnabled)
                .thresholdAmount(thresholdAmount)
                .dailyCountEnabled(Boolean.TRUE.equals(req.getDailyCountEnabled()))
                .dailyCountLimit(req.getDailyCountLimit())
                .monthlyCountEnabled(Boolean.TRUE.equals(req.getMonthlyCountEnabled()))
                .monthlyCountLimit(req.getMonthlyCountLimit())
                .dailyAmountEnabled(Boolean.TRUE.equals(req.getDailyAmountEnabled()))
                .dailyAmountLimit(req.getDailyAmountLimit())
                .monthlyAmountEnabled(Boolean.TRUE.equals(req.getMonthlyAmountEnabled()))
                .monthlyAmountLimit(req.getMonthlyAmountLimit())
                .breachAction(defaultIfNull(req.getBreachAction(), BreachAction.ESCALATE_TO_PIN))
                .effectiveDate(LocalDate.now())
                .createdBy(req.getCustomerNo())
                .build();

        PinlessConfig saved = configRepository.save(config);
        log.info(
                "[PINLESS] Config created: customer={} currency={} enabled={} threshold={}",
                saved.getCustomerNo(),
                currency,
                enabled,
                thresholdAmount);

        saveHistory(saved, ConfigAction.CREATED, null, req.getChangeReason());
        return toData(saved);
    }

    private PinlessConfigData update(PinlessConfig existing, PinlessConfigRequest req) {
        if (req.getCurrency() != null && !req.getCurrency().equals(existing.getCurrency())) {
            throw new AppException("Currency cannot be changed after config is created. " + "Current: "
                    + existing.getCurrency() + ", requested: " + req.getCurrency());
        }

        Map<String, Object> oldSnapshot = toSnapshot(existing);

        boolean autoEnableThreshold = req.getThresholdAmount() != null && req.getThresholdEnabled() == null;

        if (autoEnableThreshold) {
            log.debug("[PINLESS] Auto-enabling threshold for customer={}", req.getCustomerNo());
        }

        mapper.updateEntityFromRequest(req, existing);

        if (autoEnableThreshold) {
            existing.setThresholdEnabled(true);
        }

        existing.setUpdatedBy(req.getCustomerNo());

        PinlessConfig saved = configRepository.save(existing);
        log.info("[PINLESS] Config updated: customer={}", saved.getCustomerNo());
        saveHistory(saved, ConfigAction.UPDATED, oldSnapshot, req.getChangeReason());
        return toData(saved);
    }

    private PinlessConfigData toData(PinlessConfig config) {
        String currency = config.getCurrency() != null ? config.getCurrency() : "USD";
        return mapper.toData(config).toBuilder()
                .systemCap(systemProperties.getSystem().getCap(currency))
                .build();
    }

    private boolean resolveThresholdEnabled(Boolean requestFlag, BigDecimal amount) {
        if (requestFlag != null) return requestFlag;
        return amount != null;
    }

    private void evictCache(String customerNo) {
        Cache cache = pinlessCacheManager.getCache(PinlessCacheConfig.CACHE_NAME);
        if (cache != null) {
            cache.evict(customerNo);
            log.debug("[PINLESS] Cache evicted: customer={}", customerNo);
        }
    }

    private void saveHistory(
            PinlessConfig config, ConfigAction action, Map<String, Object> oldSnapshot, String changeReason) {
        historyRepository.save(PinlessConfigHistory.builder()
                .pinlessConfigId(config.getId())
                .customerNo(config.getCustomerNo())
                .action(action)
                .oldValue(oldSnapshot)
                .newValue(toSnapshot(config))
                .changedBy(config.getCustomerNo())
                .changeReason(changeReason)
                .build());
    }

    private Map<String, Object> toSnapshot(PinlessConfig config) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("customerNo", config.getCustomerNo());
        snapshot.put("channel", config.getChannel());
        snapshot.put("currency", config.getCurrency());
        snapshot.put("enabled", config.isEnabled());
        snapshot.put("thresholdEnabled", config.isThresholdEnabled());
        snapshot.put("thresholdAmount", config.getThresholdAmount());
        snapshot.put("dailyCountEnabled", config.isDailyCountEnabled());
        snapshot.put("dailyCountLimit", config.getDailyCountLimit());
        snapshot.put("monthlyCountEnabled", config.isMonthlyCountEnabled());
        snapshot.put("monthlyCountLimit", config.getMonthlyCountLimit());
        snapshot.put("dailyAmountEnabled", config.isDailyAmountEnabled());
        snapshot.put("dailyAmountLimit", config.getDailyAmountLimit());
        snapshot.put("monthlyAmountEnabled", config.isMonthlyAmountEnabled());
        snapshot.put("monthlyAmountLimit", config.getMonthlyAmountLimit());
        snapshot.put("breachAction", config.getBreachAction());
        snapshot.put("effectiveDate", config.getEffectiveDate());
        snapshot.put("updatedBy", config.getUpdatedBy());
        snapshot.put(
                "updatedAt",
                config.getUpdatedAt() != null ? config.getUpdatedAt().toString() : null);
        return snapshot;
    }

    private <T> T defaultIfNull(T value, T fallback) {
        return value != null ? value : fallback;
    }
}

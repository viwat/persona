package com.example.persona.transaction.service;

import com.example.persona.enums.StatusType;
import com.example.persona.exception.BusinessException;
import com.example.persona.transaction.dto.LimitThresholdResponse;
import com.example.persona.transaction.dto.request.LimitThresholdRequest;
import com.example.persona.transaction.model.LimitThreshold;
import com.example.persona.transaction.repository.LimitThresholdRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class LimitThresholdService {

    private static final String DEFAULT_DATA = "{}";

    private final LimitThresholdRepository limitThresholdRepository;

    @Transactional
    public LimitThresholdResponse createLimitThreshold(LimitThresholdRequest request) {
        validateCreateRequest(request);

        LimitThreshold threshold = buildLimitThreshold(request);
        threshold.setStatus(StatusType.ACTIVE);
        LimitThreshold savedThreshold = limitThresholdRepository.save(threshold);

        return LimitThresholdResponse.from(savedThreshold);
    }

    @Transactional
    public LimitThresholdResponse updateLimitThreshold(LimitThresholdRequest request) {
        if (request.getThresholdSn() == null) {
            throw new BusinessException("thresholdSn is required for update operation");
        }

        LimitThreshold threshold = limitThresholdRepository
                .findById(request.getThresholdSn())
                .orElseThrow(() -> new BusinessException(
                        "Limit threshold not found with thresholdSn: " + request.getThresholdSn()));

        if (!StatusType.ACTIVE.equals(threshold.getStatus())) {
            throw new BusinessException("Cannot update inactive threshold. ThresholdSn: " + request.getThresholdSn());
        }

        updateThresholdFields(threshold, request);

        LimitThreshold updatedThreshold = limitThresholdRepository.save(threshold);
        return LimitThresholdResponse.from(updatedThreshold);
    }

    @Transactional
    public void deleteLimitThreshold(LimitThresholdRequest request) {
        if (request.getThresholdSn() == null) {
            throw new BusinessException("thresholdSn is required for delete operation");
        }

        LimitThreshold threshold = limitThresholdRepository
                .findById(request.getThresholdSn())
                .orElseThrow(() -> new BusinessException(
                        "Limit threshold not found with thresholdSn: " + request.getThresholdSn()));

        threshold.setStatus(StatusType.INACTIVE);
        limitThresholdRepository.save(threshold);
    }

    @Transactional(readOnly = true)
    public LimitThresholdResponse getLimitThreshold(LimitThresholdRequest request) {
        if (request.getThresholdSn() != null) {
            LimitThreshold threshold = limitThresholdRepository
                    .findById(request.getThresholdSn())
                    .orElseThrow(() -> new BusinessException(
                            "Limit threshold not found with thresholdSn: " + request.getThresholdSn()));
            return LimitThresholdResponse.from(threshold);
        }

        throw new BusinessException("thresholdSn is required to get limit threshold");
    }

    @Transactional(readOnly = true)
    public List<LimitThresholdResponse> getAllLimitThresholds() {
        return limitThresholdRepository.findAll().stream()
                .filter(t -> StatusType.ACTIVE.equals(t.getStatus()))
                .map(LimitThresholdResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LimitThresholdResponse> getLimitThresholdsByServiceType(String serviceType) {
        return limitThresholdRepository.findActiveByServiceType(serviceType).stream()
                .map(LimitThresholdResponse::from)
                .toList();
    }

    private void validateCreateRequest(LimitThresholdRequest request) {
        if (request.getMinAmount() == null || request.getMinAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("minAmount is required and must be greater than or equal to zero");
        }
        if (request.getMaxAmount() == null || request.getMaxAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("maxAmount is required and must be greater than zero");
        }
        if (!StringUtils.hasText(request.getMinAmountType())) {
            throw new BusinessException("minAmountType is required");
        }
        if (!StringUtils.hasText(request.getMaxAmountType())) {
            throw new BusinessException("maxAmountType is required");
        }
        if (request.getMinAmount().compareTo(request.getMaxAmount()) > 0) {
            throw new BusinessException("minAmount must be less than or equal to maxAmount. minAmount: "
                    + request.getMinAmount() + ", maxAmount: " + request.getMaxAmount());
        }
    }

    private LimitThreshold buildLimitThreshold(LimitThresholdRequest request) {
        LimitThreshold threshold = new LimitThreshold();
        threshold.setServiceType(request.getServiceType());
        threshold.setMinAmount(request.getMinAmount());
        threshold.setMaxAmount(request.getMaxAmount());
        threshold.setMinAmountType(request.getMinAmountType());
        threshold.setMaxAmountType(request.getMaxAmountType());
        threshold.setDescription(request.getDescription());
        threshold.setData(StringUtils.hasText(request.getData()) ? request.getData() : DEFAULT_DATA);
        threshold.setMetadata(request.getMetadata());
        return threshold;
    }

    private void updateThresholdFields(LimitThreshold threshold, LimitThresholdRequest request) {
        if (request.getServiceType() != null) {
            threshold.setServiceType(request.getServiceType());
        }
        if (request.getMinAmount() != null) {
            if (request.getMinAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("minAmount must be greater than or equal to zero");
            }
            BigDecimal maxAmount = request.getMaxAmount() != null ? request.getMaxAmount() : threshold.getMaxAmount();
            if (request.getMinAmount().compareTo(maxAmount) > 0) {
                throw new BusinessException("minAmount must be less than or equal to maxAmount");
            }
            threshold.setMinAmount(request.getMinAmount());
        }
        if (request.getMaxAmount() != null) {
            if (request.getMaxAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("maxAmount must be greater than zero");
            }
            BigDecimal minAmount = request.getMinAmount() != null ? request.getMinAmount() : threshold.getMinAmount();
            if (minAmount.compareTo(request.getMaxAmount()) > 0) {
                throw new BusinessException("minAmount must be less than or equal to maxAmount");
            }
            threshold.setMaxAmount(request.getMaxAmount());
        }
        if (StringUtils.hasText(request.getMinAmountType())) {
            threshold.setMinAmountType(request.getMinAmountType());
        }
        if (StringUtils.hasText(request.getMaxAmountType())) {
            threshold.setMaxAmountType(request.getMaxAmountType());
        }
        if (request.getDescription() != null) {
            threshold.setDescription(request.getDescription());
        }
        if (request.getData() != null) {
            threshold.setData(request.getData());
        }
        if (request.getMetadata() != null) {
            threshold.setMetadata(request.getMetadata());
        }
    }
}

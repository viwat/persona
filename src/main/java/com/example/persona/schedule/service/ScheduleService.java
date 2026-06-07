package com.example.persona.schedule.service;

import com.example.persona.enums.ErrorCode;
import com.example.persona.enums.StatusType;
import com.example.persona.exception.AppException;
import com.example.persona.exception.BusinessException;
import com.example.persona.schedule.dto.ScheduleCreateRequest;
import com.example.persona.schedule.dto.ScheduleModifyRequest;
import com.example.persona.schedule.dto.UserScheduleCreateRequest;
import com.example.persona.schedule.dto.UserScheduleModifyRequest;
import com.example.persona.schedule.dto.response.UserScheduleHistoryResponse;
import com.example.persona.schedule.dto.response.UserScheduleResponse;
import com.example.persona.schedule.enums.ScheduleStatus;
import com.example.persona.schedule.mapper.ScheduleMapper;
import com.example.persona.schedule.mapper.UserScheduleHistoryMapper;
import com.example.persona.schedule.mapper.UserScheduleMapper;
import com.example.persona.schedule.model.*;
import com.example.persona.schedule.repository.*;
import com.example.persona.utils.SchedulerUtils;
import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final UserScheduleRepository userScheduleRepository;
    private final UserScheduleHistoryRepository userScheduleHistoryRepository;
    private final ScheduleMapper scheduleMapper;
    private final UserScheduleMapper userScheduleMapper;
    private final UserScheduleHistoryMapper userScheduleHistoryMapper;

    @Transactional
    public Schedule createSchedule(ScheduleCreateRequest request) {
        // Optional: enforce uniqueness early (DB constraint still protects)
        scheduleRepository
                .findByServiceTypeAndServiceCode(request.getServiceType(), request.getServiceCode())
                .ifPresent(s -> {
                    throw new BusinessException("Schedule already exists for serviceType and serviceCode");
                });
        Schedule schedule = scheduleMapper.toEntity(request);
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public Schedule modifySchedule(Long scheduleId, ScheduleModifyRequest request) {
        Schedule schedule = scheduleRepository
                .findById(scheduleId)
                .orElseThrow(() -> new AppException("Schedule not found with id: " + scheduleId));
        scheduleMapper.updateEntityFromRequest(request, schedule);
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public UserScheduleResponse createUserSchedule(UserScheduleCreateRequest request) {
        userScheduleRepository
                .findByCustomerKeyAndServiceTypeAndServiceCode(
                        request.getCustomerKey(), request.getServiceType(), request.getServiceCode())
                .ifPresent(us -> {
                    throw new BusinessException(String.format(
                            "UserSchedule already exists for customerKey: %s serviceType: %s and serviceCode %s",
                            request.getCustomerKey(), request.getServiceType(), request.getServiceCode()));
                });

        log.info(
                "Creating schedule for customerKey: {} with serviceType: {}",
                request.getCustomerKey(),
                request.getServiceType());

        // Validate schedule template exists and user can create more schedules
        Schedule schedule = scheduleRepository
                .findByServiceTypeAndServiceCode(request.getServiceType(), request.getServiceCode())
                .orElseThrow(() -> new AppException("Schedule template not found", ErrorCode.SCHEDULE_NOT_FOUND));

        validateScheduleCreation(request.getCustomerKey(), schedule, request);

        // Create user schedule
        UserSchedule userSchedule = new UserSchedule();
        userSchedule.fromCustomerRequest(request);
        userSchedule.setSchedule(schedule);
        userSchedule.setScheduleType(schedule.getScheduleType());

        userScheduleMapper.toEntity(request, userSchedule);

        userSchedule.setNextExecutionDateTime(SchedulerUtils.calculateNextExecutionDate(request));
        userSchedule.setLastExecutionDateTime(SchedulerUtils.calculateLastExecutionDate(request));
        userSchedule.setScheduleStatus(ScheduleStatus.ACTIVE);
        userSchedule.setStatus(StatusType.ACTIVE);
        userSchedule.setExecutionCount(0);
        userSchedule.setMaxExecutionCount(schedule.getMaxScheduleCount());
        userSchedule.setDisplayOrder(
                userScheduleRepository.countByCustomerKeyAndStatus(request.getCustomerKey(), StatusType.ACTIVE) + 1);

        try {
            UserSchedule saved = userScheduleRepository.save(userSchedule);
            saveHistory(saved, ScheduleStatus.CREATED.name());
            return UserScheduleResponse.fromEntity(userSchedule);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            throw new AppException("Failed to save due to DB constraint " + e.getMessage());
        }
    }

    private void validateScheduleCreation(String customerKey, Schedule schedule, UserScheduleCreateRequest request) {
        // Validate amount limits
        BigDecimal amount = request.getAmount(); // BigDecimal
        BigDecimal maxAmount = schedule.getMaxAmount();
        BigDecimal minAmount = schedule.getMinAmount();

        // Check maximum
        if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
            throw new BusinessException(
                    "Amount exceeds maximum limit", ErrorCode.SCHEDULE_AMOUNT_EXCEEDS_MAXIMUM_LIMIT);
        }

        // Check minimum
        if (minAmount != null && amount.compareTo(minAmount) < 0) {
            throw new BusinessException("Amount is below minimum limit", ErrorCode.SCHEDULE_AMOUNT_BELOW_MINIMUM_LIMIT);
        }

        // Validate schedule count per user
        if (schedule.getMaxScheduleCount() != null) {
            long currentCount = userScheduleRepository.countByCustomerKeyAndServiceTypeAndStatus(
                    customerKey, request.getServiceType(), StatusType.ACTIVE);
            if (currentCount >= schedule.getMaxScheduleCount()) {
                throw new BusinessException(
                        "Maximum number of schedules reached", ErrorCode.SCHEDULE_MAX_COUNT_REACHED);
            }
        }
    }

    @Transactional
    public UserScheduleResponse toggleUserSchedulePinStatus(Long userScheduleId, String customerKey) {
        UserSchedule schedule = userScheduleRepository
                .findByIdAndCustomerKey(userScheduleId, customerKey)
                .orElseThrow(() -> new BusinessException("Schedule not found", ErrorCode.SCHEDULE_NOT_FOUND));
        if (!schedule.isPinned()) {
            schedule.setPinned(true);
            reorderUserSchedules(schedule, userScheduleId, customerKey, Integer.MAX_VALUE); // bottom of pinned
        } else {
            schedule.setPinned(false);
            reorderUserSchedules(schedule, userScheduleId, customerKey, 1); // top of unpinned
        }
        return UserScheduleResponse.fromEntity(schedule);
    }

    @Transactional
    public UserScheduleResponse reorderUserSchedules(
            UserSchedule target, Long userScheduleId, String customerKey, Integer newOrder) {
        UserSchedule userSchedule = target != null
                ? target
                : userScheduleRepository
                        .findByIdAndCustomerKeyAndStatus(userScheduleId, customerKey, StatusType.ACTIVE)
                        .orElseThrow(() -> new AppException("User schedule not found"));

        List<UserSchedule> all =
                userScheduleRepository.findByCustomerKeyAndStatusOrderByDisplayOrder(customerKey, StatusType.ACTIVE);

        List<UserSchedule> pinned = all.stream()
                .filter(UserSchedule::isPinned)
                .filter(s -> !s.getId().equals(userSchedule.getId()))
                .toList();

        List<UserSchedule> unpinned = all.stream()
                .filter(s -> !s.isPinned())
                .filter(s -> !s.getId().equals(userSchedule.getId()))
                .toList();

        List<UserSchedule> targetList = new ArrayList<>(userSchedule.isPinned() ? pinned : unpinned);

        int max = targetList.size();
        int position = Math.max(1, Math.min(newOrder, max + 1));

        targetList.add(position - 1, userSchedule);

        List<UserSchedule> finalList = new ArrayList<>();

        if (userSchedule.isPinned()) {
            finalList.addAll(targetList); // pinned with target inserted
            finalList.addAll(unpinned);
        } else {
            finalList.addAll(pinned);
            finalList.addAll(targetList); // unpinned with target inserted
        }

        for (int i = 0; i < finalList.size(); i++) {
            finalList.get(i).setDisplayOrder(i + 1);
        }

        userScheduleRepository.saveAll(finalList);

        return UserScheduleResponse.fromEntity(userSchedule);
    }

    @Transactional
    public UserScheduleResponse modifyUserSchedule(Long userScheduleId, UserScheduleModifyRequest request) {
        log.info("Modifying schedule for customerKey: {} scheduleId: {}", request.getCustomerKey(), userScheduleId);
        UserSchedule existingSchedule = userScheduleRepository
                .findByIdAndCustomerKey(userScheduleId, request.getCustomerKey())
                .orElseThrow(() -> new BusinessException(
                        String.format(
                                "UserSchedule not found for userScheduleId: %d and customerKey: %s",
                                userScheduleId, request.getCustomerKey()),
                        ErrorCode.SCHEDULE_NOT_FOUND));
        if (request.getAmount() != null) {
            Schedule template = scheduleRepository
                    .findByServiceTypeAndServiceCode(
                            existingSchedule.getServiceType(), existingSchedule.getServiceCode())
                    .orElseThrow(() -> new BusinessException(
                            String.format(
                                    "Schedule not found for serviceType: %s and serviceCode: %s",
                                    existingSchedule.getServiceType(), existingSchedule.getServiceCode()),
                            ErrorCode.SCHEDULE_NOT_FOUND));

            validateScheduleAmount(template, request);
        }
        userScheduleMapper.updateEntityFromRequest(request, existingSchedule);
        if (request.getFrequency() != null
                || request.getFrequencyDetail() != null
                || request.getStartDate() != null
                || request.getStartTime() != null) {
            existingSchedule.setNextExecutionDateTime(SchedulerUtils.calculateNextExecutionDate(existingSchedule));
            existingSchedule.setLastExecutionDateTime(SchedulerUtils.calculateLastExecutionDate(existingSchedule));
        }
        UserSchedule saved = userScheduleRepository.save(existingSchedule);
        saveHistory(saved, ScheduleStatus.MODIFIED.name());
        return UserScheduleResponse.fromEntity(saved);
    }

    @Transactional
    public UserScheduleResponse pauseSchedule(Long userScheduleId, String customerKey) {
        log.info("Pausing schedule for customerKey: {} scheduleId: {}", customerKey, userScheduleId);
        UserSchedule schedule = userScheduleRepository
                .findByIdAndCustomerKeyAndStatus(userScheduleId, customerKey, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException("Schedule not found", ErrorCode.SCHEDULE_NOT_FOUND));
        if (ScheduleStatus.PAUSED.equals(schedule.getScheduleStatus())) {
            throw new BusinessException("Schedule already paused", ErrorCode.INVALID_SCHEDULE_STATE);
        }
        schedule.setScheduleStatus(ScheduleStatus.PAUSED);
        schedule.setModifiedDate(LocalDateTime.now());
        UserSchedule saved = userScheduleRepository.save(schedule);
        saveHistory(saved, ScheduleStatus.PAUSED.name());
        return UserScheduleResponse.fromEntity(saved);
    }

    @Transactional
    public UserScheduleResponse resumeSchedule(Long userScheduleId, String customerKey) {
        log.info("Resuming schedule for customerKey: {} scheduleId: {}", customerKey, userScheduleId);
        UserSchedule schedule = userScheduleRepository
                .findByIdAndCustomerKeyAndStatus(userScheduleId, customerKey, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException("Schedule not found", ErrorCode.SCHEDULE_NOT_FOUND));
        if (!ScheduleStatus.PAUSED.equals(schedule.getScheduleStatus())) {
            throw new BusinessException("Schedule is not paused", ErrorCode.INVALID_SCHEDULE_STATE);
        }
        schedule.setScheduleStatus(ScheduleStatus.ACTIVE);
        schedule.setModifiedDate(LocalDateTime.now());
        UserSchedule saved = userScheduleRepository.save(schedule);
        saveHistory(saved, ScheduleStatus.RESUMED.name());
        return UserScheduleResponse.fromEntity(saved);
    }

    @Transactional
    public UserScheduleResponse cancelSchedule(Long userScheduleId, String customerKey) {
        UserSchedule schedule = userScheduleRepository
                .findByIdAndCustomerKeyAndStatus(userScheduleId, customerKey, StatusType.ACTIVE)
                .orElseThrow(() -> new BusinessException("Schedule not found", ErrorCode.SCHEDULE_NOT_FOUND));
        if (ScheduleStatus.COMPLETED.equals(schedule.getScheduleStatus())) {
            throw new BusinessException("Completed schedule cannot be cancelled", ErrorCode.INVALID_SCHEDULE_STATE);
        }
        schedule.setScheduleStatus(ScheduleStatus.CANCELLED);
        schedule.setStatus(StatusType.INACTIVE);
        schedule.setModifiedDate(LocalDateTime.now());
        UserSchedule saved = userScheduleRepository.save(schedule);
        saveHistory(saved, ScheduleStatus.CANCELLED.name());
        return UserScheduleResponse.fromEntity(saved);
    }

    @Transactional
    public UserScheduleResponse executeSchedule(Long userScheduleId, String customerKey) {
        log.info("Execute schedule for customerKey: {} scheduleId: {}", customerKey, userScheduleId);
        UserSchedule schedule = userScheduleRepository
                .findByIdAndCustomerKey(userScheduleId, customerKey)
                .orElseThrow(() -> new BusinessException("UserSchedule not found", ErrorCode.SCHEDULE_NOT_FOUND));
        if (!ScheduleStatus.ACTIVE.equals(schedule.getScheduleStatus())) {
            throw new BusinessException(
                    "UserSchedule is invalid state for execute, which is " + schedule.getScheduleStatus(),
                    ErrorCode.INVALID_SCHEDULE_STATE);
        }
        if (schedule.getExecutionCount() >= schedule.getMaxExecutionCount()) {
            throw new BusinessException(
                    "UserSchedule is at maximum execution count, which is " + schedule.getExecutionCount(),
                    ErrorCode.SCHEDULE_MAX_COUNT_REACHED);
        }
        schedule.setModifiedDate(LocalDateTime.now());
        schedule.setExecutionCount(schedule.getExecutionCount() + 1);
        schedule.setNextExecutionDateTime(SchedulerUtils.calculateNextExecutionDate(schedule));
        UserSchedule saved = userScheduleRepository.save(schedule);
        saveHistory(saved, ScheduleStatus.EXECUTE.name());
        return UserScheduleResponse.fromEntity(saved);
    }

    @Transactional
    public Schedule disableSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository
                .findById(scheduleId)
                .orElseThrow(() -> new BusinessException("Schedule template not found", ErrorCode.SCHEDULE_NOT_FOUND));
        schedule.setStatus(StatusType.INACTIVE);
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public Schedule enableSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository
                .findById(scheduleId)
                .orElseThrow(() -> new BusinessException("Schedule template not found", ErrorCode.SCHEDULE_NOT_FOUND));
        schedule.setStatus(StatusType.ACTIVE);
        return scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByServiceType(String serviceType) {
        return scheduleRepository.findByServiceTypeAndStatus(serviceType, StatusType.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Page<@NonNull UserScheduleResponse> getSchedulesByCustomerKey(String customerKey, Pageable pageable) {
        log.info("Fetching schedules for customerKey: {}", customerKey);
        Page<@NonNull UserSchedule> page =
                userScheduleRepository.findByCustomerKeyAndStatus(customerKey, StatusType.ACTIVE, pageable);
        List<UserScheduleResponse> dtoList =
                page.map(UserScheduleResponse::fromEntity).getContent();
        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<@NonNull UserScheduleResponse> getSchedulesByServiceType(
            String customerKey, String serviceType, Pageable pageable) {
        log.info("Fetching schedules for customerKey: {} and serviceType: {}", customerKey, serviceType);
        Page<@NonNull UserSchedule> page = userScheduleRepository.findByCustomerKeyAndServiceTypeAndStatus(
                customerKey, serviceType, StatusType.ACTIVE, pageable);
        List<UserScheduleResponse> dtoList =
                page.map(UserScheduleResponse::fromEntity).getContent();
        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public UserScheduleResponse getUserSchedule(Long userScheduleId, String customerKey) {
        log.info("Fetching schedule for userScheduleId: {} and customerKey: {}", userScheduleId, customerKey);
        UserSchedule schedule = userScheduleRepository
                .findByIdAndCustomerKey(userScheduleId, customerKey)
                .orElseThrow(() -> new BusinessException("Schedule not found", ErrorCode.SCHEDULE_NOT_FOUND));
        return UserScheduleResponse.fromEntity(schedule);
    }

    @Transactional(readOnly = true)
    public Page<@NonNull UserScheduleResponse> getSchedulesByCustomerKeyAndStatus(
            String customerKey, ScheduleStatus status, Pageable pageable) {
        Page<@NonNull UserSchedule> page =
                userScheduleRepository.findByCustomerKeyAndScheduleStatus(customerKey, status, pageable);
        List<UserScheduleResponse> dtoList =
                page.map(UserScheduleResponse::fromEntity).getContent();
        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<@NonNull UserScheduleHistoryResponse> getUserScheduleHistories(
            Long userScheduleId, String customerKey, Pageable pageable) {
        Page<@NonNull UserScheduleHistory> entity =
                userScheduleHistoryRepository.findByUserScheduleIdAndCustomerKey(userScheduleId, customerKey, pageable);
        return userScheduleHistoryMapper.fromPageEntity(entity);
    }

    private void validateScheduleAmount(Schedule scheduleTemplate, UserScheduleModifyRequest request) {
        // Validate amount limits
        BigDecimal amount = request.getAmount(); // BigDecimal
        BigDecimal maxAmount = scheduleTemplate.getMaxAmount();
        BigDecimal minAmount = scheduleTemplate.getMinAmount();

        // Check maximum
        if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
            throw new BusinessException(
                    "Modified amount exceeds maximum limit", ErrorCode.SCHEDULE_AMOUNT_EXCEEDS_MAXIMUM_LIMIT);
        }

        // Check minimum
        if (minAmount != null && amount.compareTo(minAmount) < 0) {
            throw new BusinessException(
                    "Modified amount is below minimum limit", ErrorCode.SCHEDULE_AMOUNT_BELOW_MINIMUM_LIMIT);
        }
    }

    private void saveHistory(UserSchedule userSchedule, String action) {
        UserScheduleHistory history = new UserScheduleHistory();

        // Relationship
        history.setUserSchedule(userSchedule);
        history.setScheduleId(userSchedule.getSchedule().getId());

        // Customer context
        history.setCustomerKey(userSchedule.getCustomerKey());
        history.setCustomerNo(userSchedule.getCustomerNo());
        history.setAccountNo(userSchedule.getAccountNo());
        history.setPhoneNo(userSchedule.getPhoneNo());
        history.setMasterAccountNo(userSchedule.getMasterAccountNo());
        history.setChannelCode(userSchedule.getChannelCode());

        // Schedule snapshot
        history.setServiceType(userSchedule.getServiceType());
        history.setServiceCode(userSchedule.getServiceCode());
        history.setScheduleName(userSchedule.getScheduleName());
        history.setScheduleType(userSchedule.getScheduleType());
        history.setFrequency(userSchedule.getFrequency().name());
        history.setFrequencyDetail(userSchedule.getFrequencyDetail());
        history.setStartDate(userSchedule.getStartDate());
        history.setEndDate(userSchedule.getEndDate());
        history.setStartTime(userSchedule.getStartTime());
        history.setEndTime(userSchedule.getEndTime());
        history.setNextExecutionDateTime(userSchedule.getNextExecutionDateTime());
        history.setLastExecutionDateTime(userSchedule.getLastExecutionDateTime());
        history.setExecutionCount(userSchedule.getExecutionCount());
        history.setMaxExecutionCount(userSchedule.getMaxExecutionCount());
        history.setAmount(userSchedule.getAmount());
        history.setCurrency(userSchedule.getCurrency());
        history.setReferenceId(userSchedule.getReferenceId());
        history.setScheduleStatus(userSchedule.getScheduleStatus());
        history.setAdditionalData(userSchedule.getAdditionalData());

        // Config values
        history.setConfigValue1(userSchedule.getConfigValue1());
        history.setConfigValue2(userSchedule.getConfigValue2());
        history.setConfigValue3(userSchedule.getConfigValue3());
        history.setConfigValue4(userSchedule.getConfigValue4());
        history.setConfigValue5(userSchedule.getConfigValue5());
        history.setConfigValue6(userSchedule.getConfigValue6());
        history.setConfigValue7(userSchedule.getConfigValue7());
        history.setConfigValue8(userSchedule.getConfigValue8());
        history.setConfigValue9(userSchedule.getConfigValue9());
        history.setConfigValue10(userSchedule.getConfigValue10());

        // Meta
        history.setStatus(StatusType.ACTIVE);
        history.setAction(action);

        userScheduleHistoryRepository.save(history);
    }
}

package com.example.persona.controller;

import com.example.persona.dto.ApiResponse;
import com.example.persona.schedule.dto.ScheduleCreateRequest;
import com.example.persona.schedule.dto.ScheduleModifyRequest;
import com.example.persona.schedule.dto.UserScheduleCreateRequest;
import com.example.persona.schedule.dto.UserScheduleModifyRequest;
import com.example.persona.schedule.dto.response.ScheduleResponse;
import com.example.persona.schedule.dto.response.UserScheduleHistoryResponse;
import com.example.persona.schedule.dto.response.UserScheduleResponse;
import com.example.persona.schedule.enums.ScheduleStatus;
import com.example.persona.schedule.mapper.ScheduleMapper;
import com.example.persona.schedule.service.ScheduleService;
import com.example.persona.search.dto.PaginatedResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule API", description = "APIs for managing schedule data")
public class ScheduleController {
    private final ScheduleMapper scheduleMapper;
    private final ScheduleService scheduleService;

    @PostMapping("/create")
    public ResponseEntity<@NonNull ApiResponse<ScheduleResponse>> createSchedule(
            @RequestBody ScheduleCreateRequest request) {
        ScheduleResponse response = scheduleMapper.toResponse(scheduleService.createSchedule(request));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{scheduleId}/modify")
    public ResponseEntity<@NonNull ApiResponse<ScheduleResponse>> modifySchedule(
            @PathVariable Long scheduleId, @RequestBody ScheduleModifyRequest request) {
        ScheduleResponse response = scheduleMapper.toResponse(scheduleService.modifySchedule(scheduleId, request));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{scheduleId}/disable")
    public ResponseEntity<@NonNull ApiResponse<ScheduleResponse>> disableSchedule(@PathVariable Long scheduleId) {
        ScheduleResponse response = scheduleMapper.toResponse(scheduleService.disableSchedule(scheduleId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{scheduleId}/enable")
    public ResponseEntity<@NonNull ApiResponse<ScheduleResponse>> enableSchedule(@PathVariable Long scheduleId) {
        ScheduleResponse response = scheduleMapper.toResponse(scheduleService.enableSchedule(scheduleId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/find-by-service-type")
    public ResponseEntity<@NonNull ApiResponse<List<ScheduleResponse>>> getSchedulesByServiceType(
            @RequestParam String serviceType) {
        List<ScheduleResponse> responses =
                scheduleMapper.toResponse(scheduleService.getSchedulesByServiceType(serviceType));
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/user-schedules/create")
    public ResponseEntity<@NonNull ApiResponse<UserScheduleResponse>> createUserSchedule(
            @RequestBody UserScheduleCreateRequest request) {
        log.info("Intercept to create user schedule: {}", request.toString());
        return ResponseEntity.ok(ApiResponse.success(scheduleService.createUserSchedule(request)));
    }

    @PutMapping("/user-schedules/{userScheduleId}/modify")
    public ResponseEntity<@NonNull ApiResponse<UserScheduleResponse>> modifyUserSchedule(
            @PathVariable Long userScheduleId, @RequestBody UserScheduleModifyRequest request) {
        log.info("Intercept to modify user schedule: {}", request.toString());
        return ResponseEntity.ok(ApiResponse.success(scheduleService.modifyUserSchedule(userScheduleId, request)));
    }

    @PutMapping("/user-schedules/{userScheduleId}/toggle-pin")
    public ResponseEntity<@NonNull ApiResponse<UserScheduleResponse>> toggleUserSchedulePinStatus(
            @PathVariable Long userScheduleId, @RequestParam String customerKey) {
        log.info("Intercept to toggle user schedule: {} for user {}", userScheduleId, customerKey);
        return ResponseEntity.ok(
                ApiResponse.success(scheduleService.toggleUserSchedulePinStatus(userScheduleId, customerKey)));
    }

    @PutMapping("/user-schedules/{userScheduleId}/reorder")
    public ResponseEntity<@NonNull ApiResponse<UserScheduleResponse>> reorderUserSchedules(
            @PathVariable Long userScheduleId, @RequestParam String customerKey, @RequestParam Integer newOrder) {
        log.info(
                "Intercept to reorder user schedule: {} for user {} to position: {}",
                userScheduleId,
                customerKey,
                newOrder);
        return ResponseEntity.ok(
                ApiResponse.success(scheduleService.reorderUserSchedules(null, userScheduleId, customerKey, newOrder)));
    }

    @PutMapping("/user-schedules/{userScheduleId}/pause")
    public ResponseEntity<@NonNull ApiResponse<UserScheduleResponse>> pauseSchedule(
            @PathVariable Long userScheduleId, @RequestParam String customerKey) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.pauseSchedule(userScheduleId, customerKey)));
    }

    @PutMapping("/user-schedules/{userScheduleId}/resume")
    public ResponseEntity<@NonNull ApiResponse<UserScheduleResponse>> resumeSchedule(
            @PathVariable Long userScheduleId, @RequestParam String customerKey) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.resumeSchedule(userScheduleId, customerKey)));
    }

    @PutMapping("/user-schedules/{userScheduleId}/cancel")
    public ResponseEntity<@NonNull ApiResponse<UserScheduleResponse>> cancelSchedule(
            @PathVariable Long userScheduleId, @RequestParam String customerKey) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.cancelSchedule(userScheduleId, customerKey)));
    }

    @PutMapping("/user-schedules/{userScheduleId}/execute")
    public ResponseEntity<@NonNull ApiResponse<UserScheduleResponse>> executeSchedule(
            @PathVariable Long userScheduleId, @RequestParam String customerKey) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.executeSchedule(userScheduleId, customerKey)));
    }

    @GetMapping("/user-schedules/{userScheduleId}/find")
    public ResponseEntity<@NonNull ApiResponse<UserScheduleResponse>> getSchedule(
            @PathVariable Long userScheduleId, @RequestParam String customerKey) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getUserSchedule(userScheduleId, customerKey)));
    }

    @GetMapping("/user-schedules/list")
    public ResponseEntity<@NonNull ApiResponse<PaginatedResult<UserScheduleResponse>>> getScheduleList(
            @RequestParam String customerKey,
            @RequestParam(required = false) ScheduleStatus status,
            Pageable pageable) {

        Page<@NonNull UserScheduleResponse> result = (status != null)
                ? scheduleService.getSchedulesByCustomerKeyAndStatus(customerKey, status, pageable)
                : scheduleService.getSchedulesByCustomerKey(customerKey, pageable);
        PaginatedResult<UserScheduleResponse> response = PaginatedResult.of(
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getContent());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user-schedules/{userScheduleId}/schedule-histories")
    public ResponseEntity<@NonNull ApiResponse<PaginatedResult<UserScheduleHistoryResponse>>> getUserScheduleHistories(
            @PathVariable Long userScheduleId, @RequestParam String customerKey, Pageable pageable) {
        Page<@NonNull UserScheduleHistoryResponse> result =
                scheduleService.getUserScheduleHistories(userScheduleId, customerKey, pageable);
        PaginatedResult<UserScheduleHistoryResponse> response = PaginatedResult.of(
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getContent());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

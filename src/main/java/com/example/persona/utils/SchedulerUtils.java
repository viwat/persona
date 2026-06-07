package com.example.persona.utils;

import com.example.persona.enums.ErrorCode;
import com.example.persona.enums.FrequencyType;
import com.example.persona.exception.BusinessException;
import com.example.persona.schedule.dto.UserScheduleCreateRequest;
import com.example.persona.schedule.model.UserSchedule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class SchedulerUtils {
    public LocalDateTime calculateNextExecutionDate(UserScheduleCreateRequest request) {
        try {
            LocalDate startDate = request.getStartDate();
            LocalTime startTime = request.getStartTime();
            LocalDateTime baseDateTime = LocalDateTime.of(startDate, startTime);

            LocalDateTime now = LocalDateTime.now();

            // If scheduled start is already past, use now as reference
            if (baseDateTime.isBefore(now)) {
                baseDateTime = now;
            }

            return calculateByFrequency(baseDateTime, request.getFrequency(), request.getFrequencyDetail(), startTime);
        } catch (Exception e) {
            log.error("Error while calculateNextExecutionDate, return null {}", e.getMessage());
            return null;
        }
    }

    public LocalDateTime calculateNextExecutionDate(UserSchedule request) {
        try {
            if (request.getExecutionCount() >= request.getMaxExecutionCount()) {
                return null;
            }

            LocalDate startDate = request.getStartDate();
            LocalTime startTime = request.getStartTime();
            LocalDateTime baseDateTime = LocalDateTime.of(startDate, startTime);

            LocalDateTime now = LocalDateTime.now();

            // If scheduled start is already past, use now as reference
            if (baseDateTime.isBefore(now)) {
                baseDateTime = now;
            }

            return calculateByFrequency(baseDateTime, request.getFrequency(), request.getFrequencyDetail(), startTime);
        } catch (Exception e) {
            log.error("Error while calculateNextExecutionDate, return null {}", e.getMessage());
            return null;
        }
    }

    private LocalDateTime calculateByFrequency(
            LocalDateTime baseDateTime, FrequencyType frequency, String detail, LocalTime executionTime) {
        return switch (frequency) {
            case DAILY -> calculateNextDailyExecution(baseDateTime, executionTime);
            case WEEKLY -> calculateNextWeeklyExecution(baseDateTime, detail, executionTime);
            case MONTHLY -> calculateNextMonthlyExecution(baseDateTime, detail, executionTime);
            case YEARLY -> calculateNextYearlyExecution(baseDateTime, detail, executionTime);
        };
    }

    private LocalDateTime calculateNextDailyExecution(LocalDateTime baseDateTime, LocalTime executionTime) {

        LocalDateTime candidate = baseDateTime.toLocalDate().atTime(executionTime);

        if (!candidate.isAfter(baseDateTime)) {
            candidate = candidate.plusDays(1);
        }

        return candidate;
    }

    private LocalDateTime calculateNextWeeklyExecution(
            LocalDateTime baseDateTime, String frequencyDetail, LocalTime executionTime) {

        if (frequencyDetail == null || frequencyDetail.isBlank()) {
            throw new BusinessException("Weekly frequency requires frequencyDetail", ErrorCode.VALIDATION_ERROR);
        }

        Set<Integer> days = Arrays.stream(frequencyDetail.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toSet());

        LocalDate date = baseDateTime.toLocalDate();

        for (int i = 0; i < 7; i++) {
            LocalDate candidateDate = date.plusDays(i);
            LocalDateTime candidate = candidateDate.atTime(executionTime);

            if (days.contains(candidate.getDayOfWeek().getValue()) && candidate.isAfter(baseDateTime)) {
                return candidate;
            }
        }

        // next week fallback
        return date.plusWeeks(1).atTime(executionTime);
    }

    private LocalDateTime calculateNextMonthlyExecution(
            LocalDateTime baseDateTime, String frequencyDetail, LocalTime executionTime) {

        int dayOfMonth = Integer.parseInt(frequencyDetail);
        LocalDate baseDate = baseDateTime.toLocalDate();

        LocalDate candidateDate = baseDate.withDayOfMonth(Math.min(dayOfMonth, baseDate.lengthOfMonth()));
        LocalDateTime candidate = candidateDate.atTime(executionTime);

        if (!candidate.isAfter(baseDateTime)) {
            LocalDate nextMonth = baseDate.plusMonths(1);
            candidateDate = nextMonth.withDayOfMonth(Math.min(dayOfMonth, nextMonth.lengthOfMonth()));
            candidate = candidateDate.atTime(executionTime);
        }

        return candidate;
    }

    private LocalDateTime calculateNextYearlyExecution(
            LocalDateTime baseDateTime, String frequencyDetail, LocalTime executionTime) {

        MonthDay monthDay = MonthDay.parse(frequencyDetail, DateTimeFormatter.ofPattern("MM-dd"));

        LocalDate candidateDate = monthDay.atYear(baseDateTime.getYear());
        LocalDateTime candidate = candidateDate.atTime(executionTime);

        if (!candidate.isAfter(baseDateTime)) {
            candidateDate = monthDay.atYear(baseDateTime.getYear() + 1);
            candidate = candidateDate.atTime(executionTime);
        }

        return candidate;
    }

    public LocalDateTime calculateLastExecutionDate(UserScheduleCreateRequest request) {
        try {
            if (request.getEndDate() == null) {
                return null; // no end → infinite schedule
            }

            LocalDateTime endDateTime = LocalDateTime.of(
                    request.getEndDate(), request.getEndTime() == null ? request.getStartTime() : request.getEndTime());

            return calculateLastByFrequency(
                    endDateTime,
                    request.getFrequency(),
                    request.getFrequencyDetail(),
                    request.getStartTime() // execution time
                    );
        } catch (Exception e) {
            log.error("Error while calculateLastExecutionDate, return null {}", e.getMessage());
            return null;
        }
    }

    public LocalDateTime calculateLastExecutionDate(UserSchedule request) {
        try {
            if (request.getEndDate() == null) {
                return null; // no end → infinite schedule
            }

            LocalDateTime endDateTime = LocalDateTime.of(
                    request.getEndDate(), request.getEndTime() == null ? request.getStartTime() : request.getEndTime());

            return calculateLastByFrequency(
                    endDateTime,
                    request.getFrequency(),
                    request.getFrequencyDetail(),
                    request.getStartTime() // execution time
                    );
        } catch (Exception e) {
            log.error("Error while calculateLastExecutionDate, return null {}", e.getMessage());
            return null;
        }
    }

    private LocalDateTime calculateLastByFrequency(
            LocalDateTime endDateTime, FrequencyType frequency, String detail, LocalTime executionTime) {
        return switch (frequency) {
            case DAILY -> calculateLastDailyExecution(endDateTime, executionTime);
            case WEEKLY -> calculateLastWeeklyExecution(endDateTime, detail, executionTime);
            case MONTHLY -> calculateLastMonthlyExecution(endDateTime, detail, executionTime);
            case YEARLY -> calculateLastYearlyExecution(endDateTime, detail, executionTime);
        };
    }

    private LocalDateTime calculateLastDailyExecution(LocalDateTime endDateTime, LocalTime executionTime) {

        LocalDateTime candidate = endDateTime.toLocalDate().atTime(executionTime);

        if (candidate.isAfter(endDateTime)) {
            candidate = candidate.minusDays(1);
        }

        return candidate;
    }

    private LocalDateTime calculateLastWeeklyExecution(
            LocalDateTime endDateTime, String frequencyDetail, LocalTime executionTime) {
        Set<Integer> days = Arrays.stream(frequencyDetail.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toSet());

        LocalDate date = endDateTime.toLocalDate();

        for (int i = 0; i < 7; i++) {
            LocalDate candidateDate = date.minusDays(i);
            LocalDateTime candidate = candidateDate.atTime(executionTime);

            if (days.contains(candidate.getDayOfWeek().getValue()) && !candidate.isAfter(endDateTime)) {
                return candidate;
            }
        }

        return null; // no valid execution
    }

    private LocalDateTime calculateLastMonthlyExecution(
            LocalDateTime endDateTime, String frequencyDetail, LocalTime executionTime) {

        int dayOfMonth = Integer.parseInt(frequencyDetail);
        LocalDate baseDate = endDateTime.toLocalDate();

        LocalDate candidateDate = baseDate.withDayOfMonth(Math.min(dayOfMonth, baseDate.lengthOfMonth()));
        LocalDateTime candidate = candidateDate.atTime(executionTime);

        if (candidate.isAfter(endDateTime)) {
            LocalDate prevMonth = baseDate.minusMonths(1);
            candidateDate = prevMonth.withDayOfMonth(Math.min(dayOfMonth, prevMonth.lengthOfMonth()));
            candidate = candidateDate.atTime(executionTime);
        }

        return candidate;
    }

    private LocalDateTime calculateLastYearlyExecution(
            LocalDateTime endDateTime, String frequencyDetail, LocalTime executionTime) {

        MonthDay monthDay = MonthDay.parse(frequencyDetail, DateTimeFormatter.ofPattern("MM-dd"));

        LocalDate candidateDate = monthDay.atYear(endDateTime.getYear());
        LocalDateTime candidate = candidateDate.atTime(executionTime);

        if (candidate.isAfter(endDateTime)) {
            candidateDate = monthDay.atYear(endDateTime.getYear() - 1);
            candidate = candidateDate.atTime(executionTime);
        }

        return candidate;
    }
}

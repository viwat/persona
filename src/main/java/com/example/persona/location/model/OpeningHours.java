package com.example.persona.location.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

/**
 * Opening hours value object. Stores per-day open/close times.
 * A null entry means the location is closed that day.
 */
@Value
@Builder
public class OpeningHours {

    Map<DayOfWeek, DaySchedule> schedule;
    String specialNotes;

    public boolean isOpenOn(DayOfWeek day) {
        if (schedule == null) return false;
        return schedule.containsKey(day) && schedule.get(day) != null;
    }

    public Map<DayOfWeek, DaySchedule> getSchedule() {
        return schedule != null ? Collections.unmodifiableMap(schedule) : Collections.emptyMap();
    }

    @Value
    @Builder
    public static class DaySchedule {
        LocalTime openTime;
        LocalTime closeTime;

        public boolean isOpenAt(LocalTime time) {
            return !time.isBefore(openTime) && !time.isAfter(closeTime);
        }
    }
}

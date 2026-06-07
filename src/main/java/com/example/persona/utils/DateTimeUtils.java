package com.example.persona.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateTimeUtils {
    // Convert LocalDateTime → epoch millis
    public static String toEpochMillis(LocalDateTime dateTime) {
        return toEpochMillis(dateTime, ZoneId.systemDefault());
    }

    // Convert epoch millis → LocalDateTime
    public static LocalDateTime fromEpochMillis(long millis) {
        return fromEpochMillis(millis, ZoneId.systemDefault());
    }

    // Convert LocalDateTime → epoch millis
    public static String toEpochMillis(LocalDateTime dateTime, ZoneId zone) {
        if (dateTime == null) {
            return null;
        }
        return ""
                + dateTime.atZone(zone) // attach timezone
                        .toInstant() // convert to UTC instant
                        .toEpochMilli(); // milliseconds
    }

    // Convert epoch millis → LocalDateTime
    public static LocalDateTime fromEpochMillis(long millis, ZoneId zone) {
        return Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime();
    }

    public static LocalTime parse(String time) {
        try {
            return LocalTime.parse(time);
        } catch (Exception ex) {
            return null;
        }
    }
}

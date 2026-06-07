package com.example.persona.utils;

import com.example.persona.enums.DayOfWeek;
import com.example.persona.location.model.LocationOperatingHour;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LocationUtils {
    public static Double EARTH_RADIUS_KM = 6371.0;
    // AI Code
    public static double haversineDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLng / 2)
                        * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public static String formatOperatingDateTime(List<LocationOperatingHour> operatingHours) {
        if (operatingHours == null || operatingHours.isEmpty()) {
            return null;
        }

        List<DayOfWeek> orderedDays = List.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY);

        Map<DayOfWeek, LocationOperatingHour> map =
                operatingHours.stream().collect(Collectors.toMap(LocationOperatingHour::getDayOfWeek, h -> h));

        List<String> result = new ArrayList<>();
        int i = 0;

        while (i < orderedDays.size()) {

            DayOfWeek startDay = orderedDays.get(i);
            LocationOperatingHour start = map.get(startDay);

            String scheduleStr = toScheduleString(start);
            int j = i;

            // group consecutive days with same schedule
            while (j + 1 < orderedDays.size()) {
                DayOfWeek nextDay = orderedDays.get(j + 1);
                LocationOperatingHour next = map.get(nextDay);

                if (!sameSchedule(start, next)) {
                    break;
                }
                j++;
            }

            String dayLabel = shortDay(startDay);
            if (j > i) {
                dayLabel += "–" + shortDay(orderedDays.get(j));
            }

            result.add(dayLabel + " " + scheduleStr);
            i = j + 1;
        }

        return String.join(" | ", result);
    }

    private static boolean sameSchedule(LocationOperatingHour a, LocationOperatingHour b) {
        if (a == null || b == null) return false;

        return a.isClosed() == b.isClosed()
                && Objects.equals(a.getOpenTime(), b.getOpenTime())
                && Objects.equals(a.getCloseTime(), b.getCloseTime())
                && Objects.equals(a.getSecondOpenTime(), b.getSecondOpenTime())
                && Objects.equals(a.getSecondCloseTime(), b.getSecondCloseTime());
    }

    private static String toScheduleString(LocationOperatingHour h) {
        if (h == null || h.isClosed()) return "Closed";

        String first = formatTime(h.getOpenTime()) + "–" + formatTime(h.getCloseTime());

        if (h.getSecondOpenTime() != null && h.getSecondCloseTime() != null) {
            return first + ", " + formatTime(h.getSecondOpenTime()) + "–" + formatTime(h.getSecondCloseTime());
        }
        return first;
    }

    private static String formatTime(LocalTime time) {
        return time == null ? "" : time.toString(); // HH:mm
    }

    private static String shortDay(DayOfWeek day) {
        return day.getDisplayName(DayOfWeek.TextStyle.SHORT);
    }
}

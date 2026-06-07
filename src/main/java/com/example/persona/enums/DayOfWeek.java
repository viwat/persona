package com.example.persona.enums;

public enum DayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    public String getDisplayName(TextStyle style) {
        String[] shortNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        String[] fullNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String[] narrowNames = {"M", "T", "W", "T", "F", "S", "S"};

        int index = this.ordinal(); // MONDAY=0, ..., SUNDAY=6

        return switch (style) {
            case SHORT -> shortNames[index];
            case NARROW -> narrowNames[index];
            default -> fullNames[index];
        };
    }

    public enum TextStyle {
        FULL,
        SHORT,
        NARROW
    }
}

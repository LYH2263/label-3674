package com.label.community.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TimeUtil() {
    }

    public static LocalDateTime parseDateTime(String text) {
        return LocalDateTime.parse(text, FORMATTER);
    }

    public static Timestamp toTimestamp(LocalDateTime time) {
        return Timestamp.valueOf(time);
    }
}

package com.uco.ucopetapi.crosscutting.helpers;
import java.time.LocalDateTime;

public final class DateTimeHelper {
    public static final LocalDateTime DEFAULT = LocalDateTime.now();
    private DateTimeHelper() {

    }
    public static LocalDateTime getDefault() {
        return DEFAULT;
    }
    public static LocalDateTime getDefault(final LocalDateTime value) {
        return ObjectHelper.getDefault(value, getDefault());
    }
}

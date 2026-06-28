package com.jobalert.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** Parses the many date formats job APIs return into an {@link Instant}, or null. */
public final class DateUtil {

    private DateUtil() {}

    public static Instant parse(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Instant.parse(value); } catch (RuntimeException ignored) {}
        try { return OffsetDateTime.parse(value).toInstant(); } catch (RuntimeException ignored) {}
        try { return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC); } catch (RuntimeException ignored) {}
        try { return LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC); } catch (RuntimeException ignored) {}
        return null;
    }
}

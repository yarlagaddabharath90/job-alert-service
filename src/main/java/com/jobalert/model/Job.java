package com.jobalert.model;

import java.time.Instant;

/** A normalized job posting from any source. */
public record Job(
        String title,
        String company,
        String location,
        String url,
        Instant posted,
        String description,
        String source,
        boolean remote
) {
    /** Lowercased blob used for keyword matching. */
    public String text() {
        return (n(title) + "\n" + n(location) + "\n" + n(description)).toLowerCase();
    }

    private static String n(String s) {
        return s == null ? "" : s;
    }
}

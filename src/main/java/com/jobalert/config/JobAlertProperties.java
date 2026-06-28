package com.jobalert.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/** All tunable search settings, bound from the {@code jobalert.*} section of application.yml. */
@ConfigurationProperties(prefix = "jobalert")
public record JobAlertProperties(
        String cron,
        String zone,
        String country,
        Integer maxAgeHours,
        Boolean dryRun,
        String emailTo,
        Boolean splitLocationsIntoSeparateEmails,
        List<String> queries,
        Adzuna adzuna,
        String rapidapiKey,
        List<List<String>> titleRequired,
        List<String> excludeTitleKeywords,
        List<String> txMarkers,
        List<String> cities,
        List<String> contractPositive,
        List<String> contractNegative,
        List<String> sponsorshipPositive,
        List<String> sponsorshipNegative
) {
    public record Adzuna(String appId, String appKey) {}

    public int maxAgeHoursOrDefault() {
        return maxAgeHours == null ? 24 : maxAgeHours;
    }

    public boolean dryRunOrDefault() {
        return Boolean.TRUE.equals(dryRun);
    }

    public boolean splitOrDefault() {
        return Boolean.TRUE.equals(splitLocationsIntoSeparateEmails);
    }
}

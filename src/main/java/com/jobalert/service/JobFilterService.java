package com.jobalert.service;

import com.jobalert.config.JobAlertProperties;
import com.jobalert.model.Job;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Applies all local rules: title match, freshness, junior exclusion,
 * Contract/C2C vs Full-time+Sponsorship classification, and Remote/TX/Other bucketing.
 */
@Service
public class JobFilterService {

    public static final List<String> LOCATIONS = List.of("remote", "tx", "other");

    private final JobAlertProperties props;

    public JobFilterService(JobAlertProperties props) {
        this.props = props;
    }

    /** Returns { "contract": {remote,tx,other}, "fulltime": {remote,tx,other} }. */
    public Map<String, Map<String, List<Job>>> classify(List<Job> jobs) {
        Map<String, Map<String, List<Job>>> result = new LinkedHashMap<>();
        result.put("contract", emptyBuckets());
        result.put("fulltime", emptyBuckets());

        Set<String> seen = new HashSet<>();
        for (Job job : jobs) {
            if (!titleMatches(job)) continue;
            if (!isFresh(job)) continue;
            if (job.url() == null || job.url().isBlank() || seen.contains(job.url())) continue;

            String category = null;
            if (isC2cContract(job)) {
                category = "contract";
            } else if (isFtSponsorship(job)) {
                category = "fulltime";
            }
            if (category == null) continue;

            seen.add(job.url());
            result.get(category).get(locationBucket(job)).add(job);
        }

        result.values().forEach(buckets -> buckets.values().forEach(this::sortNewestFirst));
        return result;
    }

    // ---- rules ---------------------------------------------------------

    private boolean titleMatches(Job job) {
        String t = job.title() == null ? "" : job.title().toLowerCase();
        if (hasAny(t, props.excludeTitleKeywords())) return false;
        for (List<String> group : props.titleRequired()) {
            boolean groupHit = group.stream().anyMatch(v -> t.contains(v.toLowerCase()));
            if (!groupHit) return false;
        }
        return true;
    }

    private boolean isFresh(Job job) {
        if (job.posted() == null) return true; // sources already limit to ~1 day
        Instant cutoff = Instant.now().minus(props.maxAgeHoursOrDefault(), ChronoUnit.HOURS);
        return !job.posted().isBefore(cutoff);
    }

    private boolean isC2cContract(Job job) {
        String text = job.text();
        if (hasAny(text, props.contractNegative())) return false;
        return hasAny(text, props.contractPositive());
    }

    private boolean isFtSponsorship(Job job) {
        String text = job.text();
        if (hasAny(text, props.sponsorshipNegative())) return false;
        return hasAny(text, props.sponsorshipPositive());
    }

    private String locationBucket(Job job) {
        String text = ((job.location() == null ? "" : job.location()) + " "
                + (job.title() == null ? "" : job.title())).toLowerCase();
        if (job.remote() || text.contains("remote")) return "remote";
        if (hasAny(text, props.txMarkers()) || hasAny(text, props.cities())) return "tx";
        return "other";
    }

    // ---- helpers -------------------------------------------------------

    private static boolean hasAny(String text, List<String> words) {
        if (words == null) return false;
        for (String w : words) {
            if (w != null && text.contains(w.toLowerCase())) return true;
        }
        return false;
    }

    private void sortNewestFirst(List<Job> list) {
        list.sort((a, b) -> {
            Instant pa = a.posted(), pb = b.posted();
            if (pa == null && pb == null) return 0;
            if (pa == null) return 1;
            if (pb == null) return -1;
            return pb.compareTo(pa);
        });
    }

    private static Map<String, List<Job>> emptyBuckets() {
        Map<String, List<Job>> m = new LinkedHashMap<>();
        for (String loc : LOCATIONS) m.put(loc, new ArrayList<>());
        return m;
    }
}

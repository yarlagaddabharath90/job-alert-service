package com.jobalert.service;

import com.jobalert.config.JobAlertProperties;
import com.jobalert.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Ties everything together: fetch -> classify -> email. Used by scheduler and REST. */
@Service
public class JobAlertRunner {

    private static final Logger log = LoggerFactory.getLogger(JobAlertRunner.class);
    private static final Map<String, String> LOC_LABELS =
            Map.of("remote", "Remote", "tx", "TX", "other", "Other");

    private final JobAggregatorService aggregator;
    private final JobFilterService filter;
    private final EmailService email;
    private final JobAlertProperties props;

    public JobAlertRunner(JobAggregatorService aggregator, JobFilterService filter,
                          EmailService email, JobAlertProperties props) {
        this.aggregator = aggregator;
        this.filter = filter;
        this.email = email;
        this.props = props;
    }

    public Map<String, Object> run() {
        Map<String, Map<String, List<Job>>> classified = filter.classify(aggregator.fetchAll());
        Map<String, List<Job>> contract = classified.get("contract");
        Map<String, List<Job>> fulltime = classified.get("fulltime");

        if (props.splitOrDefault()) {
            sendSplit("contract", "Contract / C2C", contract);
            sendSplit("fulltime", "Full-time + Sponsorship", fulltime);
        } else {
            email.deliver("Java Full Stack — Contract / C2C (" + total(contract) + ")",
                    "Java Full Stack · Contract / C2C", contract);
            email.deliver("Java Full Stack — Full-time + Sponsorship (" + total(fulltime) + ")",
                    "Java Full Stack · Full-time + Sponsorship", fulltime);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("contractTotal", total(contract));
        summary.put("fulltimeTotal", total(fulltime));
        summary.put("dryRun", props.dryRunOrDefault());
        log.info("Run summary: {}", summary);
        return summary;
    }

    /** Classified jobs without sending — used by GET /api/preview. */
    public Map<String, Map<String, List<Job>>> preview() {
        return filter.classify(aggregator.fetchAll());
    }

    private void sendSplit(String category, String friendly, Map<String, List<Job>> buckets) {
        for (var loc : LOC_LABELS.entrySet()) {
            List<Job> jobs = buckets.get(loc.getKey());
            Map<String, List<Job>> single = new LinkedHashMap<>();
            single.put(loc.getKey(), jobs);
            email.deliver(
                    "Java Full Stack — " + friendly + " — " + loc.getValue() + " (" + jobs.size() + ")",
                    "Java Full Stack · " + friendly + " · " + loc.getValue(),
                    single);
        }
    }

    private static int total(Map<String, List<Job>> buckets) {
        return buckets.values().stream().mapToInt(List::size).sum();
    }
}

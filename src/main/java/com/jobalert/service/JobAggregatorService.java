package com.jobalert.service;

import com.jobalert.model.Job;
import com.jobalert.source.JobSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Fetches from every {@link JobSource}; one failing source never kills the run. */
@Service
public class JobAggregatorService {

    private static final Logger log = LoggerFactory.getLogger(JobAggregatorService.class);

    private final List<JobSource> sources;

    public JobAggregatorService(List<JobSource> sources) {
        this.sources = sources;
    }

    public List<Job> fetchAll() {
        List<Job> all = new ArrayList<>();
        for (JobSource source : sources) {
            try {
                List<Job> got = source.fetch();
                log.info("[{}] fetched {} raw postings", source.name(), got.size());
                all.addAll(got);
            } catch (Exception e) {
                log.warn("[{}] skipped: {}", source.name(), e.getMessage());
            }
        }
        log.info("Total raw postings: {}", all.size());
        return all;
    }
}

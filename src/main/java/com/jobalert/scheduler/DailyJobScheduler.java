package com.jobalert.scheduler;

import com.jobalert.service.JobAlertRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Fires the job run automatically. Default cron = 8:00 AM in the configured zone. */
@Component
public class DailyJobScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyJobScheduler.class);

    private final JobAlertRunner runner;

    public DailyJobScheduler(JobAlertRunner runner) {
        this.runner = runner;
    }

    @Scheduled(cron = "${jobalert.cron}", zone = "${jobalert.zone}")
    public void runDaily() {
        log.info("Scheduled daily run starting");
        runner.run();
    }
}

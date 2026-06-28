package com.jobalert;

import com.jobalert.service.JobAlertRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * When {@code jobalert.run-on-startup=true}, runs the pipeline once at boot and exits.
 * Used by the GitHub Actions job so it can run headless and finish. Off by default,
 * so running normally in IntelliJ just starts the REST service + scheduler.
 */
@Component
public class StartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    private final JobAlertRunner runner;
    private final ConfigurableApplicationContext context;

    @Value("${jobalert.run-on-startup:false}")
    private boolean runOnStartup;

    public StartupRunner(JobAlertRunner runner, ConfigurableApplicationContext context) {
        this.runner = runner;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!runOnStartup) return;
        log.info("run-on-startup enabled: running pipeline once then exiting");
        runner.run();
        System.exit(SpringApplication.exit(context, () -> 0));
    }
}

package com.jobalert.web;

import com.jobalert.model.Job;
import com.jobalert.service.JobAlertRunner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * REST surface for the service.
 *   GET  /api/health   -> liveness
 *   POST /api/run      -> run the pipeline now and send the emails
 *   GET  /api/preview  -> classified jobs as JSON, without sending anything
 */
@RestController
@RequestMapping("/api")
public class JobAlertController {

    private final JobAlertRunner runner;

    public JobAlertController(JobAlertRunner runner) {
        this.runner = runner;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "time", Instant.now().toString());
    }

    @PostMapping("/run")
    public Map<String, Object> run() {
        return runner.run();
    }

    @GetMapping("/preview")
    public Map<String, Map<String, List<Job>>> preview() {
        return runner.preview();
    }
}

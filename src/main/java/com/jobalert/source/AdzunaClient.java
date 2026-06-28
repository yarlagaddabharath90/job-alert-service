package com.jobalert.source;

import com.jobalert.config.JobAlertProperties;
import com.jobalert.model.Job;
import com.jobalert.util.DateUtil;
import com.jobalert.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/** Adzuna — https://developer.adzuna.com/ (free keys). */
@Component
public class AdzunaClient implements JobSource {

    private static final Logger log = LoggerFactory.getLogger(AdzunaClient.class);
    private final RestClient http = RestClient.create();
    private final JobAlertProperties props;

    public AdzunaClient(JobAlertProperties props) {
        this.props = props;
    }

    @Override
    public String name() {
        return "Adzuna";
    }

    @Override
    public List<Job> fetch() {
        String appId = props.adzuna() == null ? null : props.adzuna().appId();
        String appKey = props.adzuna() == null ? null : props.adzuna().appKey();
        if (isBlank(appId) || isBlank(appKey)) {
            log.debug("Adzuna: appId or appKey is blank, skipping");
            return List.of();
        }

        log.debug("Adzuna: Starting fetch with {} queries", props.queries().size());
        List<Job> out = new ArrayList<>();
        for (String q : props.queries()) {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://api.adzuna.com/v1/api/jobs/" + props.country() + "/search/1")
                    .queryParam("app_id", appId)
                    .queryParam("app_key", appKey)
                    .queryParam("what", q)
                    .queryParam("max_days_old", 1)
                    .queryParam("results_per_page", 50)
                    .queryParam("content-type", "application/json")
                    .encode().build().toUri();

            log.debug("Adzuna: Fetching for query '{}' from {}", q, uri);
            Object root = http.get().uri(uri).retrieve().body(java.util.Map.class);
            if (root == null) {
                log.warn("Adzuna: No response for query '{}'", q);
                continue;
            }

            List<Object> results = Json.arr(root, "results");
            log.debug("Adzuna: Got {} results for query '{}'", results.size(), q);
            for (Object j : results) {
                out.add(new Job(
                        Json.str(j, "title"),
                        Json.str(j, "company", "display_name"),
                        Json.str(j, "location", "display_name"),
                        Json.str(j, "redirect_url"),
                        DateUtil.parse(Json.str(j, "created")),
                        Json.str(j, "description"),
                        "Adzuna",
                        false));
            }
        }
        log.debug("Adzuna: Fetch complete, total jobs: {}", out.size());
        return out;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}

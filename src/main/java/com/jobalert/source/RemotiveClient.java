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

/** Remotive — remote roles, no API key required. */
@Component
public class RemotiveClient implements JobSource {

    private static final Logger log = LoggerFactory.getLogger(RemotiveClient.class);
    private final RestClient http = RestClient.create();
    private final JobAlertProperties props;

    public RemotiveClient(JobAlertProperties props) {
        this.props = props;
    }

    @Override
    public String name() {
        return "Remotive";
    }

    @Override
    public List<Job> fetch() {
        log.debug("Remotive: Starting fetch with {} queries", props.queries().size());
        List<Job> out = new ArrayList<>();
        for (String q : props.queries()) {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://remotive.com/api/remote-jobs")
                    .queryParam("search", q)
                    .encode().build().toUri();

            log.debug("Remotive: Fetching for query '{}' from {}", q, uri);
            Object root = http.get().uri(uri).retrieve().body(java.util.Map.class);
            if (root == null) {
                log.warn("Remotive: No response for query '{}'", q);
                continue;
            }

            List<Object> jobs = Json.arr(root, "jobs");
            log.debug("Remotive: Got {} results for query '{}'", jobs.size(), q);
            for (Object j : jobs) {
                String loc = Json.str(j, "candidate_required_location");
                out.add(new Job(
                        Json.str(j, "title"),
                        Json.str(j, "company_name"),
                        loc.isBlank() ? "Remote" : loc,
                        Json.str(j, "url"),
                        DateUtil.parse(Json.str(j, "publication_date")),
                        Json.str(j, "description"),
                        "Remotive",
                        true));
            }
        }
        log.debug("Remotive: Fetch complete, total jobs: {}", out.size());
        return out;
    }
}

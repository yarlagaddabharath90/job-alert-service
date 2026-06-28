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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** JSearch via RapidAPI — aggregates Google for Jobs (LinkedIn/Indeed/etc.). */
@Component
public class JSearchClient implements JobSource {

    private static final Logger log = LoggerFactory.getLogger(JSearchClient.class);
    private final RestClient http = RestClient.create();
    private final JobAlertProperties props;

    public JSearchClient(JobAlertProperties props) {
        this.props = props;
    }

    @Override
    public String name() {
        return "JSearch";
    }

    @Override
    public List<Job> fetch() {
        String key = props.rapidapiKey();
        if (key == null || key.isBlank()) {
            log.debug("JSearch: rapidapi key is blank, skipping");
            return List.of();
        }

        log.debug("JSearch: Starting fetch with {} queries", props.queries().size());
        List<Job> out = new ArrayList<>();
        for (String q : props.queries()) {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://jsearch.p.rapidapi.com/search")
                    .queryParam("query", q + " in USA")
                    .queryParam("date_posted", "today")
                    .queryParam("num_pages", 1)
                    .encode().build().toUri();

            log.debug("JSearch: Fetching for query '{}' from {}", q, uri);
            Object root = http.get().uri(uri)
                    .header("X-RapidAPI-Key", key)
                    .header("X-RapidAPI-Host", "jsearch.p.rapidapi.com")
                    .retrieve().body(java.util.Map.class);
            if (root == null) {
                log.warn("JSearch: No response for query '{}'", q);
                continue;
            }

            List<Object> data = Json.arr(root, "data");
            log.debug("JSearch: Got {} results for query '{}'", data.size(), q);
            for (Object j : data) {
                String loc = Stream.of(
                                Json.str(j, "job_city"),
                                Json.str(j, "job_state"),
                                Json.str(j, "job_country"))
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.joining(", "));
                out.add(new Job(
                        Json.str(j, "job_title"),
                        Json.str(j, "employer_name"),
                        loc,
                        Json.str(j, "job_apply_link"),
                        DateUtil.parse(Json.str(j, "job_posted_at_datetime_utc")),
                        Json.str(j, "job_description"),
                        "JSearch",
                        Json.bool(j, "job_is_remote")));
            }
        }
        log.debug("JSearch: Fetch complete, total jobs: {}", out.size());
        return out;
    }
}

package com.jobalert.source;

import com.jobalert.config.JobAlertProperties;
import com.jobalert.model.Job;
import com.jobalert.util.DateUtil;
import com.jobalert.util.Json;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/** Adzuna — https://developer.adzuna.com/ (free keys). */
@Component
public class AdzunaClient implements JobSource {

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
        if (isBlank(appId) || isBlank(appKey)) return List.of();

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

            Object root = http.get().uri(uri).retrieve().body(java.util.Map.class);
            if (root == null) continue;

            for (Object j : Json.arr(root, "results")) {
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
        return out;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}

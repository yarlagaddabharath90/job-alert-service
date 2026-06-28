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

/** Remotive — remote roles, no API key required. */
@Component
public class RemotiveClient implements JobSource {

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
        List<Job> out = new ArrayList<>();
        for (String q : props.queries()) {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://remotive.com/api/remote-jobs")
                    .queryParam("search", q)
                    .encode().build().toUri();

            Object root = http.get().uri(uri).retrieve().body(java.util.Map.class);
            if (root == null) continue;

            for (Object j : Json.arr(root, "jobs")) {
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
        return out;
    }
}

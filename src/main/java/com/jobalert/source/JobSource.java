package com.jobalert.source;

import com.jobalert.model.Job;

import java.util.List;

/** A job portal client. Implementations are auto-discovered as Spring beans. */
public interface JobSource {
    List<Job> fetch();
    String name();
}

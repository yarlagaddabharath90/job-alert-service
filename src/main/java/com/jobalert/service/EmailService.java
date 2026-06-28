package com.jobalert.service;

import com.jobalert.config.JobAlertProperties;
import com.jobalert.model.Job;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Builds the HTML emails and either sends them or (in dry-run) writes them to ./out. */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final Map<String, String> LABELS = Map.of(
            "remote", "Remote", "tx", "Texas (TX)", "other", "Other locations");

    private final JavaMailSender mailSender;
    private final JobAlertProperties props;
    private final String from;

    public EmailService(JavaMailSender mailSender,
                        JobAlertProperties props,
                        @Value("${spring.mail.username:}") String from) {
        this.mailSender = mailSender;
        this.props = props;
        this.from = from;
    }

    public void deliver(String subject, String title, Map<String, List<Job>> locationGroups) {
        String html = buildHtml(title, locationGroups);
        if (props.dryRunOrDefault()) {
            writePreview(subject, html);
        } else {
            send(subject, html);
        }
    }

    private void send(String subject, String html) {
        try {
            String[] to = Arrays.stream(props.emailTo().split(","))
                    .map(String::trim).filter(s -> !s.isBlank()).toArray(String[]::new);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("[email] sent: {}", subject);
        } catch (Exception e) {
            log.error("[email] failed to send '{}': {}", subject, e.getMessage());
        }
    }

    private void writePreview(String subject, String html) {
        try {
            Path dir = Path.of("out");
            Files.createDirectories(dir);
            String safe = subject.replaceAll("[\\\\/]", "-").replaceAll("\\s+", "_");
            Path file = dir.resolve(safe + ".html");
            Files.writeString(file, html);
            log.info("[dry-run] wrote {}", file.toAbsolutePath());
        } catch (Exception e) {
            log.error("[dry-run] failed to write preview: {}", e.getMessage());
        }
    }

    // ---- HTML --------------------------------------------------------------

    private String buildHtml(String title, Map<String, List<Job>> groups) {
        int total = groups.values().stream().mapToInt(List::size).sum();
        StringBuilder sections = new StringBuilder();
        for (String key : JobFilterService.LOCATIONS) {
            sections.append(section(LABELS.get(key), groups.getOrDefault(key, List.of())));
        }
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE dd MMM yyyy"));
        return """
                <html><body style="font-family:Arial,Helvetica,sans-serif;color:#222;max-width:640px;margin:auto;">
                  <h2 style="margin-bottom:0;">%s</h2>
                  <p style="color:#888;margin-top:4px;font-size:13px;">%s &middot; %d new role(s) &middot; Java Full Stack &middot; 6+ yrs</p>
                  %s
                  <p style="color:#bbb;font-size:11px;margin-top:30px;">
                    Auto-generated. C2C / sponsorship detected via posting text and may be approximate — verify on the listing.
                  </p>
                </body></html>
                """.formatted(title, today, total, sections);
    }

    private String section(String label, List<Job> jobs) {
        String body;
        if (jobs.isEmpty()) {
            body = "<p style=\"color:#999;font-size:13px;\">No new roles in the last 24h.</p>";
        } else {
            StringBuilder rows = new StringBuilder("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">");
            jobs.forEach(j -> rows.append(row(j)));
            rows.append("</table>");
            body = rows.toString();
        }
        return """
                <h3 style="margin:24px 0 6px;font-size:16px;border-left:4px solid #1155cc;padding-left:8px;">
                  %s <span style="color:#999;font-weight:400;">(%d)</span>
                </h3>
                %s
                """.formatted(label, jobs.size(), body);
    }

    private String row(Job j) {
        String when = "";
        if (j.posted() != null) {
            long hrs = Duration.between(j.posted(), Instant.now()).toHours();
            when = hrs >= 1 ? hrs + "h ago" : "just now";
        }
        return """
                <tr><td style="padding:10px 0;border-bottom:1px solid #eee;">
                  <a href="%s" style="font-size:15px;font-weight:600;color:#1155cc;text-decoration:none;">%s</a><br>
                  <span style="color:#444;font-size:13px;">%s &middot; %s</span><br>
                  <span style="color:#888;font-size:12px;">%s &middot; %s</span>
                  &nbsp;<a href="%s" style="font-size:12px;color:#0b8043;">Apply &rarr;</a>
                </td></tr>
                """.formatted(j.url(), j.title(), j.company(), j.location(), j.source(), when, j.url());
    }
}

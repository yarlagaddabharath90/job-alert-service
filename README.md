# Java Full Stack Job Alert — Spring Boot REST Service

A Spring Boot service that, every morning, pulls Java Full Stack postings from the last 24h
across several job portals, splits them into **Contract / C2C** and **Full-time + Sponsorship**,
groups them by **Remote / Texas / Other**, and emails them. Also exposes a small REST API to
trigger or preview runs on demand.

- **Spring Boot 4.0.x**, **Java 17+**, Gradle. (Latest GA is 4.1.0 — bump `version` in `build.gradle`.)
- `@Scheduled` daily run + REST endpoints (`/api/health`, `/api/run`, `/api/preview`).
- Sources: **Adzuna**, **JSearch** (RapidAPI, aggregates Google for Jobs incl. LinkedIn/Indeed),
  **Remotive** (remote roles). Add more by implementing the `JobSource` interface.

## On the word "microservices"
A daily alert bot doesn't need multiple deployable services — that would add a gateway,
service discovery, and inter-service calls for no real benefit, and make it much harder to run.
So this is built as **one well-structured Spring Boot service** with cleanly separated components.
If you genuinely want it split later, the natural seams are already there: `source/` → a
*fetcher service*, `service/JobFilterService` → a *classifier service*, `service/EmailService`
→ a *notification service*, talking over REST or a message queue. Start here; split only if you
have a reason to.

## ⚠️ Honest limitations (same as any job scraper)
1. **No API covers every portal.** LinkedIn/Indeed block direct API access; their listings come
   indirectly via JSearch. Add sources as needed.
2. **"Sponsorship" and "C2C" are not real fields** — they're detected by scanning the posting
   text for keywords (tunable in `application.yml`). Verify on the actual listing.
3. **"6+ years" can't be filtered reliably**, so obviously-junior titles are excluded instead.

## Run it
See **RUN_IN_INTELLIJ.md** for click-by-click setup. Quick version:

```bash
# 1. set your secrets as environment variables (or use the IntelliJ run config)
export EMAIL_USER=you@gmail.com EMAIL_PASS=app_password EMAIL_TO=you@gmail.com
export ADZUNA_APP_ID=... ADZUNA_APP_KEY=... RAPIDAPI_KEY=...

# 2. preview without sending
./gradlew bootRun --args='--jobalert.run-on-startup=true --spring.main.web-application-type=none --jobalert.dry-run=true'

# 3. trigger a real run via REST while it's running
curl -X POST http://localhost:8080/api/run
```

The service also runs itself automatically at the cron time in `application.yml`
(default 8:00 AM `America/Chicago`).

## REST API
| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/health` | liveness check |
| POST | `/api/run` | run pipeline now and send emails (respects `DRY_RUN`) |
| GET | `/api/preview` | classified jobs as JSON, nothing sent |

## Configuration
Everything tunable lives under `jobalert:` in `src/main/resources/application.yml` — queries,
title rules, junior exclusions, TX cities, and the C2C / sponsorship keyword lists. Secrets come
from environment variables so nothing sensitive is committed.

Email layout: `jobalert.split-locations-into-separate-emails`
- `false` (default) → **2 emails** (Contract, Full-time), each with Remote/TX/Other sections.
- `true` → **6 emails** (each category × each location).

## Scheduling at 8 AM
- **GitHub Actions** (`.github/workflows/daily.yml`) — builds the jar and runs once in the cloud,
  even with your computer off. Add your secrets in repo Settings. Cron is UTC.
- **Local / server** — keep the app running (the built-in `@Scheduled` fires daily), or run the
  jar from your OS scheduler with `--jobalert.run-on-startup=true --spring.main.web-application-type=none`.

See **PAUSE_AND_STOP.md** for pausing, taking a break, or stopping alerts.

## Project layout
```
src/main/java/com/jobalert/
├─ JobAlertApplication.java      app entry (+ scheduling, config scan)
├─ StartupRunner.java            one-shot run-and-exit mode for CI
├─ config/JobAlertProperties     type-safe settings binding
├─ model/Job                     normalized posting record
├─ source/                       Adzuna / JSearch / Remotive clients
├─ service/                      aggregator, filter, email, runner
├─ scheduler/DailyJobScheduler   @Scheduled 8 AM trigger
└─ web/JobAlertController        REST endpoints
```
## OutPut

<img width="2054" height="439" alt="image" src="https://github.com/user-attachments/assets/35b27205-3ce9-4d1a-9816-f30994804a27" />
<img width="1386" height="826" alt="image" src="https://github.com/user-attachments/assets/3042923c-671d-451b-9513-619f804089a4" />
<img width="2060" height="1121" alt="image" src="https://github.com/user-attachments/assets/0d80e532-474a-4f74-b2c7-a25872be1649" />


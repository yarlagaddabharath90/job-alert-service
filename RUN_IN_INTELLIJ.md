# ▶ Run this in IntelliJ IDEA — easy steps

This is a **Spring Boot (Java 21, Maven)** project — IntelliJ IDEA's home turf.

## Step 0 — One-time prerequisites
- **JDK 21** — IntelliJ can install it for you (Step 2 below), or get Temurin 21 from https://adoptium.net/
- **IntelliJ IDEA** — Community Edition is fine for this (Maven + Spring `@Scheduled` all work).
  Ultimate adds nicer Spring tooling but isn't required.

## Step 1 — Open the project
**File → Open…** → select the `job-alert-service` folder (the one with `pom.xml`) → **Open**.
IntelliJ detects Maven and starts downloading dependencies automatically. Wait for the progress
bar at the bottom to finish (first time needs internet — a few minutes).

## Step 2 — Make sure a JDK 21 is selected
**File → Project Structure → Project** → set **SDK** to a 21 JDK.
(If none listed: the SDK dropdown → **Add SDK → Download JDK → version 21 → Temurin**.)

## Step 3 — Add your credentials
Two easy options — pick one:

**Option A — use the ready-made run config (simplest).**
A run config named **JobAlertApplication** ships with the project (top-right dropdown).
Click it → **Edit Configurations…** → fill in the **Environment variables** values
(EMAIL_USER, EMAIL_PASS, EMAIL_TO, ADZUNA_APP_ID, ADZUNA_APP_KEY, RAPIDAPI_KEY).

**Option B — a local file.**
Create `src/main/resources/application-local.yml` (it's git-ignored), put your real values there,
and add `--spring.profiles.active=local` to the run config's *Program arguments*.

Where the keys come from:
- Gmail **App Password** (not your login): https://myaccount.google.com/apppasswords
- Adzuna (free): https://developer.adzuna.com/
- JSearch (free tier, RapidAPI): https://rapidapi.com/letscrape-6bRBa3QguO5/api/jsearch

## Step 4 — Run it
Top-right run dropdown has two configs:

- **JobAlertApplication (DRY RUN - preview only)** ← start here.
  Starts the app with `DRY_RUN=true`. Hit `POST /api/run` (below) and it writes the emails to an
  `out/` folder instead of sending. Open the `.html` files to check them.
- **JobAlertApplication** ← the real one. Sends the two emails.

Press the green ▶. The app starts on **http://localhost:8080**.

### Trigger / preview from the IDE terminal
```bash
curl http://localhost:8080/api/health           # is it up?
curl -X POST http://localhost:8080/api/run       # run now + send (or preview if DRY_RUN)
curl http://localhost:8080/api/preview           # see matched jobs as JSON, nothing sent
```

> Prefer not to use the run configs? **Run → Edit Configurations → + → Spring Boot**,
> Main class = `com.jobalert.JobAlertApplication`, add your env vars, Run.

## What "running" means here
While the app is running it (a) serves the REST API and (b) fires the daily 8 AM job on its own.
Close the app and neither happens — so for true unattended automation, use **GitHub Actions**
(`.github/workflows/daily.yml`) or your OS scheduler. See `PAUSE_AND_STOP.md`.

## Troubleshooting
- **Dependencies won't resolve** → check internet, then **Maven tool window → Reload**.
- **"invalid target release: 21"** → JDK 21 not selected (Step 2).
- **Mail auth error** → use a Gmail *App Password*, not your normal password.
- **Empty emails** → normal if nothing matched in the last 24h; try DRY RUN and broaden the
  queries/keywords in `application.yml`, and confirm your API keys are set.

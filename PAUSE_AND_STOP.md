# ⏸ Automation, taking a break & stopping alerts

## Do I have to run it every day myself?
Depends how you run it:

| Mode | Automatic? | Needs your computer on? |
|------|-----------|--------------------------|
| **IntelliJ ▶ (app running)** | Partly — fires 8 AM **only while the app stays running** | Yes |
| **GitHub Actions** (recommended) | ✅ Yes, daily at 8 AM | ❌ No — runs in the cloud |
| **OS scheduler** running the jar | ✅ Yes, daily | Yes — only if machine is on |

The built-in `@Scheduled` 8 AM trigger works **only while the Spring Boot app is actually
running**. If you close IntelliJ, nothing fires — that's why GitHub Actions is the reliable
"every day, computer off" option.

---

## 🟡 Take a break (pause, resume later)

### GitHub Actions ← easiest
**Pause:** repo on github.com → **Actions** tab → **daily-job-alert** → top-right **`···` → Disable workflow.**
**Resume:** same place → **Enable workflow.** Nothing is deleted.

### App running locally / on a server
Just stop the app (red ■ in IntelliJ, or `Ctrl+C` in the terminal). Start it again when you want it back.

### OS scheduler (cron / Task Scheduler)
- cron: `crontab -e`, put `#` in front of the line to pause, remove it to resume.
- Windows Task Scheduler: right-click the task → **Disable** / **Enable**.

---

## 🔴 Stop permanently
- **GitHub Actions:** Disable the workflow and leave it off, or delete
  `.github/workflows/daily.yml`, or delete the repo.
- **Local app:** stop it and don't restart it.
- **OS scheduler:** delete the cron line / scheduled task.

---

## ✋ Want fewer emails, not zero?
Edit the cron in **`.github/workflows/daily.yml`** (UTC):
- Weekdays only: `- cron: "0 13 * * 1-5"`
- Every 3 days: `- cron: "0 13 */3 * *"`

Or change the in-app schedule in `application.yml` → `jobalert.cron`
(Spring format: `second minute hour day month weekday`, e.g. `0 0 8 * * MON-FRI`).

To temporarily mute without losing matches, set `DRY_RUN=true` — it keeps running and writing
previews to `./out` but sends no email.

---

## ⚠️ One thing GitHub does on its own
GitHub **auto-pauses** a scheduled workflow after **60 days with no repository activity**.
If alerts go quiet for that reason, open the **Actions** tab and click **Enable workflow**
(or push any commit) to restart it.

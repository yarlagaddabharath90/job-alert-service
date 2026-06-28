# 🔐 Setting Up Credentials for Job Alert Service

This guide walks you through getting all required credentials to run the Job Alert Service.

---

## 📧 Email Credentials (Gmail SMTP)

The app sends job alert emails through Gmail's SMTP server. You'll need a **Gmail App Password** (NOT your regular password).

### Step 1: Enable 2-Step Verification (if not already enabled)
1. Go to https://myaccount.google.com/security
2. Find **"2-Step Verification"** in the left menu
3. Click it → **Get started**
4. Follow the prompts (you'll verify with your phone)

### Step 2: Generate Gmail App Password
1. Go to https://myaccount.google.com/apppasswords
2. You should now see the **"App passwords"** option (only appears if 2-Step Verification is ON)
3. Select:
   - **App:** Mail
   - **Device:** Windows Computer (or any option)
4. Click **"Generate"**
5. Google will display a **16-character password** with spaces

### Step 3: Copy Your Credentials
You now have:
- **EMAIL_USER** = your Gmail address (e.g., `yourname@gmail.com`)
- **EMAIL_PASS** = the 16-char app password (copy exactly with spaces)
- **EMAIL_TO** = recipient email (can be same as EMAIL_USER or different)

Example:
```
EMAIL_USER=john.doe@gmail.com
EMAIL_PASS=abcd efgh ijkl mnop
EMAIL_TO=john.doe@gmail.com
```

### ⚠️ Important Security Notes
- **NEVER use your regular Gmail password** — always use the App Password
- The App Password only works for SMTP sending, not for logging into Gmail
- You can revoke it anytime at https://myaccount.google.com/apppasswords
- The app never stores or logs your password

---

## 💼 Adzuna API Credentials

**Adzuna** is a job board aggregator that provides the largest job database for the app.

### Step 1: Register for Free
1. Go to https://developer.adzuna.com/
2. Click **"Sign up"** (or **"Log in"** if you have an account)
3. Fill in your details and complete registration

### Step 2: Create an Application
1. After logging in, go to **"My Applications"** or **"Create Application"**
2. Give your app a name (e.g., "Job Alert Bot")
3. Accept terms and create

### Step 3: Copy Your Credentials
You'll see:
- **App ID** → copy this as `ADZUNA_APP_ID`
- **App Key** → copy this as `ADZUNA_APP_KEY`

Example:
```
ADZUNA_APP_ID=b799af7e
ADZUNA_APP_KEY=15a677aa8b05da07b27e02f18c65976f
```

### 📊 What Adzuna Provides
- 69+ job boards aggregated
- Fast, reliable API
- Free tier is perfect for this use case
- Covers most of your job matches

---

## 🔍 RapidAPI / JSearch Credentials

**JSearch** aggregates jobs from Google for Jobs, LinkedIn, Indeed, and other platforms.

### Step 1: Register for Free
1. Go to https://rapidapi.com/letscrape-6bRBa3QguO5/api/jsearch
2. Click **"Sign up"** (or **"Log in"** if you have an account)
3. Complete registration with email verification

### Step 2: Subscribe to Free Tier
1. After login, you should be on the JSearch API page
2. Click **"Subscribe to Test"** (free tier)
3. Agree to terms

### Step 3: Copy Your API Key
1. Go to **"Code Snippets"** or **"Endpoints"** tab
2. Look for **"X-RapidAPI-Key"** header
3. Copy the value after the colon → this is your `RAPIDAPI_KEY`

Example:
```
RAPIDAPI_KEY=abc123def456xyz789
```

### 📊 What JSearch Provides
- Google for Jobs aggregator
- LinkedIn job listings
- Indeed job listings
- Covers positions not found in Adzuna

### ⚠️ Rate Limits
- Free tier has limits (usually 100 requests/month)
- If you hit the limit, the app gracefully falls back to Adzuna + Remotive
- Paid tiers available if you need more

---

## 🌍 Remotive Credentials (No Key Needed!)

**Remotive** is a remote-first job board. **No API key required** — it's completely free and public.

The app will automatically fetch from Remotive without any credentials.

---

## ⚙️ Adding Credentials to IntelliJ

Once you have all your credentials, add them to the run configuration:

### In IntelliJ IDEA:
1. Click **Run → Edit Configurations…**
2. Select **"JobAlertApplication"** (or create a new Gradle config)
3. Find the **"Environment variables"** section
4. Click the button next to it to open the editor
5. Add all these variables:

```
EMAIL_USER=your_gmail@gmail.com
EMAIL_PASS=xxxx xxxx xxxx xxxx
EMAIL_TO=recipient@gmail.com
ADZUNA_APP_ID=your_adzuna_app_id
ADZUNA_APP_KEY=your_adzuna_app_key
RAPIDAPI_KEY=your_rapidapi_key
DRY_RUN=false
```

6. Click **Apply** → **OK**
7. Run the app with the green ▶ button

---

## 🧪 Test Your Setup

Once all credentials are set:

```bash
# Check if the app is running (should return UP)
curl http://localhost:8081/api/health

# Preview jobs without sending emails (dry-run)
curl http://localhost:8081/api/preview

# Send job alert emails (will fetch jobs and email)
curl -X POST http://localhost:8081/api/run
```

### Expected Output
- `curl -X POST http://localhost:8081/api/run` should return:
  ```json
  {"contractTotal": X, "fulltimeTotal": Y, "dryRun": false}
  ```
  where X and Y are numbers > 0 (if jobs are found)

- Check your email inbox for the Job Alert emails!

---

## 🐛 Troubleshooting

### "Authentication failed" when sending emails
- ❌ Using regular Gmail password instead of App Password
- ❌ App Password is incorrect or expired
- ✅ Solution: Get a new App Password from https://myaccount.google.com/apppasswords

### "contractTotal": 0, "fulltimeTotal": 0
- ❌ Invalid Adzuna or RapidAPI credentials
- ❌ API rate limit hit
- ✅ Solution: Verify credentials are correct. Check the logs: `tail -50 app.log | grep -i "error\|warn"`

### "JSearch skipped: 404 Not Found"
- API endpoint may have changed (rare)
- Not critical — Adzuna + Remotive still provide jobs
- Solution: Check if RapidAPI subscription is active

---

## 🔗 Quick Links

| Service | Link | What You Get |
|---------|------|-------------|
| **Gmail App Password** | https://myaccount.google.com/apppasswords | `EMAIL_PASS` |
| **Adzuna API** | https://developer.adzuna.com/ | `ADZUNA_APP_ID`, `ADZUNA_APP_KEY` |
| **RapidAPI / JSearch** | https://rapidapi.com/letscrape-6bRBa3QguO5/api/jsearch | `RAPIDAPI_KEY` |
| **Remotive** | https://remotive.com/ | (No key needed) |

---

## ✅ Checklist

- [ ] Gmail 2-Step Verification enabled
- [ ] Gmail App Password generated
- [ ] EMAIL_USER, EMAIL_PASS, EMAIL_TO set in run config
- [ ] Adzuna account created
- [ ] ADZUNA_APP_ID and ADZUNA_APP_KEY copied to run config
- [ ] RapidAPI account created and JSearch subscribed
- [ ] RAPIDAPI_KEY copied to run config
- [ ] DRY_RUN set to `false` in run config
- [ ] App restarted after adding credentials
- [ ] Test: `curl -X POST http://localhost:8081/api/run` returns jobs

Once all checked, you're ready to receive daily job alerts! 🎉

# Deploy PRANA-MESH Backend to Google Cloud Run

## Prerequisites
- Google Cloud account (free tier: $300 credit)
- Python installed
- Your backend code ready

---

## Step 1: Install Google Cloud SDK

### Download and Install
1. Go to: https://cloud.google.com/sdk/docs/install
2. Download for **Windows**
3. Run installer, follow prompts
4. Restart terminal after installation

### Initialize gcloud
```bash
gcloud init
```
- Login with your Google account
- Select/Create a project (e.g., `prana-mesh-2026`)

---

## Step 2: Enable Required APIs

```bash
# Enable Cloud Run API
gcloud services enable run.googleapis.com

# Enable Container Registry API
gcloud services enable containerregistry.googleapis.com

# Enable Cloud Build API (for building container)
gcloud services enable cloudbuild.googleapis.com
```

---

## Step 3: Deploy to Cloud Run (Easiest Method)

Google Cloud Run can now deploy directly from source - no Docker needed!

```bash
cd C:\Users\LENOVO\Documents\finproject\backend

gcloud run deploy prana-mesh-backend ^
  --source . ^
  --platform managed ^
  --region us-central1 ^
  --allow-unauthenticated ^
  --memory 512Mi ^
  --timeout 300 ^
  --set-env-vars GEMINI_API_KEY=your_key_here
```

**Notes:**
- Replace `your_key_here` with actual Gemini API key (or remove that flag)
- `--allow-unauthenticated` makes it publicly accessible (needed for Streamlit)
- First deployment takes ~5 minutes

### Get Your URL
After deployment, you'll see:
```
Service [prana-mesh-backend] revision [revision-name] has been deployed and is serving 100 percent of traffic.
Service URL: https://prana-mesh-backend-xyz.run.app
```

**Copy this URL!**

---

## Step 4: Test Backend

```bash
curl https://YOUR_URL.run.app/health
```

Should return:
```json
{"status": "ok", "timestamp": 1234567890}
```

---

## Step 5: Update Streamlit Cloud

1. Go to https://streamlit.io/cloud
2. Click your app → **Settings**
3. Scroll to **Secrets** or **Environment Variables**
4. Add:
   ```
   BACKEND_URL = https://prana-mesh-backend-xyz.run.app
   ```
5. Click **Redeploy**

---

## Step 6: Verify Dashboard

Open your Streamlit Cloud URL - should now show live data!

---

## Cost Estimate

Google Cloud Run **free tier**:
- 2 million requests/month free
- 180,000 vCPU-seconds/month free
- 360,000 GB-seconds memory/month free

For a demo app: **~$0-2/month**

---

## Alternative: Deploy with Docker (If source deploy fails)

### Create Dockerfile (already exists in backend folder)

### Build and Push
```bash
cd C:\Users\LENOVO\Documents\finproject\backend

# Configure Docker for GCP
gcloud auth configure-docker

# Build container
gcloud builds submit --tag gcr.io/[PROJECT-ID]/prana-mesh-backend

# Deploy from container
gcloud run deploy prana-mesh-backend ^
  --image gcr.io/[PROJECT-ID]/prana-mesh-backend ^
  --platform managed ^
  --region us-central1 ^
  --allow-unauthenticated
```

---

## Troubleshooting

### Error: "Permission denied"
```bash
gcloud auth application-default login
```

### Error: "API not enabled"
```bash
gcloud services enable run.googleapis.com containerregistry.googleapis.com cloudbuild.googleapis.com
```

### Database Issues (SQLite)
Cloud Run is serverless - SQLite file won't persist. 

**Option A: Use in-memory for demo**
In `database.py`:
```python
SQLALCHEMY_DATABASE_URL = "sqlite:///:memory:"
```

**Option B: Use Cloud SQL (Production)**
```bash
gcloud sql instances create prana-mesh-db --database-version=POSTGRES_14 --tier=db-f1-micro --region=us-central1
```

**Option C: Use Supabase (Free, Easy)**
1. Go to https://supabase.com
2. Create free project
3. Get connection string
4. Set as `DATABASE_URL` env var

---

## Commands Summary

```bash
# 1. Install gcloud (one-time)
# Download from https://cloud.google.com/sdk/docs/install

# 2. Login
gcloud auth login

# 3. Set project
gcloud config set project YOUR_PROJECT_ID

# 4. Enable APIs
gcloud services enable run.googleapis.com containerregistry.googleapis.com cloudbuild.googleapis.com

# 5. Deploy
cd C:\Users\LENOVO\Documents\finproject\backend
gcloud run deploy prana-mesh-backend --source . --platform managed --region us-central1 --allow-unauthenticated

# 6. Get URL
gcloud run services describe prana-mesh-backend --platform managed --region us-central1 --format="value(status.url)"
```

---

## For Demo Day

1. **Deploy backend to Cloud Run** (do this 1 day before)
2. **Update Streamlit Cloud** with backend URL
3. **Test end-to-end** before presentation
4. **Keep localhost as backup** in case of any issues

Good luck! 🚀

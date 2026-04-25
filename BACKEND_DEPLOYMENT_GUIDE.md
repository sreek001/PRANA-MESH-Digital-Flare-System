# Deploy Backend for Streamlit Cloud

## Problem
Your Streamlit Cloud dashboard shows "Cannot connect to backend" because it can't reach `http://localhost:8000` on your computer.

## Solution: Deploy Backend to a Public URL

---

## OPTION 1: Render.com (Easiest - Free)

### Step 1: Create Render Account
1. Go to https://render.com
2. Sign up with GitHub

### Step 2: Create Web Service
1. Click **New +** → **Web Service**
2. Connect your GitHub repo
3. Configure:
   - **Name:** prana-mesh-backend
   - **Region:** Choose closest to you
   - **Branch:** main
   - **Root Directory:** `backend`
   - **Runtime:** Python 3
   - **Build Command:** `pip install -r requirements.txt`
   - **Start Command:** `uvicorn main:app --host 0.0.0.0 --port $PORT`

### Step 3: Environment Variables
Add these in Render dashboard:
```
GEMINI_API_KEY=your_key_here (optional)
```

### Step 4: Get Your URL
After deployment, you'll get:
```
https://prana-mesh-backend.onrender.com
```

### Step 5: Update Streamlit Cloud
1. Go to your Streamlit Cloud dashboard
2. Click your app → **Settings**
3. Add environment variable:
   ```
   BACKEND_URL = https://prana-mesh-backend.onrender.com
   ```
4. Save and redeploy

---

## OPTION 2: Railway.app (Alternative - Free)

### Step 1: Create Railway Account
1. Go to https://railway.app
2. Sign in with GitHub

### Step 2: New Project
1. Click **New Project**
2. Select **Deploy from GitHub repo**
3. Choose your repo

### Step 3: Configure
1. Click your service → **Variables**
2. Set `PORT = 8000`
3. Add `GEMINI_API_KEY` if using

### Step 4: Get URL
After deployment:
```
https://your-app.railway.app
```

### Step 5: Update Streamlit
Same as Step 5 above

---

## OPTION 3: ngrok (Quick Demo - Temporary)

### Install ngrok
```cmd
choco install ngrok
```

### Start Tunnel
```cmd
ngrok http 8000
```

### Get URL
ngrok will show:
```
Forwarding: https://abc123-xyz.ngrok.io -> http://localhost:8000
```

### Update Streamlit
Set `BACKEND_URL = https://abc123-xyz.ngrok.io`

**Note:** ngrok URL changes every restart. Free tier has 2-hour limits.

---

## OPTION 4: Google Cloud Run (Production Ready)

### Prerequisites
- Google Cloud account
- gcloud CLI installed

### Deploy
```bash
cd C:\Users\LENOVO\Documents\finproject\backend

# Login
gcloud auth login

# Set project
gcloud config set project YOUR_PROJECT_ID

# Enable APIs
gcloud services enable run.googleapis.com containerregistry.googleapis.com

# Build and deploy
gcloud run deploy prana-mesh-backend \
  --source . \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars GEMINI_API_KEY=your_key
```

### Get URL
```
https://prana-mesh-backend-xyz.run.app
```

---

## After Deploying Backend

### 1. Test Backend URL
Open in browser:
```
https://YOUR_BACKEND_URL/health
```
Should show: `{"status": "ok", "timestamp": ...}`

### 2. Update Streamlit Cloud
1. Go to https://streamlit.io/cloud
2. Select your app → **Settings**
3. Under **Secrets** or **Environment Variables**, add:
   ```
   BACKEND_URL=https://YOUR_BACKEND_URL
   ```
4. Click **Redeploy**

### 3. Verify Dashboard
Open your Streamlit Cloud URL - it should now connect!

---

## Quick Troubleshooting

**Backend returns 404:**
- Check your deployment logs
- Ensure `main:app` is the correct module

**CORS errors:**
Add this to `main.py`:
```python
from fastapi.middleware.cors import CORSMiddleware

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # For demo; restrict in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
```

**Database errors:**
- Use SQLite for simple demo: Change `database.py` to use local file
- Or use a cloud database like Supabase/PlanetScale

---

## Recommended for Demo Today

**Use Render.com** - It's:
- ✅ Free
- ✅ No credit card needed
- ✅ Deploys in 5 minutes
- ✅ Permanent URL (doesn't change)
- ✅ Auto-deploys on git push

Good luck! 🚀

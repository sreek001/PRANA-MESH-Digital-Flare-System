# PRANA-MESH - How to Run the App

Quick start guide for running the complete PRANA-MESH distress signal system.

---

## System Architecture

```
┌─────────────────┐      BLE       ┌─────────────────┐     HTTP      ┌─────────────────┐
│   GHOST NODE    │ ────────────►  │  SENTINEL NODE  │ ───────────►  │  COMMAND CENTER │
│  (Distress App) │   24-byte pkt  │   (Relay App)   │  POST /report │  (Backend + UI) │
│  Android Device │                │  Android Device │               │  FastAPI + Maps │
└─────────────────┘                └─────────────────┘               └─────────────────┘
```

---

## Prerequisites Checklist

- [ ] MySQL Server installed and running
- [ ] Python virtual environments set up for backend and dashboard
- [ ] Android APKs built (ghost-app and sentinel-app)
- [ ] Two Android devices with Bluetooth (for testing)

---

## Step 1: Start the Backend Server

**Option A: Using the startup script (Recommended)**
```powershell
cd C:\Users\LENOVO\Documents\finproject
.\start-backend.bat
```

**Option B: Manual start**
```powershell
cd C:\Users\LENOVO\Documents\finproject\backend
venv\Scripts\activate
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

**Verify backend is running:**
- Open browser: http://localhost:8000/health
- Should show: `{"status": "ok", "timestamp": ...}`
- API Docs: http://localhost:8000/docs

---

## Step 2: Start the Dashboard

**Open a NEW terminal window**, then:

**Option A: Using the startup script (Recommended)**
```powershell
cd C:\Users\LENOVO\Documents\finproject
.\start-dashboard.bat
```

**Option B: Manual start**
```powershell
cd C:\Users\LENOVO\Documents\finproject\dashboard
venv\Scripts\activate
streamlit run dashboard.py
```

**Dashboard opens at:** http://localhost:8501

---

## Step 3: Install Mobile Apps on Devices

### APK Locations
| App | Location |
|-----|----------|
| Ghost | `mobile\ghost-app\app\build\outputs\apk\debug\app-debug.apk` |
| Sentinel | `mobile\sentinel-app\app\build\outputs\apk\debug\app-debug.apk` |

### Install via USB (with ADB)
```powershell
adb install mobile\ghost-app\app\build\outputs\apk\debug\app-debug.apk
adb install mobile\sentinel-app\app\build\outputs\apk\debug\app-debug.apk
```

### Install via Direct Transfer
1. Copy APK files to your Android devices
2. On device, tap the APK file to install
3. Enable "Install from Unknown Sources" if prompted

---

## Step 4: Configure and Run Mobile Apps

### Ghost App (Device A - Distress Broadcaster)
1. Install and open the app
2. Grant all permissions:
   - Location (set to "Always Allow")
   - Bluetooth permissions
   - Notifications (Android 13+)
3. Press the **PANIC** button to activate distress mode
4. App starts broadcasting BLE signals

### Sentinel App (Device B - Relay)
1. Install and open the app
2. Grant all permissions (same as Ghost)
3. Press **START** to begin BLE scanning
4. When Ghost is detected, signals are sent to backend

> **Note:** The Sentinel app is already configured with your IP: `10.121.124.131:8000`

---

## Step 5: Watch the Dashboard

1. Open http://localhost:8501 in your browser
2. You should see:
   - Total Signals count
   - Active Nodes count
   - Live map with signal locations
   - Signal table with details

---

## Testing the Flow

1. **Start Backend** → Keep terminal open
2. **Start Dashboard** → Keep terminal open, open browser
3. **Ghost App** → Press PANIC button
4. **Sentinel App** → Press START button
5. **Watch Dashboard** → Signals should appear within seconds

---

## Troubleshooting

### Backend won't start
```
Error: Database connection failed
```
**Fix:** Ensure MySQL is running and database exists:
```sql
CREATE DATABASE prana_mesh;
```

### Dashboard shows "Cannot connect to backend"
**Fix:** Check backend is running at http://localhost:8000/health

### Sentinel app shows "Connection refused"
**Fix:** 
1. Verify your IP hasn't changed (run `ipconfig`)
2. Update IP in: `mobile/sentinel-app/app/src/main/java/com/pranamesh/sentinel/ApiClient.kt`
3. Rebuild Sentinel app

### BLE not working
**Requirements:**
- Must use real Android devices (not emulators)
- Bluetooth must be enabled
- Location permission set to "Always Allow"
- Devices within 10-30 meters

### No signals appearing on dashboard
1. Check Sentinel can reach backend: Open `http://YOUR_IP:8000/health` on Sentinel device browser
2. Verify Ghost is broadcasting (check app shows "Broadcasting")
3. Check Sentinel is scanning (check app shows scan results)

---

## Network Configuration

### Your Current Network
- **Computer IP:** 10.121.124.131
- **Backend Port:** 8000
- **Dashboard Port:** 8501

### For Sentinel App
The Sentinel app is configured to connect to:
```
http://10.121.124.131:8000
```

If your IP changes, update this file and rebuild:
```
mobile/sentinel-app/app/src/main/java/com/pranamesh/sentinel/ApiClient.kt
Line 24: private const val BASE_URL = "http://YOUR_IP:8000"
```

---

## Quick Commands Reference

| Task | Command |
|------|---------|
| Start Backend | `.\start-backend.bat` |
| Start Dashboard | `.\start-dashboard.bat` |
| Build Ghost APK | `.\build-ghost.bat` |
| Build Sentinel APK | `.\build-sentinel.bat` |
| Check Backend Health | http://localhost:8000/health |
| View API Docs | http://localhost:8000/docs |
| Open Dashboard | http://localhost:8501 |

---

## Status Codes

| Code | Status | Color |
|------|--------|-------|
| 0 | SAFE | Green |
| 1 | HELP | Orange |
| 2 | MEDICAL | Red |
| 3 | CRITICAL | Dark Red |

---

**Last Updated:** April 8, 2026

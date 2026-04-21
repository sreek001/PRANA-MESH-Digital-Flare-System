# PRANA-MESH: Digital Flare System

An emergency mesh communication system for disaster scenarios where cellular/internet infrastructure is unavailable. Uses Bluetooth LE (BLE) for offline device-to-device communication.

## Architecture

```
Ghost App (BLE Broadcast) → Sentinel App (BLE Scan + HTTP) → Backend (FastAPI) → Dashboard (Streamlit)
```

## Components

1. **Ghost App** (`mobile/ghost-app/`) - Broadcasts distress signals via BLE
2. **Sentinel App** (`mobile/sentinel-app/`) - Scans and relays to backend
3. **Backend** (`backend/`) - FastAPI + MySQL
4. **Dashboard** (`dashboard/`) - Real-time map visualization

## Quick Start

### Backend
```bash
cd backend
venv\Scripts\activate
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### Dashboard
```bash
cd dashboard
venv\Scripts\activate
streamlit run dashboard.py
```

### Mobile Apps
1. Open in Android Studio
2. Update `ApiClient.kt` with your PC's IP
3. Build and install

## Demo
1. Ghost App: Select emergency → PANIC
2. Sentinel App: START scanning
3. Dashboard: See signals on map at http://localhost:8501

## Emergency Types
- SAFE (0), HELP (1), MEDICAL (2), CRITICAL (3)

## Tech Stack
- Mobile: Kotlin, Android BLE, Retrofit
- Backend: FastAPI, MySQL
- Dashboard: Streamlit, Folium
- AI: Google Gemini

MIT License

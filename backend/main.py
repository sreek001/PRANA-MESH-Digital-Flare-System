from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from sqlalchemy.orm import Session
from database import engine, get_db
from models import Signal, Base
from datetime import datetime
import time
import os
import google.generativeai as genai

# Configure Gemini AI
GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY", "")
if GEMINI_API_KEY:
    genai.configure(api_key=GEMINI_API_KEY)
    gemini_model = genai.GenerativeModel('gemini-pro')
else:
    gemini_model = None

Base.metadata.create_all(bind=engine)

app = FastAPI(title="PRANA-MESH Command Center", version="1.0.0")

# Allow CORS for Streamlit Cloud
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # For demo; restrict in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class SignalReport(BaseModel):
    device_id: str
    lat: float
    lon: float
    status: int = 0
    battery: int = 100
    timestamp: int | None = None
    sentinel_id: str | None = None


class SignalResponse(BaseModel):
    device_id: str
    latitude: float
    longitude: float
    status: int
    status_label: str
    battery: int
    last_seen: int
    is_active: bool
    sentinel_id: str | None = None
    ai_priority: int | None = None
    ai_analysis: str | None = None


STATUS_LABELS = {0: "SAFE", 1: "HELP", 2: "MEDICAL", 3: "CRITICAL"}


def analyze_with_gemini(signal: SignalReport) -> tuple[int, str]:
    """Use Gemini AI to analyze distress signal and assign priority (1-10)"""
    if not gemini_model:
        return None, None

    status_names = {0: "SAFE", 1: "HELP", 2: "MEDICAL", 3: "CRITICAL"}

    prompt = f"""
Analyze this distress signal from a disaster zone and assign a rescue priority score (1-10, where 10 is most urgent):

Device ID: {signal.device_id}
Status: {status_names.get(signal.status, 'UNKNOWN')}
Battery Level: {signal.battery}%
Location: {signal.lat}, {signal.lon}

Consider:
- CRITICAL/MEDICAL status = higher priority
- Low battery (<20%) = higher priority (victim may lose connectivity)
- HELP status with good battery = medium priority
- SAFE status = lowest priority

Return ONLY a JSON response like: {{"priority": 7, "analysis": "Brief reason"}}
"""

    try:
        response = gemini_model.generate_content(prompt)
        import json
        # Extract JSON from response
        response_text = response.text.strip()
        # Try to parse JSON
        if "{" in response_text and "}" in response_text:
            start = response_text.find("{")
            end = response_text.rfind("}") + 1
            json_str = response_text[start:end]
            result = json.loads(json_str)
            priority = result.get("priority", 5)
            analysis = result.get("analysis", "AI analysis unavailable")
            return min(10, max(1, priority)), analysis
    except Exception as e:
        print(f"Gemini AI error: {e}")

    # Default priority based on status
    default_priorities = {0: 1, 1: 5, 2: 8, 3: 10}
    return default_priorities.get(signal.status, 5), f"Priority based on {status_names.get(signal.status, 'UNKNOWN')} status"


def signal_to_response(s) -> SignalResponse:
    return SignalResponse(
        device_id=s.device_id,
        latitude=s.latitude,
        longitude=s.longitude,
        status=s.status,
        status_label=STATUS_LABELS.get(s.status, "UNKNOWN"),
        battery=s.battery,
        last_seen=s.last_seen,
        is_active=s.is_active,
        sentinel_id=s.sentinel_id,
        ai_priority=getattr(s, 'ai_priority', None),
        ai_analysis=getattr(s, 'ai_analysis', None),
    )


@app.post("/report", response_model=SignalResponse)
def report_signal(report: SignalReport, db: Session = Depends(get_db)):
    timestamp = report.timestamp or int(time.time())

    # Analyze with Gemini AI for priority scoring
    ai_priority, ai_analysis = analyze_with_gemini(report)

    existing = db.query(Signal).filter(Signal.device_id == report.device_id).first()

    if existing:
        existing.latitude = report.lat
        existing.longitude = report.lon
        existing.status = report.status
        existing.battery = report.battery
        existing.last_seen = timestamp
        if report.sentinel_id:
            existing.sentinel_id = report.sentinel_id
        if ai_priority:
            existing.ai_priority = ai_priority
            existing.ai_analysis = ai_analysis
    else:
        new_signal = Signal(
            device_id=report.device_id,
            latitude=report.lat,
            longitude=report.lon,
            status=report.status,
            battery=report.battery,
            last_seen=timestamp,
            sentinel_id=report.sentinel_id,
            ai_priority=ai_priority,
            ai_analysis=ai_analysis,
        )
        db.add(new_signal)

    db.commit()

    signal = db.query(Signal).filter(Signal.device_id == report.device_id).first()

    return signal_to_response(signal)


@app.get("/signals", response_model=list[SignalResponse])
def get_all_signals(db: Session = Depends(get_db)):
    signals = db.query(Signal).all()
    return [signal_to_response(s) for s in signals]


@app.get("/signals/active", response_model=list[SignalResponse])
def get_active_signals(db: Session = Depends(get_db)):
    signals = db.query(Signal).all()
    active = [s for s in signals if s.is_active]
    return [signal_to_response(s) for s in active]


@app.get("/signals/medical", response_model=list[SignalResponse])
def get_medical_signals(db: Session = Depends(get_db)):
    signals = db.query(Signal).filter(Signal.status == 2).all()
    return [signal_to_response(s) for s in signals]


@app.get("/health")
def health_check():
    return {"status": "ok", "timestamp": int(time.time())}


@app.delete("/signals/{device_id}")
def delete_signal(device_id: str, db: Session = Depends(get_db)):
    signal = db.query(Signal).filter(Signal.device_id == device_id).first()
    if not signal:
        raise HTTPException(status_code=404, detail="Signal not found")
    db.delete(signal)
    db.commit()
    return {"message": "Signal deleted"}

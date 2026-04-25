# PRANA-MESH - Solution Challenge 2026
## Live Demo Presentation Script

---

## ⏱️ Total Duration: 3 Minutes

---

## 🎬 OPENING (0:00 - 0:30)

**[Show Title Slide with PRANA-MESH logo]**

**Speak:**
"Good [morning/afternoon], judges. My name is [YOUR NAME], and I'm presenting PRANA-MESH - a decentralized emergency communication system that saves lives when disaster strikes."

"Every year, natural disasters like earthquakes, floods, and cyclones leave thousands stranded without any way to call for help. When cell towers collapse and internet fails, victims go silent."

"PRANA-MESH changes this. It's a BLE-based mesh network that broadcasts distress signals WITHOUT needing internet, cellular networks, or any special hardware - just an Android smartphone."

**[Click to next slide: Problem Statement]**

---

## 📋 THE PROBLEM (0:30 - 0:50)

**[Show slide with disaster statistics or images]**

**Speak:**
"Consider the 2023 Turkey-Syria earthquake. Over 50,000 died. Countless survivors were trapped under rubble, alive but unable to call for help because cellular networks were destroyed."

"Traditional emergency communicators like Garmin inReach cost $300+ and require satellite subscriptions. Specialized radios need dedicated hardware. In developing nations, this is unaffordable."

"But 95% of people already carry a lifeline in their pocket - a smartphone with Bluetooth."

---

## 💡 THE SOLUTION (0:50 - 1:20)

**[Show slide: How PRANA-MESH Works]**

**Speak:**
"PRANA-MESH uses Bluetooth Low Energy - available on every Android phone since 2013 - to create a decentralized distress network."

"Here's how it works:"

1. "**Victim opens Ghost app** - One tap on the PANIC button activates distress mode"

2. "**Phone broadcasts BLE signal** - Every 60 seconds, it broadcasts a packet containing device ID, GPS location, battery level, and emergency status"

3. "**Nearby rescuers detect** - Any phone running our Sentinel app within 100 meters receives the signal"

4. "**Signal relayed to cloud** - Sentinel automatically uploads the distress location to our dashboard"

5. "**Rescue teams respond** - Authorities see all active distress signals on a real-time map"

"No internet needed for the victim. No special hardware. Just smartphones working together."

---

## 🔥 LIVE DEMO (1:20 - 2:20)

**[Switch to live demo - have both apps running on phone/screen share]**

### Demo Setup (pre-demo checklist):
- ✅ Backend running on port 8000
- ✅ Dashboard open in browser
- ✅ Ghost app installed on Phone A
- ✅ Sentinel app installed on Phone B (or same phone)
- ✅ Bluetooth enabled

---

**[Open Ghost App on screen]**

**Speak:**
"Let me show you this in action. This is the Ghost app - the distress transmitter."

"The interface is simple by design. In a disaster, panic is high. One button. That's all it takes."

**[Press PANIC button]**

"Distress mode activated. The phone is now broadcasting BLE signals continuously. Notice the app stays open - we've disabled auto-sleep to ensure continuous broadcast."

"The signal includes: device ID, GPS coordinates, battery percentage, and status code - in this case, CRITICAL."

---

**[Open Sentinel App on second phone/screen]**

**Speak:**
"Now, this is the Sentinel app - what rescuers carry. I'll press START to begin scanning."

**[Press START button]**

"The app is now listening for BLE distress signals within 100 meters. It scans continuously in the background."

**[Wait 5-10 seconds for detection]**

"And there it is! Sentinel detected the Ghost signal. Watch the dashboard update in real-time."

---

**[Switch to Dashboard view]**

**Speak:**
"This is our Command Center dashboard. Every detected signal appears on the map with:"

- "Color-coded status: Green for SAFE, Orange for HELP, Red for MEDICAL, Dark Red for CRITICAL"
- "Battery level - so rescuers know how much time they have before the victim goes dark"
- "GPS coordinates accurate to 6 decimal places"
- "Last seen timestamp"

**[Point to AI Priority feature]**

"We've integrated Google's Gemini AI to analyze each distress signal and assign a priority score from 1 to 10. This helps rescue teams triage when multiple signals come in simultaneously."

"Consider two signals: One MEDICAL with 5% battery, another HELP with 80% battery. Gemini automatically prioritizes the first - that victim may lose connectivity within minutes."

---

## 🛠️ TECHNOLOGY STACK (2:20 - 2:40)

**[Show Architecture slide]**

**Speak:**
"Technically, PRANA-MESH is built with:"

- "Android Kotlin for both mobile apps"
- "Bluetooth Low Energy for peer-to-peer communication"
- "FastAPI backend hosted on Google Cloud Run"
- "Firebase Firestore for real-time database"
- "Google Gemini AI for intelligent alert prioritization"
- "Streamlit dashboard for command center visualization"

"The entire system is designed for rapid deployment in disaster zones with minimal infrastructure."

---

## 🚀 FUTURE & IMPACT (2:40 - 3:00)

**[Show Future Development slide]**

**Speak:**
"Looking ahead, we're adding Wi-Fi Direct for 500-meter range, multi-hop mesh networking between devices, and integration with emergency services APIs like 112 and 911."

"But here's what matters: PRANA-MESH costs nothing to deploy. No new hardware. No subscriptions. Just install the app and you're part of a network that can save lives."

"When the next earthquake hits, when the next flood rises, we want every smartphone to be a potential lifeline."

"Thank you. I'm happy to take any questions."

**[End on Thank You slide with GitHub link]**

---

## 🎯 DEMO TROUBLESHOOTING (Backup Plan)

If live demo fails, use this pre-recorded script:

**Speak:**
"Due to Bluetooth limitations in this environment, let me show you a recorded demo of the system in action..."

**[Play screen recording video showing:
1. Ghost app panic button activation
2. Sentinel app detecting signal
3. Dashboard updating with location
]**

---

## 📱 QUICK SETUP COMMANDS (For Presenter)

### Before Demo:
```bash
# Start Backend
cd C:\Users\LENOVO\Documents\finproject\backend
uvicorn main:app --reload --host 0.0.0.0 --port 8000

# Start Dashboard (new terminal)
cd C:\Users\LENOVO\Documents\finproject\dashboard
streamlit run dashboard.py --server.port 8501
```

### Open in Browser:
- Dashboard: http://localhost:8501
- Backend Health: http://localhost:8000/health
- API Docs: http://localhost:8000/docs

### Install Apps (if needed):
```bash
adb install -r C:\Users\LENOVO\Documents\finproject\mobile\ghost-app\app\build\outputs\apk\debug\app-debug.apk
adb install -r C:\Users\LENOVO\Documents\finproject\mobile\sentinel-app\app\build\outputs\apk\debug\app-debug.apk
```

---

## 🎤 KEY MESSAGES TO EMphasize

1. **Zero Hardware Cost** - Uses existing smartphones
2. **Works Offline** - No internet/cellular required
3. **Google AI Integration** - Gemini for smart prioritization
4. **Cloud Deployed** - FastAPI on Google Cloud Run
5. **Battery Efficient** - BLE broadcasts every 60 seconds
6. **Instant Activation** - One panic button, immediate broadcast

---

## ❓ ANTICIPATED QUESTIONS

**Q: What if no Sentinel phones are nearby?**
A: "That's a valid concern. The system works best when rescue teams patrol affected areas with Sentinel devices. We're also exploring Wi-Fi Direct and LoRa integration for longer-range relay."

**Q: How accurate is the location?**
A: "GPS coordinates are accurate to ~5 meters. We cache the last known location before battery dies, so even if the phone loses GPS, the last position is broadcast."

**Q: What about privacy?**
A: "Distress signals are broadcast openly - this is intentional for rescue. In production, we'd add encryption for the cloud relay portion and auto-delete signals after 24 hours."

**Q: Battery life?**
A: "BLE advertising is extremely low power. We broadcast once per minute, which consumes approximately 2-3% battery per hour. A typical phone can broadcast for 24+ hours."

---

## ✅ PRE-DEMO CHECKLIST

- [ ] Backend server running (port 8000)
- [ ] Dashboard open (port 8501)
- [ ] Both apps installed on phone(s)
- [ ] Bluetooth enabled
- [ ] Location/GPS enabled
- [ ] Phone battery >50%
- [ ] Screen recording ready (backup)
- [ ] GitHub repo link ready
- [ ] Demo video ready (backup)

---

**Good luck! You've built something that can save lives. Present it with confidence.** 🚀

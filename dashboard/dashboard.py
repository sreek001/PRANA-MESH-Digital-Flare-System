import streamlit as st
from streamlit_folium import st_folium
import folium
from folium.plugins import HeatMap, MarkerCluster
import requests
import pandas as pd
from datetime import datetime

# Page config
st.set_page_config(
    page_title="PRANA-MESH Command Center",
    page_icon="🚨",
    layout="wide",
    initial_sidebar_state="collapsed"
)

# Backend URL - Cloud Run deployment
BACKEND_URL = "https://prana-mesh-backend-8152165795.us-central1.run.app"

# Custom CSS for modern styling
st.markdown("""
<style>
    /* Main background gradient */
    .stApp {
        background: linear-gradient(135deg, #0f0c29 0%, #1a1a2e 50%, #16213e 100%);
    }

    /* Title styling */
    h1 {
        background: linear-gradient(90deg, #ff6b6b, #feca57, #48dbfb);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        font-size: 3rem !important;
        font-weight: 800;
        text-align: center;
        margin-bottom: 10px !important;
    }

    /* Subtitle */
    .subtitle {
        text-align: center;
        color: #a0a0a0;
        font-size: 1.2rem;
        margin-bottom: 30px;
    }

    /* Metric cards */
    .metric-card {
        background: linear-gradient(145deg, rgba(255,255,255,0.1), rgba(255,255,255,0.05));
        border-radius: 20px;
        padding: 30px;
        text-align: center;
        border: 1px solid rgba(255,255,255,0.1);
        backdrop-filter: blur(10px);
        transition: transform 0.3s ease, box-shadow 0.3s ease;
        box-shadow: 0 8px 32px rgba(0,0,0,0.3);
    }
    .metric-card:hover {
        transform: translateY(-5px);
        box-shadow: 0 12px 40px rgba(0,0,0,0.4);
    }
    .metric-value {
        font-size: 3rem;
        font-weight: bold;
        color: #fff;
    }
    .metric-label {
        font-size: 1rem;
        color: #a0a0a0;
        margin-top: 10px;
        text-transform: uppercase;
        letter-spacing: 2px;
    }

    /* Status badges */
    .status-badge {
        display: inline-block;
        padding: 8px 16px;
        border-radius: 20px;
        font-weight: bold;
        font-size: 0.9rem;
        margin: 5px;
    }
    .status-safe { background: #00c853; color: white; }
    .status-help { background: #ff9800; color: white; }
    .status-medical { background: #f44336; color: white; }
    .status-critical { background: #9c27b0; color: white; }

    /* Tab styling */
    .stTabs [data-baseweb="tab-list"] {
        gap: 10px;
    }
    .stTabs [data-baseweb="tab"] {
        background: rgba(255,255,255,0.1);
        border-radius: 10px;
        padding: 15px 25px;
        color: white;
    }
    .stTabs [data-baseweb="tab"][aria-selected="true"] {
        background: linear-gradient(90deg, #ff6b6b, #feca57);
        color: #0f0c29;
        font-weight: bold;
    }

    /* Buttons */
    .stButton > button {
        background: linear-gradient(90deg, #ff6b6b, #feca57);
        color: #0f0c29;
        border: none;
        border-radius: 10px;
        padding: 12px 30px;
        font-weight: bold;
        font-size: 1rem;
        transition: transform 0.2s ease, box-shadow 0.2s ease;
    }
    .stButton > button:hover {
        transform: scale(1.05);
        box-shadow: 0 5px 20px rgba(255,107,107,0.4);
    }

    /* Cards for signal info */
    .signal-card {
        background: linear-gradient(145deg, rgba(255,255,255,0.08), rgba(255,255,255,0.02));
        border-radius: 15px;
        padding: 20px;
        margin: 10px 0;
        border-left: 4px solid #ff6b6b;
    }

    /* Alert boxes */
    .success-box {
        background: linear-gradient(145deg, rgba(0,200,83,0.2), rgba(0,200,83,0.05));
        border: 1px solid #00c853;
        border-radius: 10px;
        padding: 20px;
        color: #00c853;
    }
    .warning-box {
        background: linear-gradient(145deg, rgba(255,152,0,0.2), rgba(255,152,0,0.05));
        border: 1px solid #ff9800;
        border-radius: 10px;
        padding: 20px;
        color: #ff9800;
    }

    /* Hide Streamlit branding */
    #MainMenu {visibility: hidden;}
    footer {visibility: hidden;}
</style>
""", unsafe_allow_html=True)

# Header with animated effect
st.markdown('<h1>🚨 PRANA-MESH Command Center</h1>', unsafe_allow_html=True)
st.markdown('<p class="subtitle">Digital Flare System — Emergency Mesh Communication Dashboard</p>', unsafe_allow_html=True)

# Fetch signals
@st.cache_data(ttl=5)
def fetch_signals():
    try:
        response = requests.get(f"{BACKEND_URL}/signals", timeout=5)
        if response.status_code == 200:
            return response.json()
    except:
        pass
    return []

signals = fetch_signals()

# Status counts
total_signals = len(signals)
active_signals = [s for s in signals if s["is_active"]]
medical_signals = [s for s in signals if s["status"] == 2]
critical_signals = [s for s in signals if s["status"] == 3]
help_signals = [s for s in signals if s["status"] == 1]
safe_signals = [s for s in signals if s["status"] == 0]

# Metrics with custom cards
col1, col2, col3, col4 = st.columns(4)

with col1:
    st.markdown(f"""
    <div class="metric-card">
        <div class="metric-value" style="color: #48dbfb;">{total_signals}</div>
        <div class="metric-label">📡 Total Signals</div>
    </div>
    """, unsafe_allow_html=True)

with col2:
    st.markdown(f"""
    <div class="metric-card">
        <div class="metric-value" style="color: #00c853;">{len(active_signals)}</div>
        <div class="metric-label">✅ Active Now</div>
    </div>
    """, unsafe_allow_html=True)

with col3:
    st.markdown(f"""
    <div class="metric-card">
        <div class="metric-value" style="color: #feca57;">{len(medical_signals)}</div>
        <div class="metric-label">🏥 Medical</div>
    </div>
    """, unsafe_allow_html=True)

with col4:
    st.markdown(f"""
    <div class="metric-card">
        <div class="metric-value" style="color: #ff6b6b;">{len(critical_signals)}</div>
        <div class="metric-label">🔴 Critical</div>
    </div>
    """, unsafe_allow_html=True)

st.markdown("<br>", unsafe_allow_html=True)

# Quick stats bar
if total_signals > 0:
    st.markdown("""
    <div style="text-align: center; margin: 20px 0;">
        <span class="status-badge status-safe">✅ Safe: {safe}</span>
        <span class="status-badge status-help">⚠️ Help: {help}</span>
        <span class="status-badge status-medical">🏥 Medical: {medical}</span>
        <span class="status-badge status-critical">🔴 Critical: {critical}</span>
    </div>
    """.format(safe=len(safe_signals), help=len(help_signals), medical=len(medical_signals), critical=len(critical_signals)), unsafe_allow_html=True)

st.markdown("---")

# Main content tabs
if signals:
    tab1, tab2, tab3, tab4 = st.tabs(["🗺️ Live Map", "🔥 Heatmap", "📍 Locations", "📋 Details"])

    # Calculate center
    center_lat = sum(s["latitude"] for s in signals) / len(signals)
    center_lon = sum(s["longitude"] for s in signals) / len(signals)

    with tab1:
        st.markdown("### 🌍 Real-time Distress Signal Map")

        m = folium.Map(location=[center_lat, center_lon], zoom_start=14, tiles="CartoDB dark_matter")
        marker_cluster = MarkerCluster().add_to(m)

        status_colors = {0: "green", 1: "orange", 2: "red", 3: "darkred"}
        status_emojis = {0: "✅", 1: "⚠️", 2: "🏥", 3: "🔴"}

        for s in signals:
            color = status_colors.get(s["status"], "blue")
            emoji = status_emojis.get(s["status"], "📍")
            last_seen = datetime.fromtimestamp(s["last_seen"]).strftime("%Y-%m-%d %H:%M:%S")
            status_labels = {0: "SAFE", 1: "HELP", 2: "MEDICAL", 3: "CRITICAL"}

            popup_html = f"""
            <div style="min-width: 220px; font-family: 'Segoe UI', Arial;">
                <div style="background: linear-gradient(90deg, {color}, transparent); padding: 10px; margin: -10px -10px 10px -10px; border-radius: 8px 8px 0 0;">
                    <strong style="color: white; font-size: 1.1em;">{emoji} {status_labels.get(s['status'], 'UNKNOWN')}</strong>
                </div>
                <div style="padding: 5px;">
                    <b>📱 Device:</b> {s['device_id']}<br>
                    <b>📍 Location:</b> {s['latitude']:.6f}, {s['longitude']:.6f}<br>
                    <b>🔋 Battery:</b> {s['battery']}%<br>
                    <b>⏰ Last Seen:</b> {last_seen}<br>
                    <b>📡 Status:</b> {'✅ Active' if s['is_active'] else '❌ Inactive'}<br>
                </div>
            </div>
            """

            folium.Marker(
                location=[s["latitude"], s["longitude"]],
                icon=folium.Icon(color=color, icon="exclamation-sign", prefix="fa"),
                popup=folium.Popup(popup_html, max_width=320)
            ).add_to(marker_cluster)

        st_folium(m, width=1400, height=650)

    with tab2:
        st.markdown("### 🔥 Emergency Signal Density Heatmap")

        medical_coords = [[s["latitude"], s["longitude"]] for s in signals if s["status"] == 2]
        critical_coords = [[s["latitude"], s["longitude"]] for s in signals if s["status"] == 3]

        if medical_coords or critical_coords:
            heat_map = folium.Map(location=[center_lat, center_lon], zoom_start=14, tiles="CartoDB dark_matter")

            if medical_coords:
                HeatMap(medical_coords, radius=25, blur=15, gradient={0.4: '#00c853', 0.65: '#ff9800', 1: '#f44336'}).add_to(heat_map)

            if critical_coords:
                HeatMap(critical_coords, radius=30, blur=20, gradient={0.4: '#ff6b6b', 0.65: '#9c27b0', 1: '#4a148c'}).add_to(heat_map)

            st_folium(heat_map, width=1400, height=600)
        else:
            st.markdown('<div class="warning-box">📍 No MEDICAL or CRITICAL signals to display on heatmap.</div>', unsafe_allow_html=True)

    with tab3:
        st.markdown("### 📍 Detected Signal Locations")

        location_data = []
        for s in signals:
            status_labels = {0: "SAFE", 1: "HELP", 2: "MEDICAL", 3: "CRITICAL"}
            location_data.append({
                "Device ID": s["device_id"],
                "Status": status_labels.get(s["status"], "UNKNOWN"),
                "Latitude": f"{s['latitude']:.6f}",
                "Longitude": f"{s['longitude']:.6f}",
                "Battery": f"{s['battery']}%",
                "Last Seen": datetime.fromtimestamp(s["last_seen"]).strftime("%Y-%m-%d %H:%M:%S"),
                "Active": "✅ Yes" if s["is_active"] else "❌ No"
            })

        location_df = pd.DataFrame(location_data)
        st.dataframe(location_df, use_container_width=True, hide_index=True)

    with tab4:
        st.markdown("### 📋 All Signal Details")

        df = pd.DataFrame(signals)
        status_labels = {0: "SAFE", 1: "HELP", 2: "MEDICAL", 3: "CRITICAL"}
        df["status_label"] = df["status"].apply(lambda x: status_labels.get(x, "UNKNOWN"))
        df["last_seen_time"] = df["last_seen"].apply(
            lambda x: datetime.fromtimestamp(x).strftime("%Y-%m-%d %H:%M:%S")
        )
        display_cols = ["device_id", "status_label", "battery", "latitude", "longitude", "last_seen_time", "is_active"]
        st.dataframe(df[display_cols], use_container_width=True)

else:
    st.markdown("""
    <div style="text-align: center; padding: 60px 20px;">
        <div style="font-size: 5rem; margin-bottom: 20px;">📡</div>
        <h2 style="color: #a0a0a0;">No distress signals received yet</h2>
        <p style="color: #666;">Signals will appear here automatically when detected by Sentinel nodes.</p>
        <div class="success-box" style="display: inline-block; margin-top: 20px;">
            <strong>✨ System Status:</strong> Backend is connected and ready to receive signals
        </div>
    </div>
    """, unsafe_allow_html=True)

# Footer
st.markdown("---")
st.markdown("""
<div style="text-align: center; color: #666; padding: 20px;">
    <p>🚨 <strong>PRANA-MESH</strong> — Saving lives through decentralized emergency communication</p>
    <p style="font-size: 0.9rem; color: #444;">Built with Google Cloud Run • FastAPI • Streamlit</p>
</div>
""", unsafe_allow_html=True)

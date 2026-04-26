import streamlit as st
from streamlit_folium import st_folium
import folium
from folium.plugins import HeatMap, MarkerCluster
import requests
import pandas as pd
from datetime import datetime

st.set_page_config(page_title="PRANA-MESH Command Center", page_icon="🚨", layout="wide")

# Backend URL - Cloud Run deployment
# For local testing: change to http://localhost:8000
BACKEND_URL = "https://prana-mesh-backend-8152165795.us-central1.run.app"

st.title("🚨 PRANA-MESH Command Center")
st.markdown("**Digital Flare System** — Emergency Mesh Communication Dashboard")
st.markdown("---")

# Fetch signals with error handling
try:
    response = requests.get(f"{BACKEND_URL}/signals", timeout=5)
    if response.status_code == 200:
        signals = response.json()
    else:
        st.error(f"⚠️ Backend returned status {response.status_code}")
        signals = []
except requests.exceptions.ConnectionError:
    st.error("⚠️ Cannot connect to backend.")
    signals = []
except Exception as e:
    st.error(f"⚠️ Error: {str(e)}")
    signals = []

# Metrics
col1, col2, col3, col4 = st.columns(4)
total_signals = len(signals)
active_signals = [s for s in signals if s["is_active"]]
medical_signals = [s for s in signals if s["status"] == 2]
critical_signals = [s for s in signals if s["status"] == 3]

col1.metric("📡 Total Signals", total_signals)
col2.metric("✅ Active Now", len(active_signals))
col3.metric("🏥 Medical", len(medical_signals))
col4.metric("🔴 Critical", len(critical_signals))

st.markdown("---")

if signals:
    tab1, tab2, tab3, tab4 = st.tabs(["🗺️ Live Map", "🔥 Heatmap", "📍 Location List", "📋 Signal Table"])

    # Calculate center
    center_lat = sum(s["latitude"] for s in signals) / len(signals)
    center_lon = sum(s["longitude"] for s in signals) / len(signals)

    with tab1:
        st.subheader("Real-time Distress Signal Map")

        m = folium.Map(location=[center_lat, center_lon], zoom_start=14, tiles="CartoDB dark_matter")

        # Add marker cluster
        marker_cluster = MarkerCluster().add_to(m)

        status_colors = {0: "green", 1: "orange", 2: "red", 3: "darkred"}
        status_icons = {0: "✅", 1: "⚠️", 2: "🏥", 3: "🔴"}

        for s in signals:
            color = status_colors.get(s["status"], "blue")
            icon = status_icons.get(s["status"], "📍")
            last_seen_time = datetime.fromtimestamp(s["last_seen"]).strftime("%Y-%m-%d %H:%M:%S")
            status_labels = {0: "SAFE", 1: "HELP", 2: "MEDICAL", 3: "CRITICAL"}

            popup_html = f"""
            <div style="min-width: 200px; font-family: Arial;">
                <h4 style="margin: 0 0 10px 0; color: {color};">{icon} {status_labels.get(s['status'], 'UNKNOWN')}</h4>
                <b>📱 Device:</b> {s['device_id']}<br>
                <b>📍 Location:</b> {s['latitude']:.6f}, {s['longitude']:.6f}<br>
                <b>🔋 Battery:</b> {s['battery']}%<br>
                <b>⏰ Last Seen:</b> {last_seen_time}<br>
                <b>📡 Active:</b> {'✅ Yes' if s['is_active'] else '❌ No'}<br>
            """
            if s.get("sentinel_id"):
                popup_html += f"<b>📡 Relay:</b> {s['sentinel_id']}<br>"
            popup_html += "</div>"

            folium.Marker(
                location=[s["latitude"], s["longitude"]],
                icon=folium.Icon(color=color, icon="exclamation-sign", prefix="fa"),
                popup=folium.Popup(popup_html, max_width=300)
            ).add_to(marker_cluster)

        # Add user location marker (placeholder)
        folium.Marker(
            location=[center_lat, center_lon],
            icon=folium.Icon(color="blue", icon="info-sign"),
            popup="Command Center"
        ).add_to(m)

        st_folium(m, width=1200, height=600)

    with tab2:
        st.subheader("Emergency Signal Density Heatmap")

        # Medical emergency heatmap
        medical_coords = [[s["latitude"], s["longitude"]] for s in signals if s["status"] == 2]
        critical_coords = [[s["latitude"], s["longitude"]] for s in signals if s["status"] == 3]

        if medical_coords or critical_coords:
            heat_map = folium.Map(location=[center_lat, center_lon], zoom_start=14, tiles="CartoDB dark_matter")

            if medical_coords:
                HeatMap(medical_coords, radius=25, blur=15, gradient={0.4: 'yellow', 0.65: 'orange', 1: 'red'}).add_to(heat_map)

            if critical_coords:
                HeatMap(critical_coords, radius=30, blur=20, gradient={0.4: 'red', 0.65: 'purple', 1: 'darkred'}).add_to(heat_map)

            st_folium(heat_map, width=1200, height=500)
        else:
            st.info("📍 No MEDICAL or CRITICAL signals to display on heatmap.")

    with tab3:
        st.subheader("📍 Detected Signal Locations")

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

        # Show map with all locations
        st.markdown("### Map View")
        location_map = folium.Map(location=[center_lat, center_lon], zoom_start=14)
        for s in signals:
            color = status_colors.get(s["status"], "blue")
            folium.CircleMarker(
                location=[s["latitude"], s["longitude"]],
                radius=8,
                color=color,
                fill=True,
                fill_opacity=0.6,
                popup=f"{s['device_id']} - {status_labels.get(s['status'], 'UNKNOWN')}"
            ).add_to(location_map)
        st_folium(location_map, width=800, height=400)

    with tab4:
        st.subheader("All Signal Details")

        if signals:
            df = pd.DataFrame(signals)
            status_labels = {0: "SAFE", 1: "HELP", 2: "MEDICAL", 3: "CRITICAL"}
            df["status_label"] = df["status"].apply(lambda x: status_labels.get(x, "UNKNOWN"))
            df["last_seen_time"] = df["last_seen"].apply(
                lambda x: datetime.fromtimestamp(x).strftime("%Y-%m-%d %H:%M:%S")
            )
            display_cols = ["device_id", "status_label", "battery", "latitude", "longitude", "last_seen_time", "is_active"]
            st.dataframe(df[display_cols], use_container_width=True)
else:
    st.warning("📍 No distress signals received yet.")
    st.info("Signals will appear here when detected by Sentinel nodes.")

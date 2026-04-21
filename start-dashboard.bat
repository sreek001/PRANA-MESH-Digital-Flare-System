@echo off
echo ========================================
echo PRANA-MESH Dashboard
echo ========================================
echo.
echo Starting Streamlit dashboard...
echo Dashboard: http://localhost:8501
echo.
echo Press Ctrl+C to stop the dashboard.
echo.

cd dashboard
venv\Scripts\activate
streamlit run dashboard.py

@echo off
echo ========================================
echo PRANA-MESH Backend Server
echo ========================================
echo.
echo Starting FastAPI server on port 8000...
echo API Docs: http://localhost:8000/docs
echo Health Check: http://localhost:8000/health
echo.
echo Press Ctrl+C to stop the server.
echo.

cd backend
venv\Scripts\activate
uvicorn main:app --host 0.0.0.0 --port 8000 --reload

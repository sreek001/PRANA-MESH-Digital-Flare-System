@echo off
echo ========================================
echo Building Sentinel App APK
echo ========================================
echo.

REM Check if BASE_URL needs to be updated
echo WARNING: Make sure you have updated BASE_URL in:
echo   sentinel-app\app\src\main\java\com\pranamesh\sentinel\ApiClient.kt
echo.
echo Find your computer's IP with: ipconfig
echo Then set: private const val BASE_URL = "http://YOUR_IP:8000"
echo.
pause

cd sentinel-app

if not exist "gradlew.bat" (
    echo [ERROR] gradlew.bat not found!
    echo Make sure you're running this from the mobile folder.
    pause
    exit /b 1
)

echo Running Gradle build...
gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo SUCCESS!
    echo ========================================
    echo APK location:
    echo   sentinel-app\app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo To install on device:
    echo   adb install sentinel-app\app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo [ERROR] Build failed. Check the error messages above.
)

cd ..
pause

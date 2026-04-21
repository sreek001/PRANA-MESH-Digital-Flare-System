@echo off
echo ========================================
echo Building Ghost App APK
echo ========================================
echo.

cd ghost-app

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
    echo   ghost-app\app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo To install on device:
    echo   adb install ghost-app\app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo [ERROR] Build failed. Check the error messages above.
)

cd ..
pause

@echo off
echo ========================================
echo PRANA-MESH APK Build Setup
echo ========================================
echo.

REM Check if Java is installed
echo Checking for Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed.
    echo Please install Java JDK 17 from:
    echo https://www.oracle.com/java/technologies/downloads/#jdk17-windows
    echo.
    echo Or run: winget install Oracle.JavaRuntimeEnvironment
    pause
    exit /b 1
)
echo Java is installed.
echo.

REM Check if ANDROID_HOME is set
echo Checking for Android SDK...
if "%ANDROID_HOME%"=="" (
    echo [ERROR] ANDROID_HOME environment variable is not set.
    echo Please set it to your Android SDK location (e.g., C:\Android)
    echo.
    echo To set it temporarily for this session:
    echo   set ANDROID_HOME=C:\Android
    echo   set PATH=%%PATH%%;%%ANDROID_HOME%%\platform-tools;%%ANDROID_HOME%%\cmdline-tools\latest\bin
    echo.
    pause
    exit /b 1
)
echo Android SDK is configured at: %ANDROID_HOME%
echo.

REM Check if sdkmanager exists
if not exist "%ANDROID_HOME%\cmdline-tools" (
    echo [WARNING] SDK command-line tools not found at %ANDROID_HOME%\cmdline-tools
    echo Please install Android command-line tools from:
    echo https://developer.android.com/studio#command-tools
    pause
)

REM Check if required SDK components are installed
echo Checking SDK components...
if not exist "%ANDROID_HOME%\platforms\android-34" (
    echo Installing platform-tools and android-34...
    sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
)

echo.
echo ========================================
echo Setup complete!
echo ========================================
echo.
echo To build APKs:
echo   1. For Ghost App:  cd ghost-app ^&^& gradlew assembleDebug
echo   2. For Sentinel:   cd sentinel-app ^&^& gradlew assembleDebug
echo.
echo IMPORTANT: Before building Sentinel, update the BASE_URL in:
echo   sentinel-app\app\src\main\java\com\pranamesh\sentinel\ApiClient.kt
echo.
echo Find your IP with: ipconfig
echo.
pause

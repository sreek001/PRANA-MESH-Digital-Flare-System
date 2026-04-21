# PRANA-MESH - Complete Setup Guide

This document explains how to build and run the complete PRANA-MESH system including backend, dashboard, and mobile apps.

## System Overview

```
Ghost App (BLE) --> Sentinel App (HTTP) --> Backend (FastAPI) --> Dashboard (Streamlit)
```

## Quick Start

### Step 1: Start Backend Server
```powershell
cd C:\Users\LENOVO\Documents\finproject
.\start-backend.bat
```
Backend runs at: http://localhost:8000

### Step 2: Start Dashboard
```powershell
cd C:\Users\LENOVO\Documents\finproject
.\start-dashboard.bat
```
Dashboard runs at: http://localhost:8501

### Step 3: Install Mobile Apps
Install the APKs on two Android devices:
- **Ghost App** on Device A (distress broadcaster)
- **Sentinel App** on Device B (BLE scanner + relay)

## Prerequisites

### For Backend & Dashboard:
1. **Python 3.10+** with virtual environment
2. **MySQL Server** - Create database: `CREATE DATABASE prana_mesh;`
3. **Backend dependencies**: `pip install -r backend/requirements.txt`
4. **Dashboard dependencies**: `pip install -r dashboard/requirements.txt`

### For Mobile Apps:
1. **Java Development Kit (JDK) 17** - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/)
2. **Android SDK** - Should be installed at `C:\Android`
3. **Android SDK Build-Tools 33.0.1** - Installed automatically on first build
4. **Android SDK Platform 34** - Required for compilation

## Project Structure

```
mobile/
├── ghost-app/          # Ghost App - Distress signal broadcaster
│   ├── app/
│   │   └── src/main/java/com/pranamesh/ghost/
│   ├── gradle/
│   ├── gradlew.bat
│   └── build.gradle
├── sentinel-app/       # Sentinel App - BLE scanner and monitor
│   ├── app/
│   │   └── src/main/java/com/pranamesh/sentinel/
│   ├── gradle/
│   ├── gradlew.bat
│   └── build.gradle
├── build-ghost.bat     # Script to build Ghost app
├── build-sentinel.bat  # Script to build Sentinel app
└── README.md           # This file
```

## Quick Build (Recommended)

### Build Ghost App
```powershell
cd C:\Users\LENOVO\Documents\finproject\mobile
.\build-ghost.bat
```

### Build Sentinel App
```powershell
cd C:\Users\LENOVO\Documents\finproject\mobile
.\build-sentinel.bat
```

## Manual Build

### Build Ghost App
```powershell
cd C:\Users\LENOVO\Documents\finproject\mobile\ghost-app
.\gradlew.bat assembleDebug
```

### Build Sentinel App
```powershell
cd C:\Users\LENOVO\Documents\finproject\mobile\sentinel-app
.\gradlew.bat assembleDebug
```

## APK Output Locations

After successful build:

| App | APK Location |
|-----|--------------|
| Ghost | `ghost-app\app\build\outputs\apk\debug\app-debug.apk` |
| Sentinel | `sentinel-app\app\build\outputs\apk\debug\app-debug.apk` |

## Installing on Device

### Method 1: Using ADB
1. Enable **USB Debugging** on your Android device (Developer Options)
2. Connect device via USB
3. Run:
```powershell
adb install C:\Users\LENOVO\Documents\finproject\mobile\ghost-app\app\build\outputs\apk\debug\app-debug.apk
adb install C:\Users\LENOVO\Documents\finproject\mobile\sentinel-app\app\build\outputs\apk\debug\app-debug.apk
```

### Method 2: Direct Transfer
1. Copy the APK files to your device
2. Open the APK file on your device to install
3. You may need to enable "Install from Unknown Sources" in Settings

## Troubleshooting

### "gradlew.bat not found"
Make sure you're running commands from the correct directory:
- For Ghost: `C:\Users\LENOVO\Documents\finproject\mobile\ghost-app`
- For Sentinel: `C:\Users\LENOVO\Documents\finproject\mobile\sentinel-app`

### "JAVA_HOME is not set"
Set the JAVA_HOME environment variable:
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

Or permanently in System Properties > Environment Variables.

### Build fails with AndroidX error
The `gradle.properties` file should contain:
```
android.useAndroidX=true
android.enableJetifier=true
```

### "SDK not found" or missing build tools
The SDK should be at `C:\Android`. If not, set:
```powershell
$env:ANDROID_HOME = "C:\Android"
$env:ANDROID_SDK_ROOT = "C:\Android"
```

## Clean Build

To clean and rebuild from scratch:
```powershell
.\gradlew.bat clean assembleDebug
```

## Release Build (Signed APK)

To create a release APK, you need to sign it:

1. Generate a keystore (first time only):
```powershell
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias
```

2. Add signing config to `app/build.gradle`

3. Build release:
```powershell
.\gradlew.bat assembleRelease
```

---

**Last Updated:** April 8, 2026

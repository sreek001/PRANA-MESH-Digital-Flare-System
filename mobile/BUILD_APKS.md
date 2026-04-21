# Build APKs Without Android Studio

This guide shows you how to build the Ghost and Sentinel APKs using only command-line tools.

## Prerequisites

You need:
1. **Java JDK 17** (required for Gradle)
2. **Android SDK** (command-line tools)

---

## Step 1: Install Java JDK 17

Download from: https://www.oracle.com/java/technologies/downloads/#jdk17-windows

Or use winget (run in PowerShell as Administrator):
```powershell
winget install Oracle.JavaRuntimeEnvironment
```

Verify installation:
```bash
java -version
```

---

## Step 2: Install Android Command-Line Tools

### Option A: Using winget (Recommended)
```powershell
winget install Google.AndroidSDK
```

### Option B: Manual Download

1. Download from: https://developer.android.com/studio#command-tools
2. Extract to: `C:\Android\cmdline-tools`
3. Set environment variables (see Step 3)

---

## Step 3: Set Environment Variables

Add these to your system PATH (System Properties → Advanced → Environment Variables):

```
ANDROID_HOME=C:\Android
ANDROID_SDK_ROOT=C:\Android
JAVA_HOME=C:\Program Files\Java\jdk-17
```

Add to PATH:
```
%ANDROID_HOME%\cmdline-tools\latest\bin
%ANDROID_HOME%\platform-tools
%ANDROID_HOME%\build-tools\34.0.0
```

---

## Step 4: Install Required SDK Components

Open a **new** terminal (to pick up environment variables) and run:

```bash
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

Accept licenses:
```bash
sdkmanager --licenses
```

---

## Step 5: Build the APKs

### Build Ghost App
```bash
cd mobile/ghost-app
gradlew assembleDebug
```

APK location: `mobile\ghost-app\app\build\outputs\apk\debug\app-debug.apk`

### Build Sentinel App

**IMPORTANT:** First update the backend URL in the Sentinel app:

Edit `mobile\sentinel-app\app\src\main\java\com\pranamesh\sentinel\ApiClient.kt`

Change line 24:
```kotlin
private const val BASE_URL = "http://YOUR_IP_HERE:8000"
```

Replace `YOUR_IP_HERE` with your computer's actual IP address.

Find your IP with:
```bash
ipconfig
```
Look for "IPv4 Address" under your WiFi connection.

Then build:
```bash
cd mobile/sentinel-app
gradlew assembleDebug
```

APK location: `mobile\sentinel-app\app\build\outputs\apk\debug\app-debug.apk`

---

## Step 6: Install APKs on Android Devices

### Method 1: USB Cable (adb)
```bash
# Enable USB Debugging on device first
adb install mobile/ghost-app/app/build/outputs/apk/debug/app-debug.apk
adb install mobile/sentinel-app/app/build/outputs/apk/debug/app-debug.apk
```

### Method 2: Direct Transfer
1. Copy APK files to your phone (via USB, email, Google Drive, etc.)
2. On phone, open the APK file to install
3. You may need to enable "Install from Unknown Sources"

---

## Troubleshooting

### "gradlew not found"
Make sure you're in the correct folder (ghost-app or sentinel-app).

### "SDK not found"
Ensure ANDROID_HOME environment variable is set correctly.

### "Java not found"
Ensure JAVA_HOME is set and java -version works.

### Build fails with license errors
Run: `sdkmanager --licenses`

### APK still not found after build
Check: `mobile/ghost-app/app/build/outputs/apk/debug/`

---

## Quick Build Script

For convenience, run these commands from the `mobile` folder:

**Ghost App:**
```bash
cd ghost-app && gradlew assembleDebug && echo "Ghost APK built!"
```

**Sentinel App:**
```bash
cd sentinel-app && gradlew assembleDebug && echo "Sentinel APK built!"
```

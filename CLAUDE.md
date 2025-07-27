# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Fire OS TV app built with Kotlin that displays web development servers on Fire TV devices. The app acts as a WebView wrapper with TV-optimized UI and D-pad navigation, allowing developers to preview their local web applications on a big screen.

## Build System & Dependencies

**Build Tools:**
- Gradle 8.13+ with Android Gradle Plugin 8.11.1
- Kotlin 1.9.25 with Java 17 target
- Android SDK 35 (compileSdk and targetSdk)
- MinSdk 21 for broad Fire TV device compatibility

**Key Dependencies:**
- `androidx.leanback:leanback` - Fire TV/Android TV specific UI components
- `androidx.appcompat` - AppCompat theme and activity base classes
- `androidx.constraintlayout` - Layout system
- `androidx.preference` - Settings persistence via SharedPreferences

## Common Commands

```bash
# Clean and build debug APK
./gradlew clean assembleDebug

# Install on connected Fire TV device via ADB
export ANDROID_HOME=/home/dev/tv_app/android-sdk
./gradlew installDebug
# OR manually:
adb connect <FIRE_TV_IP>:5555
adb install app/build/outputs/apk/debug/app-debug.apk

# Connect to Fire TV device
adb connect 192.168.4.94:5555

# View logs during development
adb logcat | grep -E "(MainActivity|SettingsActivity|BrowserToolbar)"
```

## Architecture

**Core Components Structure:**
```
app/src/main/java/com/webviewer/firetv/
├── MainActivity.kt          # Main WebView container with error handling
├── SettingsActivity.kt      # Server IP/port configuration  
├── BrowserToolbarView.kt    # Custom browser navigation toolbar
├── IPAddressInputView.kt    # IP address input component
├── PortSelectorView.kt      # Port selection component
└── ControlsGuideActivity.kt # User controls documentation
```

**Two-Activity Architecture:**
- `MainActivity` - Main WebView container with error handling and retry logic
  - Manages WebView lifecycle and configuration
  - Handles D-pad navigation and key event routing
  - Shows loading states and error screens
  - Integrates BrowserToolbarView for navigation controls
  
- `SettingsActivity` - Server IP/port configuration with input validation
  - Uses SharedPreferences for persistence
  - Validates IP addresses (IPv4 format)
  - Validates port numbers (1-65535 range)
  - TV-optimized input with focus flow management

**Key Architectural Patterns:**
1. **Custom View Components**: BrowserToolbarView encapsulates browser controls with callbacks
2. **State Management**: SharedPreferences for server config, view visibility states for UI flow
3. **Error Handling**: Graceful WebView error recovery with user-friendly retry options
4. **Focus Management**: Explicit focus flow control for D-pad navigation between UI elements

**Navigation Flow:**
1. App launches → Check saved server config in SharedPreferences
2. If no config → Show error screen with Settings button focused
3. Settings → IP/port input with validation → Save to SharedPreferences  
4. Return to main → Load WebView with configured URL
5. D-pad events → Dispatch to focused view or simulate JavaScript keyboard events

## Fire TV Integration Details

**WebView Configuration (MainActivity:99-148):**
```kotlin
webView.settings.apply {
    javaScriptEnabled = true      // Required for modern web apps
    domStorageEnabled = true      // LocalStorage/SessionStorage support
    loadWithOverviewMode = true   // Fit content to screen
    useWideViewPort = true        // Support viewport meta tag
    cacheMode = LOAD_NO_CACHE     // Always fetch fresh content
}
```

**D-pad Navigation Handling (MainActivity:211-286):**
- Intercepts D-pad events at dispatch level for menu button support
- Routes arrow keys to JavaScript when WebView is visible
- Simulates keyboard events via JavaScript injection
- Supports multiple Fire TV remote button codes (82, 229, etc.)

**Focus State Management (BrowserToolbarView:200-210):**
- Scale animation (1.1x) on focus gain
- Alpha changes to indicate enabled/disabled states
- Smooth transitions (200ms) for all focus changes

## Settings & Configuration

**SharedPreferences Keys:**
- `server_ip` - Target web server IP address (validated IPv4 format)
- `server_port` - Target web server port (default: 3000, range: 1-65535)

**Validation Logic (SettingsActivity:140-169):**
```kotlin
// IP validation: checks 4 octets, each 0-255
// Port validation: ensures range 1-65535
// Both show Toast errors on validation failure
```

## Fire TV Best Practices Implementation

**UI/UX Standards** (from `best-practices.md`):**
- Typography: 30sp+ titles, 24sp+ body text (defined in `values/dimens.xml`)
- Focus indicators: Scale + glow effects in `drawable/button_background.xml`
- Safe zones: 48dp margins in layouts to prevent overscan clipping
- Dark theme: High contrast colors in `values/colors.xml`
- Button sizing: Minimum 80dp height for TV remotes

**Resource Organization:**
```
app/src/main/res/
├── layout/              # TV-optimized layouts with focus indicators
├── drawable/            # Focus state selectors, backgrounds
├── values/
│   ├── colors.xml       # Dark theme palette with high contrast
│   ├── dimens.xml       # TV-specific dimensions (10-foot UI)
│   └── strings.xml      # User-facing text
└── mipmap-*/            # App icons from IconKitchen
```

## Development Notes

**Local SDK Configuration:**
- Android SDK path: `/home/dev/tv_app/android-sdk` (set in `local.properties`)
- Includes platform-tools for ADB and build-tools 35.0.0
- No external SDK download needed - bundled in project

**Testing on Fire TV:**
1. Enable Developer Options and ADB on Fire TV
2. Connect: `adb connect <FIRE_TV_IP>:5555`
3. Install: `./gradlew installDebug`
4. View logs: `adb logcat | grep -E "(MainActivity|SettingsActivity)"`

**Network Requirements for Web Servers:**
- Development server must bind to `0.0.0.0` (not localhost/127.0.0.1)
- Examples:
  - Vite: `npm run dev -- --host 0.0.0.0`
  - Next.js: `npm run dev -- -H 0.0.0.0`
  - Express: `app.listen(3000, '0.0.0.0')`
- Fire TV and dev machine must be on same network
- No HTTPS support - uses cleartext traffic (see AndroidManifest.xml:22)

**Icon Assets:**
- App icons: `IconKitchen-Output/android/res/mipmap-*`
- TV banner: `IconKitchen-Output/androidtv/res/drawable-xhdpi/tv_banner.png`
- Proper Fire TV launcher integration via `android:banner` in manifest
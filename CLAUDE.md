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

## Monitoring & Error Detection System

**Fire TV App Monitor** - Automated monitoring and error detection system located in `scripts/` directory. This system should be used after implementing new features to ensure stability and catch runtime issues.

**Core Components:**
```
scripts/
├── monitor_cli.py        # Command-line interface for monitoring
├── monitor.py           # Main monitoring daemon
├── error_detector.py    # Real-time error detection engine
├── auto_fixer.py        # Automated fix strategies
└── adb_manager.py       # ADB connection management
```

**Monitor Commands (run from project root):**
```bash
# Start monitoring daemon (run after deployments)
cd scripts && python3 monitor_cli.py start

# Check current app status and health
cd scripts && python3 monitor_cli.py status

# Test ADB connectivity to Fire TV
cd scripts && python3 monitor_cli.py test

# View recent monitoring logs
cd scripts && python3 monitor_cli.py logs --lines 50

# Generate monitoring report with statistics
cd scripts && python3 monitor_cli.py report

# Show current monitor configuration
cd scripts && python3 monitor_cli.py config
```

**Error Detection Capabilities:**
- Network connectivity issues (ERR_CONNECTION_REFUSED, timeouts)
- UI element availability (missing buttons, WebView state)
- Memory usage monitoring and leak detection
- App crash detection and recovery
- Focus management issues in TV navigation
- WebView loading failures and JavaScript errors

**Auto-Fix Strategies:**
- App restart for crash recovery
- UI element refresh for focus issues
- Network connection retry mechanisms
- Memory optimization triggers
- WebView reload for loading failures

**Integration Workflow:**
1. **After Feature Implementation** → Run `python3 monitor_cli.py start` to begin monitoring
2. **During Testing** → Use `python3 monitor_cli.py status` to check app health
3. **Before Deployment** → Review `python3 monitor_cli.py report` for stability metrics
4. **Troubleshooting** → Check `python3 monitor_cli.py logs` for detailed error information

**Configuration:**
- Monitor settings stored in `scripts/monitor_config.json`
- Device IP/port configuration for Fire TV target
- Error detection thresholds and fix attempt limits
- Logging levels and report generation intervals

**Use Case:** Essential for maintaining app stability in production-like environments and catching edge cases that manual testing might miss. Particularly valuable for WebView-based apps where network connectivity and UI state management are critical.

## Development Best Practices

**Development Workflow Recommendations:**
- Always use before and after a feature upgrade `python3 monitor_cli.py start` to verify logs and to test the feature.

## TV Layout Optimization Guidelines

**Critical Fire TV Layout Principles** - Apply these patterns to ALL new features and dialogs:

**Ultra-Minimal Margin System:**
- Primary container padding: `@dimen/tv_action_safe_margin` (8dp) - keeps content as close to screen edges as possible
- Internal spacing: Use `@dimen/margin_medium` (16dp) and `@dimen/margin_small` (8dp) for compact layouts
- Avoid `@dimen/margin_large` (24dp) in dialogs and popups - too spacious for TV constraints

**Responsive Dialog/Popup Design Pattern:**
```xml
<!-- CORRECT: Responsive CardView that adapts to TV screen -->
<androidx.cardview.widget.CardView
    android:layout_width="0dp"                    <!-- Flexible width -->
    android:layout_height="wrap_content"
    android:layout_marginStart="@dimen/margin_medium"
    android:layout_marginEnd="@dimen/margin_medium"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintWidth_max="800dp">       <!-- Max width constraint -->

<!-- INCORRECT: Fixed width that may exceed TV bounds -->
<androidx.cardview.widget.CardView
    android:layout_width="600dp"                  <!-- Fixed width causes overflow -->
```

**TV-Safe Activity Layout Pattern:**
```xml
<!-- Apply to root ConstraintLayout of all activities -->
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background"
    android:padding="@dimen/tv_action_safe_margin">  <!-- 8dp TV-safe padding -->
```

**Form Layout Optimization:**
- Vertical spacing between form sections: `@dimen/margin_small` (8dp)
- Button container margins: `@dimen/margin_medium` (16dp)
- Internal CardView padding: `@dimen/margin_medium` (16dp)
- **Never** use `@dimen/margin_large` in dialogs - causes content overflow

**Testing Methodology:**
1. Build: `./gradlew clean assembleDebug`
2. Install: `./gradlew installDebug` 
3. Verify all content visible (especially bottom elements like "Server Port")
4. Test focus navigation through all interactive elements
5. Check focus indicators don't clip at screen edges

**Key Success Metrics:**
- ✅ All form fields visible without scrolling
- ✅ Content fits within 1920x1080 minus 8dp margins (1904x1064 usable area)
- ✅ Focus indicators display completely within screen bounds
- ✅ Consistent spacing using TV-optimized dimension resources

**Common Failure Patterns to Avoid:**
- Fixed width dialogs (600dp+ will overflow)
- Large margins/padding (`margin_large` in popups)
- Missing TV-safe container padding
- Excessive vertical spacing between form elements

**Layout Hierarchy:**
```
Activity (tv_action_safe_margin padding)
└── CardView (0dp width, 800dp max, margin_medium sides)
    └── LinearLayout (margin_medium internal padding)
        └── Form Elements (margin_small vertical spacing)
```

This pattern ensures all dialogs/popups work correctly with the ultra-compact Fire TV design while maintaining usability.
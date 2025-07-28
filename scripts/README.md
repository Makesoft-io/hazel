# Fire TV App Monitoring System

An autonomous testing and monitoring system for the Fire TV Web Viewer app that detects errors and automatically attempts fixes over ADB.

## 🌟 Features

- **Real-time Error Detection**: Monitors logcat output for crashes, ANRs, memory issues, and other errors
- **Autonomous Auto-fixing**: Automatically attempts to fix detected issues using predefined strategies
- **Health Monitoring**: Periodic checks of app state, memory usage, and UI responsiveness
- **Comprehensive Logging**: Detailed logging and reporting of all monitoring activities
- **Rate Limiting**: Prevents excessive fix attempts to avoid system instability
- **Emergency Recovery**: Fallback procedures for severe system issues
- **Configurable Rules**: Customizable monitoring thresholds and fix strategies

## 📁 System Components

### Core Modules

- **`monitor.py`** - Main monitoring daemon that orchestrates all components
- **`adb_manager.py`** - ADB connection and command execution manager
- **`error_detector.py`** - Error pattern detection and classification engine
- **`auto_fixer.py`** - Automated fix strategies for different error types
- **`monitor_cli.py`** - Command line interface for system control

### Configuration

- **`monitor_config.json`** - Main configuration file with monitoring rules and thresholds
- **`requirements.txt`** - Python dependencies (uses only standard library)

## 🚀 Quick Start

### Prerequisites

- Python 3.7+
- ADB (Android Debug Bridge) installed and in PATH
- Fire TV device connected to same network
- Fire TV Web Viewer app installed on device

### Setup

1. **Configure your device IP**:
   ```bash
   # Edit monitor_config.json
   {
     "device_ip": "192.168.4.94",  # Your Fire TV IP
     "device_port": 5555
   }
   ```

2. **Test connection**:
   ```bash
   cd scripts
   python monitor_cli.py test
   ```

3. **Check app status**:
   ```bash
   python monitor_cli.py status
   ```

### Usage

**Start monitoring daemon**:
```bash
python monitor_cli.py start
```

**Check current status**:
```bash
python monitor_cli.py status
```

**View recent logs**:
```bash
python monitor_cli.py logs --lines 50
```

**Show monitoring report**:
```bash
python monitor_cli.py report
```

**Show configuration**:
```bash
python monitor_cli.py config
```

## 🔧 Configuration

### Key Settings

```json
{
  "device_ip": "192.168.4.94",
  "device_port": 5555,
  "health_check_interval": 30,
  "max_fix_attempts_per_hour": 10,
  "enable_auto_fix": true,
  "enable_emergency_recovery": true
}
```

### Monitoring Rules

Configure how different error severities are handled:

- **Critical**: Always auto-fix, immediate action
- **High**: Auto-fix with notification
- **Medium**: Auto-fix if repeated (3+ times)
- **Low**: Log only, no auto-fix

### Thresholds

- Memory usage limit: 500MB
- Memory leak detection: 50% increase
- App startup timeout: 15 seconds
- Fix cooldown period: 60 seconds

## 🔍 Error Detection

The system monitors for:

### Application Errors
- App crashes (FATAL EXCEPTION)
- ANRs (Application Not Responding)
- Activity lifecycle issues
- Permission errors

### Performance Issues
- High memory usage (>500MB)
- Potential memory leaks
- WebView loading failures
- Network connectivity issues

### UI/Navigation Issues
- Missing UI elements
- Focus management problems
- Unexpected activity states

## 🛠️ Auto-fix Strategies

### App Crashes
1. Force stop app
2. Wait 2 seconds
3. Restart app
4. Verify successful startup

### Memory Issues
1. Force stop app
2. Clear system cache
3. Restart app
4. Monitor memory usage

### Network Errors
1. Navigate to refresh button
2. Send D-pad commands to refresh
3. Verify page reload

### UI Focus Issues
1. Send navigation keys to reset focus
2. Establish new focus point
3. Verify UI responsiveness

## 📊 Monitoring & Reporting

### Log Files
- **`monitor.log`** - Real-time monitoring activity
- **`monitor_report.json`** - Periodic status reports with statistics

### Statistics Tracked
- Total errors detected
- Fix attempts and success rate
- Memory usage trends
- App uptime and stability
- Error frequency by type

### Report Contents
- System uptime
- Error summary by type and severity
- Fix attempt statistics
- Device information
- Current app state

## 🚨 Emergency Recovery

When severe issues are detected:

1. **Trigger Conditions**:
   - 5+ consecutive critical errors
   - 20+ errors per minute
   - Memory usage >800MB

2. **Recovery Actions**:
   - Force stop app
   - Clear system cache
   - Ensure ADB connection
   - Restart app with verification

## 🔄 Background Tasks

The monitoring system runs three concurrent loops:

1. **Logcat Monitor**: Continuous logcat parsing for real-time error detection
2. **Health Check**: Periodic app state, memory, and UI verification
3. **Maintenance**: Routine cleanup, reporting, and system optimization

## 📝 Example Output

```bash
$ python monitor_cli.py status

🔍 Fire TV App Status Check
========================================
📱 Connecting to 192.168.4.94:5555...
✅ ADB connection successful
📋 Device: AFTMM
🤖 Android: 7.1.2 (API 25)
📦 App installed: ✅ Yes
🏃 App running: ✅ Yes
🎯 Current activity: com.webviewer.firetv/.MainActivity
🧠 Memory usage: 245.3 MB
📊 Monitor log found
📈 Last report: 2024-07-27T15:30:45.123Z
```

## ⚠️ Important Notes

- **Rate Limiting**: Maximum 10 fix attempts per hour to prevent system instability
- **Non-destructive**: Auto-fixes avoid clearing app data to preserve user profiles
- **Fail-safe**: System will stop attempting fixes if ADB connection is lost
- **Logging**: All activities are logged for debugging and analysis
- **Manual Override**: Can be stopped at any time with Ctrl+C

## 🔧 Troubleshooting

### Connection Issues
```bash
# Test ADB connection
adb connect 192.168.4.94:5555
adb devices

# Check device IP
# Fire TV Settings > My Fire TV > About > Network
```

### Permission Issues
```bash
# Ensure ADB debugging is enabled
# Fire TV Settings > My Fire TV > Developer Options > ADB Debugging
```

### App Issues
```bash
# Check app installation
python monitor_cli.py status

# View recent errors
python monitor_cli.py logs --lines 100
```

## 📈 Performance Impact

- **CPU Usage**: Minimal (async monitoring)
- **Memory Usage**: ~10-20MB for monitoring system
- **Network**: Negligible (only ADB commands)
- **Battery**: No impact (Fire TV is plugged in)

The monitoring system is designed to be lightweight and non-intrusive while providing comprehensive coverage of potential issues.
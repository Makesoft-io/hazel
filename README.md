# 🔥 Hazel - Fire TV Web Viewer

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Fire TV](https://img.shields.io/badge/Fire%20TV-FF9900?style=for-the-badge&logo=amazon&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

**Display your local web development servers on Amazon Fire TV**

[Features](#features) • [Installation](#installation) • [Usage](#usage) • [Development](#development) • [Contributing](#contributing)

</div>

---

## 📺 Overview

Hazel is a powerful Fire TV application that transforms your television into a development preview screen. Built specifically for Amazon Fire TV devices, it allows developers to view and interact with their local web development servers on the big screen using just the Fire TV remote.

Perfect for:
- 👨‍💻 Frontend developers testing responsive designs on large screens
- 🎮 Game developers previewing web-based games with controller input
- 📊 Dashboard and data visualization development
- 🎯 Team presentations and demos
- 🖥️ Any web application that benefits from TV-sized displays

## ✨ Features

### Core Functionality
- 🌐 **WebView Integration** - Full-featured web browser optimized for TV
- 🎮 **D-pad Navigation** - Complete control using Fire TV remote
- 🔄 **Browser Controls** - Back, forward, refresh, and home buttons
- ⚙️ **Easy Configuration** - Simple IP and port setup
- 🔌 **Network Flexibility** - Connect to any local development server
- 📁 **Multiple Profiles** - Save and switch between different server configurations

### TV-Optimized Design
- 📐 **10-foot UI** - Large, readable text and controls
- 🎯 **Clear Focus Indicators** - Glow effects and scaling animations
- 🌙 **Dark Theme** - High contrast design for comfortable viewing
- 📱 **Safe Zones** - Content properly positioned for all TV types
- ⚡ **Smooth Animations** - Responsive and fluid interactions

### Developer Features
- 🔧 **JavaScript Integration** - D-pad events translated to keyboard events
- 💾 **Persistent Settings** - Server configurations saved between sessions
- 🚨 **Error Handling** - Graceful error recovery with clear messages
- 📡 **Connection Status** - Real-time connection indicators
- 🔐 **Cleartext Support** - Works with HTTP development servers
- ⚡ **Quick Profile Switching** - Switch between development servers instantly

## 📋 Requirements

### Fire TV Device
- Amazon Fire TV Stick (2nd Gen or newer)
- Fire TV Stick 4K/4K Max
- Fire TV Cube
- Fire TV (3rd Gen or newer)
- Fire OS 5.0+ (Android 5.1+)

### Development Environment
- Local web server accessible on network
- Fire TV and development machine on same network
- Android Debug Bridge (ADB) for installation

## 🚀 Installation

### Method 1: Install via ADB

1. **Enable Developer Options on Fire TV:**
   ```
   Settings → My Fire TV → About → Click on Fire TV Stick 7 times
   Settings → My Fire TV → Developer Options → Enable ADB Debugging
   ```

2. **Find your Fire TV's IP address:**
   ```
   Settings → My Fire TV → About → Network
   ```

3. **Connect and install:**
   ```bash
   # Connect to Fire TV
   adb connect YOUR_FIRE_TV_IP:5555
   
   # Install the APK
   adb install hazel-v1.0.apk
   ```

### Method 2: Build from Source

```bash
# Clone the repository
git clone https://github.com/Makesoft-io/hazel.git
cd hazel

# Build the APK
./gradlew clean assembleDebug

# Install on connected Fire TV
./gradlew installDebug
```

## 📖 Usage

### Initial Setup

1. **Launch Hazel** on your Fire TV
2. **Create Profile** - Press the Profiles button (server icon) to create your first server profile
3. **Enter Details** - Profile name, IP address, and port
4. **Save Profile** - Your profile is saved and automatically activated
5. **Switch Profiles** - Use the Profiles button to switch between different servers

### Navigation Controls

| Remote Button | Action |
|--------------|--------|
| **↑ ↓ ← →** | Navigate within web content |
| **Select** | Click/activate elements |
| **Menu** | Open settings |
| **Back** | Browser back / Exit |
| **Home** | Return to Fire TV home |
| **Profiles** | Manage server profiles |

### Configuring Your Development Server

Your development server must be accessible from other devices on the network:

```bash
# React
npm start -- --host 0.0.0.0

# Vite
npm run dev -- --host 0.0.0.0

# Next.js
npm run dev -- -H 0.0.0.0

# Python
python -m http.server 8000 --bind 0.0.0.0

# Express.js
app.listen(3000, '0.0.0.0')
```

## 🛠️ Development

### Project Structure

```
hazel/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/webviewer/firetv/
│   │   │   │   ├── MainActivity.kt          # WebView container
│   │   │   │   ├── SettingsActivity.kt      # Configuration screen
│   │   │   │   ├── BrowserToolbarView.kt    # Navigation controls
│   │   │   │   └── ...
│   │   │   └── res/                         # Layouts, drawables, values
│   │   └── ...
│   └── build.gradle                          # App-level build config
├── build.gradle                              # Project-level build config
├── gradle.properties                         # Gradle configuration
└── local.properties                          # Local SDK path
```

### Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Check code style
./gradlew ktlintCheck
```

### Key Technologies

- **Language:** Kotlin 1.9.25
- **UI Framework:** AndroidX Leanback (TV-optimized)
- **Build System:** Gradle 8.13
- **Min SDK:** 21 (Android 5.0)
- **Target SDK:** 35 (Android 15)

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Quick Start

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Amazon Fire TV team for the development guidelines
- AndroidX Leanback library contributors
- The Kotlin community

## 📚 Resources

- [Fire TV App Development](https://developer.amazon.com/docs/fire-tv/getting-started-developing-apps-and-games-for-fire-tv.html)
- [Android TV Design Guidelines](https://developer.android.com/design/ui/tv)
- [Project Documentation](docs/)

---

<div align="center">

Made with ❤️ by [Makesoft](https://makesoft.io)

</div>
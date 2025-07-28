# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2024-07-27

### Added
- **Multiple Server Profiles** - Create and manage multiple server configurations
- Profile management UI with create, edit, and delete functionality
- Quick profile switching via toolbar button (server icon)
- Automatic migration from legacy single IP/port settings
- Profile validation for IP addresses and port numbers
- Last used timestamps for better profile organization
- JSON-based profile persistence with Gson

### Changed
- Enhanced browser toolbar with profiles button
- Updated MainActivity to use ProfileManager instead of direct SharedPreferences
- Improved URL display to show profile name and server details

### Technical
- Added ServerProfile data class with validation methods
- Implemented ProfileManager for CRUD operations
- Created ProfilesActivity, ProfileEditActivity, and ProfilesAdapter
- Added Gson dependency for JSON serialization
- Enhanced focus navigation for TV-optimized profile management

## [1.0.0] - 2024-07-27

### Added
- Initial release of Hazel Fire TV Web Viewer
- WebView integration for displaying web development servers
- D-pad navigation support with keyboard event translation
- Server configuration settings (IP address and port)
- Browser toolbar with navigation controls
- Error handling with retry functionality
- Persistent settings storage
- TV-optimized UI following Fire TV best practices
- Dark theme with high contrast colors
- Focus indicators with glow effects
- Loading states and progress indicators
- Connection status indicator
- Support for multiple Fire TV remote button codes

### Technical Details
- Built with Kotlin 1.9.25
- Targets Android SDK 35 (Android 15)
- Minimum SDK 21 (Android 5.0) for broad device support
- Uses AndroidX Leanback for TV-specific UI components
- Implements proper focus management for D-pad navigation
- Cleartext traffic support for local development servers
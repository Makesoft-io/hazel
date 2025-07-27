# Contributing to Hazel

First off, thank you for considering contributing to Hazel! 🎉 It's people like you that make Hazel such a great tool for the Fire TV development community.

## Code of Conduct

By participating in this project, you are expected to uphold our Code of Conduct:

- Use welcoming and inclusive language
- Be respectful of differing viewpoints and experiences
- Gracefully accept constructive criticism
- Focus on what is best for the community
- Show empathy towards other community members

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues as you might find out that you don't need to create one. When you are creating a bug report, please include as many details as possible:

- **Use a clear and descriptive title**
- **Describe the exact steps to reproduce the problem**
- **Provide specific examples**
- **Describe the behavior you observed and expected**
- **Include screenshots if possible**
- **Include your Fire TV model and Fire OS version**

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, please include:

- **Use a clear and descriptive title**
- **Provide a detailed description of the proposed enhancement**
- **Explain why this enhancement would be useful**
- **List any alternative solutions you've considered**

### Pull Requests

1. **Fork the repo** and create your branch from `main`
2. **Make your changes** following our coding standards
3. **Add tests** if applicable
4. **Ensure the test suite passes**
5. **Update documentation** as needed
6. **Submit the pull request**

## Development Process

### Setting Up Your Environment

1. Fork and clone the repository
   ```bash
   git clone https://github.com/YOUR_USERNAME/hazel.git
   cd hazel
   ```

2. Open the project in Android Studio

3. Sync Gradle and build the project

### Coding Standards

#### Kotlin Style Guide

- Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Keep functions small and focused
- Add KDoc comments for public APIs

#### Example:
```kotlin
/**
 * Validates the given IP address format.
 * 
 * @param ip The IP address string to validate
 * @return true if the IP address is valid, false otherwise
 */
fun isValidIpAddress(ip: String): Boolean {
    // Implementation
}
```

#### UI/UX Guidelines

- Follow Fire TV design guidelines
- Ensure all UI elements are focusable via D-pad
- Use appropriate text sizes (≥24sp for body text)
- Maintain high contrast ratios
- Test on different Fire TV models

### Commit Messages

- Use the present tense ("Add feature" not "Added feature")
- Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
- Limit the first line to 72 characters or less
- Reference issues and pull requests after the first line

Example:
```
Add multiple server profiles feature

- Allow users to save and switch between server configurations
- Add profile management UI in settings
- Store profiles in SharedPreferences

Fixes #123
```

### Testing

#### Unit Tests
```bash
./gradlew test
```

#### UI Tests
```bash
./gradlew connectedAndroidTest
```

#### Manual Testing Checklist
- [ ] App launches successfully
- [ ] Settings can be saved and loaded
- [ ] WebView loads configured server
- [ ] D-pad navigation works properly
- [ ] Error states display correctly
- [ ] Back button behavior is correct

### Documentation

- Update README.md if adding new features
- Update CLAUDE.md for architectural changes
- Add inline comments for complex logic
- Update CONTROLS_MANUAL.md for UI changes

## Project Structure

```
hazel/
├── app/src/main/java/com/webviewer/firetv/
│   ├── MainActivity.kt          # Main WebView screen
│   ├── SettingsActivity.kt      # Server configuration
│   ├── BrowserToolbarView.kt    # Custom toolbar
│   └── ...
├── app/src/main/res/           # Resources
├── docs/                       # Documentation
└── ...
```

## Release Process

1. Update version in `app/build.gradle`
2. Update CHANGELOG.md
3. Create a pull request with release notes
4. After merge, tag the release
5. Build and upload APK to releases

## Getting Help

- 💬 [GitHub Discussions](https://github.com/Makesoft-io/hazel/discussions) - Best for questions
- 🐛 [GitHub Issues](https://github.com/Makesoft-io/hazel/issues) - Best for bugs
- 📧 Email: support@makesoft.io

## Recognition

Contributors will be recognized in:
- The project README
- Release notes
- Our website (with permission)

Thank you for contributing to Hazel! 🔥📺
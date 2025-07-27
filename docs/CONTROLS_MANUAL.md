# Fire TV App Controls Manual

## Overview

This manual explains how to control the Fire TV Web Server Display app using your Fire TV remote. The app displays web development servers on your TV and translates remote control inputs into keyboard commands that web applications can understand.

## Fire TV Remote Layout

```
     [VOICE]
        |
    [POWER]
       
    ↑  ↑  ↑
    |  |  |
[←][SELECT][→]  
    |  |  |
    ↓  ↓  ↓
       
[BACK] [HOME] [MENU]
       
[REW] [PLAY] [FF]
```

## Main Screen Controls

### Navigation Controls (D-pad)
- **↑ Up Arrow**: Navigate up in web content / Focus browser toolbar
- **↓ Down Arrow**: Navigate down in web content / Return to web content from toolbar
- **← Left Arrow**: Navigate left in web content / Previous toolbar button
- **→ Right Arrow**: Navigate right in web content / Next toolbar button
- **SELECT (Center)**: Click/select in web content / Activate toolbar button

### System Controls
- **MENU Button**: Open app settings
- **BACK Button**: Go back in browser history (if available) or previous screen
- **HOME Button**: Exit to Fire TV home screen (system handled)

### Additional Menu Access
The app recognizes multiple button codes for opening settings to ensure compatibility with all Fire TV remote models:
- Standard MENU button
- Settings button (on some remotes)
- Mode button (on gaming controllers)
- Various Fire TV-specific codes

## Browser Toolbar Navigation

When you press **↑ Up** from the web content, you'll focus the browser toolbar. The toolbar contains these buttons in order:

1. **Back** - Navigate to previous page
2. **Forward** - Navigate to next page
3. **Refresh** - Reload current page
4. **Home** - Return to configured server URL
5. **Settings** - Open app settings

### Toolbar Navigation Pattern
- **← Left / → Right**: Move between buttons (circular - wraps around)
- **↓ Down**: Return focus to web content
- **SELECT**: Activate the focused button

## Settings Screen Controls

### Navigation
- **↑ Up / ↓ Down**: Move between IP address field, port field, and buttons
- **← Left / → Right**: Move cursor within text fields
- **SELECT**: 
  - On text field: Show on-screen keyboard
  - On Save button: Save settings and return
  - On Cancel button: Discard changes and return

### Text Input
When a text field is selected:
1. Press **SELECT** to show Fire TV on-screen keyboard
2. Use D-pad to navigate keyboard
3. Press **SELECT** to type characters
4. Press **BACK** when done typing

### Buttons
- **Save**: Saves the server configuration and returns to main screen
- **Cancel**: Returns without saving changes

## Web Content Interaction

The app translates Fire TV remote buttons into standard keyboard events that web applications can detect:

| Remote Button | Web Keyboard Event |
|--------------|-------------------|
| ↑ Up         | ArrowUp          |
| ↓ Down       | ArrowDown        |
| ← Left       | ArrowLeft        |
| → Right      | ArrowRight       |
| SELECT       | Enter            |

This means any web application that responds to arrow keys and Enter will work with the Fire TV remote.

## Focus Indicators

The app provides visual feedback for the currently focused element:
- **Glow Effect**: Bright outline around focused items
- **Scale Effect**: Focused buttons slightly enlarge
- **Color Change**: Focused elements use accent color

## Tips for Best Experience

1. **Web App Design**: Web applications work best when they:
   - Support keyboard navigation (arrow keys + Enter)
   - Have clear focus indicators
   - Use large, TV-friendly text (24px or larger)
   - Have sufficient spacing between interactive elements

2. **Navigation Speed**: Hold direction buttons for continuous scrolling in web content

3. **Quick Access**: Press MENU from anywhere to quickly access settings

4. **Error Recovery**: If the web page fails to load:
   - Use the Refresh button in the toolbar
   - Check server is running and accessible
   - Verify IP and port in settings

## Troubleshooting

### Remote Not Working
- Ensure Fire TV remote is paired and has batteries
- Try pressing different buttons to wake the remote
- Check if other Fire TV apps respond to remote

### Navigation Not Working in Web Content
- Verify the web page supports keyboard navigation
- Some web apps may require mouse/touch and won't work with D-pad
- Try refreshing the page
- Check browser console for JavaScript errors

### Can't Access Settings
- Try alternative menu buttons if standard MENU doesn't work
- Use the Settings button in the browser toolbar
- Restart the app if settings won't open

### Focus Lost
- Press ↑ Up to focus toolbar, then ↓ Down to return to content
- If focus indicators aren't visible, try pressing SELECT
- Navigate using D-pad to restore focus

### Settings Not Saving
- Ensure IP address format is correct (e.g., 192.168.1.100)
- Port must be between 1 and 65535
- Press Save button (not BACK) to save changes

## Advanced Usage

### For Developers
- Monitor key events in your web app using JavaScript:
  ```javascript
  document.addEventListener('keydown', (event) => {
    console.log('Key pressed:', event.key);
  });
  ```

- The app sends standard KeyboardEvent with these properties:
  - `key`: The key value (ArrowUp, ArrowDown, etc.)
  - `bubbles`: true
  - `cancelable`: true

### Server Configuration
- Development server must bind to `0.0.0.0` (not `localhost`)
- Ensure Fire TV and development machine are on same network
- No firewall blocking the configured port

## Quick Reference Card

| Action | Button |
|--------|--------|
| Open Settings | MENU |
| Navigate | D-pad (↑↓←→) |
| Select/Click | CENTER/SELECT |
| Go Back | BACK |
| Refresh Page | ↑ then → to Refresh |
| Return Home | ↑ then → to Home |

---

For more information or to report issues, visit the project repository.
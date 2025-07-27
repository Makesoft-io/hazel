Here's a Visual UI/UX Checklist for Fire OS TV Apps, specifically tailored to the 10-foot experience and remote/D-pad navigation on Amazon Fire TV devices.

✅ 1. Typography & Text
Feature	Best Practice	Notes
Font Size	≥ 30sp for titles, ≥ 24sp for body text	Larger for 4K screens
Line Height	1.2–1.5x font size	Improves readability
Text Contrast	4.5:1 minimum	Use WCAG contrast checker
Max Characters per Line	~30–40 chars	Avoid edge-to-edge paragraphs

✅ 2. Colors & Focus Indicators
Feature	Best Practice	Notes
Focus Style	Clear visual indication (scale, glow, color, border)	Required for D-pad navigation
Color Palette	Use high-contrast colors	Avoid pure white background; use dark mode theme
Selected State	Visibly different from focus	Example: focused = glow, selected = checkmark

✅ 3. Spacing & Layout
Feature	Best Practice	Notes
Padding/Margins	24dp–48dp for major sections	Avoid clutter
Safe Zone	Keep content 5–10% from edges	Prevent clipping on overscan TVs
Card/Grid Gutter	At least 12dp spacing between items	Use Leanback’s SpacingDecoration if in Android

✅ 4. Navigation & Focus Flow
Feature	Best Practice	Notes
D-Pad Navigation	All interactive elements must be focusable	Set android:focusable="true"
Directional Focus Logic	Use nextFocusUp/Down/Left/Right	Or use requestFocus() programmatically
Avoid Focus Traps	Don’t nest focus inside scroll views without escape	Use setDescendantFocusability wisely

✅ 5. Visual Hierarchy
Feature	Best Practice	Notes
Hero Elements	Big banner with clear CTA (Play, Open, etc.)	Typically at top of home screen
Secondary Cards	Use thumbnails with clear text labels	Cards should scale on focus
Grouping	Use rows, columns, or grids	Title each section clearly (e.g. "Recently Watched")

✅ 6. Images & Thumbnails
Feature	Best Practice	Notes
Resolution	Use multiple image sizes (480p/720p/1080p/4K)	Avoid full-res images for thumbnails
Aspect Ratio	Stick to 16:9 or 2:3	Avoid stretched images
Placeholders	Show skeleton loaders	Improves perceived speed

✅ 7. Loading & Transitions
Feature	Best Practice	Notes
Animated Focus	Use scale + shadow or glow on focus	Keep under 200ms
Lazy Load	Don’t load entire catalog up front	Use RecyclerView with pagination
Progress Indicators	Use spinners or shimmer	Always show during buffering/loading

✅ 8. Accessibility
Feature	Best Practice	Notes
Screen Reader	Add content descriptions	android:contentDescription for all important UI
Closed Captions	Use system CC APIs	Respect Fire TV caption settings
Colorblind-Safe Colors	Avoid red-green only indicators	Use icon + color redundancy

✅ 9. Error Handling
Feature	Best Practice	Notes
Network Errors	Show retry button	Don’t leave blank screen
Playback Failures	Use toast or modal	“Video failed to load. Try again.”
Fallbacks	Placeholder thumbnails, default text	Prevent app crashes or broken layouts

✅ 10. Bonus UI Enhancements
Feature	Best Practice	Notes
Recommendations Row	Integrate with Fire OS launcher	Requires additional permissions
Voice Search Integration	Alexa Intent Support	Opens content via voice
Animations	Use subtle transitions, not flashy	Avoid distraction from main content

🧪 Testing Checklist (Rapid)
✅ D-pad navigates all focusable elements

✅ Focus indicators are visually clear

✅ Content is readable from 10 feet

✅ App works on both 1080p and 4K Fire TVs

✅ Handles offline mode gracefully

✅ App is usable without a touchscreen
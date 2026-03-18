# Research: Live Composable Onboarding

**Feature**: 007-live-onboarding | **Date**: 2026-03-15

## R1: Device Frame Composable Pattern

**Task**: How to render a realistic phone bezel/frame as a composable that wraps inner content.

**Decision**: Pure Compose Canvas/Shape approach — draw the device bezel using `Canvas`, `RoundedCornerShape`, and layered `Box` composables. No bitmap assets needed.

**Rationale**:
- A composable-only approach renders at any resolution, scales with `Modifier.aspectRatio()`, and has zero asset management overhead
- The "frame" is simply a dark rounded rectangle with a colored status bar at the top, containing a live clock and battery indicator
- Inner content is clipped to the screen area with `Modifier.clip(RoundedCornerShape(...))`
- This matches the approach from the [Reddit post](https://www.reddit.com/r/androiddev/comments/1rticy4/) where the device frame is itself a composable

**Alternatives Considered**:
- **Bitmap frame overlays**: Rejected — requires multiple resolution assets, doesn't scale, hard to maintain
- **Platform system UI rendering**: Rejected — too complex, not Compose Multiplatform compatible

**Implementation Detail**:
```
DeviceFrame(platformStyle: DeviceFrameStyle) {
    // Inner content rendered here, clipped to "screen" area
    LaunchCardHeaderOverlay(...)
}
```

Where `DeviceFrameStyle` is an enum/sealed class with `Android`, `IPhone`, `Generic` variants. This does NOT require `expect`/`actual` — instead, we detect platform at runtime via `getPlatform()` and select the style. This avoids 3 separate platform files for what is purely a visual difference.

## R2: Platform Detection vs expect/actual for DeviceFrame

**Task**: Should `DeviceFrame` use `expect`/`actual` or runtime platform detection?

**Decision**: Runtime platform detection with a `DeviceFrameStyle` sealed class. No `expect`/`actual` needed.

**Rationale**:
- The device frame is purely visual — no platform APIs are involved
- `getPlatform().name` already exists in the project and identifies Android/iOS/Desktop
- Using `expect`/`actual` would create 3 near-identical files differing only in corner radii and notch shape
- A single `DeviceFrame.kt` in `commonMain` with a `style` parameter is simpler and more maintainable

**Alternatives Considered**:
- **expect/actual**: Rejected — unnecessary complexity for a visual-only composable; no platform-specific APIs needed

**Updated Plan**: Remove `expect`/`actual` DeviceFrame files from the project structure. Single `DeviceFrame.kt` in `commonMain/ui/onboarding/`.

## R3: Embedding Live Composables at Reduced Scale

**Task**: How to fit a full-screen composable (e.g., ScheduleContent) inside a small device frame.

**Decision**: Use `Modifier.graphicsLayer(scaleX = scale, scaleY = scale)` combined with a fixed-size `Box` to render the composable at its natural size, then scale it down visually.

**Rationale**:
- `graphicsLayer` transformation happens at the render level, not the layout level — the composable thinks it has full space but renders smaller
- This preserves all layout behavior, text sizes, and spacing exactly as in production
- The approach is used in the Reddit example and is well-documented in Compose
- Alternative: `Modifier.scale()` is a shorthand for the same thing

**Pattern**:
```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(9f / 16f)  // Phone aspect ratio
) {
    Box(
        modifier = Modifier
            .size(width = 360.dp, height = 640.dp)  // "Virtual" phone resolution
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                transformOrigin = TransformOrigin(0f, 0f)
            )
    ) {
        // Full-size composable rendered here
        SchedulePreviewContent(...)
    }
}
```

**Constraints**:
- Must disable user interaction inside the scaled composable (it's a preview, not interactive)
- Use `Modifier.pointerInput(Unit) {}` to consume all touch events, or `enabled = false` on interactive elements

## R4: ScheduleContent Stateless Preview Variant

**Task**: `ScheduleContent` is `private` and requires a `ScheduleViewModel`. Need a preview-friendly variant.

**Decision**: Create a new `SchedulePreviewContent` composable that renders a static mock version of the schedule UI using `PreviewData` launches.

**Rationale**:
- `ScheduleContent` is tightly coupled to `ScheduleViewModel` (pull-to-refresh, infinite scroll, search, filtering)
- Making it public and adding a "preview mode" would violate single-responsibility
- A lightweight `SchedulePreviewContent` composable renders a `LazyColumn` with 3-4 mock `ScheduleLaunchView` items, tabs (non-interactive), and looks like the real schedule
- This avoids Koin/ViewModel dependencies during onboarding

**Alternatives Considered**:
- **Refactor ScheduleContent to be stateless**: Rejected — too invasive; the composable has complex VM interactions (pager state sync, search, filter sheets, infinite scroll)
- **Screenshot/render-to-bitmap**: Rejected — platform-specific, doesn't auto-update with UI changes

**Implementation**: New file `ui/onboarding/pages/SchedulePage.kt` contains both the page wrapper and `SchedulePreviewContent`.

## R5: Notification Filters Preview Content

**Task**: What composable to embed for the notification filters onboarding page.

**Decision**: Create a `NotificationFiltersPreviewContent` composable that renders a static selection of `AgencyCheckboxItem`, `LocationCheckboxItem`, and `NotificationTopicToggle` composables with mock data.

**Rationale**:
- The existing helper composables (`AgencyCheckboxItem`, `LocationCheckboxItem`, `NotificationTopicToggle`) are `private` in `NotificationSettingsScreen.kt`
- Rather than making them public (which would increase the API surface), create a lightweight preview that mimics the look:
  - A "Launch Providers" header with 4 agency checkboxes (SpaceX ✓, NASA ✓, Blue Origin ☐, ULA ☐)
  - A "Locations" header with 3 location items (Florida ✓, Texas ☐, California ☐)
  - A "Notifications" header with 2 topic toggles (24h Notice ✓, 10min Notice ✓)

**Alternatives Considered**:
- **Make existing composables public**: Rejected — increases coupling; the composables are designed for the settings screen context
- **Reuse SettingsViewModel with mock data**: Rejected — unnecessary complexity for a static preview

## R6: Live Clock in Device Frame Status Bar

**Task**: How to display a live-updating clock in the device frame's status bar.

**Decision**: Use `LaunchedEffect` with `delay(1000)` loop to update a `mutableStateOf<String>` clock value using `DateTimeUtil` or `Clock.System.now()`.

**Rationale**:
- Matches the Reddit post's detail of a live-updating clock
- Simple implementation with minimal recomposition (only the text changes)
- Uses existing `DateTimeUtil` for formatting consistency

**Pattern**:
```kotlin
var currentTime by remember { mutableStateOf("") }
LaunchedEffect(Unit) {
    while (true) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        currentTime = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
        delay(60_000) // Update every minute is sufficient
    }
}
```

## R7: Notification Permission Request Integration

**Task**: How to trigger notification permission from the onboarding page.

**Decision**: Use existing `requestPlatformNotificationPermission()` expect/actual function, called from the onboarding page's button click handler.

**Rationale**:
- Android implementation uses `AndroidNotificationPermissionHandler` + `NotificationPermissionManager.requestPermissionFromSettings()`
- iOS implementation is currently a TODO (returns `true`) — needs proper `UNUserNotificationCenter.requestAuthorization()` 
- Desktop implementation returns `true` (no-op)
- The permission request is a coroutine function, works well with `rememberCoroutineScope()`

**Note**: The iOS implementation is a known gap. For this feature, the Android implementation works correctly. iOS will be addressed as a follow-up.

## R8: HorizontalPager for Carousel

**Task**: Confirm HorizontalPager availability and usage patterns in the project.

**Decision**: Use `HorizontalPager` from `foundation.pager` — already used in 3+ places in the codebase.

**Rationale**:
- `ScheduleScreen.kt` uses a 2-page `HorizontalPager` for Upcoming/Previous tabs
- `StarshipScreen.kt` and `DebugSettingsScreen.kt` also use it
- Well-tested pattern in the project
- Supports `PagerState.animateScrollToPage()` for the "Next" button

**Pattern**:
```kotlin
val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
HorizontalPager(state = pagerState) { page ->
    when (page) {
        0 -> LaunchCardPage()
        1 -> SchedulePage()
        2 -> NotificationFiltersPage()
        3 -> NotificationPermissionPage(onRequestPermission = { ... })
    }
}
```

## Summary of Resolved Unknowns

| Unknown | Resolution |
|---------|-----------|
| Device frame pattern | Pure Compose Canvas/Box approach, single `commonMain` file |
| expect/actual vs detection | Runtime platform detection — no expect/actual needed |
| Scaling live composables | `Modifier.graphicsLayer(scaleX, scaleY)` with fixed-size Box |
| ScheduleContent reuse | New `SchedulePreviewContent` with mock data |
| Notification filters | New `NotificationFiltersPreviewContent` with mock data |
| Live clock | `LaunchedEffect` + `Clock.System.now()` updated every minute |
| Notification permission | Existing `requestPlatformNotificationPermission()` expect/actual |
| Carousel | `HorizontalPager` — already used in 3+ places |
| Visual layout | ClashMarket-style: background image + centered device frame + title/subtitle + dots + Next button |
| Background asset | Space-themed image or gradient fallback (existing `spaceGradient` pattern in codebase) |

## R9: Visual Layout Design (from ClashMarket Reference)

**Task**: Define the exact per-page visual layout based on the ClashMarket reference screenshot.

**Decision**: Match the ClashMarket onboarding pattern exactly, adapted for SpaceLaunchNow's space theme.

**Reference Analysis** (from screenshot of ClashMarket on Android Pixel 9 + iPhone 17 Pro):

### Layout Structure (top to bottom)
1. **Full-bleed background**: Landscape/nature image covering entire screen. For SpaceLaunchNow, use a space-themed image (rocket launch, nebula, or starfield)
2. **"Skip" text button**: Top-right corner, over the background — simple text, no outline
3. **Device frame**: Centered horizontally, takes ~50-60% of screen height
   - **Dark bezel**: Charcoal/dark gray rounded rectangle with inner screen cutout
   - **Status bar**: Inside the frame, shows live clock (e.g., "03:23"), signal/battery icons
   - **Platform-specific shape**:
     - Android (Pixel): Rounded corners, thin bezel, small pill camera cutout at top-center
     - iOS (iPhone): Rounded corners, Dynamic Island notch at top-center
   - **Live content**: Actual composable rendered inside the screen area
4. **Title text**: Bold, large font, centered below the device frame — e.g., "Track Every Launch"
5. **Subtitle text**: Lighter weight, slightly smaller, centered below title — e.g., "See detailed launch info and countdown timers for every mission worldwide."
6. **Wavy-line progress bar**: Straight track line spanning the width, with a sine-wave line drawn over the filled portion indicating current progress through the onboarding flow
7. **"Next" button**: Full-width, rounded rectangle, accent/primary color, centered at bottom

### Color/Typography Notes
- Text color: High contrast over the background (cream/white in ClashMarket — for SpaceLaunchNow, use `Color.White` or `MaterialTheme.colorScheme.onSurface`)
- Button color: App's primary/accent color (warm gold in ClashMarket — for SpaceLaunchNow, use `MaterialTheme.colorScheme.primary`)
- Device bezel: `Color(0xFF1A1A1A)` or similar dark charcoal
- Screen area inside bezel: Actual content with app theme applied

### Responsive Behavior
- On tablets: Device frame maintains phone aspect ratio (~9:19.5), doesn't stretch
- On phones: Frame fills available width with horizontal padding (~20-24dp)

**Alternatives Considered**:
- **Gradient-only background**: Could work as fallback if no image asset is available; existing `spaceGradient` (deep blues/purples) from `OnboardingScreen` could be reused
- **No device frame (just floating composable)**: Rejected — the device frame is the hero visual element that makes the pattern distinctive

### Background Asset Strategy
- **Option A (preferred)**: Add a space-themed `.webp` image to `commonMain/composeResources/drawable/` — e.g., `onboarding_bg.webp`
- **Option B (gradient fallback)**: Reuse the space gradient from existing `OnboardingContent`:
  ```kotlin
  Brush.verticalGradient(
      colors = listOf(Color(0xFF0A0E2A), Color(0xFF1A1040), Color(0xFF2A1060))
  )
  ```
- Start with Option B (gradient) to avoid asset licensing; add image later as enhancement

## R10: Wavy-Line Progress Bar

**Task**: Replace standard page indicator dots with a distinctive wavy-line progress bar.

**Decision**: Use Compose `Canvas` with `Path` to draw a straight track line and animated sine-wave progress line.

**Rationale**:
- A wavy/sine-wave progress line is visually distinctive and fits the space/telemetry aesthetic
- `Canvas` + `Path` is fully cross-platform (commonMain) with zero platform-specific code
- Animates smoothly using `pagerState.currentPage` + `pagerState.currentPageOffsetFraction`

**Implementation Approach**:

```kotlin
@Composable
fun WavyProgressBar(
    currentPage: Int,
    pageCount: Int,
    pageOffsetFraction: Float,
    modifier: Modifier = Modifier
)
```

**Drawing Logic**:
1. **Track line**: Straight horizontal line spanning full width — muted color (`Color.White.copy(alpha = 0.3f)`)
2. **Progress line**: Sine-wave path drawn from `x = 0` to `x = progressX` where `progressX = width * (currentPage + pageOffsetFraction) / pageCount`
3. **Sine wave**: `Path.lineTo(x, centerY + sin(x * frequency) * amplitude)` iterated in small steps (e.g., 1px increments)
4. **Stroke**: `drawPath(path, color = primaryColor, style = Stroke(width = 3.dp))`
5. **Animation**: Progress animates automatically because `pageOffsetFraction` updates continuously during swipe

**Key Parameters**:
- `amplitude`: Wave height — ~4-6dp for subtle, ~8-10dp for prominent
- `frequency`: Wave density — `2π * waveCount / width` where `waveCount` is the number of full sine waves across the bar
- `strokeWidth`: Line thickness — 2-3dp

**Alternatives Considered**:
- **Animated dots**: Rejected — user specifically requested wavy line
- **Linear progress bar**: Rejected — too plain, user wants the wave effect
- **Lottie animation**: Rejected — adds a dependency; Canvas approach is lightweight and fully customizable

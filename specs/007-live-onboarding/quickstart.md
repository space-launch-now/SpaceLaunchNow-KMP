# Quickstart: Live Composable Onboarding

**Feature**: 007-live-onboarding | **Date**: 2026-03-15

## Prerequisites

- Java 21 (JetBrains JDK 21)
- Android Studio / IntelliJ with Compose Multiplatform plugin
- `.env` file with `API_KEY` in project root
- Run `./gradlew openApiGenerate` (if after a clean checkout)

## Build & Run

```bash
# Desktop (quickest iteration)
./gradlew desktopRun --quiet

# Android
./gradlew installDebug

# Run tests
./gradlew test
```

## Key Files to Modify

### 1. Screen.kt — Add Navigation Route
```kotlin
// Add after existing Onboarding route
@Serializable
data object LiveOnboarding
```

### 2. AppPreferences.kt — Add DataStore Key
```kotlin
// In companion object
private val LIVE_ONBOARDING_COMPLETED = booleanPreferencesKey("live_onboarding_completed")

// Add flow
val liveOnboardingCompletedFlow: Flow<Boolean> = dataStore.data.map { prefs ->
    prefs[LIVE_ONBOARDING_COMPLETED] ?: false
}

// Add setter
suspend fun setLiveOnboardingCompleted(completed: Boolean) {
    dataStore.edit { prefs -> prefs[LIVE_ONBOARDING_COMPLETED] = completed }
}
```

### 3. App.kt — Update Navigation Sequencing
```kotlin
// In start route logic, check live onboarding BEFORE paywall:
val liveOnboardingCompleted by appPreferences.liveOnboardingCompletedFlow.collectAsState(initial = null)

val startRoute: Any? = when {
    liveOnboardingCompleted == null -> null  // Loading
    liveOnboardingCompleted == false -> LiveOnboarding  // New users see live onboarding first
    onboardingPaywallShown == true -> Home
    else -> Onboarding  // Then paywall
}
```

### 4. DeviceFrame.kt — Device Frame Composable (new)
```kotlin
@Composable
fun DeviceFrame(
    modifier: Modifier = Modifier,
    style: DeviceFrameStyle = detectDeviceFrameStyle(),
    content: @Composable () -> Unit
)
```
- Dark bezel (`Color(0xFF1A1A1A)`) with rounded corners
- Status bar inside frame with live clock (updated every minute)
- Platform-specific shape: Android pill camera cutout vs iOS Dynamic Island
- Inner content clipped to screen area

### 5. LiveOnboardingScreen.kt — Carousel Entry Point (new)

**Visual layout per the ClashMarket reference:**
```
Background: Full-bleed space gradient (or background image)
├── "Skip" text button (top-right)
├── HorizontalPager (4 pages)
│   └── OnboardingPage
│       ├── DeviceFrame (centered, ~50-60% height)
│       │   ├── Status bar with live clock
│       │   └── Live composable content
│       ├── Bold title text
│       └── Lighter subtitle text
├── Wavy-line progress bar (straight track + sine-wave fill)
└── Full-width "Next" button (primary color)
```

```kotlin
@Composable
fun LiveOnboardingScreen(
    appPreferences: AppPreferences = koinInject(),
    onComplete: () -> Unit
)
```

### 6. OnboardingPage.kt — Single Page Layout (new)
```kotlin
@Composable
fun OnboardingPage(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    deviceFrameContent: @Composable () -> Unit
)
```

## Page Content Files (new)

| File | Purpose | Key Composable |
|------|---------|---------------|
| `LaunchCardPage.kt` | Launch card inside device frame | Uses `LaunchCardHeaderOverlay` with `PreviewData.launchNormalSpaceX` |
| `SchedulePage.kt` | Schedule screen preview | New `SchedulePreviewContent` with mock launch list |
| `NotificationFiltersPage.kt` | Filter UI preview | Mock agency/location checkboxes and topic toggles |
| `NotificationPermissionPage.kt` | Permission request | "Enable Notifications" button calling `requestPlatformNotificationPermission()` |

## Rename

- `OnboardingScreen.kt` → `OnboardingPaywallScreen.kt`
- Update all imports in `App.kt` that reference `OnboardingScreen`

## Testing Strategy

- All preview composables are stateless — no ViewModel or API dependencies
- Use `PreviewData` for all mock data
- Verify dual previews (light + dark) for each new composable
- Desktop run verifies layout without device deployment

## Verification Checklist

- [ ] `LiveOnboarding` route added to `Screen.kt`
- [ ] `LIVE_ONBOARDING_COMPLETED` key in `AppPreferences`
- [ ] `DeviceFrame` renders with live clock
- [ ] 4 carousel pages render correctly
- [ ] Page 4 triggers notification permission on Android
- [ ] "Skip" button marks onboarding complete
- [ ] Navigation: LiveOnboarding → OnboardingPaywall → Home
- [ ] Existing users skip LiveOnboarding
- [ ] Dual previews for all new composables
- [ ] `OnboardingScreen` → `OnboardingPaywallScreen` rename complete

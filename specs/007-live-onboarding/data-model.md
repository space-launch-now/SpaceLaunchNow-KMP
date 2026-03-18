# Data Model: Live Composable Onboarding

**Feature**: 007-live-onboarding | **Date**: 2026-03-15

## Entities

### 1. DeviceFrameStyle

**Purpose**: Defines the visual style of the device frame bezel.

```kotlin
sealed class DeviceFrameStyle {
    /** Android-style frame: rounded corners, pill-shaped camera cutout at top-center */
    data object Android : DeviceFrameStyle()
    /** iPhone-style frame: Dynamic Island notch at top-center */
    data object IPhone : DeviceFrameStyle()
    /** Generic rectangular frame for desktop previews */
    data object Generic : DeviceFrameStyle()
}
```

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| (sealed variants) | `Android`, `IPhone`, `Generic` | Platform visual style |

**Derived at runtime**: `getPlatform().name` → maps to appropriate style.

### 2. OnboardingPageData

**Purpose**: Data class for each onboarding carousel page's metadata.

```kotlin
data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val pageType: OnboardingPageType
)
```

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| `title` | `String` | Large heading text below the device frame |
| `subtitle` | `String` | Description text below the title |
| `pageType` | `OnboardingPageType` | Enum identifying which content to render |

### 3. OnboardingPageType

**Purpose**: Identifies the content for each onboarding page.

```kotlin
enum class OnboardingPageType {
    LAUNCH_CARD,
    SCHEDULE,
    NOTIFICATION_FILTERS,
    NOTIFICATION_PERMISSION
}
```

### 4. LiveOnboardingState (ViewModel State)

**Purpose**: UI state for the `LiveOnboardingScreen`.

```kotlin
data class LiveOnboardingState(
    val currentPage: Int = 0,
    val totalPages: Int = 4,
    val notificationPermissionGranted: Boolean = false,
    val isComplete: Boolean = false
)
```

**Fields**:
| Field | Type | Description |
|-------|------|-------------|
| `currentPage` | `Int` | Current page index (0-based) |
| `totalPages` | `Int` | Total number of pages (4) |
| `notificationPermissionGranted` | `Boolean` | Whether notification permission was granted |
| `isComplete` | `Boolean` | Whether the user completed/skipped onboarding |

## DataStore Keys

### New Key in AppPreferences

```kotlin
private val LIVE_ONBOARDING_COMPLETED = booleanPreferencesKey("live_onboarding_completed")
```

**Flow**: `liveOnboardingCompletedFlow: Flow<Boolean>`  
**Setter**: `suspend fun setLiveOnboardingCompleted(completed: Boolean)`

## Navigation Route

### New Route in Screen.kt

```kotlin
@Serializable
data object LiveOnboarding
```

## State Transitions

```
App Launch
    │
    ├── liveOnboardingCompleted == null → Defer (loading)
    ├── liveOnboardingCompleted == false → LiveOnboarding screen
    │       │
    │       ├── User swipes through pages 1-3
    │       ├── Page 4: Request notification permission
    │       │       ├── Permission granted → mark complete
    │       │       └── Permission denied/skipped → mark complete
    │       └── "Skip" at any point → mark complete
    │       │
    │       └── Navigate to → OnboardingPaywall (if not shown)
    │                            │
    │                            └── Navigate to → Home
    │
    └── liveOnboardingCompleted == true → Check onboardingPaywallShown
            ├── false → OnboardingPaywall
            └── true → Home
```

## Relationships

- `LiveOnboardingScreen` depends on `PreviewData` (mock data — no API)
- `LiveOnboardingScreen` depends on `requestPlatformNotificationPermission()` (page 4)
- `DeviceFrame` depends on `getPlatform()` for style detection
- `AppPreferences` gains new `LIVE_ONBOARDING_COMPLETED` key
- `App.kt` navigation logic must check `liveOnboardingCompleted` before `onboardingPaywallShown`

# Quickstart: Analytics Module

**Feature**: 011-analytics-module | **Date**: 2026-04-05

## Overview

The analytics module provides a unified, multi-provider analytics system for SpaceLaunchNow.
It uses a plugin architecture where any analytics platform can be added by implementing a single
interface and registering it in Koin.

## Architecture at a Glance

```
ViewModel/UI ──track(event)──> AnalyticsManager ──fan-out──> Provider 1 (Firebase)
                                                          ──> Provider 2 (Console)
                                                          ──> Provider N (Amplitude, etc.)
```

## Quick Usage

### 1. Track an Event from a ViewModel

```kotlin
class LaunchViewModel(
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    fun onLaunchClicked(launchId: String, launchName: String) {
        analyticsManager.track(AnalyticsEvent.LaunchViewed(launchId, launchName))
    }

    fun onShareClicked(launchId: String) {
        analyticsManager.track(AnalyticsEvent.LaunchShared(launchId, method = "share_sheet"))
    }
}
```

### 2. Automatic Screen Tracking (already wired in NavHost)

```kotlin
// In your main NavHost composable — this is already set up:
AnalyticsScreenTracker(
    navController = navController,
    analyticsManager = analyticsManager
)
// No manual screen tracking needed!
```

### 3. Track Subscription Events from BillingManager

```kotlin
// In SubscriptionViewModel
fun onPurchaseStarted(productId: String) {
    analyticsManager.track(AnalyticsEvent.PurchaseStarted(productId))
}

fun onPurchaseCompleted(productId: String, revenue: Double?) {
    analyticsManager.track(AnalyticsEvent.PurchaseCompleted(productId, revenue))
}
```

### 4. User Identity (set after login/restore)

```kotlin
// In UserViewModel
fun onUserIdentified(userId: String) {
    analyticsManager.setUserId(userId)
    analyticsManager.setUserProperty("subscription_tier", "premium")
}

fun onLogout() {
    analyticsManager.reset()
}
```

## Adding a New Analytics Provider

### Step 1: Implement the Interface

```kotlin
// commonMain (or platform-specific source set if SDK requires it)
class AmplitudeAnalyticsProvider : AnalyticsProvider {
    override val name = "amplitude"
    override var isEnabled = true

    override suspend fun initialize(config: Map<String, Any>) {
        // Initialize Amplitude SDK
    }

    override suspend fun trackEvent(event: AnalyticsEvent) {
        // Convert event.name and event.toParameters() to Amplitude format
        amplitude.logEvent(event.name, event.toParameters())
    }

    override suspend fun trackScreenView(screenName: String, screenClass: String?) {
        amplitude.logEvent("screen_view", mapOf("screen_name" to screenName))
    }

    override fun setUserId(userId: String?) {
        amplitude.setUserId(userId)
    }

    override fun setUserProperty(key: String, value: String) {
        amplitude.setUserProperty(key, value)
    }

    override suspend fun flush() { amplitude.flush() }
    override fun reset() { amplitude.reset() }
}
```

### Step 2: Register in Koin

```kotlin
// In platform-specific nativeConfig() or analyticsModule
single<AnalyticsProvider>(named("amplitude")) {
    AmplitudeAnalyticsProvider()
}
```

That's it. The `AnalyticsManager` automatically picks up all registered `AnalyticsProvider`
instances via Koin's `getAll<AnalyticsProvider>()`.

## Adding a New Event

### Step 1: Add to the Sealed Class

```kotlin
// In AnalyticsEvent.kt
data class MyNewEvent(val itemId: String, val detail: String) :
    AnalyticsEvent("my_new_event") {
    override fun toParameters() = mapOf(
        "item_id" to itemId,
        "detail" to detail
    )
}
```

### Step 2: Use It

```kotlin
analyticsManager.track(AnalyticsEvent.MyNewEvent(itemId, detail))
```

No other changes needed — all providers automatically receive the new event.

## Usage: Engagement & Video Events

### Track Third-Party Referrals (Flight Club, etc.)

```kotlin
// In FlightClubCard onClick
analyticsManager.track(AnalyticsEvent.ThirdPartyReferral(
    provider = "flight_club",
    url = flightClubUrl,
    contentType = "launch",
    contentId = launch.id
))

// In any outbound partner link
analyticsManager.track(AnalyticsEvent.ThirdPartyReferral(
    provider = "spaceflight_now",
    url = articleUrl
))
```

### Track Content Shares

```kotlin
// In share action handler
analyticsManager.track(AnalyticsEvent.ContentShared(
    contentType = "article",
    contentId = article.id,
    method = "share_sheet"
))
```

### Track Video Watch Time

```kotlin
// When video player stops / navigates away
analyticsManager.track(AnalyticsEvent.VideoWatchTime(
    videoUrl = vidUrl.url,
    videoSource = VideoUtil.getVideoSourceName(vidUrl),
    durationSeconds = elapsedSeconds,
    launchId = launchId
))
```

### Track Opening Video in External App

```kotlin
// In FullscreenVideoScreen "Open in external app" button
analyticsManager.track(AnalyticsEvent.VideoOpenedExternal(
    videoUrl = vidUrl.url,
    videoSource = VideoUtil.getVideoSourceName(vidUrl),
    launchId = launchId
))
```

## File Structure

```
composeApp/src/
├── commonMain/kotlin/.../analytics/
│   ├── core/
│   │   ├── AnalyticsProvider.kt          # Provider interface
│   │   ├── AnalyticsManager.kt           # Manager interface
│   │   ├── AnalyticsManagerImpl.kt       # Fan-out dispatcher implementation
│   │   └── AnalyticsPreferences.kt       # Consent preferences
│   ├── events/
│   │   └── AnalyticsEvent.kt             # Sealed event hierarchy
│   ├── navigation/
│   │   └── AnalyticsScreenTracker.kt     # Auto screen view tracking
│   └── providers/
│       └── ConsoleAnalyticsProvider.kt   # Debug/Desktop provider
│
├── androidMain/kotlin/.../analytics/providers/
│   └── FirebaseAnalyticsProvider.kt      # Firebase Android impl
│
├── iosMain/kotlin/.../analytics/providers/
│   └── FirebaseAnalyticsProvider.kt      # Firebase iOS impl
│
└── desktopMain/kotlin/.../analytics/providers/
    └── (none needed — ConsoleAnalyticsProvider is in commonMain)
```

## Koin Module

```kotlin
// di/AnalyticsModule.kt (commonMain)
val analyticsModule = module {
    // Console provider (all platforms)
    single<AnalyticsProvider>(named("console")) { ConsoleAnalyticsProvider() }

    // Preferences
    single { AnalyticsPreferences(get<DataStore<Preferences>>(named("AppSettingsDataStore"))) }

    // Manager — automatically collects all AnalyticsProvider instances
    single<AnalyticsManager> {
        AnalyticsManagerImpl(providers = getAll())
    }
}

// Add to koinConfig modules list:
// modules(networkModule, apiModule, appModule, debugModule, imageLoaderModule, analyticsModule)
```

## Testing

```kotlin
// Use a fake provider for testing
class FakeAnalyticsProvider : AnalyticsProvider {
    val trackedEvents = mutableListOf<AnalyticsEvent>()
    override val name = "fake"
    override var isEnabled = true
    override suspend fun initialize(config: Map<String, Any>) {}
    override suspend fun trackEvent(event: AnalyticsEvent) { trackedEvents.add(event) }
    override suspend fun trackScreenView(screenName: String, screenClass: String?) {}
    override fun setUserId(userId: String?) {}
    override fun setUserProperty(key: String, value: String) {}
    override suspend fun flush() {}
    override fun reset() { trackedEvents.clear() }
}

// In tests:
val fakeProvider = FakeAnalyticsProvider()
val manager = AnalyticsManagerImpl(providers = listOf(fakeProvider))
manager.track(AnalyticsEvent.LaunchViewed("uuid", "Falcon 9"))
assertEquals(1, fakeProvider.trackedEvents.size)
```

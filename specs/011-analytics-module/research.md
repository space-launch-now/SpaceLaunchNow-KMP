# Research: Analytics Module Multi-Provider Architecture

**Feature**: 011-analytics-module | **Date**: 2026-04-05

## Research Task 1: KMP Analytics SDK Options

### Decision: Use native platform SDKs via expect/actual + GitLive Firebase KMP

### Rationale

- **Firebase Analytics**: GitLive `dev.gitlive:firebase-analytics` already in version catalog
  (`gitlive-firebase = "2.4.0"`). Provides true KMP support for Firebase Analytics on Android + iOS.
  Desktop gets a no-op actual.
- **Amplitude**: No official KMP SDK. Best approach: expect/actual with native Amplitude Android SDK
  + Amplitude Swift SDK. Same pattern as BillingManager.
- **Datadog**: Already integrated via `com.datadoghq:dd-sdk-kotlin-multiplatform-*`. The existing
  `DatadogLogger` can be wrapped for event forwarding, but Datadog is primarily RUM/logs, not event
  analytics.

### Alternatives Considered

- **Segment KMP**: No official KMP support. Would require custom wrapper.
- **Single KMP analytics library**: None mature enough for production multi-provider use.
- **Manual HTTP analytics**: Too much overhead, reinventing the wheel.

## Research Task 2: Fan-Out Dispatcher Pattern for KMP

### Decision: Coroutine-based AnalyticsManager with SupervisorJob per-provider dispatch

### Rationale

Using `supervisorScope` or `SupervisorJob` ensures one provider's failure doesn't cancel others.
Pattern:

```kotlin
class AnalyticsManager(private val providers: List<AnalyticsProvider>) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun track(event: AnalyticsEvent) {
        providers.filter { it.isEnabled }.forEach { provider ->
            scope.launch {
                try {
                    provider.trackEvent(event)
                } catch (e: Exception) {
                    // Log error, don't propagate
                }
            }
        }
    }
}
```

### Alternatives Considered

- **Sequential dispatch**: Simpler but slow if a provider blocks.
- **Channel-based**: Overkill for fire-and-forget analytics.
- **Flow-based**: Unnecessary backpressure handling for analytics events.

## Research Task 3: Event Taxonomy Design for Space Launch Apps

### Decision: Sealed class hierarchy with typed parameters

### Rationale

Sealed classes provide:

1. **Compile-time safety**: No typos in event names
2. **Typed parameters**: Each event carries only relevant data
3. **Exhaustive when**: Compiler warns if a provider doesn't handle new events
4. **Serialization-ready**: Can be serialized for batching if needed

Each event converts to provider-specific format via `toMap()`:

```kotlin
sealed class AnalyticsEvent(val name: String) {
    abstract fun toParameters(): Map<String, Any?>

    data class LaunchViewed(val launchId: String, val launchName: String) :
        AnalyticsEvent("launch_viewed") {
        override fun toParameters() = mapOf("launch_id" to launchId, "launch_name" to launchName)
    }
}
```

### Alternatives Considered

- **String-based events with Map params**: Fragile, no type safety.
- **Enum + separate param classes**: Too verbose, breaks open-closed principle.
- **Annotation processor**: Over-engineered for this use case.

## Research Task 4: Navigation Screen Tracking in Compose Multiplatform

### Decision: NavController listener + route-to-screen-name mapping

### Rationale

The project uses Jetpack Navigation with serializable routes. Track screen views by observing
`NavController.currentBackStackEntryFlow`:

```kotlin
@Composable
fun AnalyticsScreenTracker(
    navController: NavHostController,
    analyticsManager: AnalyticsManager
) {
    val currentEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentEntry) {
        currentEntry?.destination?.route?.let { route ->
            val screenName = mapRouteToScreenName(route)
            analyticsManager.trackScreenView(screenName, route)
        }
    }
}
```

Route mapping uses a simple `when` on the route class hierarchy rather than string parsing.

### Alternatives Considered

- **Manual tracking in each screen**: Error-prone, easy to forget.
- **Lifecycle observer**: Not available consistently in KMP Compose.
- **AOP/instrumentation**: Not viable in KMP.

## Research Task 5: Consent Management Approach

### Decision: Per-provider enabled flags in AppPreferences DataStore

### Rationale

Mirrors existing pattern for notification preferences. Store individual flags:

- `analytics_firebase_enabled: Boolean` (default: true)
- `analytics_amplitude_enabled: Boolean` (default: true)
- `analytics_console_enabled: Boolean` (default: true for debug, false for release)

The `AnalyticsManager` checks `provider.isEnabled` before dispatching. Providers can also be
disabled entirely by not registering them in Koin.

### Alternatives Considered

- **Global kill switch only**: Not granular enough for GDPR compliance.
- **Consent management SDK (OneTrust, etc.)**: Overkill for current app scale.
- **Build variant-based**: Too rigid, users should control at runtime.

## Research Task 6: Existing Code Impact Assessment

### Decision: Additive change — new package alongside existing Datadog code

### Rationale

**What stays untouched**:

- `analytics/DatadogConfig.kt` (expect/actual) — Datadog RUM initialization
- `util/logging/DataDogLogWriter.kt` — Datadog log forwarding
- `util/logging/SpaceLogger.kt` — Application logging infrastructure
- All existing Koin modules

**What gets added**:

- `analytics/core/` — AnalyticsProvider interface, AnalyticsEvent sealed class, AnalyticsManager
- `analytics/providers/` — FirebaseAnalyticsProvider, ConsoleAnalyticsProvider
- `analytics/events/` — Event taxonomy sealed classes
- `analytics/navigation/` — Screen tracking composable
- `di/AnalyticsModule.kt` — Koin module for analytics DI

**What gets modified (minor)**:

- `di/AppModule.kt` — Add `analyticsModule` to Koin config modules list
- Navigation host — Add `AnalyticsScreenTracker` composable

## Summary of Decisions

| Topic | Decision | Risk Level |
|-------|----------|------------|
| SDK Strategy | Native SDKs via expect/actual + GitLive Firebase | Low |
| Dispatch Pattern | SupervisorJob coroutine fan-out | Low |
| Event Model | Sealed class hierarchy with typed params | Low |
| Screen Tracking | NavController listener + route mapping | Low |
| Consent | Per-provider flags in AppPreferences | Low |
| Code Impact | Additive — new package, minimal existing changes | Low |

# Data Model: Analytics Module

**Feature**: 011-analytics-module | **Date**: 2026-04-05

## Entity Diagram

```
┌──────────────────────┐     dispatches to     ┌──────────────────────┐
│   AnalyticsManager   │────────────────────────│  AnalyticsProvider   │
│                      │         1:N            │     «interface»      │
│  - providers: List   │                        │                      │
│  - scope: Scope      │                        │  + initialize()      │
│  + track(event)      │                        │  + trackEvent()      │
│  + trackScreenView() │                        │  + trackScreenView() │
│  + setUserId()       │                        │  + setUserId()       │
│  + setUserProperty() │                        │  + setUserProperty() │
│  + setEnabled()      │                        │  + isEnabled: Bool   │
│  + flush()           │                        │  + flush()           │
│  + reset()           │                        │  + reset()           │
└──────────────────────┘                        └──────────────────────┘
                                                          │
                                            ┌─────────────┼─────────────┐
                                            │             │             │
                                  ┌─────────┴──┐  ┌──────┴────┐  ┌────┴──────────┐
                                  │  Firebase   │  │  Console  │  │   Amplitude   │
                                  │  Provider   │  │  Provider │  │   Provider    │
                                  │ (And + iOS) │  │ (Desktop) │  │   (future)    │
                                  └────────────┘  └───────────┘  └───────────────┘

┌──────────────────────────┐
│    AnalyticsEvent        │
│     «sealed class»       │
│                          │
│  + name: String          │
│  + toParameters(): Map   │
└──────────────────────────┘
           │
    ┌──────┼──────────┬──────────────┬────────────────┐
    │      │          │              │                │
┌───┴───┐ ┌┴────┐ ┌──┴───┐ ┌───────┴──────┐ ┌──────┴────┐
│Launch │ │Nav  │ │Content│ │Subscription  │ │Notification│
│Events │ │Events│ │Events│ │Events        │ │Events      │
└───────┘ └─────┘ └──────┘ └──────────────┘ └────────────┘


┌──────────────────────┐     reads/writes     ┌──────────────────────┐
│ AnalyticsPreferences │──────────────────────│   AppPreferences     │
│                      │                      │   (DataStore)        │
│  + isProviderEnabled │                      │                      │
│  + setProviderEnabled│                      │  analytics_*_enabled │
└──────────────────────┘                      └──────────────────────┘
```

## Core Entities

### 1. AnalyticsProvider (Interface)

**Location**: `commonMain/analytics/core/AnalyticsProvider.kt`

| Field/Method | Type | Description |
|---|---|---|
| `name` | `String` | Provider identifier (e.g., "firebase", "amplitude") |
| `isEnabled` | `Boolean` | Whether this provider should receive events |
| `initialize(config)` | `suspend (Map<String, Any>) -> Unit` | Platform-specific initialization |
| `trackEvent(event)` | `suspend (AnalyticsEvent) -> Unit` | Track a typed event |
| `trackScreenView(screenName, screenClass)` | `suspend (String, String?) -> Unit` | Track navigation |
| `setUserId(userId)` | `(String?) -> Unit` | Set or clear user identity |
| `setUserProperty(key, value)` | `(String, String) -> Unit` | Set user attributes |
| `setEnabled(enabled)` | `(Boolean) -> Unit` | Toggle consent |
| `flush()` | `suspend () -> Unit` | Force send queued events |
| `reset()` | `() -> Unit` | Clear user data on logout |

**Validation Rules**:

- `name` must be non-empty and unique across registered providers
- `trackEvent` must not throw — all errors caught internally
- `initialize` called exactly once before any other method

### 2. AnalyticsEvent (Sealed Class Hierarchy)

**Location**: `commonMain/analytics/events/AnalyticsEvent.kt`

| Event | Parameters | Event Name String |
|---|---|---|
| `LaunchViewed` | `launchId: String, launchName: String` | `launch_viewed` |
| `LaunchShared` | `launchId: String, method: String` | `launch_shared` |
| `LaunchReminderSet` | `launchId: String` | `launch_reminder_set` |
| `LaunchFavorited` | `launchId: String, favorited: Boolean` | `launch_favorited` |
| `TabSelected` | `tab: String` | `tab_selected` |
| `SearchPerformed` | `query: String, resultCount: Int` | `search_performed` |
| `ArticleViewed` | `articleId: String, source: String` | `article_viewed` |
| `EventViewed` | `eventId: Int` | `event_viewed` |
| `AgencyViewed` | `agencyId: Int` | `agency_viewed` |
| `AstronautViewed` | `astronautId: Int` | `astronaut_viewed` |
| `RocketViewed` | `rocketId: Int` | `rocket_viewed` |
| `SpaceStationViewed` | `stationId: Int` | `space_station_viewed` |
| `PaywallViewed` | `source: String` | `paywall_viewed` |
| `PurchaseStarted` | `productId: String` | `purchase_started` |
| `PurchaseCompleted` | `productId: String, revenue: Double?` | `purchase_completed` |
| `PurchaseRestored` | `success: Boolean` | `purchase_restored` |
| `NotificationReceived` | `type: String` | `notification_received` |
| `NotificationTapped` | `type: String, launchId: String?` | `notification_tapped` |
| `NotificationSettingChanged` | `type: String, enabled: Boolean` | `notification_setting_changed` |
| `AppOpened` | `source: String` | `app_opened` |
| `OnboardingStep` | `step: Int, completed: Boolean` | `onboarding_step` |
| `ThemeChanged` | `theme: String` | `theme_changed` |
| `FilterChanged` | `filterType: String, value: String` | `filter_changed` |
| `WidgetConfigured` | `widgetType: String` | `widget_configured` |
| `WidgetTapped` | `widgetType: String, launchId: String?` | `widget_tapped` |
| `ScreenViewed` | `screenName: String, screenClass: String?` | `screen_view` |

**Validation Rules**:

- Event `name` follows snake_case convention
- String parameters must not be empty (enforced at construction)
- `toParameters()` returns only non-null values

### 3. AnalyticsManager (Dispatcher)

**Location**: `commonMain/analytics/core/AnalyticsManager.kt`

| Field/Method | Type | Description |
|---|---|---|
| `providers` | `List<AnalyticsProvider>` | Registered providers (injected) |
| `scope` | `CoroutineScope` | SupervisorJob + Dispatchers.Default |
| `track(event)` | `(AnalyticsEvent) -> Unit` | Fan-out to all enabled providers |
| `trackScreenView(name, class)` | `(String, String?) -> Unit` | Fan-out screen views |
| `setUserId(userId)` | `(String?) -> Unit` | Propagate to all providers |
| `setUserProperty(key, value)` | `(String, String) -> Unit` | Propagate to all providers |
| `flush()` | `suspend () -> Unit` | Flush all providers |
| `reset()` | `() -> Unit` | Reset all providers |
| `enableProvider(name, enabled)` | `(String, Boolean) -> Unit` | Toggle specific provider |

**State Transitions**:

```
UNINITIALIZED ──initialize()──> READY ──track(event)──> READY
                                  │
                                  ├──reset()──> READY (cleared user)
                                  └──flush()──> READY (events sent)
```

### 4. AnalyticsPreferences

**Location**: `commonMain/analytics/core/AnalyticsPreferences.kt`

Wraps `AppPreferences` DataStore. Keys:

| Key | Type | Default | Description |
|---|---|---|---|
| `analytics_enabled` | `Boolean` | `true` | Global analytics kill switch |
| `analytics_firebase_enabled` | `Boolean` | `true` | Firebase Analytics toggle |
| `analytics_console_enabled` | `Boolean` | `false` | Console logging toggle |

## Relationships

- `AnalyticsManager` 1:N `AnalyticsProvider` — manager dispatches to all providers
- `AnalyticsManager` uses `AnalyticsEvent` — type-safe event dispatching
- `AnalyticsManager` reads `AnalyticsPreferences` — consent management
- `AnalyticsProvider` implementations are platform-specific (expect/actual or conditional)
- ViewModels inject `AnalyticsManager` — no direct provider access
- Navigation host uses `AnalyticsScreenTracker` — automatic screen view tracking

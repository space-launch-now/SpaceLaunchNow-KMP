# Feature Spec: Analytics Module (Multi-Provider Plugin Architecture)

**Branch**: `011-analytics-module` | **Priority**: High | **Status**: Draft

## Problem Statement

SpaceLaunchNow currently has ad-hoc Datadog integration directly coupled to the analytics package
(`DatadogConfig.kt`, `DatadogRUM`, `DatadogLogger`). There is no unified analytics abstraction,
meaning adding a new analytics provider (Firebase Analytics, Amplitude, Mixpanel, etc.) requires
scattering provider-specific code throughout the codebase. Additionally, screen view tracking, user
action tracking, and business event tracking are not standardized, making it impossible to
consistently measure user engagement across the app.

## Goals

1. **Unified Analytics Interface**: A single common API (`AnalyticsProvider`) that all analytics
   platforms implement, so the rest of the codebase calls one interface
2. **Plugin Architecture**: New analytics providers can be added by implementing the interface and
   registering in Koin — zero changes to call sites
3. **Multi-Provider Dispatch**: An `AnalyticsManager` dispatches events to all registered providers
   simultaneously (fan-out pattern)
4. **Predefined Event Taxonomy**: A sealed class/enum of all trackable events with typed parameters
   — no magic strings at call sites
5. **Screen View Tracking**: Automatic tracking of screen transitions via navigation integration
6. **User Identity Management**: Centralized user identification that propagates to all providers
7. **Consent & Privacy**: Respect user consent preferences; disable providers individually
8. **Platform Compliance**: Works on Android, iOS, and Desktop (Desktop = no-op/console logging)

## Non-Goals

- Server-side analytics aggregation
- Custom dashboards within the app
- A/B testing framework (separate concern)
- Replacing Datadog RUM/logging (those remain separate — this is *event analytics*)

## Functional Requirements

### FR-1: Analytics Provider Interface

A common interface in `commonMain` that each analytics platform implements:

- `initialize(config: Map<String, Any>)` — platform-specific init
- `trackEvent(event: AnalyticsEvent)` — track a typed event
- `trackScreenView(screenName: String, screenClass: String?)` — screen view
- `setUserId(userId: String?)` — identify user
- `setUserProperty(key: String, value: String)` — user properties
- `setEnabled(enabled: Boolean)` — consent toggle
- `flush()` — force-send batched events
- `reset()` — clear user data (logout)

### FR-2: Analytics Event Taxonomy

Sealed class hierarchy for all app events with typed parameters:

- **Launch Events**: `ViewLaunch(id, name)`, `ShareLaunch(id)`, `SetLaunchReminder(id)`,
  `FavoriteLaunch(id, favorited)`
- **Navigation Events**: `TabSelected(tab)`, `SearchPerformed(query, resultCount)`
- **Content Events**: `ViewArticle(id, source)`, `ViewEvent(id)`, `ViewAgency(id)`,
  `ViewAstronaut(id)`, `ViewRocket(id)`
- **Subscription Events**: `ViewPaywall(source)`, `StartPurchase(productId)`,
  `CompletePurchase(productId, revenue)`, `RestorePurchase(success)`
- **Notification Events**: `NotificationReceived(type)`, `NotificationTapped(type, launchId)`,
  `NotificationSettingChanged(type, enabled)`
- **App Lifecycle**: `AppOpened(source)`, `OnboardingStep(step, completed)`,
  `AppRated(rating)`
- **Settings Events**: `ThemeChanged(theme)`, `FilterChanged(filterType, value)`
- **Widget Events**: `WidgetConfigured(type)`, `WidgetTapped(type, launchId)`
- **Error Events**: `AnalyticsError(provider, message, throwable)`

### FR-3: Analytics Manager (Fan-Out Dispatcher)

- Holds a list of `AnalyticsProvider` instances
- Dispatches every `trackEvent` call to ALL enabled providers
- Handles per-provider errors gracefully (log + continue)
- Supports adding/removing providers at runtime
- Thread-safe

### FR-4: Provider Implementations

Initial providers to implement:

1. **Firebase Analytics** (Android + iOS via GitLive KMP or native)
2. **ConsoleAnalyticsProvider** (Desktop + debug builds — prints to log)
3. **Datadog Events** (optional — wraps existing DatadogLogger for event tracking)

Future providers (just implement `AnalyticsProvider`):
- Amplitude
- Mixpanel
- Custom backend

### FR-5: Navigation Integration

- Automatically track screen views when navigation destination changes
- Map navigation routes to human-readable screen names
- Support both the serializable routes and legacy `Screen` sealed class

### FR-6: Koin Integration

- `AnalyticsModule.kt` registers the `AnalyticsManager` and all platform-specific providers
- Platform `nativeConfig()` provides platform-specific provider instances
- ViewModels and repositories inject `AnalyticsManager` to track events

### FR-7: Consent Management

- Per-provider enable/disable stored in `AppPreferences`
- Analytics providers check enabled state before sending
- Default: all providers enabled (user can opt out in Settings)

## Technical Constraints

- Must follow KMP expect/actual pattern for platform-specific SDKs
- Desktop must compile with no-op stubs (same as existing Datadog pattern)
- Must not break existing Datadog RUM/logging infrastructure
- Must use Koin DI — no service locators or static singletons
- Events must be dispatched on background coroutine scope
- Must follow project convention: no magic strings, use data classes

## Success Criteria

- [ ] Single `analyticsManager.track(LaunchViewed(id, name))` call reaches all providers
- [ ] Adding new provider requires only: implement interface + register in Koin module
- [ ] Screen views auto-tracked on navigation changes
- [ ] Desktop builds compile and run (no-op provider)
- [ ] All three platforms (Android, iOS, Desktop) build successfully
- [ ] Existing Datadog RUM/logging unaffected
- [ ] Consent toggles respected per-provider

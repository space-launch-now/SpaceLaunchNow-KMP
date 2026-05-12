# Premium-Funnel Observability

**Date:** 2026-05-06
**Branch:** `add_premium_trial_one_day`
**Status:** Approved (pending review)

## Goal

Make the rewarded-ad → 24h-temporary-premium flow visible from two angles:

1. **Event analytics (`AnalyticsManager` → Datadog/Firebase)** — measure the funnel `Requested → Shown → Granted` plus failure modes for the existing `TemporaryPremiumCard` surface.
2. **RevenueCat subscriber attributes** — attach device, app, cohort, and premium-flow context to every RC user so cohorts can be filtered in the RC dashboard and re-engagement integrations (Iterable/Braze/etc., if ever wired).

Both are observability work; they share data sources (subscription state, app version, theme, counters) and ship together so we are not running the SDK twice for similar reads.

## Why now

- The rewarded-ad flow currently ships with **zero analytics** — we cannot answer "is anyone tapping this?" or "is the ad filling?".
- RevenueCat receives **zero subscriber attributes** today — every customer in the RC dashboard is anonymous beyond their entitlement state. No cohort filtering, no platform breakdown, no churn-by-app-version analysis is possible.

Premium-funnel decisions (e.g. whether to put the card on a different surface, whether to cap extensions, whether to run a reactivation campaign on FCM-tokened users) all need this data first.

## Non-goals

- Any new UI surface or placement of `TemporaryPremiumCard`.
- Changing grant logic, feature scope, cooldown/cap rules.
- Server-side dashboards — ride existing `AnalyticsManager` → Datadog/Firebase pipeline and the RC dashboard.
- Collecting PII (email, name, phone). App has no auth; nothing to set.
- Overriding SDK-managed identifiers (IDFA, GPS Ad ID).

---

## Part 1: Event Analytics

### Events (4) — `analytics/events/AnalyticsEvent.kt`

Add under a new "Rewarded Ad / Temp Access" section.

```kotlin
data class RewardedAdRequested(
    val source: String,
    val isExtension: Boolean
) : AnalyticsEvent("rewarded_ad_requested") {
    override fun toParameters() = mapOf(
        "source" to source,
        "is_extension" to isExtension
    )
}

data class RewardedAdShown(
    val source: String
) : AnalyticsEvent("rewarded_ad_shown") {
    override fun toParameters() = mapOf("source" to source)
}

data class RewardedAdFailed(
    val source: String,
    val error: String
) : AnalyticsEvent("rewarded_ad_failed") {
    override fun toParameters() = mapOf(
        "source" to source,
        "error" to error
    )
}

data class TemporaryAccessGranted(
    val feature: String,
    val source: String,
    val isExtension: Boolean
) : AnalyticsEvent("temporary_access_granted") {
    override fun toParameters() = mapOf(
        "feature" to feature,
        "source" to source,
        "is_extension" to isExtension
    )
}
```

### Field semantics

| Field | Type | Values |
|---|---|---|
| `source` | String | `"theme_settings"` today. Identifies the screen/card instance. |
| `isExtension` | Boolean | `true` if user already had any active temp access at click time; snapshot taken at click and reused for all per-feature grant events. |
| `feature` | String | `PremiumFeature.name` (uppercase, e.g. `"CUSTOM_THEMES"`) — matches `ThemeChanged(theme = theme.name)` pattern. |
| `error` | String | Error message from `RewardedAdHandler.onAdFailed` callback. |

### Firing rules

| Event | Origin | Cardinality |
|---|---|---|
| `RewardedAdRequested` | Button onClick | 1 per user click |
| `RewardedAdShown` | `onAdShown` callback | 1 per displayed ad |
| `RewardedAdFailed` | `onAdFailed` callback | 1 per failed load/show |
| `TemporaryAccessGranted` | inside `onRewardEarned` per-feature loop | N per ad (N = `features.size`, currently 3) |

> **Funnel note.** Because one ad = N grant events, dashboards comparing `RewardedAdShown` count to `TemporaryAccessGranted` count must dedupe by user+timestamp or compare to `distinct(timestamp)` rather than raw count.

### Wiring

#### `TemporaryPremiumCard.kt`

Add one new required param:

```kotlin
@Composable
fun TemporaryPremiumCard(
    temporaryPremiumAccess: TemporaryPremiumAccess,
    source: String,                  // ← NEW
    features: List<PremiumFeature> = …,
    title: String = …,
    description: String = …,
    icon: ImageVector,
    hasPermanentPremium: Boolean = false,
    modifier: Modifier = Modifier,
)
```

Inside the composable:
- `val analyticsManager = koinInject<AnalyticsManager>()`
- Snapshot `val wasExtensionAtClick = hasAnyAccess` inside each button onClick.
- Wrap each button onClick to fire `RewardedAdRequested(source, wasExtensionAtClick)` before setting `showRewardedAd = true`.
- Pass `onAdShown = { analyticsManager.track(RewardedAdShown(source)); existingLog.i { … } }`.
- Pass `onAdFailed = { error -> analyticsManager.track(RewardedAdFailed(source, error)); existingLog.e { … }; showRewardedAd = false }`.
- Inside `onRewardEarned`, for each granted feature fire `TemporaryAccessGranted(feature.name, source, wasExtensionAtClick)`.

#### `ThemeCustomizationScreen.kt`

Add `source = "theme_settings"` to the existing `TemporaryPremiumCard(…)` call. Only call site today.

---

## Part 2: RevenueCat Subscriber Attributes

### Boundary — `RevenueCatAttributes` interface (commonMain)

Keep RC types out of common code. New file `data/billing/RevenueCatAttributes.kt`:

```kotlin
interface RevenueCatAttributes {
    /** Push the given attribute map to RevenueCat. Called frequently; impl should be idempotent. */
    suspend fun set(attributes: Map<String, String?>)

    /** Set the FCM (Android) or APNS (iOS) push token via the reserved attribute. */
    suspend fun setPushToken(token: String)
}

expect fun createRevenueCatAttributes(): RevenueCatAttributes
```

`actual` per platform calls into the RC SDK:
- **Android:** `Purchases.sharedInstance.setAttributes(attributes)`; `Purchases.sharedInstance.setPushToken(token)`.
- **iOS:** `Purchases.shared().attribution.setAttributes(attributes)`; `Purchases.shared().attribution.setPushToken(deviceToken)`.
- **Desktop:** No-op (no RC SDK).

Wired through Koin in `AppModule.kt` as a singleton.

### Attribute set

#### Reserved (RC-recognized)

| Key | Source | Refresh |
|---|---|---|
| `$fcmTokens` | Android: `FirebaseMessaging.getInstance().token`. Set via `setPushToken`. | On token refresh + cold start. |
| `$apnsTokens` | iOS: `AppDelegate.didRegisterForRemoteNotificationsWithDeviceToken`. Bridge token to common via existing `NotificationRepositoryPlatform`-style hook, set via `setPushToken`. | On registration callback. |

Skipped reserved: `$email`, `$displayName`, `$phoneNumber` (no auth → no PII), `$ip`/`$idfa`/`$idfv`/`$gpsAdId` (SDK-managed).

#### Device / app context

| Key | Source | Refresh |
|---|---|---|
| `app_version` | `BuildConfig.VERSION_NAME` (or platform equivalent) | Cold start |
| `app_build` | `BuildConfig.VERSION_CODE` (or platform equivalent) | Cold start |
| `platform` | `getPlatform().type.name.lowercase()` (`"android"`/`"ios"`/`"desktop"`) | Cold start |
| `os_version` | Platform API | Cold start |
| `device_model` | Platform API | Cold start |
| `locale` | `Locale.current.toLanguageTag()` | Cold start |
| `country` | `Locale.current.country` (when available) | Cold start |
| `form_factor` | `"phone"` / `"tablet"` / `"desktop"` (derived from `isTabletOrDesktop()`) | Cold start |
| `wear_paired` | Android only — existing wear DataLayer reachability check | Cold start + on pair/unpair if observable |
| `first_seen_app_version` | Sticky — write once on first app open if not already in DataStore | Once, ever |

#### App-state cohorts

| Key | Source | Refresh trigger |
|---|---|---|
| `subscription_state` | `SubscriptionState.subscriptionType.name.lowercase()` (`"free"`/`"legacy"`/`"premium"`/`"lifetime"`) | On `subscriptionRepository.state` change |
| `theme_mode` | `appPreferences.themeFlow` (`"system"`/`"light"`/`"dark"`) | On change |
| `has_custom_theme` | `themePreferences.customPrimaryColorFlow != null` (bool→string) | On change |
| `notifications_permission` | Platform permission state (`"granted"`/`"denied"`/`"not_asked"`) | Cold start + on app resume |
| `widgets_installed_count` | Android: `AppWidgetManager` count, bucketed `"0"`/`"1-2"`/`"3+"`. iOS: skip (WidgetKit doesn't expose count). | Cold start + on widget add/remove broadcast |
| `onboarding_completed` | `appPreferences.onboardingCompletedFlow` | On completion |
| `consent_status` | UMP consent string state | After UMP resolves |

#### Premium-flow counters (read by Part 1's events)

Stored in `TemporaryPremiumAccess` DataStore, pushed to RC on increment.

| DataStore key | RC attr | Increments when |
|---|---|---|
| `temp_grants_total` | `temporary_access_grants_total` | Each `grantTemporaryAccess` call (counts per-feature; 3 per ad). |
| `rewarded_ads_shown_total` | `rewarded_ads_shown_total` | Card's `onAdShown` callback fires. |
| `temp_access_active` (computed) | `temporary_access_active` | Whenever any feature's expiry > now (re-evaluate on grant + on `cleanupExpiredAccess`). |

### Refresh strategy

- **App startup** — single push of every device/app/cohort attribute (one `set(map)` call to minimise SDK overhead).
- **State-change driven** — observe `subscriptionRepository.state`, theme/permission flows, and counter updates. Coalesce to a single `set(map)` call per change.
- **Push tokens** — set via `setPushToken` on registration or refresh, not in the bulk map.
- **No periodic timer.** Refresh is event-driven; cold start covers stale gaps.

### Where this lives

New file `data/billing/RevenueCatAttributesSyncer.kt` (commonMain) — coroutine-scoped class that:
- Holds the dependencies it needs (`RevenueCatAttributes`, `SubscriptionRepository`, `AppPreferences`, `ThemePreferences`, `WidgetPreferences`, `TemporaryPremiumAccess`, platform info provider).
- Has `start(scope: CoroutineScope)` called from `MainApplication.onCreate` (Android) / `KoinInitializer.kt` (iOS) after Koin is up.
- Pushes the snapshot at startup, then collects relevant flows and pushes deltas.

Why a syncer class and not scattered call-sites: single source of truth for "what attrs RC knows", easy to extend, easy to disable in tests.

---

## Files changed

| File | Change |
|---|---|
| `composeApp/src/commonMain/.../analytics/events/AnalyticsEvent.kt` | +4 event classes |
| `composeApp/src/commonMain/.../ui/subscription/TemporaryPremiumCard.kt` | +`source` param, `koinInject<AnalyticsManager>()`, 4 track calls |
| `composeApp/src/commonMain/.../ui/settings/ThemeCustomizationScreen.kt` | +`source = "theme_settings"` arg |
| `composeApp/src/commonMain/.../data/billing/RevenueCatAttributes.kt` | NEW — interface + expect factory |
| `composeApp/src/androidMain/.../data/billing/RevenueCatAttributes.android.kt` | NEW — actual using `Purchases.sharedInstance` |
| `composeApp/src/iosMain/.../data/billing/RevenueCatAttributes.ios.kt` | NEW — actual using `Purchases.shared()` |
| `composeApp/src/desktopMain/.../data/billing/RevenueCatAttributes.desktop.kt` | NEW — no-op actual |
| `composeApp/src/commonMain/.../data/billing/RevenueCatAttributesSyncer.kt` | NEW — orchestrates set() calls |
| `composeApp/src/commonMain/.../data/storage/TemporaryPremiumAccess.kt` | +counter keys, +`incrementGrantsTotal()`, +`incrementAdsShownTotal()`, +read accessors |
| `composeApp/src/commonMain/.../di/AppModule.kt` | Wire `RevenueCatAttributes` + `RevenueCatAttributesSyncer` |
| `composeApp/src/androidMain/.../MainApplication.kt` | Start syncer after Koin |
| `composeApp/src/iosMain/.../KoinInitializer.kt` | Start syncer after Koin |
| `iosApp/iosApp/AppDelegate.swift` | Bridge APNS token to RC via the new wrapper |
| `composeApp/src/androidMain/.../{wherever FCM token is observed}` | Bridge FCM token to RC via the new wrapper |

## Testing

- **Manual:** trigger ad on Theme settings, watch logs for the 4 events firing in expected order: requested → shown → granted×3.
- **Manual:** open RC dashboard → verify subscriber appears with all attributes populated. Toggle theme → confirm `theme_mode` updates within seconds.
- **Manual:** ad-fail path (airplane mode / no fill) should fire `RewardedAdFailed` and not increment `rewarded_ads_shown_total`.
- **Unit (light):** counter increments in `TemporaryPremiumAccess`. Skip mocking the RC SDK.
- **Out of test scope:** SDK behavior, network reliability of attribute pushes — RC handles.

## Risks / open questions

- **Attribute write rate.** RC has rate limits on attribute writes (per docs, generous but not infinite). Coalesce flow updates with `debounce` (e.g. 1s) to avoid storms when many flows change in quick succession.
- **`apnsTokens` plumbing on iOS.** Need a clean `Data → ByteArray → hex string` (or pass-through to RC SDK Swift API directly from `AppDelegate`). Easiest path: call into RC SDK directly from `AppDelegate`, bypassing the common interface for the push-token case. Document the choice.
- **`widgets_installed_count` on iOS.** WidgetKit gives no count API. Skip on iOS, document attribute as "Android only — absent on iOS".
- **Multiple grant events per ad.** Same caveat as Part 1 — dashboards must dedupe.
- **PII / consent.** No PII collected. Push tokens fall under platform-level user consent already required for notifications; if user denies notifications, no token to send.

## Out of scope

Possible follow-ups, not part of this work:

- Daily/lifetime cap on rewarded-ad extensions (data-dependent decision).
- New placement of `TemporaryPremiumCard` outside Theme settings.
- Custom Datadog/RC dashboards on top of this data.
- Reactivation campaigns wired to FCM/APNS tokens via RC integrations.

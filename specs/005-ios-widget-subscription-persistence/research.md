# Research: iOS Widget Subscription Persistence

**Feature**: 005-ios-widget-subscription-persistence  
**Research Date**: 2026-03-05

## Research Tasks

### 1. Current Architecture Analysis

**Question**: How does widget access state flow from the main app to iOS widgets?

**Finding**: 
- Main app uses `WidgetAccessSharer.ios.kt` which writes to `NSUserDefaults` in app group `group.me.calebjones.spacelaunchnow`
- Key used: `widget_has_access` (Boolean)
- Widget reads this in `LaunchData.swift` → `fetchLaunches()` with default of `false`
- This is triggered when `SimpleSubscriptionRepository` detects subscription changes via `localStorage.subscriptionData.onEach`

**Current Flow**:
```
App Start → RevenueCat Sync → SubscriptionSyncer → LocalSubscriptionStorage 
    → SimpleSubscriptionRepository.state.onEach → WidgetAccessSharer.syncWidgetAccess()
    → NSUserDefaults(widget_has_access) → Widget reads on next refresh
```

**Problem Identified**: The value only gets set when the app runs AND completes a subscription sync. If app is force closed before sync, or if the app process doesn't start (device restart), the widget reads `false` by default.

### 2. Data Available from RevenueCat

**Question**: What subscription expiry information is available?

**Finding**:
- `PurchaseState` data class has:
  - `trialExpiresAt: Long?` - Trial period expiry timestamp
  - `lastRefreshed: Long` - Last time purchase state was refreshed
- `IosBillingManager` extracts from RevenueCat:
  - `EntitlementInfo.expirationDateMillis` - Subscription expiration date
  - `EntitlementInfo.periodType` - TRIAL, NORMAL, INTRO, etc.

**Gap**: `LocalSubscriptionData` doesn't store:
- Subscription expiry date
- Whether the subscription has ever been valid
- History of premium status

### 3. iOS Widget Extension Limitations

**Question**: What can widget extensions access without main app running?

**Finding**:
- iOS widgets run in a separate process from the main app
- Can access shared App Group containers via `NSUserDefaults(suiteName:)` and file-based storage
- CANNOT initialize Koin or call Kotlin code reliably from widget extension (current code already does this but it's not reliable without main app)
- Widget timeline refreshes every 15 minutes minimum
- `WidgetKit.reloadAllTimelines()` can force refresh but only from main app

**Current Code Issue** (LaunchData.swift:161-166):
```swift
KoinInitializerKt.doInitKoin()  // May fail or return stale data
let helper = KoinHelper.Companion().instance()
let hasAccess = defaults?.bool(forKey: "widget_has_access") ?? false
```

**Best Practice**: Widget should read ONLY from shared storage, never initialize heavy frameworks.

### 4. App Group Storage Options

**Question**: What storage mechanisms can be shared between app and widget?

**Options**:
1. **NSUserDefaults (App Group)** - Currently used, fast, but limited to simple types
2. **FileManager shared container** - Can store JSON files in app group container
3. **CoreData with shared container** - Overkill for this use case
4. **Keychain with shared access group** - For secure data, not needed here

**Decision**: Use enhanced `NSUserDefaults` with more fields:
- `widget_has_access: Bool` (existing)
- `widget_subscription_expiry: TimeInterval` (NEW)
- `widget_last_verified: TimeInterval` (NEW)
- `widget_was_ever_premium: Bool` (NEW - fail-safe flag)

### 5. Fail-Safe Logic Research

**Question**: How should widget behave in ambiguous states?

**Scenarios**:
1. **Never had subscription**: `was_ever_premium = false` → Show locked
2. **Had subscription, expired**: Check `expiry < now` → Show locked
3. **Had subscription, expiry in future**: Check `expiry > now` → Show unlocked
4. **Had subscription, no expiry stored**: `was_ever_premium = true` → Show unlocked (grace period)
5. **Subscription active, value missing**: Check `last_verified < 7 days` → Show unlocked with warning

**Decision**: If `was_ever_premium = true` AND `subscription_expiry` is not set or in future, default to UNLOCKED. Only lock when:
- `was_ever_premium = false`, OR
- `subscription_expiry < now` (definitively expired)

## Decisions Summary

| Decision | Rationale | Alternatives Rejected |
|----------|-----------|----------------------|
| Enhance NSUserDefaults with expiry data | Simple, synchronous reads, already in use | CoreData (overkill), Keychain (not needed) |
| Add `was_ever_premium` flag | Prevents false locks for ambiguous states | Relying only on expiry (could lock users unfairly) |
| Widget reads cached data only | Reliable, no framework initialization needed | Calling Koin from widget (unreliable without main app) |
| Store expiry date from RevenueCat | Enables offline validation | Only storing boolean (no expiry validation possible) |
| Default to unlocked for paid users | Better UX - paid users shouldn't see locked state | Default to locked (poor UX for paying customers) |

## Technical Constraints

1. Widget extension has ~16MB memory limit
2. Widget refreshes are throttled by iOS
3. App Group name must match: `group.me.calebjones.spacelaunchnow`
4. Existing Android implementation should remain unchanged (different mechanism)

## Implementation Recommendations

1. **Phase 1**: Extend `WidgetAccessSharer` to sync expiry data and premium history flag
2. **Phase 2**: Update widget Swift code to read enhanced cache with fail-safe logic
3. **Phase 3**: Ensure cache is populated during app initialization before UI appears
4. **Phase 4**: Add aggressive cache writing on app termination signals

## References

- [LaunchData.swift](../../iosApp/LaunchWidget/LaunchData.swift) - Widget data provider
- [WidgetAccessSharer.ios.kt](../../composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/widgets/WidgetAccessSharer.ios.kt) - Kotlin cache writer
- [SimpleSubscriptionRepository.kt](../../composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/SimpleSubscriptionRepository.kt) - Subscription state management
- [IOS_WIDGET_PREMIUM_GATING.md](../../docs/premium/IOS_WIDGET_PREMIUM_GATING.md) - Existing documentation

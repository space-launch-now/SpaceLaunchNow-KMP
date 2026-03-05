# Quickstart: iOS Widget Subscription Persistence

**Feature**: 005-ios-widget-subscription-persistence  
**Date**: 2026-03-05
**Estimated Time**: 4-6 hours

## Quick Summary

**Problem**: Paid iOS users see locked widgets after force-closing the app.

**Solution**: Cache subscription expiry date and premium history flag in shared App Group storage, implement fail-safe logic that defaults to unlocked for users who have ever been premium.

## Implementation Checklist

### Phase 1: Kotlin - Enhanced Cache Writer (2 hours)

- [ ] Create `WidgetAccessCache.kt` data class in `composeApp/src/commonMain/kotlin/.../widgets/`
- [ ] Update `WidgetAccessSharer.ios.kt` to write all cache fields
- [ ] Update `SimpleSubscriptionRepository` to:
  - Track `wasEverPremium` flag (set true once, never false)
  - Extract subscription expiry from `PurchaseState`
  - Call enhanced `syncWidgetAccessCache()`

### Phase 2: Swift - Enhanced Cache Reader (1.5 hours)

- [ ] Create `WidgetAccessState.swift` in `iosApp/LaunchWidget/`
- [ ] Update `LaunchData.swift` to use `WidgetAccessState.readFromCache().shouldShowUnlocked`
- [ ] Remove Koin initialization calls from widget provider (unreliable)

### Phase 3: Expiry Data Extraction (1 hour)

- [ ] Update `IosBillingManager.updatePurchaseState()` to extract entitlement expiry
- [ ] Add `subscriptionExpiryMs: Long?` field to `LocalSubscriptionData`
- [ ] Pass expiry through `SubscriptionSyncer` to local storage

### Phase 4: Testing & Edge Cases (1.5 hours)

- [ ] Test: Premium user force closes app → widget stays unlocked
- [ ] Test: Premium subscription expires → widget locks after expiry date
- [ ] Test: Lifetime user → widget always unlocked (no expiry)
- [ ] Test: New free user → widget shows locked
- [ ] Test: Device restart → widget stays unlocked for premium

## Key Files to Modify

| File | Purpose |
|------|---------|
| `composeApp/src/commonMain/.../widgets/WidgetAccessSharer.kt` | Add enhanced sync method signature |
| `composeApp/src/iosMain/.../widgets/WidgetAccessSharer.ios.kt` | Write all cache fields to NSUserDefaults |
| `composeApp/src/commonMain/.../subscription/LocalSubscriptionData.kt` | Add expiry field |
| `composeApp/src/commonMain/.../repository/SimpleSubscriptionRepository.kt` | Add wasEverPremium tracking |
| `iosApp/LaunchWidget/LaunchData.swift` | Use new WidgetAccessState for access check |

## Key Code Snippets

### Kotlin: Enhanced WidgetAccessSharer

```kotlin
// WidgetAccessSharer.ios.kt
actual object WidgetAccessSharer {
    actual fun syncWidgetAccess(hasAccess: Boolean) {
        syncWidgetAccessCache(WidgetAccessCache(hasAccess = hasAccess))
    }
    
    fun syncWidgetAccessCache(cache: WidgetAccessCache) {
        val defaults = NSUserDefaults(suiteName = "group.me.calebjones.spacelaunchnow")
        defaults?.setBool(cache.hasAccess, forKey = "widget_has_access")
        
        cache.subscriptionExpiryMs?.let { 
            defaults?.setDouble(it.toDouble() / 1000.0, forKey = "widget_subscription_expiry")
        }
        
        defaults?.setDouble(
            cache.lastVerifiedMs.toDouble() / 1000.0, 
            forKey = "widget_last_verified"
        )
        defaults?.setBool(cache.wasEverPremium, forKey = "widget_was_ever_premium")
        defaults?.setObject(cache.subscriptionType.name, forKey = "widget_subscription_type")
    }
}
```

### Swift: Fail-Safe Access Check

```swift
// In LaunchData.swift fetchLaunches()
let accessState = WidgetAccessState.readFromCache()
if !accessState.shouldShowUnlocked {
    return LaunchEntry(..., hasWidgetAccess: false)
}
// Continue fetching launches...
```

## Risk Mitigation

| Risk | Mitigation |
|------|------------|
| Breaking existing widgets | Keep `widget_has_access` key working as before |
| Memory issues in widget | Only read from cache, no heavy framework init |
| Race conditions on sync | `wasEverPremium` is sticky - once true, always true |

## Success Criteria

✅ Paid user force closes app → Widget shows launches (not locked)  
✅ Paid user restarts device → Widget shows launches  
✅ Subscription expires → Widget locks after expiry date  
✅ New free user → Widget shows locked state  
✅ No network calls required for widget access check

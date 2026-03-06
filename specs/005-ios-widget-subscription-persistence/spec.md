# Feature Spec: iOS Widget Subscription Persistence

**Branch**: `005-ios-widget-subscription-persistence`  
**Priority**: HIGH (User-Facing Bug)  
**Date**: 2026-03-05

## Problem Statement

When a paid user on iOS force closes the app, the widgets may appear locked, requiring them to reopen the app to unlock. This creates a poor user experience where paying customers see "locked" widget states despite having an active subscription.

**Root Cause Analysis**:

1. The current implementation stores `widget_has_access` in shared App Group `NSUserDefaults` (`group.me.calebjones.spacelaunchnow`)
2. This value is written by the main app when `SimpleSubscriptionRepository` detects subscription changes
3. The widget reads this value on each refresh (every 15 minutes) with a default of `false` if not set
4. **Problem**: If the app is force closed before the subscription sync completes, or if `NSUserDefaults` fails to persist properly, the widget defaults to locked state

**Current Flow (Problematic)**:
```
[App Start] → [RevenueCat Sync] → [Write widget_has_access to NSUserDefaults] → [Widget Reads]
                    ↓
             (Force close here = widget never gets updated value)
```

**Affected Code**:
- `WidgetAccessSharer.ios.kt`: Writes to NSUserDefaults
- `LaunchData.swift`: Reads from NSUserDefaults with `false` default
- `SimpleSubscriptionRepository.kt`: Only syncs on app activity

## Requirements

### Functional Requirements

1. **FR-1: Robust Widget Access Caching**
   - Write subscription status to shared App Group storage immediately on first detection of paid status
   - Include a timestamp to verify cache freshness
   - Cache should persist across app force closes and device restarts

2. **FR-2: Redundant Access Verification in Widget**  
   - If `widget_has_access` is `false` or missing, attempt secondary verification
   - Read from a more persistent cache that includes last known subscription state
   - Only show locked state if both primary and fallback checks fail

3. **FR-3: Proactive Cache Population**
   - Populate widget access cache during app initialization before any UI appears
   - Ensure cache is written before user can potentially force close the app
   - Add synchronous fallback for reading cached state

4. **FR-4: Subscription Expiry Tracking**
   - Store subscription expiry date in shared cache
   - Widget can verify if cached premium status is still valid based on expiry
   - Never lock widgets for users whose subscription hasn't expired

5. **FR-5: Fail-Safe for Paid Users**
   - If cache indicates user WAS premium, maintain unlocked state until explicit revocation
   - Require explicit "subscription expired" signal to lock widgets
   - Default to unlocked for ambiguous states where paid history exists

### Non-Functional Requirements

1. **NFR-1**: Cache reads must be synchronous and non-blocking for widget timeline updates
2. **NFR-2**: No network calls required to validate cached premium status
3. **NFR-3**: Solution must work when main app process is not running
4. **NFR-4**: Maintain compatibility with existing Android widget caching (minimal changes)

## Acceptance Criteria

- [ ] Paid users never see locked widgets after force closing the app
- [ ] Widget access state persists across app force closes
- [ ] Widget access state persists across device restarts
- [ ] Subscription expiry date is cached and used for validation
- [ ] Widget only locks when subscription definitively expires (not on ambiguous states)
- [ ] Cache includes timestamp for freshness validation
- [ ] Fallback verification prevents false-positive lock states
- [ ] No network calls needed for widget access verification
- [ ] Changes are iOS-focused but maintain cross-platform architecture where sensible

## Technical Context

This feature involves:
- iOS WidgetKit extension (`LaunchWidget/`)
- Kotlin Multiplatform shared code (`composeApp/src/`)
- App Group shared storage (`group.me.calebjones.spacelaunchnow`)
- RevenueCat subscription SDK integration

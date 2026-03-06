# Data Model: iOS Widget Subscription Persistence

**Feature**: 005-ios-widget-subscription-persistence  
**Date**: 2026-03-05

## Overview

This document defines the data structures required for robust widget subscription state caching on iOS.

## Shared Storage Keys (NSUserDefaults App Group)

### App Group Identifier
```
group.me.calebjones.spacelaunchnow
```

### Storage Keys

| Key | Type | Description | Default |
|-----|------|-------------|---------|
| `widget_has_access` | `Bool` | Current premium access status | `false` |
| `widget_subscription_expiry` | `Double` | Unix timestamp of subscription expiry | `nil` |
| `widget_last_verified` | `Double` | Unix timestamp of last verification | `0` |
| `widget_was_ever_premium` | `Bool` | Flag if user was ever premium | `false` |
| `widget_subscription_type` | `String` | Subscription type string | `"FREE"` |

## Kotlin Data Transfer Object

### WidgetAccessCache (NEW)

**Location**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/widgets/WidgetAccessCache.kt`

```kotlin
package me.calebjones.spacelaunchnow.widgets

import kotlinx.serialization.Serializable
import me.calebjones.spacelaunchnow.data.model.SubscriptionType

/**
 * Cache data shared with iOS widget extension via App Group NSUserDefaults
 * Contains all information needed for widget to determine access without main app
 */
@Serializable
data class WidgetAccessCache(
    /** Current premium access status */
    val hasAccess: Boolean = false,
    
    /** Unix timestamp (milliseconds) when subscription expires. Null for lifetime/unknown */
    val subscriptionExpiryMs: Long? = null,
    
    /** Unix timestamp (milliseconds) when this cache was last updated */
    val lastVerifiedMs: Long = 0L,
    
    /** Whether user has ever had a valid premium subscription */
    val wasEverPremium: Boolean = false,
    
    /** Current subscription type string */
    val subscriptionType: SubscriptionType = SubscriptionType.FREE
) {
    /**
     * Determines if widget should show unlocked state based on cache
     * Implements fail-safe logic that defaults to unlocked for paid users
     */
    fun shouldShowUnlocked(): Boolean {
        val currentTimeMs = kotlin.time.Clock.System.now().toEpochMilliseconds()
        
        return when {
            // Explicit access granted
            hasAccess -> true
            
            // Never been premium - definitely locked
            !wasEverPremium -> false
            
            // Was premium, check expiry
            subscriptionExpiryMs != null -> {
                // Subscription hasn't expired yet
                subscriptionExpiryMs > currentTimeMs
            }
            
            // Was premium but no expiry stored (lifetime or data gap)
            // Default to unlocked to avoid false locks
            wasEverPremium -> true
            
            else -> false
        }
    }
    
    companion object {
        /** Cache freshness threshold - 7 days */
        const val FRESHNESS_THRESHOLD_MS = 7 * 24 * 60 * 60 * 1000L
        
        /** Default empty cache */
        val EMPTY = WidgetAccessCache()
    }
}
```

## Swift Data Structure

### WidgetAccessState (NEW)

**Location**: `iosApp/LaunchWidget/WidgetAccessState.swift`

```swift
import Foundation

/// Cached subscription state read from App Group UserDefaults
/// Provides fail-safe access determination for widget without main app
struct WidgetAccessState {
    let hasAccess: Bool
    let subscriptionExpiry: Date?
    let lastVerified: Date
    let wasEverPremium: Bool
    let subscriptionType: String
    
    /// Determines if widget should show unlocked content
    /// Implements fail-safe logic favoring paid users
    var shouldShowUnlocked: Bool {
        // Explicit access
        if hasAccess { return true }
        
        // Never been premium
        if !wasEverPremium { return false }
        
        // Check expiry if available
        if let expiry = subscriptionExpiry {
            return expiry > Date()
        }
        
        // Was premium with no expiry (lifetime or data gap) - default unlocked
        return wasEverPremium
    }
    
    /// Read cached state from App Group UserDefaults
    static func readFromCache() -> WidgetAccessState {
        guard let defaults = UserDefaults(suiteName: "group.me.calebjones.spacelaunchnow") else {
            print("⚠️ Widget: Failed to access App Group UserDefaults")
            return .locked
        }
        
        let hasAccess = defaults.bool(forKey: "widget_has_access")
        
        let expiryTimestamp = defaults.double(forKey: "widget_subscription_expiry")
        let subscriptionExpiry: Date? = expiryTimestamp > 0 
            ? Date(timeIntervalSince1970: expiryTimestamp) 
            : nil
        
        let lastVerifiedTimestamp = defaults.double(forKey: "widget_last_verified")
        let lastVerified = Date(timeIntervalSince1970: lastVerifiedTimestamp)
        
        let wasEverPremium = defaults.bool(forKey: "widget_was_ever_premium")
        let subscriptionType = defaults.string(forKey: "widget_subscription_type") ?? "FREE"
        
        return WidgetAccessState(
            hasAccess: hasAccess,
            subscriptionExpiry: subscriptionExpiry,
            lastVerified: lastVerified,
            wasEverPremium: wasEverPremium,
            subscriptionType: subscriptionType
        )
    }
    
    /// Locked state - used when cache read fails
    static let locked = WidgetAccessState(
        hasAccess: false,
        subscriptionExpiry: nil,
        lastVerified: Date.distantPast,
        wasEverPremium: false,
        subscriptionType: "FREE"
    )
}
```

## Entity Relationships

```
┌─────────────────────────────────────────────────────────────────┐
│                    Main App (Kotlin)                            │
├─────────────────────────────────────────────────────────────────┤
│  LocalSubscriptionData        WidgetAccessCache                 │
│  ┌─────────────────┐          ┌─────────────────┐               │
│  │ isSubscribed    │─────────▶│ hasAccess       │               │
│  │ subscriptionType│─────────▶│ subscriptionType│               │
│  │ lastSynced      │─────────▶│ lastVerifiedMs  │               │
│  │ NEW: expiryMs   │─────────▶│ subscriptionExpiryMs│           │
│  └─────────────────┘          │ wasEverPremium  │               │
│                               └────────┬────────┘               │
│                                        │                        │
│           WidgetAccessSharer.syncWidgetAccessCache()            │
└────────────────────────────────┬───────────────────────────────┘
                                 │
                    NSUserDefaults (App Group)
                                 │
┌────────────────────────────────┴───────────────────────────────┐
│                    Widget Extension (Swift)                     │
├─────────────────────────────────────────────────────────────────┤
│               WidgetAccessState.readFromCache()                 │
│               ┌─────────────────┐                               │
│               │ hasAccess       │                               │
│               │ subscriptionExpiry│                             │
│               │ lastVerified    │                               │
│               │ wasEverPremium  │                               │
│               │ subscriptionType│                               │
│               └────────┬────────┘                               │
│                        │                                        │
│        shouldShowUnlocked (fail-safe logic)                     │
└─────────────────────────────────────────────────────────────────┘
```

## Validation Rules

1. **hasAccess**: Set to `true` when subscription is active and verified
2. **subscriptionExpiryMs**: 
   - Set from EntitlementInfo.expirationDateMillis from RevenueCat
   - Null for lifetime subscriptions
   - Must be in milliseconds since epoch
3. **lastVerifiedMs**: Updated every time subscription state is successfully synced
4. **wasEverPremium**: 
   - Set to `true` once when first premium subscription is detected
   - NEVER set back to `false` (sticky flag)
5. **subscriptionType**: Enum value as string: "FREE", "LEGACY", "PREMIUM", "LIFETIME"

## State Transitions

| Current State | Event | New State | Widget Access |
|---------------|-------|-----------|---------------|
| FREE | Purchase premium | PREMIUM | ✅ Unlocked |
| PREMIUM | Subscription expires | FREE | ❌ Locked |
| PREMIUM | App force closed | (cached) | ✅ Unlocked (via expiry check) |
| PREMIUM | Device restart | (cached) | ✅ Unlocked (via expiry + wasEverPremium) |
| LIFETIME | Any | LIFETIME | ✅ Unlocked (no expiry) |
| FREE + wasEverPremium | Network fails | (cached) | ✅ Unlocked (grace period) |

## Migration Notes

### Backward Compatibility

Existing `widget_has_access` key continues to work. New keys are additive:
- Older app versions will only read `widget_has_access`
- Newer widget code reads all keys but falls back gracefully if missing
- No migration required - new keys will be populated on next subscription sync

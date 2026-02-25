# Quickstart: Fix iOS V5 Notification Filter Bug

**Date**: 2026-02-23 | **Branch**: `fix_notif_filters`  
**Scope**: iOS Swift + Kotlin iosMain (commonMain/androidMain changes handled in Android branch)

---

## What This Fix Does

1. **Removes** the dead V5 Swift filter stack (V5FilterPreferences, V5NotificationFilter, V5PreferencesSyncManager) — never properly connected, always allowed everything.
2. **Adds** a Kotlin preference bridge (`NSEPreferenceBridge`) that writes `NotificationState` filter fields to shared UserDefaults via App Group.
3. **Adds** a lightweight Swift-native filter in the NSE (`NSEFilterPreferences` + `NSENotificationFilter`) that reads UserDefaults and filters notifications independently when the app is killed.

The NSE is the **only** code that reliably runs when the app is killed/terminated. Without this filter, users see notifications they opted out of.

## Prerequisites

- Xcode 15+ (for iOS build)
- Android V5 branch merged or rebased (for Kotlin-side changes)

## Files to Delete

| File | Lines | Why |
|------|-------|-----|
| `iosApp/iosApp/V5FilterPreferences.swift` | ~150 | Replaced by Kotlin `NotificationState` + UserDefaults bridge |
| `iosApp/iosApp/V5NotificationFilter.swift` | ~280 | Replaced by `NSENotificationFilter` (Swift, reads UserDefaults) |
| `iosApp/iosApp/V5PreferencesSyncManager.swift` | ~180 | Replaced by `NSEPreferenceBridge` (Kotlin, writes UserDefaults) |

## Files to Create

| File | Purpose |
|------|---------|
| `composeApp/src/iosMain/.../notifications/NSEPreferenceBridge.kt` | Kotlin: writes expanded filter IDs to `UserDefaults(suiteName:)` |
| `iosApp/NotificationServiceExtension/NSEFilterPreferences.swift` | Swift: reads filter prefs from shared UserDefaults |
| `iosApp/NotificationServiceExtension/NSENotificationFilter.swift` | Swift: lightweight filter (agency/location match, strict/flexible) |

## Files to Modify

| File | What |
|------|------|
| `iosApp/NotificationServiceExtension/NotificationService.swift` | Add filter call: load prefs → apply filter → deliver/suppress |
| `iosApp/iosApp.xcodeproj/project.pbxproj` | Remove deleted file refs, add new NSE files to NSE target |

## Files to Keep (no changes)

| File | Why |
|------|-----|
| `iosApp/iosApp/V5NotificationData.swift` | Still used by NSE for payload parsing + image URL + filter IDs |
| `iosApp/iosApp/AppDelegate.swift` | Already routes through Kotlin bridge |
| `composeApp/src/iosMain/.../IosNotificationBridge.kt` | Already uses V4 NotificationFilter |

## Key Architecture

### UserDefaults Keys (App Group: `group.me.spacelaunchnow.spacelaunchnow`)

| Key | Type | Default |
|-----|------|---------|
| `nse_enable_notifications` | Bool | `true` |
| `nse_follow_all_launches` | Bool | `true` |
| `nse_use_strict_matching` | Bool | `false` |
| `nse_subscribed_agencies` | [String] | `[]` (empty = allow all when `followAll` is true) |
| `nse_subscribed_locations` | [String] | `[]` |

IDs are **expanded** at write time (includes `additionalIds` from `NotificationAgency`/`NotificationLocation`).

### NSE Filter Logic

```
1. !enableNotifications → suppress (empty content)
2. followAllLaunches → allow
3. Check String(payload.lspId) in subscribedAgencies
4. Check String(payload.locationId) in subscribedLocations
5. Strict: BOTH match → allow. Flexible: EITHER match → allow.
```

## Verification

```bash
# After deletions, verify no dangling refs
grep -r "V5FilterPreferences" iosApp/ --include="*.swift"
grep -r "V5NotificationFilter" iosApp/ --include="*.swift"
grep -r "V5PreferencesSyncManager" iosApp/ --include="*.swift"

# All should return 0 results
```

Then build both targets in Xcode:
- iosApp (Debug) ✅
- NotificationServiceExtension ✅

## Expected Outcomes

- **Lines removed**: ~650 (3 dead Swift files + old NSE filter logic)
- **Lines added**: ~200 (NSEPreferenceBridge.kt + NSEFilterPreferences.swift + NSENotificationFilter.swift + NotificationService.swift filter integration)
- **Net**: ~-450 lines
- **Bug fixed**: V5 filter stack replaced. NSE filters independently via UserDefaults. Kotlin bridge handles in-app filtering.

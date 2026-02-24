# Data Model: Fix iOS V5 Notification Filter Bug

**Date**: 2026-02-20 | **Spec**: fix_ios.md  
**Scope**: iOS Swift only (Kotlin model changes handled in Android branch)

---

## Entities Deleted (Swift)

### 1. V5FilterPreferences.swift (DELETE)

**File**: `iosApp/iosApp/V5FilterPreferences.swift`

**Reason**: Replaced by Kotlin `NotificationState` accessed via `IosNotificationBridge`. This Swift struct maintained a parallel Int-based filter preference system that was never connected to user settings. The Kotlin `NotificationState` (String-based) is already persisted to App Groups via DataStore.

### 2. V5NotificationFilter.swift (DELETE)

**File**: `iosApp/iosApp/V5NotificationFilter.swift`

**Reason**: Replaced by Kotlin `NotificationFilter` called via `IosNotificationBridge`. This was a 280-line Swift port of filter logic that duplicated Kotlin code already accessible from iOS.

### 3. V5PreferencesSyncManager.swift (DELETE)

**File**: `iosApp/iosApp/V5PreferencesSyncManager.swift`

**Reason**: Synced `V5FilterPreferences` to App Groups, but the main app never called `savePreferences()`. `NotificationState` is already in App Groups via Kotlin DataStore, making this sync layer redundant.

---

## Entities Kept (Swift)

### 4. V5NotificationData.swift (KEEP — no changes)

**File**: `iosApp/iosApp/V5NotificationData.swift`

Used by the NSE for:
- V5 payload detection: `isV5Payload()` checks for `lsp_id` field
- Payload parsing: `fromUserInfo()` extracts title, body, image URL
- Image attachment: `launchImage` URL used by NSE to download and attach image

```
V5NotificationData
├── notificationType: String
├── title: String
├── body: String
├── launchUuid: String
├── launchId: String
├── launchName: String
├── launchImage: String?        ← Used by NSE for image attachment
├── launchNet: String
├── launchLocation: String
├── webcast: Bool
├── webcastLive: Bool
├── lspId: Int?                 ← Used by NSE filter (converted to String for comparison)
├── locationId: Int?            ← Used by NSE filter (converted to String for comparison)
├── programIds: [Int]
├── statusId: Int?
├── orbitId: Int?
├── missionTypeId: Int?
└── launcherFamilyId: Int?
```

---

## Entities Added (Swift — NSE)

### 6. NSEFilterPreferences (NEW)

**File**: `iosApp/NotificationServiceExtension/NSEFilterPreferences.swift`

**Purpose**: Reads filter preferences from shared UserDefaults (written by Kotlin via App Group). Used by the NSE to filter notifications independently when the app is killed.

```
NSEFilterPreferences
├── enableNotifications: Bool       ← UserDefaults "nse_enable_notifications" (default: true)
├── followAllLaunches: Bool         ← UserDefaults "nse_follow_all_launches" (default: true)
├── useStrictMatching: Bool         ← UserDefaults "nse_use_strict_matching" (default: false)
├── subscribedAgencies: Set<String> ← UserDefaults "nse_subscribed_agencies" (default: empty → allow all)
└── subscribedLocations: Set<String>← UserDefaults "nse_subscribed_locations" (default: empty → allow all)
```

**UserDefaults suite**: `group.me.spacelaunchnow.spacelaunchnow`

**Defaults behavior**: If keys are absent (fresh install, app never opened), defaults to `enableNotifications: true, followAllLaunches: true` — all notifications pass through.

### 7. NSENotificationFilter (NEW)

**File**: `iosApp/NotificationServiceExtension/NSENotificationFilter.swift`

**Purpose**: Lightweight Swift-native filter for the NSE. Mirrors the Kotlin `NotificationFilter` logic but simplified — no lookup tables needed because IDs are pre-expanded at write time.

**Filter logic**:
1. If `!enableNotifications` → suppress
2. If `followAllLaunches` → allow
3. Check `String(payload.lspId)` in `subscribedAgencies`
4. Check `String(payload.locationId)` in `subscribedLocations`
5. Apply strict (AND) or flexible (OR) matching

**Suppression**: Returns `false` → NSE delivers empty content (`title: "", body: "", sound: nil`), which iOS does not display.

---

## Entities Added (Kotlin — iosMain)

### 8. NSEPreferenceBridge (NEW)

**File**: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/NSEPreferenceBridge.kt`

**Purpose**: Writes `NotificationState` filter fields to `NSUserDefaults(suiteName:)` in the shared App Group. Called whenever notification preferences change. Expands `additionalIds` at write time.

**Keys written**:
| UserDefaults Key | Type | Source |
|---|---|---|
| `nse_enable_notifications` | `Bool` | `NotificationState.enableNotifications` |
| `nse_follow_all_launches` | `Bool` | `NotificationState.followAllLaunches` |
| `nse_use_strict_matching` | `Bool` | `NotificationState.useStrictMatching` |
| `nse_subscribed_agencies` | `[String]` | Expanded from `subscribedAgencies` + `NotificationAgency.additionalIds` |
| `nse_subscribed_locations` | `[String]` | Expanded from `subscribedLocations` + `NotificationLocation.additionalIds` |

**Expansion example**: User subscribes to Russia ("111"). `NotificationAgency.RUSSIA.additionalIds` = ["96", "193", "63"]. Bridge writes `["111", "96", "193", "63"]` to UserDefaults.

---

## Entities Modified (Swift)

### 5. NotificationService.swift (SIMPLIFY + ADD FILTER)

**File**: `iosApp/NotificationServiceExtension/NotificationService.swift`

Remove all filter logic. The NSE becomes a content-enrichment pass-through:

**Before**:
```
didReceive() → parse V5 → load V5FilterPreferences → apply V5NotificationFilter → deliver/suppress
```

**After**:
```
didReceive() → parse V5 → load NSEFilterPreferences (UserDefaults) → apply NSENotificationFilter → deliver/suppress
             → if allowed: set title/body → attach image → deliver
             → if blocked: deliver empty content (suppressed)
```

---

## Entities Unchanged (Kotlin — handled in Android branch)

These changes are out of scope for this iOS branch but listed for reference:

| Entity | Change | Branch |
|--------|--------|--------|
| `NotificationData.kt` | `fromMap()` reads `lsp_id` fallback | Android |
| `NotificationState.kt` | Remove `v5Preferences` field | Android |
| `V5FilterPreferences.kt` | DELETE | Android |
| `V5NotificationFilter.kt` | DELETE | Android |
| `NotificationWorker.kt` | Unified filter path | Android |
| `IosNotificationBridge.kt` | NO CHANGE | N/A |

---

## Data Flow (iOS — After Fix)

```
Server Payload (V5)
  ├── lsp_id: "121"          (String from server)
  ├── location_id: "12"      (String from server)
  └── launch_image: "https://..."
         │
    ┌────▼────────────────────────────┐
    │ NSE (NotificationService)          │
    │ 1. Parse V5NotificationData         │
    │ 2. Load NSEFilterPreferences         │
    │    (from UserDefaults via App Group) │
    │ 3. Apply NSENotificationFilter       │
    │    ├─ ALLOWED: enrich + deliver      │
    │    └─ BLOCKED: deliver empty content │
    └────┬────────────────────────────────┘
         │
         ▼
    AppDelegate → IosNotificationBridge (Kotlin)
         │
         ▼
    NotificationData.fromMap()     ← reads lsp_id → agencyId (String)
    NotificationFilter.shouldShow  ← String "121" in Set<String>
         │
    ALLOWED / BLOCKED
```

### Preference Sync Flow

```
User changes settings in UI
         │
         ▼
NotificationStateStorage.saveState()   ← Writes to DataStore (.preferences_pb)
         │
         ▼
NSEPreferenceBridge.syncToUserDefaults()  ← Writes expanded IDs to UserDefaults
         │                                         (App Group: group.me.spacelaunchnow.spacelaunchnow)
         ▼
UserDefaults keys:
  nse_enable_notifications: true
  nse_follow_all_launches: false
  nse_use_strict_matching: false
  nse_subscribed_agencies: ["121"]           ← SpaceX only
  nse_subscribed_locations: ["27", "12"]     ← Florida (27) + Cape Canaveral (12, additionalId)
```

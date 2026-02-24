# Feature Specification: Fix V5 Notification Filter Bug

**Branch**: `fix_notif_filters` | **Date**: 2026-02-18  
**Type**: Bug Fix | **Priority**: Critical

## Problem Statement

Users with custom V5 notification filters (e.g., "SpaceX + Florida") are receiving notifications for **ALL** launches regardless of their filter settings. For example, a user subscribed ONLY to SpaceX (lsp_id="121") and Florida (location_id="12") is receiving notifications for China launches, EU launches, and all other launches.

### Reproduction

**Given**: User has configured notification filters:
- Agency: SpaceX (ID: 121)
- Location: Cape Canaveral, Florida (ID: 12)

**When**: Server sends notification for China launch:
```json
{
  "lsp_id": "96",          // China National Space Administration
  "location_id": "17"      // Jiuquan Satellite Launch Center
}
```

**Expected**: Notification is BLOCKED (does not match user's filters)  
**Actual**: Notification is SHOWN (filters are not working)

### Example Notification Payload (Working Example)

```json
{
  "notification_type": "twentyFourHour",
  "title": "Falcon 9 Block 5 | Starlink Group 10-36",
  "body": "Launch attempt from Cape Canaveral SFS, FL, USA in -1 hours.",
  "launch_uuid": "57cc5e9e-97c2-4833-8bcd-e4e62ce63ec6",
  "launch_name": "Falcon 9 Block 5 | Starlink Group 10-36",
  "launch_net": "2026-02-18T22:00:00Z",
  "launch_location": "Cape Canaveral SFS, FL, USA",
  "webcast": "True",
  "lsp_id": "121",         // String, not Int!
  "location_id": "12",     // String, not Int!
  "program_id": "25"       // String, not Int!
}
```

**Critical Observation**: Server sends **String IDs**, not Integers.

## Root Cause Analysis

### Current Implementation (Overcomplicated)

The V5 notification system attempts to:

1. **Type Conversion**: Parse String IDs → Int IDs (`"121" → 121`)
2. **Dual State Systems**: Maintain V4 (String-based) and V5 (Int-based) preferences separately
3. **State Synchronization**: Sync V4 String IDs → V5 Int IDs on every state update
4. **Null Semantics**: Use complex null logic (null = follow all, empty = block all)
5. **Int-based Filtering**: Check `Int in Set<Int>?` with null handling

**Result**: ~500 lines of complex sync logic, dual preference systems, and filtering bugs.

### Why V4 Works Correctly

The V4 notification system:
- Uses String IDs (`"121"`, `"12"`) matching server format
- Simple membership check: `"121" in state.subscribedAgencies`
- No type conversion errors
- No sync logic needed

### Why the NSE Must Filter (Not Just Enrich)

The iOS Notification Service Extension (NSE) is the **only** code that reliably runs when the app is killed or terminated. Apple does not guarantee `application(_:didReceiveRemoteNotification:fetchCompletionHandler:)` fires when the app is not running.

| App State | `didReceiveRemoteNotification` fires? | NSE fires? |
|---|---|---|
| **Foreground** | Yes | No (for data-only) |
| **Background** | Sometimes (needs BAR + `content-available`) | Yes |
| **Killed/Terminated** | **No** | **Yes** |

If the NSE only enriches content without filtering, users will see notifications they opted out of whenever the app isn't running. **The NSE must independently apply filter preferences.**

The NSE runs in a separate process and cannot call Kotlin code (`IosNotificationBridge`, `NotificationFilter`). It also cannot read Kotlin DataStore's protobuf format (`.preferences_pb`). Therefore, the main app must write filter preferences to a shared location the NSE can read — **UserDefaults via App Group** (`group.me.spacelaunchnow.spacelaunchnow`).

## Proposed Solution: Reuse V4 String-Based Approach

### Philosophy

**"Always match the data format from the source."**

The server sends String IDs → Use String IDs in the app.  
Don't over-engineer type conversions without a clear benefit.

### Implementation Strategy

#### 1. Change V5NotificationPayload to String IDs

```kotlin
// BEFORE (complex Int conversion)
data class V5NotificationPayload(
    val lspId: Int?,                    // Converted from String
    val locationId: Int?,               // Converted from String
    val programIds: List<Int>,          // Parsed and converted
    // ...
)

// AFTER (simple String passthrough)
data class V5NotificationPayload(
    val lspId: String?,                 // "121" for SpaceX
    val locationId: String?,            // "12" for Cape Canaveral
    val programId: String?,             // "25" for Artemis (single value)
    // ...
)
```

#### 2. Filter Using NotificationState Directly

```kotlin
// BEFORE (used separate V5FilterPreferences with Int-based, null semantics)
fun shouldShow(
    payload: V5NotificationPayload,
    preferences: V5FilterPreferences  // subscribedLspIds: Set<Int>?
): FilterResult

// AFTER (reuse NotificationState from V4)
fun shouldShow(
    payload: V5NotificationPayload,
    state: NotificationState  // subscribedAgencies: Set<String>
): FilterResult
```

#### 3. Simplified Filter Logic

```kotlin
fun shouldShow(payload: V5NotificationPayload, state: NotificationState): FilterResult {
    // 1. Check master enable
    if (!state.enableNotifications) return BLOCKED("Notifications disabled")
    
    // 2. Check follow all launches bypass
    if (state.followAllLaunches) return ALLOWED
    
    // 3. Check agency/LSP match
    val agencyMatches = payload.lspId in state.subscribedAgencies
    
    // 4. Check location match
    val locationMatches = payload.locationId in state.subscribedLocations
    
    // 5. Apply matching mode
    if (state.useStrictMatching) {
        // Strict: BOTH must match
        return if (agencyMatches && locationMatches) ALLOWED else BLOCKED
    } else {
        // Flexible: AT LEAST ONE must match
        return if (agencyMatches || locationMatches) ALLOWED else BLOCKED
    }
}
```

#### 4. NSE Preference Bridge (Kotlin → UserDefaults)

The NSE cannot call Kotlin code. When `NotificationState` is saved on iOS, Kotlin also writes key filter fields to shared `UserDefaults(suiteName: "group.me.spacelaunchnow.spacelaunchnow")`.

**Keys written** (prefixed `nse_` to avoid collision):

| UserDefaults Key | Type | Source |
|---|---|---|
| `nse_enable_notifications` | `Bool` | `NotificationState.enableNotifications` |
| `nse_follow_all_launches` | `Bool` | `NotificationState.followAllLaunches` |
| `nse_use_strict_matching` | `Bool` | `NotificationState.useStrictMatching` |
| `nse_subscribed_agencies` | `[String]` | Expanded `subscribedAgencies` + `additionalIds` |
| `nse_subscribed_locations` | `[String]` | Expanded `subscribedLocations` + `additionalIds` |

**ID Expansion**: Subscribed IDs are expanded at write time to include `additionalIds` from `NotificationAgency` and `NotificationLocation` enums. E.g., subscribing to Russia ("111") writes `["111", "96", "193", "63"]`. Subscribing to Florida ("27") writes `["27", "12"]`. This keeps the NSE filter simple — just `Set.contains()` with no lookup tables.

**Write trigger**: Whenever `NotificationStateStorage.saveState()` runs on iOS, also call the bridge to sync to UserDefaults. Also sync on app launch to ensure consistency.

**Implementation location**: New class `NSEPreferenceBridge` in `iosMain` (Kotlin/Native can access `NSUserDefaults` directly).

#### 5. NSE Swift-Native Filter

The NSE reads filter preferences from shared UserDefaults and applies a lightweight filter:

```swift
struct NSEFilterPreferences {
    let enableNotifications: Bool
    let followAllLaunches: Bool
    let useStrictMatching: Bool
    let subscribedAgencies: Set<String>
    let subscribedLocations: Set<String>
    
    static func load() -> NSEFilterPreferences {
        let defaults = UserDefaults(suiteName: "group.me.spacelaunchnow.spacelaunchnow")
        // Read fields, default to allow-all if unset
        ...
    }
}
```

Filter logic mirrors Kotlin `NotificationFilter` (simplified for NSE context):

```swift
struct NSENotificationFilter {
    static func shouldShow(
        payload: V5NotificationData,
        preferences: NSEFilterPreferences
    ) -> Bool {
        // 1. Master kill switch
        if !preferences.enableNotifications { return false }
        
        // 2. Follow all bypass
        if preferences.followAllLaunches { return true }
        
        // 3. Agency/location matching (IDs are already expanded)
        let agencyMatch = payload.lspId
            .map { preferences.subscribedAgencies.contains(String($0)) } ?? false
        let locationMatch = payload.locationId
            .map { preferences.subscribedLocations.contains(String($0)) } ?? false
        
        // 4. Strict vs flexible
        if preferences.useStrictMatching {
            return agencyMatch && locationMatch
        } else {
            return agencyMatch || locationMatch
        }
    }
}
```

**Suppression**: When a notification is blocked, the NSE delivers empty content (`title: "", body: "", sound: nil`) which iOS does not display.

**Fallback**: If UserDefaults has never been written (fresh install, app never opened), defaults are `enableNotifications: true, followAllLaunches: true` → allow all notifications through. This is safe because users haven't configured filters yet.

### Benefits

1. **Simplicity**: No complex Int conversions, no dual systems, no sync logic
2. **Correctness**: Matches actual server data format (String IDs)
3. **Maintainability**: ~180 fewer lines of code, single source of truth
4. **Performance**: No String → Int conversion overhead, simple Set.contains() checks
5. **Consistency**: V4 and V5 use the same filtering approach
6. **Reliability**: NSE filters independently when app is killed — users never see unwanted notifications

## Requirements

### Functional Requirements

**FR-1**: V5 notification filtering MUST use String IDs matching server payload format  
**FR-2**: V5 filtering MUST reuse `NotificationState.subscribedAgencies` and `NotificationState.subscribedLocations`  
**FR-3**: User with "SpaceX + Florida" subscription MUST NOT receive notifications for China/EU/other launches  
**FR-4**: User with "Follow All Launches" enabled MUST receive all notifications (bypass filters)  
**FR-5**: Strict matching MUST require both agency AND location to match  
**FR-6**: Flexible matching MUST allow if agency OR location matches  
**FR-7**: Existing user settings MUST continue to work without migration  
**FR-8**: The NSE MUST independently filter notifications when the app is killed/terminated, using preferences synced via App Group UserDefaults  
**FR-9**: Kotlin MUST write expanded filter preferences (including `additionalIds`) to shared UserDefaults whenever `NotificationState` changes  
**FR-10**: The NSE MUST suppress blocked notifications by delivering empty content (iOS will not display it)  
**FR-11**: If UserDefaults has never been written (fresh install), the NSE MUST default to allowing all notifications

### Non-Functional Requirements

**NFR-1**: Filter evaluation MUST complete in <10ms  
**NFR-2**: Solution MUST reduce code complexity (measured by lines of code and cyclomatic complexity)  
**NFR-3**: Solution MUST NOT introduce new dependencies  
**NFR-4**: All existing V4 notification behavior MUST remain unchanged  
**NFR-5**: NSE preference sync MUST use `NSUserDefaults.synchronize()` to ensure writes are flushed before the NSE reads them

### Testing Requirements

**TR-1**: Test case: SpaceX + Florida → ALLOW SpaceX from Florida  
**TR-2**: Test case: SpaceX + Florida → BLOCK China from Jiuquan  
**TR-3**: Test case: SpaceX + Florida (flexible) → ALLOW ULA from Florida (location match)  
**TR-4**: Test case: SpaceX + Florida (strict) → BLOCK ULA from Florida (agency mismatch)  
**TR-5**: Test case: Follow all launches → ALLOW everything  
**TR-6**: Test case: Multiple agencies → ALLOW any match  
**TR-7**: Test case: Notifications disabled → BLOCK everything  
**TR-8**: Test case: NSE filter blocks non-matching notification (delivers empty content)  
**TR-9**: Test case: NSE preference bridge writes expanded agency IDs (Russia "111" → ["111", "96", "193", "63"])  
**TR-10**: Test case: NSE preference bridge writes expanded location IDs (Florida "27" → ["27", "12"])  
**TR-11**: Test case: NSE with no UserDefaults data (fresh install) → allows all notifications  
**TR-12**: Test case: NSE filter allows matching notification (delivers with image attachment)

## Success Criteria

1. ✅ User with "SpaceX + Florida" receives ONLY matching notifications
2. ✅ All integration tests pass
3. ✅ Code reduction: Net -150+ lines of code
4. ✅ No data migration required
5. ✅ Filter evaluation <10ms
6. ✅ Existing V4 behavior unchanged
7. ✅ NSE filters independently when app is killed — no unwanted notifications leak through
8. ✅ Kotlin preference bridge writes expanded IDs to UserDefaults on every state save
9. ✅ NSE builds and runs with new Swift filter code

## Out of Scope

- V5 preference migration (will be deprecated)
- Additional filter categories beyond LSP and Location
- UI changes (settings UI already works with String IDs)
- Server-side filtering changes
- Unifying App Group identifiers (both `group.me.calebjones.spacelaunchnow` and `group.me.spacelaunchnow.spacelaunchnow` exist in entitlements; this PR uses the one Kotlin already writes to)

## Dependencies

- Existing `NotificationState` data class
- Existing `V5NotificationPayload` parsing logic
- Existing `NotificationWorker` integration
- App Group `group.me.spacelaunchnow.spacelaunchnow` (shared between main app and NSE)
- `NSUserDefaults(suiteName:)` API (Kotlin/Native and Swift)
- `NotificationAgency` and `NotificationLocation` enums (for `additionalIds` expansion)

## Migration Strategy

**No user-facing migration required** - Settings automatically work with the new filter since both V4 and V5 now use the same String-based NotificationState.

## References

- V4 Client-Side Filtering: `docs/notifications/V4_CLIENT_SIDE_FILTERING.md`
- V5 Simplified Solution: `docs/notifications/V5_SIMPLIFIED_SOLUTION.md`
- NotificationState Model: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/NotificationState.kt`
- V4 NotificationFilter Working Implementation: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/NotificationData.kt`
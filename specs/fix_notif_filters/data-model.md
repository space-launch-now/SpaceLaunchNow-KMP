# Data Model: V5 Notification Filter Fix

**Date**: 2026-02-18  
**Branch**: `fix_notif_filters`

## Overview

This document describes the data model changes required to fix the V5 notification filter bug. The fix simplifies the data model by eliminating duplicate state systems and type conversions.

## Modified Entities

### 1. V5NotificationPayload (MODIFIED)

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationPayload.kt`

**Current State** (Buggy):
```kotlin
@Serializable
data class V5NotificationPayload(
    // ... display fields ...
    
    // V5 Filtering IDs (Int-based - WRONG!)
    val lspId: Int?,                    // ❌ Converted from String
    val locationId: Int?,               // ❌ Converted from String
    val programIds: List<Int>,          // ❌ Parsed and converted
    val statusId: Int?,                 // ❌ Converted from String
    val orbitId: Int?,                  // ❌ Converted from String
    val missionTypeId: Int?,            // ❌ Converted from String
    val launcherFamilyId: Int?          // ❌ Converted from String
)
```

**New State** (Fixed):
```kotlin
@Serializable
data class V5NotificationPayload(
    // Display Content (unchanged)
    val notificationType: String,
    val title: String,
    val body: String,
    
    // Launch Identification (unchanged)
    val launchUuid: String,
    val launchId: String,
    val launchName: String,
    
    // Launch Details (unchanged)
    val launchImage: String?,
    val launchNet: String,
    val launchLocation: String,
    val webcast: Boolean,
    val webcastLive: Boolean,
    
    // V5 Filtering IDs (String-based - CORRECT!)
    val lspId: String?,                 // ✅ "121" for SpaceX
    val locationId: String?,            // ✅ "12" for Cape Canaveral
    val programId: String?,             // ✅ "25" for Artemis (single value, not list)
    val statusId: String?,              // ✅ "1" for Go
    val orbitId: String?,               // ✅ "8" for LEO
    val missionTypeId: String?,         // ✅ "10" for Communications
    val launcherFamilyId: String?       // ✅ "1" for Falcon 9
) {
    companion object {
        /**
         * Parse V5 payload from FCM data map
         * NOTE: Changed to keep IDs as Strings (no Int conversion)
         */
        fun fromMap(data: Map<String, String>): V5NotificationPayload? {
            return try {
                V5NotificationPayload(
                    // ... display fields (unchanged) ...
                    
                    // V5 Extended Fields - KEEP AS STRING
                    lspId = data["lsp_id"],  // ✅ No toIntOrNull()
                    locationId = data["location_id"],  // ✅ No toIntOrNull()
                    programId = data["program_id"],  // ✅ Single value (not comma-separated list)
                    statusId = data["status_id"],
                    orbitId = data["orbit_id"],
                    missionTypeId = data["mission_type_id"],
                    launcherFamilyId = data["launcher_family_id"]
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
```

**Changes**:
- **Field Type Changes**: All ID fields changed from `Int?` / `List<Int>` → `String?`
- **programIds → programId**: Changed from list to single value (server sends single ID, not comma-separated)
- **Parsing Changes**: Removed `toIntOrNull()` conversions - keep values as Strings
- **Validation**: Removed - String IDs don't need validation, server is source of truth

**Relationships**:
- **Consumed by**: V5NotificationFilter.shouldShow()
- **Parsed from**: FCM data map (`Map<String, String>`)
- **Used in**: NotificationWorker (Android), IosNotificationBridge (iOS)

---

### 2. V5NotificationFilter (MODIFIED)

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationFilter.kt`

**Current Signature** (Buggy):
```kotlin
object V5NotificationFilter {
    fun shouldShow(
        payload: V5NotificationPayload,
        preferences: V5FilterPreferences  // ❌ Complex Int-based, null semantics
    ): FilterResult
}
```

**New Signature** (Fixed):
```kotlin
object V5NotificationFilter {
    /**
     * Determine if V5 notification should be shown
     * 
     * Uses same filtering logic as V4 NotificationFilter, adapted for V5 payload structure.
     * Reuses NotificationState (String-based subscriptions) instead of V5FilterPreferences.
     * 
     * @param payload V5 notification data with String IDs
     * @param state User notification preferences (from V4, works for V5 too)
     * @return FilterResult indicating whether to show or block
     */
    fun shouldShow(
        payload: V5NotificationPayload,
        state: NotificationState  // ✅ Reuse V4 model
    ): FilterResult {
        // 1. Check master enable
        if (!state.enableNotifications) {
            return FilterResult.blocked("Notifications disabled")
        }
        
        // 2. Check follow all launches (bypass filters)
        if (state.followAllLaunches) {
            return FilterResult.Allowed
        }
        
        // 3. Check if both filters empty (block all)
        if (state.subscribedAgencies.isEmpty() && state.subscribedLocations.isEmpty()) {
            return FilterResult.blocked("No agencies or locations subscribed")
        }
        
        // 4. Determine active filters
        val hasAgencyFilter = state.subscribedAgencies.isNotEmpty()
        val hasLocationFilter = state.subscribedLocations.isNotEmpty()
        
        // 5. Check agency/LSP match (String membership)
        val agencyMatches = if (hasAgencyFilter) {
            payload.lspId in state.subscribedAgencies  // ✅ String in Set<String>
        } else {
            true  // No filter = don't block on agency
        }
        
        // 6. Check location match (String membership)
        val locationMatches = if (hasLocationFilter) {
            payload.locationId in state.subscribedLocations  // ✅ String in Set<String>
        } else {
            true  // No filter = don't block on location
        }
        
        // 7. Apply matching mode
        return if (state.useStrictMatching) {
            // Strict: BOTH must match
            if (!hasAgencyFilter || !hasLocationFilter) {
                FilterResult.blocked("Strict matching requires both agency AND location filters")
            } else if (agencyMatches && locationMatches) {
                FilterResult.Allowed
            } else {
                FilterResult.blocked("Strict matching failed: agency=$agencyMatches, location=$locationMatches")
            }
        } else {
            // Flexible: AT LEAST ONE must match
            if (agencyMatches || locationMatches) {
                FilterResult.Allowed
            } else {
                FilterResult.blocked("No filters matched: agency=$agencyMatches, location=$locationMatches")
            }
        }
    }
}
```

**Changes**:
- **Parameter Type**: `V5FilterPreferences` → `NotificationState`
- **Logic Simplification**: Removed category filter checks (LSP, Location, Program, Orbit, etc.) - focus on LSP and Location only
- **String Membership**: `lspId in state.subscribedAgencies`, `locationId in state.subscribedLocations`
- **Removed**: Complex null semantics, immediate block checks, category result tracking
- **Line Count**: ~300 lines → ~120 lines (-60%)

**Relationships**:
- **Consumes**: V5NotificationPayload, NotificationState
- **Returns**: FilterResult
- **Called by**: NotificationWorker.processV5Notification()

---

### 3. NotificationWorker (MODIFIED - Android Only)

**File**: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/workers/NotificationWorker.kt`

**Current Code** (Buggy):
```kotlin
private suspend fun processV5Notification(dataMap: Map<String, String>): Result {
    val v5Payload = V5NotificationPayload.fromMap(dataMap) ?: return Result.failure()
    
    val settings = notificationStateStorage.getState()
    val v5Preferences = settings.v5Preferences  // ❌ Using V5FilterPreferences
    
    val filterResult = V5NotificationFilter.shouldShow(v5Payload, v5Preferences)
    // ...
}
```

**New Code** (Fixed):
```kotlin
private suspend fun processV5Notification(dataMap: Map<String, String>): Result {
    val v5Payload = V5NotificationPayload.fromMap(dataMap) ?: return Result.failure()
    
    val state = notificationStateStorage.getState()  // ✅ Using NotificationState directly
    
    val filterResult = V5NotificationFilter.shouldShow(v5Payload, state)  // ✅ Pass state
    // ...
}
```

**Changes**:
- **Variable Name**: `settings` → `state` (clarity)
- **Remove**: `settings.v5Preferences` accessor
- **Pass**: Full `NotificationState` to filter

---

## Reused Entities (NO CHANGES)

### NotificationState (REUSED AS-IS)

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/NotificationState.kt`

**Why No Changes Needed**: This model already has everything V5 needs:

```kotlin
@Serializable
data class NotificationState(
    val enableNotifications: Boolean = true,
    val followAllLaunches: Boolean = true,
    val useStrictMatching: Boolean = false,
    
    // String-based subscriptions (works for V4 AND V5!)
    val subscribedAgencies: Set<String> = getDefaultAgencyIds(),  // ["121", "44", ...]
    val subscribedLocations: Set<String> = getDefaultLocationIds(),  // ["12", "27", ...]
    
    // Topic settings (timing filters)
    val topicSettings: Map<String, Boolean> = NotificationTopic.getDefaultTopicSettings(),
    
    // ... other fields
)
```

**Usage**:
- ✅ Already persisted to DataStore
- ✅ Already bound to Settings UI
- ✅ Already used by V4 NotificationFilter
- ✅ Now also used by V5 NotificationFilter

---

## Deprecated Entities (TO BE REMOVED IN FUTURE)

### V5FilterPreferences (DEPRECATED)

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5FilterPreferences.kt`

**Status**: Mark as deprecated, remove in future release after confirming V5 works with NotificationState

**Current Usage**:
- `NotificationState.v5Preferences: V5FilterPreferences` field
- Sync logic in `NotificationState.withAgencyEnabled()`, etc.

**Removal Plan**:
1. This PR: Stop using V5FilterPreferences in V5NotificationFilter
2. Add @Deprecated annotation to V5FilterPreferences class
3. Future PR: Remove v5Preferences field from NotificationState
4. Future PR: Delete V5FilterPreferences.kt file entirely

---

## State Transitions

### User Settings Update Flow

```
User toggles "SpaceX" in Settings UI
    ↓
SettingsViewModel.toggleAgencySubscription(NotificationAgency(id=121))
    ↓
NotificationRepository.setAgencyEnabled(agency.id.toString(), enabled)
    ↓
NotificationState.withAgencyEnabled("121", enabled = true)
    ↓
state.subscribedAgencies = ["121", ...]  // ✅ String ID added
    ↓
NotificationStateStorage.saveState(state)  // Persist to DataStore
    ↓
UI updates immediately (StateFlow propagation)
```

**No V4→V5 sync needed!** Both use the same String-based state.

### Notification Reception Flow (V5)

```
FCM Push → {"lsp_id": "121", "location_id": "12"}
    ↓
NotificationWorker.doWork()
    ↓
V5NotificationPayload.fromMap(data)
    ├─→ lspId = "121"  // ✅ Keep as String
    └─→ locationId = "12"  // ✅ Keep as String
    ↓
NotificationStateStorage.getState()
    ├─→ subscribedAgencies = ["121", "44"]
    └─→ subscribedLocations = ["12", "27"]
    ↓
V5NotificationFilter.shouldShow(payload, state)
    ├─→ agencyMatches = "121" in ["121", "44"] → true ✅
    ├─→ locationMatches = "12" in ["12", "27"] → true ✅
    └─→ Result: ALLOWED (flexible mode: agency OR location match)
    ↓
NotificationDisplayHelper.displayV5Notification(payload)
    ↓
Notification shown to user ✅
```

### Notification Blocking Flow (China Launch Example)

```
FCM Push → {"lsp_id": "96", "location_id": "17"}  // China National Space Admin, Jiuquan
    ↓
V5NotificationPayload.fromMap(data)
    ├─→ lspId = "96"
    └─→ locationId = "17"
    ↓
NotificationStateStorage.getState()
    ├─→ subscribedAgencies = ["121"]  // Only SpaceX
    └─→ subscribedLocations = ["12"]  // Only Cape Canaveral
    ↓
V5NotificationFilter.shouldShow(payload, state)
    ├─→ agencyMatches = "96" in ["121"] → false ❌
    ├─→ locationMatches = "17" in ["12"] → false ❌
    └─→ Result: BLOCKED (flexible mode: no matches)
    ↓
Notification suppressed ✅
```

---

## Validation Rules

### V5NotificationPayload Validation

**Required Fields** (from server):
- `notification_type`: Must be non-empty String
- `title`: Must be non-empty String (fallback to `launch_name`)
- `launch_uuid`: Must be valid UUID String
- `launch_name`: Must be non-empty String
- `launch_net`: Must be ISO 8601 datetime String

**Optional Fields** (from server):
- `lsp_id`: String (can be null)
- `location_id`: String (can be null)
- `program_id`: String (can be null)
- All other filter IDs: String (can be null)

**No Type Validation**: Since we keep IDs as Strings, no need to validate Int parsing

### NotificationState Validation

**Invariants** (already enforced by existing code):
- `subscribedAgencies` must contain at least 1 ID (UI prevents unchecking last agency)
- `subscribedLocations` must contain at least 1 ID (UI prevents unchecking last location)
- Both sets must be empty only temporarily (should auto-reset to defaults)

**No Additional Validation Needed**: String IDs don't need format validation

---

## Migration Strategy

**No migration required!**

### Why No Migration?

1. **Settings already stored as Strings**: `subscribedAgencies` and `subscribedLocations` are already `Set<String>` in DataStore
2. **UI already uses Strings**: Settings UI binds to `agency.id.toString()` and `location.id.toString()`
3. **V4 still works**: Existing V4 notifications continue using the same NotificationState
4. **V5 now also works**: V5 filter now reads from the same String-based state

### Backward Compatibility

- ✅ Existing user preferences: Work without changes
- ✅ V4 notifications: Continue working as before
- ✅ V5 notifications: Now work correctly with same preferences
- ✅ DataStore schema: No changes needed

---

## Summary of Changes

| Component | Change Type | Lines Changed | Impact |
|-----------|-------------|---------------|---------|
| V5NotificationPayload | Modified | ~30 lines | Int → String for all ID fields |
| V5NotificationFilter | Simplified | -180 lines | Removed complex logic, reuse V4 pattern |
| NotificationWorker | Modified | ~5 lines | Pass NotificationState instead of V5FilterPreferences |
| NotificationState | Reused | 0 lines | No changes needed |
| V5FilterPreferences | Deprecated | 0 lines (mark @Deprecated) | Remove in future |

**Total**: Net -180 lines of code, simpler data model, bug fixed.

# Data Model: V5 Client-Side Notification System

**Date**: 2026-01-26  
**Feature**: V5 Notification Handling

## Entity Definitions

### 1. V5NotificationPayload (NEW)

**Purpose**: Represents the V5 notification payload received from server

**Location**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationPayload.kt`

```kotlin
@Serializable
data class V5NotificationPayload(
    // Display Content
    val notificationType: String,      // tenMinutes, oneHour, twentyFourHour, netstampChanged, inFlight, success, failure, webcastLive
    val title: String,                  // Server-provided title
    val body: String,                   // Server-provided body
    
    // Launch Identification
    val launchUuid: String,             // UUID for deep linking (primary key)
    val launchId: String,               // Library ID (legacy)
    val launchName: String,             // Display name
    
    // Launch Details
    val launchImage: String?,           // Image URL (optional)
    val launchNet: String,              // ISO 8601 datetime
    val launchLocation: String,         // Location display name
    val webcast: Boolean,               // Has webcast
    val webcastLive: Boolean,           // Is webcast currently live
    
    // V5 Filtering IDs (Extended)
    val lspId: Int?,                    // Launch Service Provider ID
    val locationId: Int?,               // Launch location ID
    val programIds: List<Int>,          // Program IDs (parsed from comma-separated string)
    val statusId: Int?,                 // Launch status ID (optional)
    val orbitId: Int?,                  // Target orbit ID (optional)
    val missionTypeId: Int?,            // Mission type ID (optional)
    val launcherFamilyId: Int?          // Launcher family ID (optional)
) {
    companion object {
        /**
         * Parse V5 payload from FCM data map
         */
        fun fromMap(data: Map<String, String>): V5NotificationPayload? {
            return try {
                V5NotificationPayload(
                    notificationType = data["notification_type"] ?: return null,
                    title = data["title"] ?: data["launch_name"] ?: return null,
                    body = data["body"] ?: "",
                    launchUuid = data["launch_uuid"] ?: return null,
                    launchId = data["launch_id"] ?: "",
                    launchName = data["launch_name"] ?: return null,
                    launchImage = data["launch_image"],
                    launchNet = data["launch_net"] ?: return null,
                    launchLocation = data["launch_location"] ?: "",
                    webcast = data["webcast"]?.lowercase() == "true",
                    webcastLive = data["webcast_live"]?.lowercase() == "true",
                    // V5 Extended Fields
                    lspId = data["lsp_id"]?.toIntOrNull(),
                    locationId = data["location_id"]?.toIntOrNull(),
                    programIds = data["program_ids"]?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList(),
                    statusId = data["status_id"]?.toIntOrNull(),
                    orbitId = data["orbit_id"]?.toIntOrNull(),
                    missionTypeId = data["mission_type_id"]?.toIntOrNull(),
                    launcherFamilyId = data["launcher_family_id"]?.toIntOrNull()
                )
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * Detect if payload is V5 format (has lsp_id field)
         */
        fun isV5Payload(data: Map<String, String>): Boolean {
            return data.containsKey("lsp_id")
        }
    }
}
```

### 2. V5FilterPreferences (NEW)

**Purpose**: User preferences for V5 notification filtering

**Location**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5FilterPreferences.kt`

```kotlin
@Serializable
data class V5FilterPreferences(
    // Master enable/disable
    val enableNotifications: Boolean = true,
    
    // Notification type toggles
    val enabledNotificationTypes: Set<String> = setOf(
        "tenMinutes", "oneHour", "twentyFourHour", "netstampChanged",
        "inFlight", "success", "failure", "webcastLive"
    ),
    
    // V5 Filter Categories (null = follow all, empty = block all, non-empty = filter)
    val subscribedLspIds: Set<Int>? = null,           // null = all LSPs
    val subscribedLocationIds: Set<Int>? = null,      // null = all locations
    val subscribedProgramIds: Set<Int>? = null,       // null = all programs
    val subscribedOrbitIds: Set<Int>? = null,         // null = all orbits
    val subscribedMissionTypeIds: Set<Int>? = null,   // null = all mission types
    val subscribedLauncherFamilyIds: Set<Int>? = null,// null = all launcher families
    
    // Filter mode
    val useStrictMatching: Boolean = false,           // AND vs OR logic
    
    // Special filters
    val webcastOnly: Boolean = false,                 // Only notifications with webcast
    
    // Migration tracking
    val hasV5Migration: Boolean = false               // Has migrated from V4
) {
    companion object {
        val DEFAULT = V5FilterPreferences()
    }
}
```

### 3. V5NotificationFilter (NEW)

**Purpose**: Filter logic for V5 notifications

**Location**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationFilter.kt`

```kotlin
object V5NotificationFilter {
    /**
     * Determine if V5 notification should be shown based on user preferences
     */
    fun shouldShow(
        payload: V5NotificationPayload,
        preferences: V5FilterPreferences
    ): FilterResult {
        // 1. Check master enable
        if (!preferences.enableNotifications) {
            return FilterResult.Blocked("Notifications disabled globally")
        }
        
        // 2. Check notification type
        if (payload.notificationType !in preferences.enabledNotificationTypes) {
            return FilterResult.Blocked("Notification type '${payload.notificationType}' disabled")
        }
        
        // 3. Check webcast-only filter
        if (preferences.webcastOnly && !payload.webcast) {
            return FilterResult.Blocked("Webcast-only filter enabled, launch has no webcast")
        }
        
        // 4. Check filter categories
        val filters = mutableListOf<Boolean>()
        
        // LSP filter
        preferences.subscribedLspIds?.let { lspIds ->
            if (lspIds.isEmpty()) return FilterResult.Blocked("No LSPs subscribed")
            payload.lspId?.let { filters.add(it in lspIds) }
        }
        
        // Location filter
        preferences.subscribedLocationIds?.let { locationIds ->
            if (locationIds.isEmpty()) return FilterResult.Blocked("No locations subscribed")
            payload.locationId?.let { filters.add(it in locationIds) }
        }
        
        // Program filter
        preferences.subscribedProgramIds?.let { programIds ->
            if (programIds.isEmpty()) return FilterResult.Blocked("No programs subscribed")
            filters.add(payload.programIds.any { it in programIds })
        }
        
        // Orbit filter
        preferences.subscribedOrbitIds?.let { orbitIds ->
            if (orbitIds.isEmpty()) return FilterResult.Blocked("No orbits subscribed")
            payload.orbitId?.let { filters.add(it in orbitIds) }
        }
        
        // Mission type filter
        preferences.subscribedMissionTypeIds?.let { missionTypeIds ->
            if (missionTypeIds.isEmpty()) return FilterResult.Blocked("No mission types subscribed")
            payload.missionTypeId?.let { filters.add(it in missionTypeIds) }
        }
        
        // Launcher family filter
        preferences.subscribedLauncherFamilyIds?.let { launcherFamilyIds ->
            if (launcherFamilyIds.isEmpty()) return FilterResult.Blocked("No launcher families subscribed")
            payload.launcherFamilyId?.let { filters.add(it in launcherFamilyIds) }
        }
        
        // 5. Apply filter logic (strict = AND, flexible = OR)
        if (filters.isNotEmpty()) {
            val passed = if (preferences.useStrictMatching) {
                filters.all { it }  // AND: all must match
            } else {
                filters.any { it }  // OR: any must match
            }
            
            if (!passed) {
                return FilterResult.Blocked("Filter criteria not met (strict=${ preferences.useStrictMatching})")
            }
        }
        
        return FilterResult.Allowed
    }
}

sealed class FilterResult {
    object Allowed : FilterResult()
    data class Blocked(val reason: String) : FilterResult()
}
```

### 4. NotificationState (UPDATE)

**Changes**: Add V5 filter preferences to existing state

```kotlin
@Serializable
data class NotificationState(
    // ... existing V4 fields ...
    
    // V5 Extended Filter Preferences
    val v5Preferences: V5FilterPreferences = V5FilterPreferences.DEFAULT,
    
    // V5 Migration State
    val hasCompletedV5Migration: Boolean = false
)
```

### 5. NotificationTopicConfig (NEW)

**Purpose**: Centralize V5 topic configuration

```kotlin
object NotificationTopicConfig {
    // V5 Platform Topics
    const val PROD_V5_ANDROID = "prod_v5_android"
    const val DEBUG_V5_ANDROID = "debug_v5_android"
    const val PROD_V5_IOS = "prod_v5_ios"
    const val DEBUG_V5_IOS = "debug_v5_ios"
    
    // V4 Topics (for migration)
    const val PROD_V4 = "k_prod_v4"
    const val DEBUG_V4 = "k_debug_v4"
    
    // Notification Type Topics
    val NOTIFICATION_TYPE_TOPICS = setOf(
        "tenMinutes",
        "oneHour",
        "twentyFourHour",
        "netstampChanged",
        "inFlight",
        "success",
        "failure",
        "webcastLive"
    )
    
    fun getV5Topic(platform: Platform, isDebug: Boolean): String {
        return when {
            platform == Platform.ANDROID && isDebug -> DEBUG_V5_ANDROID
            platform == Platform.ANDROID && !isDebug -> PROD_V5_ANDROID
            platform == Platform.IOS && isDebug -> DEBUG_V5_IOS
            platform == Platform.IOS && !isDebug -> PROD_V5_IOS
            else -> PROD_V5_ANDROID // Desktop fallback (no-op anyway)
        }
    }
}
```

## State Transitions

### Notification Processing Flow

```
┌─────────────────────┐
│ FCM Message Received│
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Detect V5 Payload   │ (check for lsp_id field)
│ (isV5Payload)       │
└──────────┬──────────┘
           │
    ┌──────┴──────┐
    │             │
    ▼             ▼
┌────────┐   ┌────────┐
│  V5    │   │  V4    │ (backward compatibility)
│ Parser │   │ Parser │
└───┬────┘   └───┬────┘
    │            │
    └──────┬─────┘
           ▼
┌─────────────────────┐
│ Load Preferences    │
│ (NotificationState) │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Apply Filter        │
│ (V5NotificationFilter)│
└──────────┬──────────┘
           │
    ┌──────┴──────┐
    │             │
    ▼             ▼
┌────────┐   ┌────────┐
│ ALLOWED│   │BLOCKED │
└───┬────┘   └───┬────┘
    │            │
    ▼            ▼
┌────────┐   ┌────────┐
│ Display│   │ Log &  │
│ Notif  │   │ Skip   │
└────────┘   └────────┘
```

### V4 → V5 Migration Flow

```
┌─────────────────────┐
│ App Launch          │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ Check Migration Flag│
│ hasCompletedV5Migration
└──────────┬──────────┘
           │
    ┌──────┴──────┐
    │             │
    ▼             ▼
┌────────┐   ┌────────────────┐
│ TRUE   │   │ FALSE          │
│ (skip) │   │ (run migration)│
└────────┘   └───────┬────────┘
                     │
                     ▼
             ┌───────────────────┐
             │ Subscribe V5 Topic│
             └───────┬───────────┘
                     │
                     ▼
             ┌───────────────────┐
             │ Unsubscribe V4    │
             └───────┬───────────┘
                     │
                     ▼
             ┌───────────────────┐
             │ Convert Preferences│
             │ (agency_id → lsp_id)
             └───────┬───────────┘
                     │
                     ▼
             ┌───────────────────┐
             │ Set Migration Flag│
             │ = true            │
             └───────────────────┘
```

## Validation Rules

### V5NotificationPayload Validation

| Field | Validation | Error |
|-------|------------|-------|
| `notificationType` | Required, non-empty | `Missing notification_type` |
| `title` | Required, non-empty | `Missing title` |
| `launchUuid` | Required, valid UUID format | `Invalid launch_uuid` |
| `launchName` | Required, non-empty | `Missing launch_name` |
| `launchNet` | Required, ISO 8601 format | `Invalid launch_net datetime` |
| `lspId` | Optional, positive integer | Silent fallback to null |
| `programIds` | Optional, comma-separated integers | Silent fallback to empty list |

### V5FilterPreferences Validation

| Field | Validation | Default |
|-------|------------|---------|
| `enabledNotificationTypes` | At least one type enabled if notifications enabled | All types |
| `subscribedLspIds` | null (all) or non-empty set | null (all) |
| `subscribedLocationIds` | null (all) or non-empty set | null (all) |

## Relationships

```
V5NotificationPayload
    │
    ├── lspId ─────────────────┬── V5FilterPreferences.subscribedLspIds
    ├── locationId ────────────┼── V5FilterPreferences.subscribedLocationIds
    ├── programIds[] ──────────┼── V5FilterPreferences.subscribedProgramIds
    ├── orbitId ───────────────┼── V5FilterPreferences.subscribedOrbitIds
    ├── missionTypeId ─────────┼── V5FilterPreferences.subscribedMissionTypeIds
    └── launcherFamilyId ──────┴── V5FilterPreferences.subscribedLauncherFamilyIds

NotificationState
    │
    ├── v5Preferences: V5FilterPreferences
    └── hasCompletedV5Migration: Boolean
```

# Data Model: Wear OS Premium Experience

**Date**: 2026-04-15 | **Status**: Complete

## Entity Diagram

```
┌─────────────────────┐       ┌──────────────────────┐
│  WearEntitlementState│       │   WatchLaunchCache    │
├─────────────────────┤       ├──────────────────────┤
│ hasWearOs: Boolean   │       │ launches: List<...>   │
│ lastSyncTimestamp    │       │ lastUpdated: Instant  │
│ source: SyncSource   │       │ dataSource: DataSource│
│ expiresAt: Instant?  │       └──────────────────────┘
└─────────────────────┘                │
        │                              │
        │ gates access to              │ provides data to
        ▼                              ▼
┌─────────────────────┐       ┌──────────────────────┐
│  Complication        │       │  Tile / App UI       │
│  Tile                │       │                      │
│  Companion App       │       │                      │
└─────────────────────┘       └──────────────────────┘

┌──────────────────────────────┐
│     DataLayerSyncPayload     │
├──────────────────────────────┤
│ launches: List<SyncLaunch>   │  Phone ──DataLayer──> Watch
│ entitlementActive: Boolean   │
│ syncTimestamp: Instant       │
│ phoneAppVersion: String      │
└──────────────────────────────┘
```

## Entities

### WearEntitlementState

**Purpose**: Cached premium entitlement state on the watch, persisted via DataStore.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| hasWearOs | Boolean | No | Whether WEAR_OS entitlement is active |
| lastSyncTimestamp | Instant | No | When the entitlement was last synced from phone |
| source | SyncSource | No | How the current state was obtained |
| expiresAt | Instant | Yes | Subscription expiry (for grace period logic) |

**Enum: SyncSource**
| Value | Description |
|-------|-------------|
| PHONE_SYNC | Received via DataLayer from phone |
| LOCAL_CACHE | Read from local DataStore (no recent sync) |
| DEFAULT | Never synced — defaults to free tier |

**Validation Rules**:
- `hasWearOs` defaults to `false` until first successful phone sync
- `lastSyncTimestamp` must be set whenever state changes
- If `expiresAt` is in the past and `source` is `LOCAL_CACHE`, treat as expired (grace period: 24 hours)

**State Transitions**:
```
DEFAULT ──phone sync──> PHONE_SYNC (hasWearOs=true/false)
PHONE_SYNC ──no sync for 24h──> LOCAL_CACHE (keeps last known hasWearOs)
LOCAL_CACHE ──phone sync──> PHONE_SYNC (updated)
Any state ──clear data──> DEFAULT
```

**Serialization**: Proto DataStore (`wear_entitlement.pb`)

---

### WatchLaunchCache

**Purpose**: Local cache of upcoming launches on the watch, persisted via DataStore.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| launches | List\<CachedLaunch\> | No | Ordered list of upcoming launches (max 20) |
| lastUpdated | Instant | No | When the cache was last refreshed |
| dataSource | DataSource | No | How the current data was obtained |

**Enum: DataSource**
| Value | Description |
|-------|-------------|
| DIRECT_API | Fetched directly from LL API by the watch |
| PHONE_SYNC | Received via DataLayer from phone |
| STALE_CACHE | Local cache, not recently refreshed (>1 hour old) |

**Validation Rules**:
- `launches` list capped at 20 entries (oldest removed first)
- Only launches with `net` (No Earlier Than) in the future are stored
- `lastUpdated` must be set on every cache write

**Serialization**: Proto DataStore (`watch_launch_cache.pb`)

---

### CachedLaunch

**Purpose**: Minimal launch data stored on the watch for display across surfaces.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| id | String | No | Launch UUID (for deep linking to phone) |
| name | String | No | Full launch name |
| net | Instant | No | No Earlier Than datetime |
| statusAbbrev | String | Yes | Status abbreviation (Go, TBD, TBC, etc.) |
| statusName | String | Yes | Full status name |
| lspName | String | Yes | Launch Service Provider name |
| lspAbbrev | String | Yes | LSP abbreviation (for title formatting) |
| rocketConfigName | String | Yes | Rocket configuration name |
| missionName | String | Yes | Mission name |
| missionDescription | String | Yes | Mission description (for detail view) |
| padLocationName | String | Yes | Launch pad location name |
| imageUrl | String | Yes | Launch image URL (for tile/app) |

**Validation Rules**:
- `id` must be a valid UUID string
- `net` must be a future datetime (at time of caching)
- `name` required — use as fallback if `lspName`/`rocketConfigName` are null

**Relationship to LaunchFormatUtil**: Title display uses `LaunchFormatUtil.formatLaunchTitle()` with manual parameters: `lspName`, `lspAbbrev`, `rocketConfigName`, and `name` as fallback.

---

### DataLayerSyncPayload

**Purpose**: Data structure sent from phone to watch via Wearable DataLayer API.

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| launches | List\<SyncLaunch\> | No | Up to 20 upcoming launches |
| entitlementActive | Boolean | No | Whether WEAR_OS entitlement is active |
| syncTimestamp | Instant | No | When the phone created this payload |
| phoneAppVersion | String | No | Phone app version (for compatibility checks) |

**Serialization**: JSON via kotlinx-serialization, stored as byte array in DataLayer `DataItem` at path `/spacelaunchnow/sync`.

**Constraints**:
- DataLayer items have a ~100KB limit per DataItem — 20 launches well within this
- Phone writes on: app foreground, subscription change, WorkManager periodic (30 min)

---

### SyncLaunch

**Purpose**: Lightweight launch representation for DataLayer transfer (maps to CachedLaunch on receive).

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| id | String | No | Launch UUID |
| name | String | No | Full launch name |
| net | String | No | ISO-8601 datetime string |
| statusAbbrev | String | Yes | Status abbreviation |
| statusName | String | Yes | Full status name |
| lspName | String | Yes | LSP name |
| lspAbbrev | String | Yes | LSP abbreviation |
| rocketConfigName | String | Yes | Rocket config name |
| missionName | String | Yes | Mission name |
| missionDescription | String | Yes | Mission description |
| padLocationName | String | Yes | Pad location name |
| imageUrl | String | Yes | Image URL |

**Note**: Uses String for `net` instead of Instant for JSON serialization simplicity across DataLayer. Converted to Instant on the watch side.

---

### PremiumFeature Enum Modification

**Existing entity in**: `composeApp/src/commonMain/kotlin/.../data/model/SubscriptionState.kt`

**Change**: Add `WEAR_OS` to the `PremiumFeature` enum.

```kotlin
enum class PremiumFeature {
    AD_FREE,
    CUSTOM_THEMES,
    CAL_SYNC,
    ADVANCED_WIDGETS,
    WIDGETS_CUSTOMIZATION,
    NOTIFICATION_CUSTOMIZATION,
    WEAR_OS  // NEW: Grants access to all Wear OS surfaces
}
```

**Entitlement Mapping**: `WEAR_OS` is granted by `PREMIUM` and `LIFETIME` tiers only. Not granted by `LEGACY` entitlements.

## Entity Relationships

```
PremiumFeature.WEAR_OS ──granted by──> PREMIUM / LIFETIME tiers (RevenueCat)
                       ──synced via──> DataLayerSyncPayload.entitlementActive
                       ──cached as──> WearEntitlementState.hasWearOs

DataLayerSyncPayload ──contains──> List<SyncLaunch>
                     ──maps to──> WatchLaunchCache.launches (as CachedLaunch)

WearEntitlementState ──gates──> NextLaunchComplicationService
                     ──gates──> NextLaunchTileService
                     ──gates──> WearApp (companion app)

WatchLaunchCache ──provides data to──> NextLaunchComplicationService (first launch)
                 ──provides data to──> NextLaunchTileService (first launch)
                 ──provides data to──> LaunchListScreen (all launches)
                 ──provides data to──> LaunchDetailScreen (single launch by id)
```

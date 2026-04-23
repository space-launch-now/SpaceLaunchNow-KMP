# Data Model: Domain Model Layer

**Feature**: Introduce domain models to decouple UI/ViewModel from API response types  
**Date**: 2026-04-19 | **Status**: Complete

---

## Package Structure

```
composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/
├── model/
│   ├── Launch.kt              # Launch + all launch value types
│   ├── Event.kt               # Event + event value types
│   ├── Common.kt              # Shared value types (Provider, Pad, etc.)
│   ├── PaginatedResult.kt     # Generic paginated wrapper
│   ├── LaunchFilterParams.kt  # Launch filter parameters
│   └── EventFilterParams.kt   # Event filter parameters
└── mapper/
    ├── LaunchMappers.kt       # LaunchBasic/Normal/Detailed → Launch
    ├── EventMappers.kt        # EventEndpointNormal/Detailed → Event
    └── CommonMappers.kt       # Shared nested type mappers
```

---

## Entity Relationship Diagram

```
┌──────────────────┐
│  PaginatedResult │  Generic wrapper for all paginated API responses
│  <T>             │
├──────────────────┤
│ count: Int       │
│ next: String?    │
│ previous: String?│
│ results: List<T> │
└──────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                         Launch                               │
├─────────────────────────────────────────────────────────────┤
│ id: String                    (from all tiers)               │
│ name: String                  (from all tiers)               │
│ slug: String                  (from all tiers)               │
│ net: Instant?                 (from all tiers)               │
│ windowStart: Instant?         (from all tiers)               │
│ windowEnd: Instant?           (from all tiers)               │
│ lastUpdated: Instant?         (from all tiers)               │
│ status: LaunchStatus?         (from all tiers)               │
│ provider: Provider            (from all tiers)               │
│ imageUrl: String?             (from all tiers - flattened)   │
│ thumbnailUrl: String?         (from all tiers - flattened)   │
│ infographic: String?          (from all tiers)               │
│ netPrecision: NetPrecision?   (from all tiers)               │
│──────────────── Normal+ fields ─────────────────────────────│
│ rocket: RocketConfig?         (from Normal+)                 │
│ mission: Mission?             (from Normal+)                 │
│ pad: Pad?                     (from Normal+)                 │
│ programs: List<ProgramSummary>(from Normal+)                 │
│ probability: Int?             (from Normal+)                 │
│ weatherConcerns: String?      (from Normal+)                 │
│ failreason: String?           (from Normal+)                 │
│ hashtag: String?              (from Normal+)                 │
│ webcastLive: Boolean          (from Normal+, default false)  │
│ launchAttemptCounts: LaunchAttemptCounts? (from Normal+)     │
│──────────────── Detailed-only fields ───────────────────────│
│ updates: List<Update>         (from Detailed, default empty) │
│ infoUrls: List<InfoLink>      (from Detailed, default empty) │
│ vidUrls: List<VideoLink>      (from Detailed, default empty) │
│ timeline: List<TimelineEntry> (from Detailed, default empty) │
│ missionPatches: List<MissionPatchSummary> (default empty)    │
│ rocketDetail: RocketDetail?   (from Detailed)                │
│ flightclubUrl: String?        (from Detailed)                │
│ padTurnaround: String?        (from Detailed)                │
│ providerDetail: ProviderDetail?(from Detailed)               │
└─────────────────────────────────────────────────────────────┘
         │ has               │ has               │ has
         ▼                   ▼                   ▼
  ┌──────────┐     ┌──────────────┐     ┌──────────┐
  │ Provider │     │ RocketConfig │     │   Pad    │
  └──────────┘     └──────────────┘     └──────────┘
         │                                    │
         ▼                                    ▼
  ┌──────────────┐                    ┌───────────┐
  │ProviderDetail│                    │ Location  │
  └──────────────┘                    └───────────┘

┌─────────────────────────────────────────────────────────────┐
│                          Event                               │
├─────────────────────────────────────────────────────────────┤
│ id: Int                                                      │
│ name: String                                                 │
│ slug: String                                                 │
│ type: EventType                                              │
│ description: String?                                         │
│ date: Instant?                                               │
│ location: String?                                            │
│ imageUrl: String?                                            │
│ webcastLive: Boolean                                         │
│ lastUpdated: Instant?                                        │
│ duration: String?                                            │
│ datePrecision: NetPrecision?                                 │
│ infoUrls: List<InfoLink>                                     │
│ vidUrls: List<VideoLink>                                     │
│ updates: List<Update>                                        │
│──────────────── Detailed-only fields ───────────────────────│
│ agencies: List<Provider>       (from Detailed, default empty)│
│ launches: List<Launch>         (from Detailed, default empty)│
│ expeditions: List<ExpeditionSummary> (default empty)         │
│ spaceStations: List<SpaceStationSummary> (default empty)     │
│ programs: List<ProgramSummary> (from Detailed, default empty)│
│ astronauts: List<AstronautSummary> (default empty)           │
└─────────────────────────────────────────────────────────────┘
```

---

## Domain Types: `Common.kt`

### Provider (maps from AgencyMini/AgencyNormal)
```kotlin
data class Provider(
    val id: Int,
    val name: String,
    val abbrev: String?,
    val type: String?,
    val countryCode: String?,
    val logoUrl: String?,
    val imageUrl: String?
)
```

### ProviderDetail (maps from AgencyDetailed/AgencyEndpointDetailed — detail-screen only)
```kotlin
data class ProviderDetail(
    val description: String?,
    val administrator: String?,
    val foundingYear: Int?,
    val totalLaunchCount: Int?,
    val successfulLaunches: Int?,
    val failedLaunches: Int?,
    val pendingLaunches: Int?,
    val consecutiveSuccessfulLaunches: Int?,
    val successfulLandings: Int?,
    val failedLandings: Int?,
    val attemptedLandings: Int?,
    val consecutiveSuccessfulLandings: Int?,
    val infoUrl: String?,
    val wikiUrl: String?
)
```

### RocketConfig (maps from LauncherConfigList/LauncherConfigNormal)
```kotlin
data class RocketConfig(
    val id: Int,
    val name: String,
    val fullName: String?,
    val family: String?,
    val variant: String?,
    val imageUrl: String?,
    val active: Boolean?,
    val reusable: Boolean?
)
```

### RocketDetail (maps from RocketDetailed — includes stages and spacecraft)
```kotlin
data class RocketDetail(
    val stages: List<RocketStage>,
    val spacecraftFlights: List<SpacecraftFlightSummary>,
    val payloads: List<PayloadSummary>
)

data class RocketStage(
    val id: Int,
    val type: String?,
    val reused: Boolean?,
    val launcherFlightNumber: Int?,
    val launcher: LauncherSummary?,
    val landingAttempt: LandingAttemptSummary?
)

data class LauncherSummary(
    val id: Int,
    val serialNumber: String?,
    val flightProven: Boolean,
    val imageUrl: String?
)

data class LandingAttemptSummary(
    val id: Int,
    val outcome: String?,
    val description: String?,
    val location: String?,
    val type: String?
)

data class SpacecraftFlightSummary(
    val id: Int,
    val serialNumber: String?,
    val spacecraftName: String?,
    val destination: String?,
    val missionEnd: Instant?
)

data class PayloadSummary(
    val id: Int,
    val name: String?,
    val description: String?
)
```

### Pad & Location
```kotlin
data class Pad(
    val id: Int,
    val name: String?,
    val latitude: Double?,
    val longitude: Double?,
    val mapUrl: String?,
    val mapImage: String?,
    val totalLaunchCount: Int?,
    val location: Location?
)

data class Location(
    val id: Int,
    val name: String?,
    val countryCode: String?
)
```

### Mission & Orbit
```kotlin
data class Mission(
    val id: Int,
    val name: String?,
    val description: String?,
    val type: String?,
    val orbit: Orbit?,
    val imageUrl: String?
)

data class Orbit(
    val id: Int,
    val name: String,
    val abbrev: String
)
```

### LaunchStatus & NetPrecision
```kotlin
data class LaunchStatus(
    val id: Int,
    val name: String,
    val abbrev: String?,
    val description: String?
)

data class NetPrecision(
    val id: Int,
    val name: String?,
    val abbrev: String?,
    val description: String?
)
```

### LaunchAttemptCounts
```kotlin
data class LaunchAttemptCounts(
    val orbital: Int?,
    val location: Int?,
    val pad: Int?,
    val agency: Int?,
    val orbitalYear: Int?,
    val locationYear: Int?,
    val padYear: Int?,
    val agencyYear: Int?
)
```

### ProgramSummary
```kotlin
data class ProgramSummary(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val description: String?,
    val infoUrl: String?,
    val wikiUrl: String?,
    val type: String?
)
```

### Media Types
```kotlin
data class VideoLink(
    val url: String,
    val title: String?,
    val source: String?,
    val description: String?,
    val featureImage: String?,
    val isLive: Boolean,
    val priority: Int?
)

data class InfoLink(
    val url: String,
    val title: String?,
    val source: String?,
    val description: String?,
    val featureImage: String?,
    val type: String?,
    val priority: Int?
)

data class MissionPatchSummary(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val priority: Int?
)

data class TimelineEntry(
    val type: String?,
    val relativeTime: String?
)
```

### Update
```kotlin
data class Update(
    val id: Int,
    val profileImage: String?,
    val comment: String?,
    val infoUrl: String?,
    val createdBy: String?,
    val createdOn: Instant?
)
```

---

## Domain Types: `Event.kt`

### EventType
```kotlin
data class EventType(
    val id: Int,
    val name: String
)
```

### ExpeditionSummary, SpaceStationSummary, AstronautSummary (detail-only)
```kotlin
data class ExpeditionSummary(
    val id: Int,
    val name: String?,
    val start: Instant?,
    val end: Instant?
)

data class SpaceStationSummary(
    val id: Int,
    val name: String,
    val imageUrl: String?
)

data class AstronautSummary(
    val id: Int,
    val name: String,
    val nationality: String?,
    val profileImageUrl: String?,
    val status: String?
)
```

---

## Filter Parameters

### LaunchFilterParams (`LaunchFilterParams.kt`)
```kotlin
data class LaunchFilterParams(
    val statusIds: List<Int> = emptyList(),
    val providerIds: List<Int> = emptyList(),
    val locationIds: List<Int> = emptyList(),
    val rocketConfigIds: List<Int> = emptyList(),
    val programIds: List<Int> = emptyList(),
    val orbitIds: List<Int> = emptyList(),
    val includeSuborbital: Boolean? = null,
    val search: String? = null,
    val limit: Int = 20,
    val offset: Int = 0,
    val ordering: String? = null,
    val upcoming: Boolean? = null
)
```

### EventFilterParams (`EventFilterParams.kt`)
```kotlin
data class EventFilterParams(
    val typeIds: List<Int> = emptyList(),
    val programIds: List<Int> = emptyList(),
    val agencyIds: List<Int> = emptyList(),
    val search: String? = null,
    val limit: Int = 20,
    val offset: Int = 0,
    val upcoming: Boolean? = null
)
```

---

## Mapping Rules

### LaunchBasic → Launch
| API Field | Domain Field | Notes |
|-----------|-------------|-------|
| `id` | `id` | Direct |
| `name` | `name` | Fallback to "" |
| `slug` | `slug` | Direct |
| `net` | `net` | Direct |
| `windowStart` | `windowStart` | Direct |
| `windowEnd` | `windowEnd` | Direct |
| `lastUpdated` | `lastUpdated` | Direct |
| `status` | `status` | Map via `LaunchStatus.toDomain()` |
| `launchServiceProvider` | `provider` | Map `AgencyMini` → `Provider` |
| `image?.imageUrl` | `imageUrl` | Flatten |
| `image?.thumbnailUrl` | `thumbnailUrl` | Flatten |
| `infographic` | `infographic` | Direct |
| `netPrecision` | `netPrecision` | Map via `NetPrecision.toDomain()` |
| `locationName` | Used to populate `pad.location.name` | Create minimal Pad+Location |
| — | `rocket` | **null** (not in Basic) |
| — | `mission` | **null** (not in Basic) |
| — | `programs` | **empty** (not in Basic) |
| — | All detail fields | **null/empty** |

### LaunchNormal → Launch
Same as Basic plus:
| API Field | Domain Field |
|-----------|-------------|
| `rocket.configuration` | `rocket` via `RocketConfig.toDomain()` |
| `mission` | `mission` via `Mission.toDomain()` |
| `pad` | `pad` via `Pad.toDomain()` |
| `program` | `programs` via `ProgramMini.toDomain()` |
| `probability` | `probability` |
| `weatherConcerns` | `weatherConcerns` |
| `failreason` | `failreason` |
| `hashtag` | `hashtag` |
| `webcastLive` | `webcastLive` |
| Count fields | `launchAttemptCounts` |

### LaunchDetailed → Launch
Same as Normal plus:
| API Field | Domain Field |
|-----------|-------------|
| `updates` | `updates` via `Update.toDomain()` |
| `infoUrls` | `infoUrls` via `InfoLink.toDomain()` |
| `vidUrls` | `vidUrls` via `VideoLink.toDomain()` |
| `timeline` | `timeline` via `TimelineEntry.toDomain()` |
| `missionPatches` | `missionPatches` via `MissionPatchSummary.toDomain()` |
| `rocket` (RocketDetailed) | `rocketDetail` via `RocketDetail.toDomain()` |
| `flightclubUrl` | `flightclubUrl` |
| `padTurnaround` | `padTurnaround` |
| `launchServiceProvider` (AgencyDetailed) | `providerDetail` |

### EventEndpointNormal → Event
| API Field | Domain Field |
|-----------|-------------|
| `id` | `id` |
| `name` | `name` |
| `slug` | `slug` |
| `type` | `type` via `EventType.toDomain()` |
| `description` | `description` |
| `date` | `date` |
| `location` | `location` |
| `image?.imageUrl` | `imageUrl` |
| `webcastLive` | `webcastLive` |
| `lastUpdated` | `lastUpdated` |
| `duration` | `duration` |
| `datePrecision` | `datePrecision` |
| `infoUrls` | `infoUrls` |
| `vidUrls` | `vidUrls` |
| `updates` | `updates` |

### EventEndpointDetailed → Event
Same as Normal plus:
| API Field | Domain Field |
|-----------|-------------|
| `agencies` | `agencies` via `Provider.toDomain()` |
| `launches` | `launches` via `LaunchBasic.toDomain()` |
| `expeditions` | `expeditions` via `ExpeditionSummary.toDomain()` |
| `spacestations` | `spaceStations` via `SpaceStationSummary.toDomain()` |
| `program` | `programs` via `ProgramSummary.toDomain()` |
| `astronauts` | `astronauts` via `AstronautSummary.toDomain()` |

---

## Validation Rules

1. `Launch.id` must never be blank — mapper should fail loudly if API returns empty id
2. `Launch.name` defaults to `""` if API returns null (prevents null checks everywhere)
3. `Launch.provider` is non-null — every launch has a provider. Mapper uses `Provider(id = 0, name = "Unknown", ...)` as fallback
4. All list fields default to `emptyList()` — never null
5. `Event.name` is non-null from the API (required field)

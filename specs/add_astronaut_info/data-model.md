# Data Model: Astronaut Feature

**Date**: 2026-01-29  
**Feature**: Astronaut List and Detail Views  
**API Version**: Launch Library 2.4.0

## Overview

This document defines the data model for astronaut-related features in SpaceLaunchNow KMP. All models are generated from OpenAPI spec and located in `me.calebjones.spacelaunchnow.api.launchlibrary.models` package.

---

## Core Entities

### 1. AstronautEndpointNormal (List View)

**Purpose**: Represents astronaut data in list views and normal-detail API responses.

**Fields**:
```kotlin
data class AstronautEndpointNormal(
    val id: Int,                           // Unique astronaut ID
    val url: String,                       // API endpoint URL
    val responseMode: String,              // "normal"
    val name: String?,                     // Full name (nullable)
    val status: AstronautStatus?,          // Current status (active/retired/etc)
    val agency: AgencyMini?,               // Primary agency affiliation
    val image: Image?,                     // Profile photo
    val age: Int?,                         // Current age (nullable)
    val bio: String,                       // Biography text
    val type: AstronautType,               // Astronaut/Cosmonaut/Taikonaut/etc
    val nationality: List<Country>         // List of nationalities
)
```

**Relationships**:
- **→ AstronautStatus** (1:1): Status reference
- **→ AgencyMini** (1:1): Primary agency
- **→ Image** (0:1): Profile photo
- **→ AstronautType** (1:1): Type classification
- **→ Country** (1:n): Nationalities

**Validation Rules**:
- `id` must be positive integer
- `bio` may be empty string but never null
- `nationality` list may be empty but never null

**Usage**:
- Astronaut list screen items
- Search results
- Related astronauts in other views

---

### 2. AstronautEndpointDetailed (Detail View)

**Purpose**: Comprehensive astronaut data for detail view with career statistics and flight history.

**Fields** (extends AstronautEndpointNormal):
```kotlin
data class AstronautEndpointDetailed(
    // All fields from AstronautEndpointNormal, plus:
    
    val inSpace: Boolean?,                 // Currently in space?
    val timeInSpace: String?,              // Total time in space (ISO duration)
    val evaTime: String?,                  // EVA time (ISO duration)
    val dateOfBirth: LocalDate?,           // Birth date
    val dateOfDeath: LocalDate?,           // Death date (if deceased)
    val wiki: String?,                     // Wikipedia URL
    
    // Career statistics
    val lastFlight: Instant?,              // Most recent flight date
    val firstFlight: Instant?,             // First flight date
    val flightsCount: Int?,                // Total flights
    val landingsCount: Int?,               // Total landings
    val spacewalksCount: Int?,             // Total EVAs/spacewalks
    val lastWalk: Instant?,                // Most recent EVA
    val lastLanding: Instant?,             // Most recent landing
    
    // Flight history
    val flights: List<LaunchBasic>         // List of launches/missions
)
```

**Relationships**:
- Inherits all from `AstronautEndpointNormal`
- **→ LaunchBasic** (1:n): Flight history

**Validation Rules**:
- `timeInSpace` and `evaTime` are ISO 8601 duration strings (e.g., "P8DT14H12M")
- `dateOfDeath` must be after `dateOfBirth` if both present
- `flightsCount` must match `flights.size` (server-side validation)

**Usage**:
- Astronaut detail screen
- Deep profile views

---

### 3. AstronautNormal (Embedded in Other Entities)

**Purpose**: Astronaut reference in spacecraft flights, events, and expeditions.

**Fields**:
```kotlin
data class AstronautNormal(
    val id: Int,
    val url: String,
    val responseMode: String,              // "normal"
    val name: String?,
    val status: AstronautStatus?,
    val agency: AgencyMini?,
    val image: Image?,
    val age: Int?,
    val bio: String,
    val type: AstronautType,
    val nationality: List<Country>
)
```

**Note**: Identical structure to `AstronautEndpointNormal` but different class for API serialization context.

**Usage**:
- Embedded in `AstronautFlight`
- Crew lists in spacecraft flights
- Event participants

---

### 4. AstronautFlight (Association Entity)

**Purpose**: Links astronauts to specific flights with role information.

**Fields**:
```kotlin
data class AstronautFlight(
    val id: Int,                           // Unique flight participation ID
    val role: AstronautRole?,              // Role on this flight
    val astronaut: AstronautNormal         // Astronaut details
)
```

**Relationships**:
- **→ AstronautRole** (0:1): Role on flight
- **→ AstronautNormal** (1:1): Astronaut reference

**Usage**:
- Spacecraft crew lists (`launchCrew`, `onboardCrew`, `landingCrew`)
- Spacewalk crew
- Expedition crew
- Launch detail crew sections

---

## Supporting Entities

### 5. AstronautStatus

**Purpose**: Astronaut current status classification.

**Fields**:
```kotlin
data class AstronautStatus(
    val id: Int,                           // Status ID
    val name: String                       // Status name
)
```

**Common Values**:
- `1` → "Active"
- `2` → "Retired"
- `3` → "Deceased"
- `11` → "Management"

**Usage**:
- Status badges in astronaut cards
- Filtering astronaut lists

---

### 6. AstronautType

**Purpose**: Astronaut type/classification.

**Fields**:
```kotlin
data class AstronautType(
    val id: Int,                           // Type ID
    val name: String                       // Type name
)
```

**Common Values**:
- `1` → "Astronaut" (NASA, ESA, etc.)
- `2` → "Cosmonaut" (Roscosmos)
- `3` → "Taikonaut" (CNSA)
- `4` → "Spationaut" (CNES)
- `5` → "Test Pilot"

**Usage**:
- Profile metadata
- Filtering/categorization

---

### 7. AstronautRole

**Purpose**: Role of astronaut on a specific flight.

**Fields**:
```kotlin
data class AstronautRole(
    val id: Int,                           // Role ID
    val role: String,                      // Role name
    val priority: Int?                     // Display priority
)
```

**Common Values**:
- "Commander"
- "Pilot"
- "Mission Specialist"
- "Flight Engineer"
- "Payload Specialist"

**Usage**:
- Crew role display
- Flight participation details

---

### 8. AgencyMini (Reference)

**Purpose**: Minimal agency data for astronaut affiliation.

**Fields**:
```kotlin
data class AgencyMini(
    val id: Int,
    val name: String?,
    val abbrev: String?,
    val type: AgencyType?,
    val logo: Image?,
    val socialLogo: Image?
)
```

**Usage**:
- Astronaut agency affiliation
- Agency badges in astronaut cards

---

### 9. Image (Reference)

**Purpose**: Image metadata for astronaut photos and agency logos.

**Fields**:
```kotlin
data class Image(
    val id: Int,
    val name: String?,
    val imageUrl: String?,                 // Full image URL
    val thumbnailUrl: String?,             // Thumbnail URL
    val creatorUrl: String?                // Attribution URL
)
```

**Usage**:
- Astronaut profile photos
- Agency logos
- Placeholder for missing images

---

### 10. Country (Reference)

**Purpose**: Country data for astronaut nationalities.

**Fields**:
```kotlin
data class Country(
    val id: Int,
    val name: String,
    val alpha2Code: String,                // ISO 3166-1 alpha-2 (e.g., "US")
    val alpha3Code: String,                // ISO 3166-1 alpha-3 (e.g., "USA")
    val code: Int,                         // Numeric country code
    val flag: Image?                       // Flag image
)
```

**Usage**:
- Astronaut nationality display
- Flag icons
- Country-based filtering

---

## API Response Wrappers

### PaginatedAstronautEndpointNormalList

**Purpose**: Paginated list response for astronaut queries.

**Fields**:
```kotlin
data class PaginatedAstronautEndpointNormalList(
    val count: Int,                        // Total result count
    val next: String?,                     // Next page URL (nullable if last page)
    val previous: String?,                 // Previous page URL (nullable if first page)
    val results: List<AstronautEndpointNormal>  // Current page results
)
```

**Pagination Logic**:
- Default page size: 20
- `next` is null when on last page
- `previous` is null when on first page
- `count` represents total matches, not results.size

---

### PaginatedAstronautEndpointDetailedList

**Purpose**: Paginated detailed list (rarely used, detail endpoint preferred for single astronaut).

**Fields**: Same structure as `PaginatedAstronautEndpointNormalList` but with `AstronautEndpointDetailed`.

---

## Data Flow Diagram

```
┌─────────────────────────────────────────┐
│       Launch Library API 2.4.0          │
│   /astronauts/ (AstronautsApi)          │
└──────────────┬──────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────┐
│   Extension Functions (Clean Interface)  │
│   • getAstronautList()                   │
│   • getAstronautDetail(id)               │
└──────────────┬───────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────┐
│   AstronautRepository                    │
│   • Result<PaginatedList> wrapper        │
│   • Error handling                       │
└──────────────┬───────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────┐
│   AstronautViewModel (StateFlow)         │
│   • UiState data classes                 │
│   • Pagination logic                     │
└──────────────┬───────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────┐
│   UI Components (Compose)                │
│   • AstronautCard                        │
│   • AstronautDetailView                  │
│   • AstronautProfileCard                 │
└──────────────────────────────────────────┘
```

---

## State Transitions

### Astronaut List Loading States

```
┌─────────────┐
│   Initial   │ (isLoading = false, astronauts = empty)
└──────┬──────┘
       │ loadAstronauts()
       ▼
┌─────────────┐
│   Loading   │ (isLoading = true, astronauts = empty)
└──────┬──────┘
       │ API Success
       ▼
┌─────────────┐
│   Loaded    │ (isLoading = false, astronauts = [...])
└──────┬──────┘
       │ loadMore()
       ▼
┌─────────────┐
│ Loading More│ (isLoadingMore = true, astronauts = [...])
└──────┬──────┘
       │ API Success
       ▼
┌─────────────┐
│   Loaded    │ (isLoadingMore = false, astronauts = [..., ...new])
└─────────────┘

Error State:
┌─────────────┐
│   Error     │ (isLoading = false, error = "message")
└──────┬──────┘
       │ retry()
       ▼
     (back to Loading)
```

### Astronaut Detail Loading States

```
┌─────────────┐
│   Initial   │ (isLoading = true, astronaut = null)
└──────┬──────┘
       │ loadAstronautDetail()
       ▼
┌─────────────┐
│   Loading   │ (isLoading = true, astronaut = null)
└──────┬──────┘
       │ API Success
       ▼
┌─────────────┐
│   Loaded    │ (isLoading = false, astronaut = {...})
└─────────────┘
```

---

## Filtering and Search

### Supported Filters

**Status Filter**:
```kotlin
statusIds: List<Int>? = null
// Example: [1, 2] → Active and Retired
```

**Agency Filter**:
```kotlin
agencyIds: List<Int>? = null
// Example: [44] → NASA astronauts only
```

**Search**:
```kotlin
search: String? = null
// Searches: name, bio (server-side)
```

**Ordering**:
```kotlin
ordering: String? = null
// Options: "name", "-name", "first_flight", "-first_flight", "flights_count"
```

---

## Data Validation Rules

### Client-Side Validation

1. **Image URLs**: Must be valid HTTPS URLs or null
2. **Dates**: `dateOfDeath` > `dateOfBirth` if both present
3. **Counts**: Must be non-negative integers
4. **Durations**: Must be valid ISO 8601 format (P#Y#M#DT#H#M#S)

### Server-Side Validation (Trust API)

1. **ID Uniqueness**: Enforced by database
2. **Relationship Integrity**: Agency IDs exist
3. **Status/Type IDs**: Must reference valid lookup tables

---

## Example Data Snapshots

### Astronaut List Item (Normal)

```json
{
  "id": 101,
  "url": "https://lldev.thespacedevs.com/2.4.0/astronauts/101/",
  "response_mode": "normal",
  "name": "Neil Armstrong",
  "status": {
    "id": 3,
    "name": "Deceased"
  },
  "agency": {
    "id": 44,
    "name": "National Aeronautics and Space Administration",
    "abbrev": "NASA",
    "type": {
      "id": 1,
      "name": "Government"
    }
  },
  "image": {
    "id": 123,
    "name": "Neil Armstrong",
    "image_url": "https://example.com/armstrong.jpg",
    "thumbnail_url": "https://example.com/armstrong_thumb.jpg"
  },
  "age": 82,
  "bio": "Neil Alden Armstrong was an American astronaut and aeronautical engineer...",
  "type": {
    "id": 1,
    "name": "Astronaut"
  },
  "nationality": [
    {
      "id": 234,
      "name": "United States of America",
      "alpha_2_code": "US",
      "alpha_3_code": "USA",
      "code": 840
    }
  ]
}
```

### Astronaut Detail (Detailed)

```json
{
  ...all fields from normal, plus:
  "in_space": false,
  "time_in_space": "P8DT14H12M",
  "eva_time": "P0DT2H21M",
  "date_of_birth": "1930-08-05",
  "date_of_death": "2012-08-25",
  "wiki": "https://en.wikipedia.org/wiki/Neil_Armstrong",
  "first_flight": "1966-03-16T00:00:00Z",
  "last_flight": "1969-07-16T13:32:00Z",
  "flights_count": 2,
  "landings_count": 2,
  "spacewalks_count": 1,
  "last_walk": "1969-07-21T02:56:00Z",
  "last_landing": "1969-07-24T16:50:00Z",
  "flights": [
    {
      "id": "abc123",
      "url": "...",
      "name": "Gemini 8",
      "net": "1966-03-16T00:00:00Z",
      "status": {...}
    },
    {
      "id": "def456",
      "url": "...",
      "name": "Apollo 11",
      "net": "1969-07-16T13:32:00Z",
      "status": {...}
    }
  ]
}
```

---

## Data Mapping Considerations

### ISO Duration Parsing

**Problem**: API returns durations as ISO 8601 strings (e.g., "P8DT14H12M")  
**Solution**: Use `parseIsoDurationToHumanReadable()` utility

```kotlin
val timeInSpace = astronaut.timeInSpace?.let { 
    parseIsoDurationToHumanReadable(it) 
} ?: "N/A"
// Output: "8 days, 14 hours, 12 minutes"
```

### Date Formatting

**Problem**: API returns dates in multiple formats  
**Solution**: Use `DateTimeUtil` with UTC toggle support

```kotlin
val birthDate = astronaut.dateOfBirth?.let {
    DateTimeUtil.formatDate(it, useUtc = false)
} ?: "Unknown"
```

### Nullable Name Handling

**Problem**: `name` field is nullable  
**Solution**: Provide sensible fallbacks

```kotlin
val displayName = astronaut.name ?: "Unknown Astronaut"
```

---

## Schema Evolution Considerations

### Future API Changes

**Adding New Fields**: Non-breaking, OpenAPI regeneration handles  
**Removing Fields**: Breaking, requires client updates  
**Changing Field Types**: Breaking, requires migration

**Mitigation**: Use generated models → OpenAPI handles versioning

---

## Summary

This data model leverages Launch Library API 2.4.0's comprehensive astronaut data to provide rich browsing and viewing experiences. All models are generated from OpenAPI spec, ensuring type safety and automatic updates. The three-tier model structure (List/Normal/Detailed) optimizes for different use cases while the supporting entities enable rich relationships and filtering.

**Key Takeaways**:
- Use `AstronautEndpointNormal` for list views (optimized payload)
- Use `AstronautEndpointDetailed` for detail views (comprehensive data)
- Leverage `AstronautFlight` for crew associations in spacecraft/events
- Trust server-side validation, add client-side checks for UX
- Handle nullability gracefully with fallbacks

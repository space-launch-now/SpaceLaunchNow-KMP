# In-Flight Launches API Contract

**Feature**: 010-live-favorite-launches  
**Date**: 2026-04-01

## Endpoint

Uses existing Launch Library API v2.4.0 endpoint with status filtering.

### GET /2.4.0/launches/

Fetch launches filtered by in-flight status.

**Query Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `status__ids` | List<Int> | Yes | Filter by status IDs. Use `6` for In Flight |
| `lsp__id` | List<Int> | No | Filter by Launch Service Provider ID |
| `location__ids` | List<Int> | No | Filter by location IDs |
| `limit` | Int | No | Max results (default: 10, max: 100) |
| `ordering` | String | No | Sort order (use `net` for launch time) |

**Example Request**:
```
GET /2.4.0/launches/?status__ids=6&limit=5&ordering=net
Authorization: Bearer <API_KEY>
User-Agent: SpaceLaunchNow-Android/4.x.x
```

**Response** (200 OK):
```json
{
  "count": 1,
  "next": null,
  "previous": null,
  "results": [
    {
      "id": "d5bca89f-3c3c-4371-a5e2-9c2b2d2f0f68",
      "name": "SpaceX Falcon 9 | Starlink Group 6-50",
      "status": {
        "id": 6,
        "name": "In Flight",
        "abbrev": "InFlight"
      },
      "net": "2026-04-01T14:30:00Z",
      "window_start": "2026-04-01T14:30:00Z",
      "window_end": "2026-04-01T18:30:00Z",
      "launch_service_provider": {
        "id": 121,
        "name": "SpaceX",
        "abbrev": "SpX"
      },
      "pad": {
        "id": 87,
        "name": "Launch Complex 39A",
        "location": {
          "id": 12,
          "name": "Kennedy Space Center, FL, USA"
        }
      },
      "mission": {
        "name": "Starlink Group 6-50",
        "description": "Deployment of Starlink satellites",
        "type": "Communications"
      },
      "image": {
        "image_url": "https://example.com/launch-image.jpg"
      },
      "webcast_live": true,
      "vidURLs": [
        {
          "url": "https://youtube.com/watch?v=abc123",
          "type": "Livestream"
        }
      ]
    }
  ]
}
```

**Response** (No in-flight launches):
```json
{
  "count": 0,
  "next": null,
  "previous": null,
  "results": []
}
```

## Extension Function Usage

```kotlin
// In LaunchesApiExtensions.kt (already exists, statusIds parameter supported)
suspend fun LaunchesApi.getLaunchList(
    statusIds: List<Int>? = null,
    lspId: List<Int>? = null,
    locationIds: List<Int>? = null,
    limit: Int? = null,
    ordering: String? = null,
    // ... other parameters
): HttpResponse<PaginatedLaunchNormalList>

// Usage for in-flight launches:
val response = launchesApi.getLaunchList(
    statusIds = listOf(6),              // In Flight status only
    lspId = filterParams.agencyIds,
    locationIds = filterParams.locationIds,
    limit = 5,
    ordering = "net"
)
```

## Repository Method Contract

### New Method: `getInFlightLaunches()`

```kotlin
interface LaunchRepository {
    /**
     * Fetch launches currently in flight (status_id = 6) matching user filters.
     *
     * @param forceRefresh If true, bypass cache
     * @param agencyIds Filter by LSP IDs (from user preferences)
     * @param locationIds Filter by location IDs (from user preferences)
     * @return Result with DataResult containing in-flight launches
     */
    suspend fun getInFlightLaunches(
        forceRefresh: Boolean = false,
        agencyIds: List<Int>? = null,
        locationIds: List<Int>? = null
    ): Result<DataResult<PaginatedLaunchNormalList>>
}
```

**Implementation Notes**:
- Uses Stale-While-Revalidate caching pattern (per Constitution)
- Cache key: `"in_flight_launches_{agencyIds}_{locationIds}"`
- TTL: 5 minutes fresh, 30 minutes stale
- Returns empty list when no in-flight launches (not an error)

## Error Handling

| Status Code | Meaning | Client Handling |
|-------------|---------|-----------------|
| 200 | Success | Display results or hide LIVE card if empty |
| 403 | Rate limited | Return stale cache, show error if no cache |
| 500+ | Server error | Return stale cache, show error if no cache |
| Network error | No connectivity | Return stale cache, show offline indicator |

# API Contracts: Astronaut Endpoints

**API Version**: Launch Library 2.4.0  
**Base URL**: `https://lldev.thespacedevs.com/2.4.0/`  
**Authentication**: API Key (via `?key=` query parameter or `Authorization: Bearer <key>` header)

---

## Overview

This document defines the API contracts for astronaut-related endpoints used in SpaceLaunchNow. All endpoints are generated from OpenAPI spec (`ll_2.4.0.json`) and accessed via extension functions.

---

## Endpoints

### 1. List Astronauts (Normal Detail)

**Endpoint**: `GET /astronauts/`  
**Generated Method**: `AstronautsApi.astronautsList()`  
**Extension Function**: `AstronautsApi.getAstronautList()`

**Purpose**: Retrieve paginated list of astronauts with normal detail level.

#### Request Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `limit` | Int | No | 20 | Results per page (max 100) |
| `offset` | Int | No | 0 | Pagination offset |
| `search` | String | No | null | Search name/bio (case-insensitive) |
| `status_ids` | List<Int> | No | null | Filter by status IDs (comma-separated) |
| `agency_ids` | List<Int> | No | null | Filter by agency IDs (comma-separated) |
| `ordering` | String | No | "name" | Sort field: `name`, `-name`, `first_flight`, `-first_flight` |

#### Response (200 OK)

**Content-Type**: `application/json`

```json
{
  "count": 573,
  "next": "https://lldev.thespacedevs.com/2.4.0/astronauts/?limit=20&offset=20",
  "previous": null,
  "results": [
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
        },
        "logo": {
          "id": 123,
          "image_url": "https://example.com/nasa_logo.png"
        }
      },
      "image": {
        "id": 456,
        "image_url": "https://example.com/armstrong.jpg",
        "thumbnail_url": "https://example.com/armstrong_thumb.jpg"
      },
      "age": null,
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
  ]
}
```

**Response Model**: `PaginatedAstronautEndpointNormalList`

#### Error Responses

| Status | Description | Example |
|--------|-------------|---------|
| 400 | Bad Request (invalid parameters) | `{"detail": "Invalid ordering field"}` |
| 401 | Unauthorized (missing/invalid API key) | `{"detail": "Authentication credentials were not provided"}` |
| 429 | Rate Limit Exceeded | `{"detail": "Request was throttled. Expected available in X seconds."}` |
| 500 | Server Error | `{"detail": "Internal server error"}` |

#### Extension Function Signature

```kotlin
suspend fun AstronautsApi.getAstronautList(
    limit: Int? = 20,
    offset: Int? = 0,
    search: String? = null,
    statusIds: List<Int>? = null,
    agencyIds: List<Int>? = null,
    ordering: String? = "name"
): HttpResponse<PaginatedAstronautEndpointNormalList>
```

#### Example Usage

```kotlin
val repository: AstronautRepository = get()

// Basic list
val result = repository.getAstronauts(limit = 20, offset = 0)

// Filtered by NASA
val nasaAstronauts = repository.getAstronauts(
    agencyIds = listOf(44), // NASA
    statusIds = listOf(1)   // Active
)

// Search by name
val searchResults = repository.searchAstronauts(query = "Armstrong")
```

---

### 2. Retrieve Astronaut Detail

**Endpoint**: `GET /astronauts/{id}/`  
**Generated Method**: `AstronautsApi.astronautsRetrieve(id: Int)`  
**Extension Function**: `AstronautsApi.getAstronautDetail(id: Int)`

**Purpose**: Retrieve comprehensive details for a single astronaut.

#### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Int | Yes | Astronaut unique ID |

#### Response (200 OK)

**Content-Type**: `application/json`

```json
{
  "id": 101,
  "url": "https://lldev.thespacedevs.com/2.4.0/astronauts/101/",
  "response_mode": "detailed",
  "name": "Neil Armstrong",
  "status": {
    "id": 3,
    "name": "Deceased"
  },
  "agency": {
    "id": 44,
    "name": "National Aeronautics and Space Administration",
    "abbrev": "NASA"
  },
  "image": {
    "id": 456,
    "image_url": "https://example.com/armstrong.jpg"
  },
  "age": null,
  "bio": "Neil Alden Armstrong was an American astronaut...",
  "type": {
    "id": 1,
    "name": "Astronaut"
  },
  "nationality": [
    {
      "id": 234,
      "name": "United States of America",
      "alpha_2_code": "US"
    }
  ],
  "in_space": false,
  "time_in_space": "P8DT14H12M30S",
  "eva_time": "P0DT2H21M0S",
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
      "id": "abc-123-uuid",
      "url": "https://lldev.thespacedevs.com/2.4.0/launches/abc-123-uuid/",
      "name": "Gemini 8",
      "status": {
        "id": 3,
        "name": "Launch Successful",
        "abbrev": "Success"
      },
      "net": "1966-03-16T00:00:00Z",
      "image": {
        "image_url": "https://example.com/gemini8.jpg"
      }
    },
    {
      "id": "def-456-uuid",
      "url": "https://lldev.thespacedevs.com/2.4.0/launches/def-456-uuid/",
      "name": "Apollo 11",
      "status": {
        "id": 3,
        "name": "Launch Successful",
        "abbrev": "Success"
      },
      "net": "1969-07-16T13:32:00Z",
      "image": {
        "image_url": "https://example.com/apollo11.jpg"
      }
    }
  ]
}
```

**Response Model**: `AstronautEndpointDetailed`

#### Error Responses

| Status | Description | Example |
|--------|-------------|---------|
| 404 | Not Found (invalid ID) | `{"detail": "Not found."}` |
| 401 | Unauthorized | `{"detail": "Authentication credentials were not provided"}` |
| 500 | Server Error | `{"detail": "Internal server error"}` |

#### Extension Function Signature

```kotlin
suspend fun AstronautsApi.getAstronautDetail(
    id: Int
): HttpResponse<AstronautEndpointDetailed>
```

#### Example Usage

```kotlin
val repository: AstronautRepository = get()
val result = repository.getAstronautDetail(id = 101)

result.onSuccess { astronaut ->
    println("Name: ${astronaut.name}")
    println("Flights: ${astronaut.flightsCount}")
    println("Time in space: ${astronaut.timeInSpace}")
}
```

---

## Data Types Reference

### AstronautStatus

```typescript
{
  id: number,        // 1 = Active, 2 = Retired, 3 = Deceased, 11 = Management
  name: string       // "Active", "Retired", "Deceased", "Management"
}
```

### AstronautType

```typescript
{
  id: number,        // 1 = Astronaut, 2 = Cosmonaut, 3 = Taikonaut
  name: string       // "Astronaut", "Cosmonaut", "Taikonaut"
}
```

### AgencyMini

```typescript
{
  id: number,
  name: string,
  abbrev: string,    // "NASA", "ESA", "Roscosmos"
  type: {
    id: number,
    name: string     // "Government", "Commercial", "International"
  },
  logo: {
    id: number,
    image_url: string,
    thumbnail_url: string
  },
  social_logo: { ... }
}
```

### Image

```typescript
{
  id: number,
  name: string,
  image_url: string,           // Full resolution
  thumbnail_url: string,       // Thumbnail (usually 200x200)
  creator_url: string          // Attribution link
}
```

### Country

```typescript
{
  id: number,
  name: string,                // "United States of America"
  alpha_2_code: string,        // "US"
  alpha_3_code: string,        // "USA"
  code: number,                // 840 (ISO numeric code)
  flag: {
    image_url: string
  }
}
```

---

## Common Patterns

### Pagination

All list endpoints follow this pattern:

```json
{
  "count": <total_results>,
  "next": <next_page_url | null>,
  "previous": <previous_page_url | null>,
  "results": [...]
}
```

**Pagination Logic**:
- `count`: Total number of results matching query
- `next`: URL for next page (null if last page)
- `previous`: URL for previous page (null if first page)
- `results`: Current page items (up to `limit`)

**Calculating Pages**:
```kotlin
val totalPages = (count + limit - 1) / limit
val currentPage = offset / limit
val hasNextPage = next != null
```

### Filtering

Multiple filter parameters are combined with AND logic:

```
/astronauts/?status_ids=1,2&agency_ids=44
// Returns: Active OR Retired astronauts from NASA
```

### Ordering

Prefix with `-` for descending order:

```
/astronauts/?ordering=name       // A-Z
/astronauts/?ordering=-name      // Z-A
/astronauts/?ordering=first_flight  // Oldest first
/astronauts/?ordering=-first_flight // Newest first
```

### Searching

Search is case-insensitive and searches:
- `name` field
- `bio` field

```
/astronauts/?search=armstrong
// Matches: "Neil Armstrong", "Lance Armstrong" (if bio mentions)
```

---

## Rate Limiting

**Limits**:
- Free tier: 15 requests/hour
- Authenticated: 300 requests/hour
- Premium: Custom limits

**Headers** (returned in response):
```
X-RateLimit-Limit: 300
X-RateLimit-Remaining: 299
X-RateLimit-Reset: 1640995200  // Unix timestamp
```

**Handling**:
```kotlin
catch (e: ResponseException) {
    when (e.response.status.value) {
        429 -> {
            val retryAfter = e.response.headers["Retry-After"]?.toIntOrNull() ?: 60
            delay(retryAfter.seconds)
            // Retry request
        }
    }
}
```

---

## Caching Strategy

**Recommendations**:
- **List Endpoint**: Cache for 1 hour (astronaut data rarely changes)
- **Detail Endpoint**: Cache for 1 hour
- **Images**: Cache indefinitely (immutable URLs)

**Cache Keys**:
```kotlin
// List
"astronauts:list:limit=$limit:offset=$offset:search=$search:..."

// Detail
"astronauts:detail:id=$id"
```

---

## Testing Endpoints

### Test IDs

**Known Good IDs** (for integration tests):
- `101` → Neil Armstrong (deceased, historical data)
- `780` → Chris Cassidy (active NASA astronaut)
- `813` → Jeanette Epps (active NASA astronaut)

### Mock Responses

Located in `tests/fixtures/astronauts/`:
```
astronauts/
├── list_response.json         # Sample list response
├── detail_neil_armstrong.json # Sample detail response
└── empty_list.json            # Empty results
```

---

## Security Considerations

### API Key Management

**Never commit API keys**:
```kotlin
// ✅ Good: Read from environment
val apiKey = EnvironmentManager.getEnv("API_KEY", "")

// ❌ Bad: Hardcoded
val apiKey = "abc123xyz"
```

### HTTPS Only

All API requests MUST use HTTPS. HTTP requests will be rejected:
```
http://lldev.thespacedevs.com/...  // ❌ Rejected
https://lldev.thespacedevs.com/... // ✅ Allowed
```

---

## Error Handling Patterns

```kotlin
suspend fun getAstronauts(...): Result<PaginatedAstronautEndpointNormalList> {
    return try {
        val response = astronautsApi.getAstronautList(...)
        Result.success(response.body())
    } catch (e: ResponseException) {
        when (e.response.status.value) {
            401 -> Result.failure(Exception("Unauthorized: Check API key"))
            404 -> Result.failure(Exception("Astronaut not found"))
            429 -> Result.failure(Exception("Rate limit exceeded"))
            else -> Result.failure(e)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## Contract Testing

Integration tests validate:
1. Endpoint availability
2. Response schema matches models
3. Pagination works correctly
4. Filters apply as expected
5. Error responses are handled

Example test:
```kotlin
@Test
fun `getAstronautList returns valid paginated response`() = runTest {
    val response = astronautsApi.getAstronautList(limit = 5)
    
    assertTrue(response.isSuccessful)
    val body = response.body()
    assertTrue(body.results.size <= 5)
    assertEquals("normal", body.results[0].responseMode)
}
```

---

## Summary

These API contracts define how SpaceLaunchNow interacts with Launch Library 2.4.0 astronaut endpoints. Extension functions provide clean interfaces, repository pattern handles errors, and ViewModels manage state. All contracts are validated by integration tests.

**Key Points**:
- Use extension functions, not generated methods
- Handle pagination with `next`/`previous` URLs
- Respect rate limits (300 req/hour authenticated)
- Cache responses for 1 hour
- Test with known good IDs (101, 780, 813)

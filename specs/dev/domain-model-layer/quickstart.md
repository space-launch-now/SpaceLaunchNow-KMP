# Quickstart: Domain Model Layer Migration

**Feature**: Introduce domain models to decouple UI/ViewModel from API response types  
**Date**: 2026-04-19

---

## Prerequisites

- Java 21 (JetBrains JDK)
- Generated API clients present: `./gradlew openApiGenerate`
- All current tests passing: `./gradlew compileKotlinDesktop`

---

## Step 1: Create Domain Model Package

```bash
# Create directory structure
mkdir -p composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/model
mkdir -p composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/domain/mapper
```

Create files in order:
1. `domain/model/PaginatedResult.kt`
2. `domain/model/Common.kt` — all shared value types
3. `domain/model/Launch.kt` — the unified Launch data class
4. `domain/model/Event.kt` — the unified Event data class
5. `domain/model/LaunchFilterParams.kt`
6. `domain/model/EventFilterParams.kt`

---

## Step 2: Create Mappers

1. `domain/mapper/CommonMappers.kt` — extension functions for shared types
2. `domain/mapper/LaunchMappers.kt` — `LaunchBasic.toDomain()`, `LaunchNormal.toDomain()`, `LaunchDetailed.toDomain()`
3. `domain/mapper/EventMappers.kt` — `EventEndpointNormal.toDomain()`, `EventEndpointDetailed.toDomain()`

**Verify**: Write unit tests for each mapper in `commonTest`.

---

## Step 3: Update Repository Layer

### LaunchRepository Interface
Add new domain-returning methods alongside existing ones:

```kotlin
// NEW — returns domain Launch
suspend fun getUpcomingLaunches(limit: Int, offset: Int = 0): Result<PaginatedResult<Launch>>

// OLD — keep during transition, mark @Deprecated
@Deprecated("Use getUpcomingLaunches returning domain Launch")
suspend fun getUpcomingLaunchesList(limit: Int): Result<PaginatedLaunchBasicList>
```

### LaunchRepositoryImpl
Implement new methods by calling existing API extension functions and mapping:

```kotlin
override suspend fun getUpcomingLaunches(limit: Int, offset: Int): Result<PaginatedResult<Launch>> {
    return try {
        val response = launchesApi.getLaunchMiniList(limit = limit, offset = offset, upcoming = true, ordering = "net")
        Result.success(response.body().toDomain())
    } catch (e: ResponseException) {
        Result.failure(e)
    }
}
```

Same pattern for EventsRepository.

---

## Step 4: Update ViewModels

Change StateFlow types from API models to domain models:

```kotlin
// BEFORE
val nextLaunch: StateFlow<LaunchNormal?>
// AFTER
val nextLaunch: StateFlow<Launch?>
```

Call new repository methods. Example:
```kotlin
fun fetchNextLaunch() {
    viewModelScope.launch {
        repository.getUpcomingLaunches(limit = 1).onSuccess { result ->
            _nextLaunch.value = result.results.firstOrNull()
        }
    }
}
```

---

## Step 5: Update UI

### Replace LaunchCardData
```kotlin
// BEFORE
@Composable
fun LaunchCardHeaderOverlay(launchData: LaunchCardData, ...)

// AFTER
@Composable
fun LaunchCardHeaderOverlay(launch: Launch, ...)
```

### Update LaunchFormatUtil
```kotlin
// Add new overload
fun formatLaunchTitle(launch: Launch): String {
    return formatLaunchTitle(
        launchServiceProviderName = launch.provider.name,
        launchServiceProviderAbbrev = launch.provider.abbrev,
        rocketConfigurationName = launch.rocket?.fullName ?: launch.rocket?.name,
        launchName = launch.name
    )
}
```

### Update LaunchCache
```kotlin
// BEFORE: separate normalCache + detailedCache
// AFTER: single cache
class LaunchCache {
    private val cache = mutableMapOf<String, Launch>()
    private val timestamps = mutableMapOf<String, Instant>()
    
    fun cacheLaunch(launch: Launch) { ... }
    fun getCachedLaunch(id: String, duration: Duration = 15.minutes): Launch? { ... }
}
```

---

## Step 6: Update LaunchLocalDataSource

The local data source still stores/retrieves API JSON but converts on output:

```kotlin
// BEFORE
suspend fun getUpcomingBasicLaunches(limit: Int): List<LaunchBasic>
// AFTER  
suspend fun getUpcomingLaunches(limit: Int): List<Launch>
// Internal: json.decodeFromString<LaunchBasic>(jsonData).toDomain()
```

---

## Step 7: Verify

```bash
# Compile all targets
./gradlew compileKotlinDesktop

# Run tests
./gradlew test

# Run desktop app
./gradlew desktopRun --quiet
```

Check:
- [ ] Home screen loads upcoming launches
- [ ] Schedule page filters work
- [ ] Launch detail shows all sections
- [ ] Events tab loads
- [ ] No imports from `me.calebjones.spacelaunchnow.api.launchlibrary.models` in composables

---

## Key Migration Rules

1. **Mappers are the ONLY files that import API model types** (plus repository impls and local data sources)
2. **ViewModels and UI only import from `domain/model/`**
3. **One `Launch` type everywhere** — no more LaunchBasic/LaunchNormal/LaunchDetailed in business logic
4. **Nullable = not yet loaded** — if `launch.mission` is null, it means we have Basic-level data
5. **Lists default to empty** — never null lists in domain models

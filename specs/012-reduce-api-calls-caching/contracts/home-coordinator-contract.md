# Contract: Home Data Coordinator (Phase 4)

**Feature**: `012-reduce-api-calls-caching`

## Interface

```kotlin
/**
 * Coordinates launch data loading for the Home screen.
 * Makes a single API call that serves both FeaturedLaunchViewModel and LaunchesViewModel.
 * Registered as singleton in AppModule.kt.
 */
class HomeDataCoordinator(
    private val launchRepository: LaunchRepository
) {
    private val log = logger()
    
    // Status IDs for "featured" launches (Go, TBD, Success, In Flight, Partial Failure, TBC, Pre Flight Checks)
    private val featuredStatusIds = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
    
    private val _homeUpcomingData = MutableSharedFlow<DataResult<PaginatedLaunchNormalList>>(replay = 1)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Load unified upcoming launch data. Call once from HomeScreen LaunchedEffect.
     * Subsequent collectors receive replayed data.
     */
    suspend fun loadHomeData(
        forceRefresh: Boolean = false,
        agencyIds: List<Int>? = null,
        locationIds: List<Int>? = null
    ) {
        _isLoading.value = true
        try {
            val result = launchRepository.getUpcomingLaunchesNormal(
                limit = 15,  // Enough for featured (4) + carousel (10) with overlap
                forceRefresh = forceRefresh,
                agencyIds = agencyIds,
                locationIds = locationIds
            )
            result.onSuccess { data ->
                _homeUpcomingData.emit(data)
            }
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Featured launches (first 4 matching status criteria).
     * Client-side filter from shared data.
     */
    val featuredLaunches: Flow<List<LaunchNormal>> = _homeUpcomingData
        .map { dataResult ->
            dataResult.data.results
                .filter { launch -> launch.status?.id in featuredStatusIds }
                .take(4)
        }
    
    /**
     * Upcoming launches carousel (first 10 items).
     */
    val upcomingLaunches: Flow<List<LaunchNormal>> = _homeUpcomingData
        .map { dataResult ->
            dataResult.data.results.take(10)
        }
    
    /**
     * Data source indicator for UI badges (CACHE, STALE_CACHE, NETWORK).
     */
    val dataSource: Flow<DataSource> = _homeUpcomingData
        .map { it.source }
}
```

## DI Registration (AppModule.kt)

```kotlin
// HomeDataCoordinator - singleton shared across Home ViewModels
single { HomeDataCoordinator(get()) }
```

## ViewModel Integration

### FeaturedLaunchViewModel Changes

```kotlin
class FeaturedLaunchViewModel(
    // Replace: private val launchRepository: LaunchRepository
    private val homeDataCoordinator: HomeDataCoordinator,  // NEW
    private val remoteConfigRepository: RemoteConfigRepository
) : ViewModel() {
    
    fun loadFeaturedLaunch() {
        viewModelScope.launch {
            homeDataCoordinator.featuredLaunches.collect { launches ->
                _featuredLaunches.value = ViewState(data = launches)
            }
        }
    }
    
    // loadInFlightLaunch() remains unchanged (separate query, status=6 only)
    // loadPinnedContent() remains unchanged (remote config)
}
```

### LaunchesViewModel Changes

```kotlin
class LaunchesViewModel(
    // Replace: private val launchRepository: LaunchRepository
    private val homeDataCoordinator: HomeDataCoordinator,  // NEW
    private val launchRepository: LaunchRepository  // Keep for previous launches
) : ViewModel() {
    
    fun loadUpcomingLaunches() {
        viewModelScope.launch {
            homeDataCoordinator.upcomingLaunches.collect { launches ->
                _upcomingLaunches.value = ViewState(data = launches)
            }
        }
    }
    
    // loadPreviousLaunches() unchanged (separate query direction)
}
```

### HomeScreen Changes

```kotlin
// Before (Phase 3 state):
LaunchedEffect(Unit) {
    featuredLaunchViewModel.loadFeaturedLaunch()
    featuredLaunchViewModel.loadInFlightLaunch()
    launchesViewModel.loadUpcomingLaunches()
    feedViewModel.loadUpdates()
    feedViewModel.loadArticles()
    eventsViewModel.loadEvents()
}

// After (Phase 4):
val homeDataCoordinator = koinInject<HomeDataCoordinator>()
LaunchedEffect(Unit) {
    homeDataCoordinator.loadHomeData()  // Single call serves both VMs
    featuredLaunchViewModel.loadInFlightLaunch()  // Separate (status=6)
    feedViewModel.loadUpdates()
    feedViewModel.loadArticles()
    eventsViewModel.loadEvents()
}
```

## API Call Reduction

| Before Phase 4 | After Phase 4 |
|----------------|---------------|
| `getFeaturedLaunch()` — 1 API call | `loadHomeData()` — 1 API call (shared) |
| `getUpcomingLaunchesNormal()` — 1 API call | *(served from shared data)* |
| `getInFlightLaunches()` — 1 API call (keep) | `getInFlightLaunches()` — 1 API call (cached) |
| **3 calls** | **2 calls** |

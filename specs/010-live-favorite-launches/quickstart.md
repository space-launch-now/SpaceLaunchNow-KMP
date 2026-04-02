# Quickstart: Live Favorite Launches Card

**Feature**: 010-live-favorite-launches  
**Date**: 2026-04-01

## Quick Implementation Guide

### 1. Add Repository Method (5 minutes)

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LaunchRepository.kt`

```kotlin
// Add to interface
suspend fun getInFlightLaunches(
    forceRefresh: Boolean = false,
    agencyIds: List<Int>? = null,
    locationIds: List<Int>? = null
): Result<DataResult<PaginatedLaunchNormalList>>
```

**File**: `LaunchRepositoryImpl.kt`

```kotlin
override suspend fun getInFlightLaunches(
    forceRefresh: Boolean,
    agencyIds: List<Int>?,
    locationIds: List<Int>?
): Result<DataResult<PaginatedLaunchNormalList>> {
    return try {
        val response = launchesApi.getLaunchList(
            statusIds = listOf(6),  // In Flight
            lspId = agencyIds,
            locationIds = locationIds,
            limit = 5,
            ordering = "net"
        )
        val data = response.body()
        Result.success(DataResult(data, DataSource.NETWORK, Clock.System.now().toEpochMilliseconds()))
    } catch (e: Exception) {
        log.e(e) { "Error fetching in-flight launches" }
        Result.failure(e)
    }
}
```

### 2. Add HomeViewModel State (5 minutes)

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/HomeViewModel.kt`

```kotlin
// Add state
private val _inFlightLaunchState = MutableStateFlow(ViewState<LaunchNormal?>(data = null))
val inFlightLaunchState: StateFlow<ViewState<LaunchNormal?>> = _inFlightLaunchState.asStateFlow()

// Add load function
fun loadInFlightLaunch() {
    viewModelScope.launch {
        _inFlightLaunchState.update { it.copy(isLoading = true) }
        
        val filters = notificationStateStorage.stateFlow.first()
        val filterParams = launchFilterService.getFilterParams(filters)
        
        launchRepository.getInFlightLaunches(
            agencyIds = filterParams.agencyIds,
            locationIds = filterParams.locationIds
        ).onSuccess { result ->
            _inFlightLaunchState.update {
                it.copy(data = result.data.results.firstOrNull(), isLoading = false)
            }
        }.onFailure { e ->
            _inFlightLaunchState.update { it.copy(error = e.message, isLoading = false) }
        }
    }
}

// Call in loadHomeScreenData()
fun loadHomeScreenData() {
    viewModelScope.launch {
        // Add to parallel loads
        launch { loadInFlightLaunch() }  // NEW
        // ... existing loads
    }
}
```

### 3. Create LiveIndicator Component (10 minutes)

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/compose/LiveIndicator.kt`

```kotlin
@Composable
fun LiveIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_alpha"
    )
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFFE53935).copy(alpha = alpha)
    ) {
        Text(
            text = "LIVE",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
```

### 4. Create LiveLaunchCard Component (15 minutes)

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/home/components/LiveLaunchCard.kt`

```kotlin
@Composable
fun LiveLaunchCard(
    launch: LaunchNormal,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { navController.navigate(LaunchDetail(launch.id)) },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1976D2).copy(alpha = 0.1f)
        ),
        border = BorderStroke(2.dp, Color(0xFF1976D2))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Launch image
            SubcomposeAsyncImage(
                model = launch.image?.imageUrl,
                contentDescription = "Launch image",
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiveIndicator()
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = LaunchFormatUtil.formatLaunchTitle(launch),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = launch.mission?.name ?: launch.name,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
```

### 5. Integrate in HomeScreen (5 minutes)

**File**: Home screen LazyColumn

```kotlin
LazyColumn {
    // LIVE card at top (if in-flight launch exists)
    item {
        val inFlightState by viewModel.inFlightLaunchState.collectAsStateWithLifecycle()
        inFlightState.data?.let { launch ->
            LiveLaunchCard(launch = launch, navController = navController)
            Spacer(Modifier.height(16.dp))
        }
    }
    
    // Existing featured launch
    item {
        NextLaunchView(...)
    }
    // ... rest of home screen
}
```

## Verification Steps

1. ✅ Build compiles: `./gradlew compileKotlinDesktop`
2. ✅ Run desktop app: `./gradlew desktopRun`
3. ✅ Verify LIVE card appears when launch status = 6
4. ✅ Verify user filters are respected
5. ✅ Verify navigation to launch detail works

## Files Created/Modified

| File | Action | Lines |
|------|--------|-------|
| `LaunchRepository.kt` | Modified | +5 |
| `LaunchRepositoryImpl.kt` | Modified | +30 |
| `HomeViewModel.kt` | Modified | +25 |
| `LiveIndicator.kt` | New | ~50 |
| `LiveLaunchCard.kt` | New | ~80 |
| `HomeScreen.kt` | Modified | +10 |

**Total Estimate**: ~40 minutes implementation, ~200 lines of code

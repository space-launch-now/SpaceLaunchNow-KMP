# Quickstart Guide: Implementing Astronaut Views

**Feature**: Astronaut List and Detail Views  
**Estimated Time**: 8-12 hours for basic implementation  
**Prerequisites**: Familiarity with Kotlin, Compose, and KMP

---

## Table of Contents

1. [Setup](#setup)
2. [Phase 1: API Layer](#phase-1-api-layer-1-hour)
3. [Phase 2: Repository](#phase-2-repository-1-hour)
4. [Phase 3: ViewModels](#phase-3-viewmodels-2-hours)
5. [Phase 4: UI Components](#phase-4-ui-components-4-hours)
6. [Phase 5: Navigation Integration](#phase-5-navigation-integration-1-hour)
7. [Phase 6: Testing](#phase-6-testing-2-hours)
8. [Troubleshooting](#troubleshooting)

---

## Setup

### Prerequisites Check

```bash
# Verify Java 21
java -version  # Should show "21.x.x"

# Verify Gradle
./gradlew --version

# Regenerate API if needed
./gradlew openApiGenerate
```

### Project Structure Overview

```
composeApp/src/commonMain/kotlin/
├── api/
│   └── extensions/
│       └── AstronautsApiExtensions.kt     # ← Phase 1
├── data/
│   └── repository/
│       ├── AstronautRepository.kt         # ← Phase 2
│       └── AstronautRepositoryImpl.kt
├── ui/
│   ├── astronaut/
│   │   ├── AstronautListScreen.kt         # ← Phase 4
│   │   ├── AstronautDetailView.kt
│   │   └── components/
│   │       ├── AstronautCard.kt
│   │       ├── AstronautProfileCard.kt
│   │       ├── AstronautInfoCard.kt
│   │       └── AstronautStatsCard.kt
│   └── viewmodel/
│       ├── AstronautListViewModel.kt      # ← Phase 3
│       └── AstronautDetailViewModel.kt
├── di/
│   └── AppModule.kt                       # ← Update for DI
└── navigation/
    └── Screen.kt                          # ← Phase 5
```

---

## Phase 1: API Layer (1 hour)

### Step 1.1: Create Extension Functions

**File**: `api/extensions/AstronautsApiExtensions.kt`

```kotlin
package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AstronautsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAstronautEndpointNormalList

/**
 * Get astronaut list with clean parameter interface
 */
suspend fun AstronautsApi.getAstronautList(
    limit: Int? = 20,
    offset: Int? = 0,
    search: String? = null,
    statusIds: List<Int>? = null,
    agencyIds: List<Int>? = null,
    ordering: String? = "name"
): HttpResponse<PaginatedAstronautEndpointNormalList> = astronautsList(
    // Map all 70+ parameters (see research.md for full mapping)
    limit = limit,
    offset = offset,
    search = search,
    statusIds = statusIds,
    agencyIds = agencyIds,
    ordering = ordering,
    // ... null out all other 60+ parameters
    age = null,
    ageGt = null,
    // (continue pattern from LaunchesApiExtensions.kt)
)

/**
 * Get single astronaut detail
 */
suspend fun AstronautsApi.getAstronautDetail(
    id: Int
): HttpResponse<AstronautEndpointDetailed> = astronautsRetrieve(id)
```

**✅ Verify**: Extension functions compile without errors

---

## Phase 2: Repository (1 hour)

### Step 2.1: Create Repository Interface

**File**: `data/repository/AstronautRepository.kt`

```kotlin
package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAstronautEndpointNormalList

interface AstronautRepository {
    suspend fun getAstronauts(
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null,
        statusIds: List<Int>? = null,
        agencyIds: List<Int>? = null
    ): Result<PaginatedAstronautEndpointNormalList>
    
    suspend fun getAstronautDetail(id: Int): Result<AstronautEndpointDetailed>
}
```

### Step 2.2: Implement Repository

**File**: `data/repository/AstronautRepositoryImpl.kt`

```kotlin
package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.extensions.getAstronautDetail
import me.calebjones.spacelaunchnow.api.extensions.getAstronautList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AstronautsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.ResponseException
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAstronautEndpointNormalList

class AstronautRepositoryImpl(
    private val astronautsApi: AstronautsApi
) : AstronautRepository {
    
    override suspend fun getAstronauts(
        limit: Int,
        offset: Int,
        search: String?,
        statusIds: List<Int>?,
        agencyIds: List<Int>?
    ): Result<PaginatedAstronautEndpointNormalList> {
        return try {
            val response = astronautsApi.getAstronautList(
                limit = limit,
                offset = offset,
                search = search,
                statusIds = statusIds,
                agencyIds = agencyIds
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAstronautDetail(id: Int): Result<AstronautEndpointDetailed> {
        return try {
            val response = astronautsApi.getAstronautDetail(id)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Step 2.3: Register in Koin

**File**: `di/AppModule.kt`

```kotlin
// Add to existing module
val appModule = module {
    // ... existing registrations
    
    // Astronaut repository
    singleOf(::AstronautRepositoryImpl) { bind<AstronautRepository>() }
}
```

**✅ Verify**: Repository compiles and Koin setup works

---

## Phase 3: ViewModels (2 hours)

### Step 3.1: Astronaut List ViewModel

**File**: `ui/viewmodel/AstronautListViewModel.kt`

```kotlin
package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointNormal
import me.calebjones.spacelaunchnow.data.repository.AstronautRepository

data class AstronautListUiState(
    val astronauts: List<AstronautEndpointNormal> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val currentPage: Int = 0
)

class AstronautListViewModel(
    private val repository: AstronautRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AstronautListUiState())
    val uiState: StateFlow<AstronautListUiState> = _uiState.asStateFlow()
    
    private val pageSize = 20
    
    init {
        loadAstronauts()
    }
    
    fun loadAstronauts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.getAstronauts(limit = pageSize, offset = 0)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        astronauts = response.results,
                        isLoading = false,
                        hasMore = response.next != null,
                        currentPage = 0
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load astronauts"
                    )
                }
        }
    }
    
    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            
            val nextPage = _uiState.value.currentPage + 1
            val offset = nextPage * pageSize
            
            repository.getAstronauts(limit = pageSize, offset = offset)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        astronauts = _uiState.value.astronauts + response.results,
                        isLoadingMore = false,
                        hasMore = response.next != null,
                        currentPage = nextPage
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoadingMore = false)
                }
        }
    }
    
    fun refresh() {
        _uiState.value = AstronautListUiState()
        loadAstronauts()
    }
}
```

### Step 3.2: Astronaut Detail ViewModel

**File**: `ui/viewmodel/AstronautDetailViewModel.kt`

```kotlin
package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointDetailed
import me.calebjones.spacelaunchnow.data.repository.AstronautRepository

data class AstronautDetailUiState(
    val astronaut: AstronautEndpointDetailed? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AstronautDetailViewModel(
    private val repository: AstronautRepository,
    private val astronautId: Int
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AstronautDetailUiState(isLoading = true))
    val uiState: StateFlow<AstronautDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadAstronautDetail()
    }
    
    fun loadAstronautDetail() {
        viewModelScope.launch {
            _uiState.value = AstronautDetailUiState(isLoading = true)
            
            repository.getAstronautDetail(astronautId)
                .onSuccess { astronaut ->
                    _uiState.value = AstronautDetailUiState(
                        astronaut = astronaut,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = AstronautDetailUiState(
                        isLoading = false,
                        error = error.message ?: "Failed to load astronaut"
                    )
                }
        }
    }
    
    fun retry() = loadAstronautDetail()
}
```

### Step 3.3: Register ViewModels in Koin

**File**: `di/AppModule.kt`

```kotlin
val appModule = module {
    // ... existing ViewModels
    
    // Astronaut ViewModels
    viewModelOf(::AstronautListViewModel)
    viewModelOf(::AstronautDetailViewModel)
}
```

**✅ Verify**: ViewModels compile and state flows work

---

## Phase 4: UI Components (4 hours)

### Step 4.1: Astronaut Card Component

**File**: `ui/astronaut/components/AstronautCard.kt`

```kotlin
package me.calebjones.spacelaunchnow.ui.astronaut.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointNormal

@Composable
fun AstronautCard(
    astronaut: AstronautEndpointNormal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            AsyncImage(
                model = astronaut.image?.imageUrl,
                contentDescription = astronaut.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(Modifier.width(16.dp))
            
            // Info column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = astronaut.name ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                astronaut.agency?.let { agency ->
                    Text(
                        text = agency.abbrev ?: agency.name ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                astronaut.status?.let { status ->
                    Text(
                        text = status.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
```

### Step 4.2: Astronaut List Screen

**File**: `ui/astronaut/AstronautListScreen.kt`

```kotlin
package me.calebjones.spacelaunchnow.ui.astronaut

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import me.calebjones.spacelaunchnow.navigation.AstronautDetail
import me.calebjones.spacelaunchnow.ui.astronaut.components.AstronautCard
import me.calebjones.spacelaunchnow.ui.viewmodel.AstronautListViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstronautListScreen(
    navController: NavController,
    viewModel: AstronautListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Astronauts") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading && uiState.astronauts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.error != null && uiState.astronauts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.error ?: "Error")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = padding,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.astronauts) { astronaut ->
                        AstronautCard(
                            astronaut = astronaut,
                            onClick = {
                                navController.navigate(AstronautDetail(astronaut.id))
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    
                    // Load more trigger
                    if (uiState.hasMore) {
                        item {
                            LaunchedEffect(Unit) {
                                viewModel.loadMore()
                            }
                            if (uiState.isLoadingMore) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

### Step 4.3: Astronaut Detail View

**File**: `ui/astronaut/AstronautDetailView.kt`

```kotlin
package me.calebjones.spacelaunchnow.ui.astronaut

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import me.calebjones.spacelaunchnow.ui.astronaut.components.AstronautInfoCard
import me.calebjones.spacelaunchnow.ui.astronaut.components.AstronautStatsCard
import me.calebjones.spacelaunchnow.ui.compose.SharedDetailScaffold
import me.calebjones.spacelaunchnow.ui.viewmodel.AstronautDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AstronautDetailView(
    astronautId: Int,
    navController: NavController,
    viewModel: AstronautDetailViewModel = koinViewModel { parametersOf(astronautId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when {
        uiState.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        
        uiState.error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.error ?: "Error")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.retry() }) {
                        Text("Retry")
                    }
                }
            }
        }
        
        uiState.astronaut != null -> {
            val astronaut = uiState.astronaut!!
            
            SharedDetailScaffold(
                titleText = astronaut.name ?: "Unknown",
                taglineText = astronaut.agency?.name,
                imageUrl = astronaut.image?.imageUrl,
                onNavigateBack = { navController.popBackStack() },
                backgroundColors = listOf(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.height(100.dp)) // Header spacing
                    
                    AstronautStatsCard(astronaut)
                    AstronautInfoCard(astronaut)
                    
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}
```

### Step 4.4: Supporting Card Components

**File**: `ui/astronaut/components/AstronautStatsCard.kt`

```kotlin
package me.calebjones.spacelaunchnow.ui.astronaut.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointDetailed
import me.calebjones.spacelaunchnow.ui.components.InfoTile
import me.calebjones.spacelaunchnow.util.parseIsoDurationToHumanReadable

@Composable
fun AstronautStatsCard(astronaut: AstronautEndpointDetailed) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Career Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                astronaut.flightsCount?.let {
                    InfoTile(
                        label = "Flights",
                        value = it.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                astronaut.timeInSpace?.let {
                    InfoTile(
                        label = "Time in Space",
                        value = parseIsoDurationToHumanReadable(it),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                astronaut.spacewalksCount?.let {
                    InfoTile(
                        label = "Spacewalks",
                        value = it.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                astronaut.evaTime?.let {
                    InfoTile(
                        label = "EVA Time",
                        value = parseIsoDurationToHumanReadable(it),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
```

**File**: `ui/astronaut/components/AstronautInfoCard.kt`

```kotlin
package me.calebjones.spacelaunchnow.ui.astronaut.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointDetailed

@Composable
fun AstronautInfoCard(astronaut: AstronautEndpointDetailed) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Biography",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                astronaut.bio,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
```

**✅ Verify**: UI components render correctly with mock data

---

## Phase 5: Navigation Integration (1 hour)

### Step 5.1: Add Navigation Routes

**File**: `navigation/Screen.kt`

```kotlin
// Add to existing routes
@Serializable
data object Astronauts

@Serializable
data class AstronautDetail(val astronautId: Int)
```

### Step 5.2: Register Navigation Composables

**File**: `App.kt` (update navigation graph)

```kotlin
// Add to navigation graph
composableWithCompositionLocal<Astronauts> {
    AstronautListScreen(navController = navController)
}

composableWithCompositionLocal<AstronautDetail> { backStackEntry ->
    val args = backStackEntry.toRoute<AstronautDetail>()
    AstronautDetailView(
        astronautId = args.astronautId,
        navController = navController
    )
}
```

### Step 5.3: Add Settings Link

**File**: `ui/settings/SettingsScreen.kt`

```kotlin
// In "Explore (WORK IN PROGRESS)" section
SettingsNavigationRow(
    title = "Astronauts",
    subtitle = "Browse astronauts and cosmonauts",
    onClick = { navController.navigate(me.calebjones.spacelaunchnow.navigation.Astronauts) }
)
```

**✅ Verify**: Navigation works end-to-end

---

## Phase 6: Testing (2 hours)

### Step 6.1: Repository Test

**File**: `composeApp/src/commonTest/kotlin/AstronautRepositoryTest.kt`

```kotlin
class AstronautRepositoryTest {
    private lateinit var repository: AstronautRepository
    private lateinit var mockApi: AstronautsApi
    
    @Before
    fun setup() {
        mockApi = mockk()
        repository = AstronautRepositoryImpl(mockApi)
    }
    
    @Test
    fun `getAstronauts returns success`() = runTest {
        // Given
        val mockResponse = PaginatedAstronautEndpointNormalList(...)
        coEvery { mockApi.getAstronautList(...) } returns HttpResponse(mockResponse, 200, Headers.Empty)
        
        // When
        val result = repository.getAstronauts()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(20, result.getOrNull()?.results?.size)
    }
}
```

### Step 6.2: ViewModel Test

**File**: `composeApp/src/commonTest/kotlin/AstronautListViewModelTest.kt`

```kotlin
class AstronautListViewModelTest {
    private lateinit var viewModel: AstronautListViewModel
    private lateinit var mockRepository: AstronautRepository
    
    @Before
    fun setup() {
        mockRepository = mockk()
        viewModel = AstronautListViewModel(mockRepository)
    }
    
    @Test
    fun `initial load sets loading state`() = runTest {
        // Given
        coEvery { mockRepository.getAstronauts(...) } returns Result.success(...)
        
        // When
        advanceUntilIdle()
        
        // Then
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.astronauts.isNotEmpty())
    }
}
```

**✅ Verify**: Tests pass with `./gradlew test`

---

## Troubleshooting

### Common Issues

**Issue**: Extension functions not found  
**Solution**: Run `./gradlew openApiGenerate` to regenerate API

**Issue**: Koin injection fails  
**Solution**: Verify `viewModelOf` and `singleOf` are registered in `AppModule.kt`

**Issue**: Images not loading  
**Solution**: Check internet permissions and Coil configuration

**Issue**: Navigation not working  
**Solution**: Verify routes are added to both `Screen.kt` and `App.kt` navigation graph

---

## Next Steps

1. **Add Profile Cards to Launch Detail**: See `ui/astronaut/components/AstronautProfileCard.kt` pattern
2. **Implement Search**: Add search bar to `AstronautListScreen`
3. **Add Filters**: Implement status/agency filters
4. **Offline Support**: Add caching layer

---

## Quick Reference

### Key Files

- **API**: `api/extensions/AstronautsApiExtensions.kt`
- **Repository**: `data/repository/AstronautRepository.kt`
- **ViewModels**: `ui/viewmodel/Astronaut*ViewModel.kt`
- **UI**: `ui/astronaut/AstronautListScreen.kt`, `AstronautDetailView.kt`
- **Navigation**: `navigation/Screen.kt`, `App.kt`

### Testing Commands

```bash
# Run all tests
./gradlew test

# Run Android app
./gradlew installDebug

# Run Desktop app
./gradlew desktopRun

# Regenerate API
./gradlew openApiGenerate
```

---

**Congratulations!** You've implemented astronaut list and detail views. Users can now browse astronauts from Settings → Explore.

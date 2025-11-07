# Implementation Guide: Space Stations, Astronauts, and Programs List Screens

**Last Updated:** November 6, 2025  
**Reference Implementation:** AgencyListScreen (completed Nov 2025)  
**Target Audience:** AI Agents & Future Developers

---

## Overview

This document provides a complete step-by-step guide to implement **Space Stations**, **Astronauts**, and **Programs** list screens following the established patterns in the SpaceLaunchNow KMP app.

### Reference Implementation

The **AgencyListScreen** feature (completed Nov 2025) serves as the canonical pattern. All three new features should follow this exact structure.
s

---

## Implementation Checklist

For each feature (Space Stations, Astronauts, Programs), complete these steps:

### ✅ Step 1: Create Repository Interface

**File Location:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/`

**Template:**
```kotlin
// XRepository.kt
package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedXNormalList

interface XRepository {
    suspend fun getXs(limit: Int, offset: Int = 0): Result<PaginatedXNormalList>
    suspend fun searchXs(searchQuery: String, limit: Int = 50): Result<PaginatedXNormalList>
}
```

**Replace `X` with:**
- `SpaceStation` → `PaginatedSpaceStationNormalList`
- `Astronaut` → `PaginatedAstronautNormalList`
- `Program` → `PaginatedProgramNormalList`

---

### ✅ Step 2: Create Repository Implementation

**File Location:** Same directory as interface

**Template:**
```kotlin
// XRepositoryImpl.kt
package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getXList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.XApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedXNormalList

class XRepositoryImpl(
    private val xApi: XApi
) : XRepository {

    override suspend fun getXs(limit: Int, offset: Int): Result<PaginatedXNormalList> {
        return try {
            val response = xApi.getXList(
                limit = limit,
                offset = offset,
                ordering = "-id" // Or appropriate sort field
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchXs(searchQuery: String, limit: Int): Result<PaginatedXNormalList> {
        return try {
            val response = xApi.getXList(
                limit = limit,
                search = searchQuery,
                ordering = "-id"
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Ordering field suggestions:**
- Space Stations: `"-id"` or `"name"`
- Astronauts: `"-id"` or `"name"`
- Programs: `"-start_date"` or `"name"`

---

### ✅ Step 3: Create API Extension Functions

**File Location:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/api/extensions/`

**CRITICAL:** You MUST check the actual generated API signature first!

**Instructions:**
1. Open `composeApp/src/openApiLL/src/commonMain/kotlin/me/calebjones/spacelaunchnow/api/launchlibrary/apis/XApi.kt`
2. Find the `xList()` method signature (around line 284)
3. Copy ALL parameter names exactly as they appear
4. Create extension function mapping only the commonly used parameters

**Template:**
```kotlin
// XApiExtensions.kt
package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.apis.XApi
import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedXNormalList

/**
 * Extension functions for XApi to provide cleaner, named-parameter interfaces
 * instead of the verbose generated API methods
 */

/**
 * Get X list with commonly used parameters
 */
suspend fun XApi.getXList(
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null,
    ordering: String? = null
    // Add other commonly used filters here
): HttpResponse<PaginatedXNormalList> = xList(
    // Map ALL parameters from generated API here
    // Set unused parameters to null
    // Only pass through the parameters defined above
    limit = limit,
    offset = offset,
    search = search,
    ordering = ordering,
    // ... all other parameters = null
)
```

**Reference:** See `AgenciesApiExtensions.kt` for complete example

---

### ✅ Step 4: Create or Update ViewModel

**File Location:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/`

**Option A: Create New ViewModel**

```kotlin
// XViewModel.kt
package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.XNormal
import me.calebjones.spacelaunchnow.data.repository.XRepository

class XViewModel(
    private val repository: XRepository
) : ViewModel() {

    private val _xs = MutableStateFlow<List<XNormal>>(emptyList())
    val xs: StateFlow<List<XNormal>> = _xs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchXs(limit: Int = 50, offset: Int = 0) {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true

            val result = repository.getXs(limit, offset)
            result.onSuccess { paginatedList ->
                _xs.value = paginatedList.results
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun searchXs(query: String, limit: Int = 50) {
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true

            val result = repository.searchXs(query, limit)
            result.onSuccess { paginatedList ->
                _xs.value = paginatedList.results
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }
}
```

**Option B: Extend Existing ViewModel** (if detail screen already exists)

Add list-related state and methods to existing ViewModel. See `AgencyViewModel.kt` for example.

---

### ✅ Step 5: Create List Screen Composable

**File Location:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/x/`

```kotlin
// XListScreen.kt
package me.calebjones.spacelaunchnow.ui.x

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.calebjones.spacelaunchnow.ui.x.compose.XListView
import me.calebjones.spacelaunchnow.ui.viewmodel.XViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun XListScreen(
    onNavigateToXDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<XViewModel>()
    val xs by viewModel.xs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        if (xs.isEmpty() && !isLoading && error == null) {
            viewModel.fetchXs(limit = 50)
        }
    }

    XListView(
        xs = xs,
        isLoading = isLoading,
        error = error,
        onXClick = onNavigateToXDetail,
        onNavigateBack = onNavigateBack,
        onRetry = { viewModel.fetchXs(limit = 50) }
    )
}
```

---

### ✅ Step 6: Create List View UI

**File Location:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/x/compose/`

**Complete Template:**

```kotlin
// XListView.kt
package me.calebjones.spacelaunchnow.ui.x.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Satellite // Choose appropriate icon
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import me.calebjones.spacelaunchnow.api.launchlibrary.models.XNormal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XListView(
    xs: List<XNormal>,
    isLoading: Boolean,
    error: String?,
    onXClick: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("X Plural Title") }, // e.g., "Space Stations", "Astronauts"
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                error != null -> {
                    ErrorContent(
                        errorMessage = error,
                        onRetry = onRetry
                    )
                }

                isLoading && xs.isEmpty() -> {
                    LoadingContent()
                }

                xs.isEmpty() -> {
                    EmptyContent()
                }

                else -> {
                    XList(
                        xs = xs,
                        onXClick = onXClick
                    )
                }
            }
        }
    }
}

@Composable
private fun XList(
    xs: List<XNormal>,
    onXClick: (Int) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(xs) { x ->
            XListItem(
                x = x,
                onClick = { onXClick(x.id) }
            )
        }
    }
}

@Composable
private fun XListItem(
    x: XNormal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image/Icon (use CircleShape for profiles, RoundedCornerShape for items)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape) // or RoundedCornerShape(8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                x.imageUrl?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "${x.name} image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: run {
                    Icon(
                        imageVector = Icons.Default.Satellite, // Choose appropriate
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Item info - customize based on data model
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = x.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Add relevant metadata fields
                x.description?.take(100)?.let { desc ->
                    Text(
                        text = desc + if (desc.length >= 100) "..." else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Add status badges or additional info
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Example: Show status, country, etc.
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error loading X",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Satellite,
            contentDescription = "No items",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No X found",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
    }
}
```

**Icon Suggestions:**
- Space Stations: `Icons.Default.Satellite` or custom rocket icon
- Astronauts: `Icons.Default.Person` or custom astronaut icon
- Programs: `Icons.Default.Flag` or `Icons.Default.Star`

---

### ✅ Step 7: Register Routes in Screen.kt

**File Location:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/navigation/Screen.kt`

**Add these serializable routes:**

```kotlin
@Serializable
data object Xs

@Serializable
data class XDetail(val xId: Int)
```

**Examples:**
```kotlin
@Serializable
data object SpaceStations

@Serializable
data class SpaceStationDetail(val spaceStationId: Int)

@Serializable
data object Astronauts

@Serializable
data class AstronautDetail(val astronautId: Int)

@Serializable
data object Programs

@Serializable
data class ProgramDetail(val programId: Int)
```

---

### ✅ Step 8: Register in Koin Dependency Injection

**File Location:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/AppModule.kt`

**Add imports:**
```kotlin
import me.calebjones.spacelaunchnow.data.repository.XRepository
import me.calebjones.spacelaunchnow.data.repository.XRepositoryImpl
```

**Register in module (around line 85):**
```kotlin
val appModule = module {
    // ... existing registrations ...
    
    viewModelOf(::XViewModel)
    singleOf(::XRepositoryImpl) { bind<XRepository>() }
}
```

**IMPORTANT:** Add them in logical order after existing ViewModels/Repositories.

---

### ✅ Step 9: Verify API Registration

**File Location:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/ApiModule.kt`

**Check if API is already registered:**

```kotlin
single<XApi> {
    XApi(
        baseUrl = get<String>(named("BaseUrl")),
        httpClientEngine = get<HttpClientEngine>(),
        httpClientConfig = httpClientConfig,
    ).apply {
        setApiKey(get<String>(named("API_KEY")), "Authorization")
        setApiKeyPrefix("Token", "Authorization")
    }
}
```

**If NOT registered, add it following the existing pattern.**

**Known registered APIs:**
- ✅ LaunchesApi
- ✅ AgenciesApi
- ✅ LauncherConfigurationsApi
- ❓ SpaceStationApi
- ❓ AstronautApi
- ❓ ProgramApi

---

### ✅ Step 10: Add to Settings Screen

**File Location:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/SettingsScreen.kt`

**Find the "EXPLORE" section (around line 235) and add:**

```kotlin
// EXPLORE
item {
    SectionHeaderText("Explore (WORK IN PROGRESS VIEWS)")
    Spacer(Modifier.height(2.dp))
    SettingsCardRow {
        Column(Modifier.fillMaxWidth()) {
            SettingsNavigationRow(
                title = "Rockets",
                subtitle = "Browse launcher configurations and details",
                onClick = { navController.navigate(me.calebjones.spacelaunchnow.navigation.Rockets) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingsNavigationRow(
                title = "Agencies",
                subtitle = "Explore space agencies and their missions",
                onClick = { navController.navigate(me.calebjones.spacelaunchnow.navigation.Agencies) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingsNavigationRow(
                title = "Space Stations",
                subtitle = "View active and historical space stations",
                onClick = { navController.navigate(me.calebjones.spacelaunchnow.navigation.SpaceStations) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingsNavigationRow(
                title = "Astronauts",
                subtitle = "Browse astronauts and cosmonauts",
                onClick = { navController.navigate(me.calebjones.spacelaunchnow.navigation.Astronauts) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingsNavigationRow(
                title = "Programs",
                subtitle = "Explore space programs and missions",
                onClick = { navController.navigate(me.calebjones.spacelaunchnow.navigation.Programs) }
            )
        }
    }
}
```

---

### ✅ Step 11: Add Navigation Routes (PhoneLayout)

**File Location:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/layout/phone/PhoneLayout.kt`

**Step 11.1: Add imports (around line 25-50):**

```kotlin
import me.calebjones.spacelaunchnow.navigation.Xs
import me.calebjones.spacelaunchnow.navigation.XDetail
import me.calebjones.spacelaunchnow.ui.x.XListScreen
import me.calebjones.spacelaunchnow.ui.detail.XDetailScreen
```

**Step 11.2: Hide bottom nav (around line 73):**

```kotlin
Xs::class.qualifiedName -> false // Hide for Xs list
```

**Step 11.3: Don't show interstitial ads (around line 91):**

```kotlin
currentRoute?.contains("Xs") != true &&
```

**Step 11.4: Add composable routes (around line 207-230):**

```kotlin
composableWithCompositionLocal<Xs> {
    XListScreen(
        onNavigateToXDetail = { id -> navController.navigate(XDetail(id)) },
        onNavigateBack = { navController.popBackStack() }
    )
}
composableWithCompositionLocal<XDetail> { backStackEntry ->
    val xDetail = backStackEntry.toRoute<XDetail>()
    XDetailScreen(
        xId = xDetail.xId,
        onNavigateBack = { navController.popBackStack() }
    )
}
```

---

### ✅ Step 12: Add Navigation Routes (TabletDesktopLayout)

**File Location:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/layout/desktop/TabletDesktopLayout.kt`

**Step 12.1: Add imports (around line 43-70):**

```kotlin
import me.calebjones.spacelaunchnow.navigation.Xs
import me.calebjones.spacelaunchnow.navigation.XDetail
import me.calebjones.spacelaunchnow.ui.x.XListScreen
import me.calebjones.spacelaunchnow.ui.detail.XDetailScreen
```

**Step 12.2: Add composable routes (around line 255-275):**

```kotlin
composableWithCompositionLocal<Xs> {
    XListScreen(
        onNavigateToXDetail = { id -> navController.navigate(XDetail(id)) },
        onNavigateBack = { navController.popBackStack() }
    )
}
composableWithCompositionLocal<XDetail> { backStackEntry ->
    val xDetail = backStackEntry.toRoute<XDetail>()
    XDetailScreen(
        xId = xDetail.xId,
        onNavigateBack = { navController.popBackStack() }
    )
}
```

---

## Feature-Specific Implementation Notes

### 🛰️ Space Stations

**API Model:** `SpaceStationNormal`  
**API Endpoint:** `SpaceStationApi.spaceStationList()`  
**Key Fields:**
- `name` - Station name
- `status` - Active/Inactive/Deorbited
- `orbit` - Orbit information
- `owners` - List of owning agencies
- `imageUrl` - Station image

**UI Considerations:**
- Use satellite icon
- Show orbit altitude
- Display owner flags/logos
- Badge for active status

**Ordering:** `"-id"` or `"name"`

---

### 👨‍🚀 Astronauts

**API Model:** `AstronautNormal`  
**API Endpoint:** `AstronautApi.astronautList()`  
**Key Fields:**
- `name` - Full name
- `nationality` - Country
- `profileImage` - Photo URL
- `agency` - Associated space agency
- `status` - Active/Retired/Deceased
- `flightsCount` - Number of missions

**UI Considerations:**
- Use circular profile images
- Show country flag
- Display mission count badge
- Active status indicator

**Ordering:** `"name"` or `"-flights_count"`

---

### 🚀 Programs

**API Model:** `ProgramNormal`  
**API Endpoint:** `ProgramApi.programList()`  
**Key Fields:**
- `name` - Program name
- `description` - Program description
- `startDate` - Program start
- `endDate` - Program end (if completed)
- `agencies` - Participating agencies
- `imageUrl` - Program logo/patch

**UI Considerations:**
- Use program logo/patch
- Show date range
- Display participating agencies
- Ongoing vs completed status

**Ordering:** `"-start_date"` or `"name"`

---

## Testing Checklist

After implementation, verify:

- [ ] List screen loads with spinner
- [ ] Data displays correctly in cards
- [ ] Images load (or placeholder shows)
- [ ] Tapping card navigates to detail screen
- [ ] Back button returns to list
- [ ] Error state shows with retry button
- [ ] Empty state displays when no results
- [ ] Settings navigation works
- [ ] Bottom nav hides on list screen (phone)
- [ ] Works on phone, tablet, desktop layouts
- [ ] No compilation errors
- [ ] Koin injection works correctly

---

## Common Pitfalls

### ❌ Don't:
1. **Guess API parameter names** - Always check generated API file
2. **Use wrong data model** - Use `XNormal` for lists, `XDetailed` for detail screens
3. **Forget Koin registration** - App will crash at runtime
4. **Miss platform layouts** - Must add to BOTH PhoneLayout AND TabletDesktopLayout
5. **Skip import organization** - Keep imports alphabetically sorted

### ✅ Do:
1. **Check existing API registration** in ApiModule.kt first
2. **Use extension functions** to wrap generated APIs
3. **Follow naming conventions** exactly (XRepository, XViewModel, etc.)
4. **Test on multiple screen sizes** after implementation
5. **Verify parameter names** match generated API exactly

---

## File Checklist Summary

For each feature (X = SpaceStation/Astronaut/Program):

```
✅ XRepository.kt
✅ XRepositoryImpl.kt
✅ XApiExtensions.kt
✅ XViewModel.kt (or extend existing)
✅ XListScreen.kt
✅ XListView.kt
✅ Screen.kt (add routes)
✅ AppModule.kt (register DI)
✅ ApiModule.kt (verify API registered)
✅ SettingsScreen.kt (add navigation button)
✅ PhoneLayout.kt (add routes)
✅ TabletDesktopLayout.kt (add routes)
```

**Total:** ~12 files modified/created per feature

---

## Completion Criteria

Feature is **DONE** when:

1. ✅ User can navigate from Settings → Feature List
2. ✅ List displays with proper UI (images, text, metadata)
3. ✅ Tapping item navigates to detail screen
4. ✅ Loading, error, and empty states work
5. ✅ No compilation errors
6. ✅ Works on all platforms (Android, iOS, Desktop)
7. ✅ Follows existing code patterns exactly

---

## Support & References

**Reference Files:**
- `AgencyListScreen.kt` - Complete working example
- `AgencyListView.kt` - UI patterns
- `AgenciesApiExtensions.kt` - API wrapping pattern
- `RocketListScreen.kt` - Alternative example

**API Documentation:**
- Launch Library 2.4.0: `composeApp/src/openApiLL/docs/`
- Generated models: `composeApp/src/openApiLL/src/commonMain/kotlin/me/calebjones/spacelaunchnow/api/launchlibrary/models/`

**Conventional Commits:**
```
feat(ui): add Space Stations list screen
feat(ui): add Astronauts list screen
feat(ui): add Programs list screen
```

---

**Document Version:** 1.0  
**Last Updated:** November 6, 2025  
**Maintainer:** AI Agents & Development Team

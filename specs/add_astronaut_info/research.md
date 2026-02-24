# Research: Astronaut Views Implementation

**Date**: 2026-01-29  
**Feature**: Astronaut List and Detail Views  
**Status**: Phase 0 - Complete

## Executive Summary

This document consolidates research findings for implementing astronaut browsing and viewing capabilities in SpaceLaunchNow KMP. All technical unknowns have been resolved through analysis of existing patterns and API capabilities.

---

## 1. API Structure and Capabilities

### Decision: Use AstronautsApi with Extension Functions
**Rationale**: Launch Library API 2.4.0 provides comprehensive astronaut endpoints via the generated `AstronautsApi` class.

**API Endpoints Available**:
```kotlin
// Generated methods (DO NOT use directly)
- astronautsList() → PaginatedAstronautEndpointNormalList
- astronautsDetailedList() → PaginatedAstronautEndpointDetailedList
- astronautsMiniList() → PaginatedAstronautEndpointListList
- astronautsRetrieve(id: Int) → AstronautEndpointDetailed
```

**Extension Functions Needed** (following LaunchesApiExtensions.kt pattern):
```kotlin
// Create: api/extensions/AstronautsApiExtensions.kt
suspend fun AstronautsApi.getAstronautList(
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null,
    statusIds: List<Int>? = null,
    agencyIds: List<Int>? = null,
    ordering: String? = null
): HttpResponse<PaginatedAstronautEndpointNormalList>

suspend fun AstronautsApi.getAstronautDetail(
    id: Int
): HttpResponse<AstronautEndpointDetailed>
```

**Alternatives Considered**:
- ❌ Direct API calls → 70+ parameters unmanageable
- ❌ Custom REST client → Redundant with OpenAPI generation
- ✅ Extension functions → Clean, maintainable, follows project pattern

---

## 2. Data Models and API Response Types

### Decision: Use Generated Models from OpenAPI
**Rationale**: All required models already exist in `api.launchlibrary.models` package.

**Key Models**:
```kotlin
// List items (normal detail level)
data class AstronautEndpointNormal(
    val id: Int,
    val name: String?,
    val status: AstronautStatus?,
    val agency: AgencyMini?,
    val image: Image?,
    val age: Int?,
    val bio: String,
    val type: AstronautType,
    val nationality: List<Country>
)

// Detail view (comprehensive data)
data class AstronautEndpointDetailed(
    // All fields from Normal, plus:
    val inSpace: Boolean?,
    val timeInSpace: String?,
    val evaTime: String?,
    val dateOfBirth: LocalDate?,
    val dateOfDeath: LocalDate?,
    val wiki: String?,
    val lastFlight: Instant?,
    val firstFlight: Instant?,
    val flightsCount: Int?,
    val landingsCount: Int?,
    val spacewalksCount: Int?,
    val lastWalk: Instant?,
    val lastLanding: Instant?,
    val flights: List<LaunchBasic>
)

// Supporting models
data class AstronautStatus(val id: Int, val name: String)
data class AstronautType(val id: Int, val name: String)
data class AstronautFlight(val id: Int, val role: AstronautRole?, val astronaut: AstronautNormal)
```

**Model Usage Strategy**:
- **List Screen**: `AstronautEndpointNormal` (sufficient detail, smaller payload)
- **Detail Screen**: `AstronautEndpointDetailed` (comprehensive data)
- **Launch Detail Cards**: `AstronautNormal` (already embedded in spacecraft flight data)

**Alternatives Considered**:
- ❌ Custom DTOs → Unnecessary mapping overhead
- ❌ Mini list → Insufficient data for good UX
- ✅ Generated models → Type-safe, complete, maintained by OpenAPI

---

## 3. Navigation Architecture

### Decision: Type-Safe Navigation with Kotlin Serialization
**Rationale**: Project uses modern Compose Navigation with serializable routes.

**Implementation**:
```kotlin
// Add to navigation/Screen.kt
@Serializable
data object Astronauts

@Serializable
data class AstronautDetail(val astronautId: Int)
```

**Navigation Flows**:
1. Settings → Explore → Astronauts (list)
2. Astronauts (list) → tap card → AstronautDetail(id)
3. Launch Detail → Crew card → AstronautDetail(id)
4. Event Detail → AstronautsCard → tap → AstronautDetail(id)

**Deep Linking Support**:
```
spacelaunchnow://astronaut/{astronautId}
```

**Alternatives Considered**:
- ❌ String-based routes → Type-unsafe, error-prone
- ❌ Legacy Screen sealed class → Not for dynamic IDs
- ✅ Serializable data classes → Type-safe, modern, consistent

---

## 4. Repository Pattern

### Decision: Follow Existing Repository Architecture
**Rationale**: Maintain consistency with `LaunchRepository`, `AgencyRepository` patterns.

**Repository Interface**:
```kotlin
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

**Implementation**:
```kotlin
class AstronautRepositoryImpl(
    private val astronautsApi: AstronautsApi
) : AstronautRepository {
    override suspend fun getAstronauts(...): Result<...> {
        return try {
            val response = astronautsApi.getAstronautList(...)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        }
    }
}
```

**Alternatives Considered**:
- ❌ Direct ViewModel → API calls → No abstraction for testing
- ❌ UseCase layer → Overkill for simple CRUD operations
- ✅ Repository pattern → Testable, mockable, consistent

---

## 5. ViewModel State Management

### Decision: StateFlow with Sealed State Classes
**Rationale**: Follows project patterns from `NextUpViewModel`, `AgenciesViewModel`.

**List ViewModel**:
```kotlin
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
    
    fun loadAstronauts() { /* ... */ }
    fun loadMore() { /* ... */ }
    fun refresh() { /* ... */ }
}
```

**Detail ViewModel**:
```kotlin
data class AstronautDetailUiState(
    val astronaut: AstronautEndpointDetailed? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AstronautDetailViewModel(
    private val repository: AstronautRepository,
    private val astronautId: Int
) : ViewModel() {
    private val _uiState = MutableStateFlow(AstronautDetailUiState())
    val uiState: StateFlow<AstronautDetailUiState> = _uiState.asStateFlow()
    
    init { loadAstronautDetail() }
    
    private fun loadAstronautDetail() { /* ... */ }
}
```

**Alternatives Considered**:
- ❌ LiveData → Not multiplatform-friendly
- ❌ Channel/SharedFlow → Unnecessary complexity for state
- ✅ StateFlow → Multiplatform, reactive, collect safely

---

## 6. UI Component Architecture

### Decision: Reusable Component Pattern with SharedDetailScaffold
**Rationale**: Maximize component reuse, maintain design consistency.

**Component Hierarchy**:
```
ui/astronaut/
├── AstronautListScreen.kt           # Main list screen (LazyColumn)
├── AstronautDetailView.kt           # Detail screen (SharedDetailScaffold)
└── components/
    ├── AstronautCard.kt             # List item (vertical card)
    ├── AstronautProfileCard.kt      # Horizontal mini card (launch detail)
    ├── AstronautInfoCard.kt         # Biography/info section
    ├── AstronautStatsCard.kt        # Career statistics
    └── AstronautFlightHistoryCard.kt # Mission history
```

**Shared Components to Reuse**:
- `SharedDetailScaffold` → Header with parallax image
- `InfoTile` → Statistics display
- `StatusChip` → Astronaut status badges
- `AgencyChip` → Agency affiliation
- `AsyncImage` (Coil) → Profile images with caching
- `SmartBannerAd` → Ad placements

**Alternatives Considered**:
- ❌ Monolithic screens → Hard to test, no reusability
- ❌ Custom scaffold → Reinventing existing patterns
- ✅ Component composition → Reusable, testable, consistent

---

## 7. Image Handling and Caching

### Decision: Use Coil with AsyncImage
**Rationale**: Already integrated, handles caching, multiplatform support.

**Implementation Pattern**:
```kotlin
AsyncImage(
    model = astronaut.image?.imageUrl,
    contentDescription = astronaut.name,
    modifier = Modifier.size(64.dp).clip(CircleShape),
    contentScale = ContentScale.Crop,
    placeholder = painterResource(Res.drawable.astronaut_placeholder),
    error = painterResource(Res.drawable.astronaut_placeholder)
)
```

**Placeholder Strategy**:
- Use generic astronaut icon for missing/failed images
- Circular clipping for profile images
- Maintain aspect ratio for detail view hero image

**Alternatives Considered**:
- ❌ Custom image loader → Reinventing Coil
- ❌ Kamel (KMP image library) → Coil already integrated
- ✅ Coil AsyncImage → Proven, cached, multiplatform

---

## 8. Pagination Strategy

### Decision: Infinite Scroll with Load More Trigger
**Rationale**: Standard mobile pattern, good UX for browsing long lists.

**Implementation**:
```kotlin
LazyColumn {
    items(astronauts) { astronaut ->
        AstronautCard(
            astronaut = astronaut,
            onClick = { navigateToDetail(astronaut.id) }
        )
    }
    
    // Load more trigger
    item {
        if (hasMore && !isLoadingMore) {
            LaunchedEffect(Unit) {
                viewModel.loadMore()
            }
        }
        if (isLoadingMore) {
            LoadingIndicator()
        }
    }
}
```

**Page Size**: 20 items per page (balances performance and UX)

**Alternatives Considered**:
- ❌ Manual "Load More" button → Extra tap required
- ❌ Paging 3 library → Overkill for simple pagination
- ✅ LaunchedEffect trigger → Simple, works well

---

## 9. Astronaut Profile Card Integration

### Decision: Horizontal Compact Card for Launch Details
**Rationale**: Space-efficient, non-intrusive, clickable for more info.

**Placement Options**:
1. **Spacecraft Tab** (preferred) → Alongside spacecraft flight data
2. **Overview Tab** → In dedicated "Crew" section
3. **Mission Tab** → If mission is crewed

**Component Design**:
```kotlin
@Composable
fun AstronautProfileCard(
    astronaut: AstronautNormal,
    role: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar (48dp circle)
            AsyncImage(
                model = astronaut.image?.imageUrl,
                contentDescription = astronaut.name,
                modifier = Modifier.size(48.dp).clip(CircleShape)
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(astronaut.name ?: "Unknown", style = MaterialTheme.typography.titleSmall)
                Text(role ?: "Crew", style = MaterialTheme.typography.bodySmall)
            }
            
            // Agency logo/badge
            astronaut.agency?.logo?.let { logo ->
                AsyncImage(
                    model = logo.imageUrl,
                    contentDescription = astronaut.agency.name,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
```

**Alternatives Considered**:
- ❌ Full-height vertical cards → Takes too much space
- ❌ Just names as text → Misses opportunity for rich UI
- ✅ Horizontal compact cards → Space-efficient, informative

---

## 10. Testing Strategy

### Decision: Layered Testing Approach
**Rationale**: Comprehensive coverage without excessive test burden.

**Test Layers**:
```
tests/
├── unit/
│   ├── AstronautRepositoryTest.kt      # Mock API calls
│   └── AstronautViewModelTest.kt       # State transitions
├── integration/
│   └── AstronautApiIntegrationTest.kt  # Real API calls (CI)
└── ui/
    ├── AstronautListScreenTest.kt      # Navigation, loading states
    └── AstronautDetailViewTest.kt      # Content display
```

**Testing Priorities**:
1. ✅ Repository error handling (network failures)
2. ✅ ViewModel state transitions (loading → success → error)
3. ✅ Navigation flows (list → detail → back)
4. ⚠️ UI rendering (smoke tests only, full E2E optional)

**Alternatives Considered**:
- ❌ No tests → Risky for mission-critical features
- ❌ 100% coverage → Diminishing returns
- ✅ Focus on critical paths → Pragmatic coverage

---

## 11. Koin Dependency Injection

### Decision: Follow Existing Module Pattern
**Rationale**: Maintain consistency with current DI setup.

**Module Registration**:
```kotlin
// Add to AppModule.kt
val appModule = module {
    // ViewModels
    viewModelOf(::AstronautListViewModel)
    viewModelOf(::AstronautDetailViewModel)
    
    // Repositories
    singleOf(::AstronautRepositoryImpl) { bind<AstronautRepository>() }
}
```

**ViewModel Injection with Parameters**:
```kotlin
// For AstronautDetailViewModel with ID parameter
composable<AstronautDetail> { backStackEntry ->
    val args = backStackEntry.toRoute<AstronautDetail>()
    val viewModel: AstronautDetailViewModel = koinViewModel { 
        parametersOf(args.astronautId) 
    }
    AstronautDetailView(viewModel, navController)
}
```

**Alternatives Considered**:
- ❌ Manual instantiation → No testability
- ❌ Dagger/Hilt → Not KMP-compatible
- ✅ Koin → Multiplatform, simple, already integrated

---

## 12. Error Handling and Edge Cases

### Decision: Comprehensive Error States
**Rationale**: Graceful degradation improves user experience.

**Error Scenarios**:
```kotlin
sealed class AstronautError {
    data object NetworkError : AstronautError()
    data object NotFound : AstronautError()
    data class ServerError(val code: Int) : AstronautError()
    data class Unknown(val message: String) : AstronautError()
}
```

**UI Error States**:
- **Network Error** → Retry button with offline indicator
- **Empty List** → "No astronauts found" with icon
- **Image Load Failure** → Placeholder astronaut icon
- **API Error** → Generic error message with retry

**Alternatives Considered**:
- ❌ Silent failures → Poor UX
- ❌ Toast messages → Easy to miss
- ✅ Inline error states → Clear, actionable

---

## 13. Performance Considerations

### Decision: Lazy Loading with Caching
**Rationale**: Optimize for smooth scrolling and reduced network calls.

**Optimizations**:
1. **Pagination**: Load 20 items at a time
2. **Image Caching**: Coil handles disk/memory cache
3. **LazyColumn**: Only renders visible items
4. **debounce**: Search queries debounced 300ms
5. **State Restoration**: Preserve scroll position on back navigation

**Memory Budget**:
- List item: ~2KB (text data) + ~50KB (cached image) = ~52KB
- 20 items visible: ~1MB working set
- Coil image cache: ~10MB disk limit

**Alternatives Considered**:
- ❌ Load all at once → Poor performance, high memory
- ❌ No caching → Excessive network usage
- ✅ Lazy + cached → Smooth, efficient

---

## 14. Accessibility Standards

### Decision: WCAG AA Compliance
**Rationale**: Inclusive design expands user base, required by platform guidelines.

**Implementation Checklist**:
- ✅ Content descriptions for all images
- ✅ Semantic roles for screen readers
- ✅ Minimum touch targets: 48dp
- ✅ Contrast ratio: 4.5:1 for text
- ✅ Keyboard navigation (desktop)
- ✅ Focus indicators

**Example**:
```kotlin
AsyncImage(
    model = astronaut.image?.imageUrl,
    contentDescription = "Profile photo of ${astronaut.name}",
    modifier = Modifier.semantics {
        role = Role.Image
        contentDescription = "Astronaut profile picture"
    }
)
```

**Alternatives Considered**:
- ❌ Ignore accessibility → Excludes users, violates guidelines
- ❌ Minimal descriptions → Poor screen reader experience
- ✅ Comprehensive labels → Inclusive, compliant

---

## Summary of Key Decisions

| Aspect | Decision | Rationale |
|--------|----------|-----------|
| API Integration | Extension functions over generated API | Clean interface, follows project pattern |
| Data Models | Use generated OpenAPI models | Type-safe, maintained, no mapping overhead |
| Navigation | Serializable data classes | Type-safe, modern Compose Navigation |
| Repository | Result<T> pattern with try/catch | Testable, consistent error handling |
| ViewModel | StateFlow with data classes | Multiplatform, reactive, composable-friendly |
| UI Architecture | SharedDetailScaffold + components | Reusable, consistent, maintainable |
| Images | Coil AsyncImage with placeholders | Cached, multiplatform, handles failures |
| Pagination | Infinite scroll (20/page) | Standard mobile UX, performant |
| Profile Cards | Horizontal compact design | Space-efficient for launch details |
| Testing | Layered (unit → integration → UI) | Pragmatic coverage of critical paths |
| DI | Koin modules | Multiplatform, simple, integrated |
| Errors | Inline error states with retry | Clear feedback, actionable |
| Performance | Lazy loading + Coil caching | Smooth scrolling, reduced network |
| Accessibility | WCAG AA compliance | Inclusive, platform-compliant |

---

## Technical Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|----------|
| API rate limiting | High | Implement exponential backoff, cache results |
| Large astronaut list | Medium | Pagination limits to 20 items, lazy loading |
| Image load failures | Low | Graceful fallback to placeholder icons |
| Memory pressure | Low | Coil manages cache size, LazyColumn recycles |
| Network unreliability | High | Offline error states, retry mechanisms |

---

## Next Steps (Phase 1)

1. **Create API Extension Functions** (`api/extensions/AstronautsApiExtensions.kt`)
2. **Define Data Model** (`data-model.md`) - Document entity relationships
3. **Generate API Contracts** (`contracts/`) - OpenAPI subset for astronauts
4. **Create Quickstart Guide** (`quickstart.md`) - Implementation walkthrough
5. **Update Agent Context** - Add astronaut patterns to Copilot instructions

---

**Research Complete**: All technical unknowns resolved. Ready to proceed to Phase 1 (Design & Contracts).

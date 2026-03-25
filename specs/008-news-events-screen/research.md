# Research: News and Events Screen

**Feature**: 008-news-events-screen  
**Date**: 2026-03-23  
**Status**: Complete

## Technical Context Resolution

### Language/Version
- **Decision**: Kotlin 2.0.21 with Kotlin Multiplatform
- **Rationale**: Project uses KMP with Compose Multiplatform, targeting Android, iOS, and Desktop
- **Alternatives Considered**: None - project architecture is established

### Primary Dependencies
- **Decision**: Use existing APIs and repositories:
  - **News**: SNAPI v4 (`ArticlesApi`, `ArticlesRepository`)
  - **Events**: Launch Library 2.4.0 (`EventsApi`, `EventsRepository`)
  - **Config**: Launch Library `ConfigApi` for event types
- **Rationale**: Repositories and API extensions already exist and follow established patterns
- **Alternatives Considered**: None - existing infrastructure is sufficient

### UI Framework
- **Decision**: Compose Multiplatform with Material3
- **Rationale**: Project standard; AgencyListScreen provides canonical pattern
- **Reference**: `ui/agencies/AgencyListScreen.kt` - complete working example with pagination, filtering, search

### State Management
- **Decision**: ViewModel with StateFlow, UiState data class pattern
- **Rationale**: Matches `AgencyListViewModel` pattern with `MutableStateFlow<UiState>`
- **Reference**: `ui/viewmodel/AgencyListViewModel.kt` - mutex-based load protection, pagination

### Filter Options Sources
| Filter Type | API Source | Endpoint/Method |
|------------|------------|-----------------|
| Event Types | Launch Library ConfigApi | `configEventTypesList()` |
| News Sites | SNAPI InfoApi | `infoRetrieve()` returns `news_sites: String[]` |

### Navigation Pattern
- **Decision**: Use existing `NavController` with route-based navigation
- **Articles**: Open external browser via `LocalUriHandler`
- **Events**: Navigate to `EventDetail(eventId)` route
- **Reference**: `App.kt` composable entries for event detail

### Caching Strategy
- **Decision**: Repository-level caching with DataResult pattern
- **Rationale**: Existing pattern in `ArticlesRepositoryImpl` and `EventsRepositoryImpl`
- **Pattern**: `Result<DataResult<T>>` with `DataSource.CACHE`, `NETWORK`, `STALE_CACHE`

## Component Patterns

### Tabbed Interface
- **Decision**: Use `TabRow` with `Tab` components (Material3)
- **Rationale**: Standard pattern for segmented content in Compose
- **Reference**: See Android documentation for Tab components

### Search & Filter UI
- **Existing Components**:
  - `SearchBar.kt` - Reusable search input with clear button
  - `FilterChip.kt` - Active filter display chip
  - `ScheduleFilterBottomSheet.kt` - Modal filter sheet pattern
- **Decision**: Reuse existing components; create NewsEventsFilterBottomSheet following ScheduleFilterBottomSheet pattern

### List Items
- **Existing Components**:
  - `ArticleItem` in `ArticlesView.kt`
  - `EventItem` in `EventsView.kt`
- **Decision**: Extract and enhance existing item composables for full-screen list use

### Empty/Error States
- **Decision**: Follow AgencyListScreen pattern
- **Components**: `Icon`, `Text`, `Button` (retry) in centered Column
- **Reference**: AgencyListScreen lines 200-250

## ViewModel Design

### UiState Structure
```kotlin
data class NewsEventsUiState(
    // Tab selection
    val selectedTab: NewsEventsTab = NewsEventsTab.NEWS,
    
    // News state
    val articles: List<Article> = emptyList(),
    val isLoadingNews: Boolean = false,
    val newsError: String? = null,
    val hasMoreNews: Boolean = true,
    
    // Events state  
    val events: List<EventEndpointNormal> = emptyList(),
    val isLoadingEvents: Boolean = false,
    val eventsError: String? = null,
    val hasMoreEvents: Boolean = true,
    
    // Filters
    val searchQuery: String = "",
    val selectedNewsSites: List<String> = emptyList(),
    val selectedEventTypes: List<Int> = emptyList(),
    val showUpcomingEvents: Boolean = true,
    
    // Filter options (loaded from API)
    val availableNewsSites: List<String> = emptyList(),
    val availableEventTypes: List<EventType> = emptyList()
)

enum class NewsEventsTab {
    NEWS, EVENTS
}
```

### Pagination Pattern
```kotlin
companion object {
    private const val PAGE_SIZE = 20
}

private var newsOffset = 0
private var eventsOffset = 0

fun loadMoreNews() {
    if (isLoadingNews || !hasMoreNews) return
    // increment offset, fetch, append to list
}
```

## API Extension Requirements

### Existing Extensions (No Changes Needed)
- `EventsApiExtensions.kt` - `getEventList()`, `getUpcomingEvents()`, `getEventsByType()`
- `ArticlesApiExtensions.kt` - `getArticles()`, `getFeaturedArticles()`, `searchArticles()`

### New Extension Needed
Add offset parameter to ArticlesApiExtensions for pagination:
```kotlin
suspend fun ArticlesApi.getArticles(
    limit: Int? = null,
    offset: Int? = null,  // ADD THIS
    search: String? = null,
    newsSite: String? = null, // ADD THIS
    // ... existing params
)
```

## Performance Considerations

### Debouncing
- **Decision**: Debounce search input by 300ms before API call
- **Implementation**: `debounce()` operator in ViewModel flow

### Image Loading
- **Decision**: Use Coil3 with SubcomposeAsyncImage
- **Rationale**: Project standard, handles placeholders and errors
- **Reference**: Existing ArticleItem and EventItem implementations

### Pagination Trigger
- **Decision**: Trigger load when 5 items from bottom (same as AgencyListScreen)
- **Implementation**: `derivedStateOf` with LazyListState

## Files to Create

| File | Purpose |
|------|---------|
| `ui/newsevents/NewsEventsScreen.kt` | Main screen with tabs |
| `ui/newsevents/NewsEventsFilterBottomSheet.kt` | Filter UI |
| `ui/newsevents/components/NewsListItem.kt` | Full-screen news item |
| `ui/newsevents/components/EventListItem.kt` | Full-screen event item |
| `ui/viewmodel/NewsEventsViewModel.kt` | Combined state management |

## Files to Modify

| File | Changes |
|------|---------|
| `ArticlesApiExtensions.kt` | Add offset and newsSite params |
| `ArticlesRepository.kt` | Add newsSite filter param |
| `ArticlesRepositoryImpl.kt` | Implement newsSite filter |
| `App.kt` | Add NewsEvents navigation route |
| `di/AppModule.kt` | Register NewsEventsViewModel |

## Risks and Mitigations

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| API rate limiting | Medium | High | Use caching, show stale data on error |
| Large news site list | Low | Low | Lazy load filter options, consider top N common sites |
| Tab state loss | Medium | Medium | Persist tab selection in ViewModel |

## Open Questions (Resolved)

| Question | Resolution |
|----------|------------|
| Where to get news site list? | SNAPI `/v4/info/` endpoint returns `news_sites` array |
| Where to get event types? | LL2 `ConfigApi.configEventTypesList()` |
| Combine or separate ViewModels? | Single combined ViewModel for simplicity |

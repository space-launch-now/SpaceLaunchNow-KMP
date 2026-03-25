# Contracts: News and Events Screen

**Feature**: 008-news-events-screen  
**Date**: 2025-01-23

## Overview

This document defines the interface contracts and API extension changes required for the News and Events screen feature.

## Repository Interface Changes

### ArticlesRepository (Modifications)

Add pagination support with offset and news site filtering:

```kotlin
// File: data/repository/ArticlesRepository.kt
interface ArticlesRepository {
    // Existing methods remain unchanged...
    
    /**
     * NEW: Get articles with pagination and filtering support
     * @param limit Number of articles per page
     * @param offset Pagination offset
     * @param search Search query (optional)
     * @param newsSites Filter by news site names (optional, OR logic)
     * @param forceRefresh Force network fetch
     */
    suspend fun getArticlesPaginated(
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null,
        newsSites: List<String>? = null,
        forceRefresh: Boolean = false
    ): Result<DataResult<PaginatedArticleList>>
}
```

### EventsRepository (Modifications)

Add pagination offset and search support:

```kotlin
// File: data/repository/EventsRepository.kt
interface EventsRepository {
    // Existing methods remain unchanged...
    
    /**
     * NEW: Get events with full pagination and filtering support
     * @param limit Number of events per page
     * @param offset Pagination offset
     * @param search Search query (optional)
     * @param typeIds Filter by event type IDs (optional, OR logic)
     * @param upcoming True for upcoming, False for past, null for all
     * @param forceRefresh Force network fetch
     */
    suspend fun getEventsPaginated(
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null,
        typeIds: List<Int>? = null,
        upcoming: Boolean? = true,
        forceRefresh: Boolean = false
    ): Result<DataResult<PaginatedEventEndpointNormalList>>
}
```

### InfoRepository (New)

New repository for SNAPI info endpoint:

```kotlin
// File: data/repository/InfoRepository.kt
package me.calebjones.spacelaunchnow.data.repository

interface InfoRepository {
    /**
     * Get SNAPI info including available news sites
     * @return List of news site names
     */
    suspend fun getNewsSites(): Result<List<String>>
}
```

### ConfigRepository (Existing - No Changes)

The existing ConfigRepository already supports event types:

```kotlin
// Already exists: data/repository/ConfigRepository.kt
interface ConfigRepository {
    suspend fun getEventTypes(): Result<List<EventType>>
    // ... other methods
}
```

## API Extension Changes

### ArticlesApiExtensions.kt

```kotlin
// File: api/extensions/ArticlesApiExtensions.kt

/**
 * Get articles with pagination and filtering
 */
suspend fun ArticlesApi.getArticlesPaginated(
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null,
    newsSite: String? = null,  // Single site filter (API limitation)
    ordering: String? = "-published_at"
): HttpResponse<PaginatedArticleList> {
    return articlesListOriginalFormat(
        limit = limit,
        offset = offset,
        search = search,
        newsSite = newsSite,
        ordering = ordering,
        // ... other params as null
    )
}
```

### EventsApiExtensions.kt

```kotlin
// File: api/extensions/EventsApiExtensions.kt

/**
 * Get events with pagination and filtering
 */
suspend fun EventsApi.getEventsPaginated(
    limit: Int? = null,
    offset: Int? = null,
    search: String? = null,
    typeIds: List<Int>? = null,
    upcoming: Boolean? = null,
    ordering: String? = "date"
): HttpResponse<PaginatedEventEndpointNormalList> {
    return eventsNormalList(
        limit = limit,
        offset = offset,
        search = search,
        typeIds = typeIds?.map { it.toString() },
        upcoming = upcoming,
        ordering = ordering,
        // ... other params as null
    )
}
```

### InfoApiExtensions.kt (New)

```kotlin
// File: api/extensions/InfoApiExtensions.kt
package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.snapi.apis.InfoApi
import me.calebjones.spacelaunchnow.api.snapi.models.Info

/**
 * Get SNAPI service info including news sites list
 */
suspend fun InfoApi.getInfo(): Info {
    return infoRetrieve().body()
}
```

## ViewModel Interface

### NewsEventsViewModel

```kotlin
// File: ui/viewmodel/NewsEventsViewModel.kt
class NewsEventsViewModel(
    private val articlesRepository: ArticlesRepository,
    private val eventsRepository: EventsRepository,
    private val configRepository: ConfigRepository,
    private val infoRepository: InfoRepository
) : ViewModel() {
    
    val uiState: StateFlow<NewsEventsUiState>
    
    // Tab management
    fun selectTab(tab: NewsEventsTab)
    
    // Search
    fun updateSearchQuery(query: String)
    fun clearSearch()
    
    // News filters
    fun toggleNewsSite(site: String)
    fun clearNewsFilters()
    
    // Events filters
    fun toggleEventType(typeId: Int)
    fun toggleUpcomingEvents()
    fun clearEventFilters()
    
    // Pagination
    fun loadMoreNews()
    fun loadMoreEvents()
    
    // Refresh
    fun refresh()
    
    // Filter sheet
    fun showFilterSheet()
    fun hideFilterSheet()
    fun applyFilters()
}
```

## Screen Navigation Contract

### Route Definition

```kotlin
// In navigation/AppNavigation.kt or Screen.kt
sealed class Screen {
    // Existing screens...
    
    data object NewsEvents : Screen()
}
```

### Navigation Entry

```kotlin
// In App.kt composable
composable<Screen.NewsEvents> {
    val viewModel: NewsEventsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    NewsEventsScreen(
        uiState = uiState,
        onTabSelected = viewModel::selectTab,
        onSearchQueryChanged = viewModel::updateSearchQuery,
        onArticleClick = { article ->
            // Open URL in browser
        },
        onEventClick = { event ->
            navController.navigate(Screen.EventDetail(event.id))
        },
        onFilterClick = viewModel::showFilterSheet,
        onLoadMore = {
            when (uiState.selectedTab) {
                NewsEventsTab.NEWS -> viewModel.loadMoreNews()
                NewsEventsTab.EVENTS -> viewModel.loadMoreEvents()
            }
        }
    )
}
```

## Component Contracts

### NewsEventsScreen

```kotlin
@Composable
fun NewsEventsScreen(
    uiState: NewsEventsUiState,
    onTabSelected: (NewsEventsTab) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onArticleClick: (Article) -> Unit,
    onEventClick: (EventEndpointNormal) -> Unit,
    onFilterClick: () -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
)
```

### NewsEventsFilterBottomSheet

```kotlin
@Composable
fun NewsEventsFilterBottomSheet(
    selectedTab: NewsEventsTab,
    // News filter state
    availableNewsSites: List<String>,
    selectedNewsSites: Set<String>,
    onNewsSiteToggle: (String) -> Unit,
    // Events filter state
    availableEventTypes: List<EventType>,
    selectedEventTypes: Set<Int>,
    onEventTypeToggle: (Int) -> Unit,
    showUpcomingEvents: Boolean,
    onUpcomingToggle: () -> Unit,
    // Actions
    onApply: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
)
```

### NewsListItem

```kotlin
@Composable
fun NewsListItem(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

### EventListItem

```kotlin
@Composable
fun EventListItem(
    event: EventEndpointNormal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

## Dependency Injection Registration

```kotlin
// In di/AppModule.kt

// ViewModel
viewModelOf(::NewsEventsViewModel)

// Repository (if new InfoRepository needed)
singleOf(::InfoRepositoryImpl) { bind<InfoRepository>() }
```

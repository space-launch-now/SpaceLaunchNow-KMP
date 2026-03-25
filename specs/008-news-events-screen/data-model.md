# Data Model: News and Events Screen

**Feature**: 008-news-events-screen  
**Date**: 2025-01-23

## Domain Entities

### Existing Entities (No Changes)

#### Article (from SNAPI)
```kotlin
// Generated in: me.calebjones.spacelaunchnow.api.snapi.model.Article
data class Article(
    val id: Int,
    val title: String,
    val url: String,
    val imageUrl: String?,
    val newsSite: String,
    val summary: String,
    val publishedAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val featured: Boolean,
    val launches: List<Launch>?,
    val events: List<Event>?
)
```

#### EventEndpointNormal (from LL2)
```kotlin
// Generated in: me.calebjones.spacelaunchnow.api.ll.model.EventEndpointNormal  
data class EventEndpointNormal(
    val id: Int,
    val url: String?,
    val slug: String,
    val name: String,
    val updates: List<Update>?,
    val type: EventType?,
    val description: String?,
    val webcastLive: Boolean?,
    val location: String?,
    val newsUrl: String?,
    val videoUrl: String?,
    val featureImage: String?,
    val date: OffsetDateTime?,
    val datePrecision: DatePrecision?,
    val duration: String?,
    val agencies: List<AgencyNormal>?,
    val launches: List<LaunchBasic>?,
    val expeditions: List<Expedition>?,
    val spacestations: List<SpaceStationMinimal>?,
    val program: List<ProgramBasic>?
)
```

#### EventType (from LL2)
```kotlin
// Generated in: me.calebjones.spacelaunchnow.api.ll.model.EventType
data class EventType(
    val id: Int,
    val name: String,
    val description: String?
)
```

## UI State Models (New)

### NewsEventsUiState
```kotlin
// File: ui/viewmodel/NewsEventsViewModel.kt

data class NewsEventsUiState(
    // Tab selection
    val selectedTab: NewsEventsTab = NewsEventsTab.NEWS,
    
    // News (Articles) state
    val articles: List<Article> = emptyList(),
    val isLoadingNews: Boolean = false,
    val newsError: String? = null,
    val hasMoreNews: Boolean = true,
    
    // Events state
    val events: List<EventEndpointNormal> = emptyList(),
    val isLoadingEvents: Boolean = false,
    val eventsError: String? = null,
    val hasMoreEvents: Boolean = true,
    
    // Shared filters
    val searchQuery: String = "",
    
    // News-specific filters
    val selectedNewsSites: Set<String> = emptySet(),
    
    // Events-specific filters
    val selectedEventTypes: Set<Int> = emptySet(),
    val showUpcomingEvents: Boolean = true,
    
    // Filter options (loaded from API)
    val availableNewsSites: List<String> = emptyList(),
    val availableEventTypes: List<EventType> = emptyList(),
    
    // UI state
    val showFilterSheet: Boolean = false
) {
    val isLoading: Boolean
        get() = when (selectedTab) {
            NewsEventsTab.NEWS -> isLoadingNews
            NewsEventsTab.EVENTS -> isLoadingEvents
        }
    
    val currentError: String?
        get() = when (selectedTab) {
            NewsEventsTab.NEWS -> newsError
            NewsEventsTab.EVENTS -> eventsError
        }
    
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotBlank() ||
                selectedNewsSites.isNotEmpty() ||
                selectedEventTypes.isNotEmpty() ||
                !showUpcomingEvents
    
    val activeFilterCount: Int
        get() = when (selectedTab) {
            NewsEventsTab.NEWS -> selectedNewsSites.size + if (searchQuery.isNotBlank()) 1 else 0
            NewsEventsTab.EVENTS -> selectedEventTypes.size + 
                if (!showUpcomingEvents) 1 else 0 +
                if (searchQuery.isNotBlank()) 1 else 0
        }
}
```

### NewsEventsTab
```kotlin
enum class NewsEventsTab {
    NEWS,
    EVENTS;
    
    val displayName: String
        get() = when (this) {
            NEWS -> "News"
            EVENTS -> "Events"
        }
}
```

### FilterState (Internal ViewModel State)
```kotlin
// Used for filter bottom sheet state management
data class NewsEventsFilterState(
    // News filters (temporary state in sheet)
    val tempNewsSites: Set<String> = emptySet(),
    
    // Events filters (temporary state in sheet)
    val tempEventTypes: Set<Int> = emptySet(),
    val tempShowUpcoming: Boolean = true
)
```

## Relationships

```
NewsEventsUiState
├── selectedTab: NewsEventsTab
├── articles: List<Article>
│   └── Article (existing SNAPI model)
├── events: List<EventEndpointNormal>
│   └── EventEndpointNormal (existing LL2 model)
├── availableNewsSites: List<String>
│   └── (from SNAPI Info endpoint)
└── availableEventTypes: List<EventType>
    └── EventType (existing LL2 model)
```

## Validation Rules

### Search Query
- Max length: 100 characters
- Trimmed before API call
- Empty string = no search filter

### News Sites Filter
- Must be valid news site from availableNewsSites
- Multiple selection allowed (OR logic)
- Empty set = all news sites

### Event Types Filter
- Must be valid EventType.id from availableEventTypes
- Multiple selection allowed (OR logic)
- Empty set = all event types

### Pagination
- PAGE_SIZE = 20 (matches AgencyListScreen)
- offset increments by PAGE_SIZE
- hasMore = response.count > current list size

## State Transitions

### Tab Selection
```
User taps tab → selectedTab changes → Display corresponding list
                                    → Keep other tab's data cached
```

### Search Flow
```
User types → Debounce 300ms → Clear current list
                            → Reset offset to 0
                            → Fetch with search param
                            → Update state
```

### Filter Application
```
User opens filter sheet → Show current filters
User modifies filters → Update temp state only
User taps Apply → Copy temp to actual filters
                → Clear current list
                → Reset offset to 0
                → Fetch with new filters
                → Dismiss sheet
User taps Clear → Reset all filters to default
                → Clear and refetch
```

### Pagination
```
User scrolls near bottom → Check hasMore
                        → Check not isLoading
                        → Increment offset
                        → Fetch more
                        → Append to list
```

### Error Handling
```
API error → Show error state with retry button
User taps retry → Clear error
               → Refetch with same params
```

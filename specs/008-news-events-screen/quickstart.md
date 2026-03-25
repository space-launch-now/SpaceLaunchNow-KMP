# Quickstart: News and Events Screen

**Feature**: 008-news-events-screen  
**Date**: 2025-01-23

## Prerequisites

1. Java 21 (JetBrains JDK recommended)
2. Generated API clients (`./gradlew openApiGenerate`)
3. Valid `.env` file with `API_KEY`

## Implementation Order

### Phase 1: API Layer Updates

1. **Add InfoApiExtensions.kt**
   ```
   composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/api/extensions/InfoApiExtensions.kt
   ```

2. **Update ArticlesApiExtensions.kt**
   - Add `offset` parameter to `getArticles()`
   - Add `newsSite` parameter for filtering

3. **Update EventsApiExtensions.kt**  
   - Add `offset` parameter to `getEventList()`
   - Add `search` parameter

### Phase 2: Repository Layer

1. **Create InfoRepository.kt and InfoRepositoryImpl.kt**
   ```
   composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/InfoRepository.kt
   composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/InfoRepositoryImpl.kt
   ```

2. **Update ArticlesRepository.kt**
   - Add `getArticlesPaginated()` method

3. **Update ArticlesRepositoryImpl.kt**
   - Implement `getArticlesPaginated()`

4. **Update EventsRepository.kt**
   - Add `getEventsPaginated()` method

5. **Update EventsRepositoryImpl.kt**
   - Implement `getEventsPaginated()`

### Phase 3: ViewModel

1. **Create NewsEventsViewModel.kt**
   ```
   composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/NewsEventsViewModel.kt
   ```

### Phase 4: UI Components

1. **Create NewsListItem.kt**
   ```
   composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/newsevents/components/NewsListItem.kt
   ```

2. **Create EventListItem.kt**
   ```
   composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/newsevents/components/EventListItem.kt
   ```

3. **Create NewsEventsFilterBottomSheet.kt**
   ```
   composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/newsevents/NewsEventsFilterBottomSheet.kt
   ```

4. **Create NewsEventsScreen.kt**
   ```
   composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/newsevents/NewsEventsScreen.kt
   ```

### Phase 5: Integration

1. **Register in AppModule.kt**
   ```kotlin
   viewModelOf(::NewsEventsViewModel)
   singleOf(::InfoRepositoryImpl) { bind<InfoRepository>() }
   ```

2. **Register InfoApi in ApiModule.kt**
   ```kotlin
   single { InfoApi(baseUrl = snapiBaseUrl, httpClient = get()) }
   ```

3. **Add navigation route in Screen.kt**
   ```kotlin
   data object NewsEvents : Screen()
   ```

4. **Add composable entry in App.kt**

## Quick Verification Steps

### Step 1: API Extensions Work
```kotlin
// Test in GeneratedApiClientExample.kt
val info = infoApi.getInfo()
println("News sites: ${info.newsSites}")
```

### Step 2: Repository Works
```kotlin
// In ViewModel or test
val result = articlesRepository.getArticlesPaginated(limit = 20, offset = 0)
result.onSuccess { println("Got ${it.data.results.size} articles") }
```

### Step 3: UI Renders
```kotlin
// Preview in NewsEventsScreen.kt
@Preview
@Composable
fun NewsEventsScreenPreview() {
    SpaceLaunchNowPreviewTheme {
        NewsEventsScreen(
            uiState = NewsEventsUiState(
                articles = listOf(/* mock data */),
                events = listOf(/* mock data */)
            ),
            // ... callbacks
        )
    }
}

@Preview
@Composable
fun NewsEventsScreenDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        NewsEventsScreen(
            uiState = NewsEventsUiState(
                articles = listOf(/* mock data */),
                events = listOf(/* mock data */)
            ),
            // ... callbacks
        )
    }
}
```

## Build Commands

```bash
# Generate API clients (if needed)
./gradlew openApiGenerate

# Build and check for errors
./gradlew compileKotlinDesktop

# Run desktop app
./gradlew desktopRun

# Run tests
./gradlew test
```

## Key Files Reference

| Purpose | Reference File |
|---------|---------------|
| List Screen Pattern | `ui/agencies/AgencyListScreen.kt` |
| List ViewModel Pattern | `ui/viewmodel/AgencyListViewModel.kt` |
| Filter Bottom Sheet | `ui/schedule/ScheduleFilterBottomSheet.kt` |
| Search Bar | `ui/components/SearchBar.kt` |
| API Extensions | `api/extensions/LaunchesApiExtensions.kt` |
| Repository Pattern | `data/repository/ArticlesRepositoryImpl.kt` |

## Common Issues

### "Unresolved reference" after API changes
Run `./gradlew clean openApiGenerate` and refresh IDE

### News sites list empty
Check SNAPI API key in `.env` and InfoApi registration in Koin

### Pagination not working
Verify offset is being incremented and hasMore flag is correctly set from API response count

### Dark theme preview not showing
Ensure both `@Preview` functions exist with `SpaceLaunchNowPreviewTheme()` and `SpaceLaunchNowPreviewTheme(isDark = true)`

@file:OptIn(ExperimentalMaterialApi::class)

package me.calebjones.spacelaunchnow.ui.newsevents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.ui.components.SearchBar
import me.calebjones.spacelaunchnow.ui.newsevents.components.EventListItem
import me.calebjones.spacelaunchnow.ui.newsevents.components.NewsListItem
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.viewmodel.NewsEventsTab
import me.calebjones.spacelaunchnow.ui.viewmodel.NewsEventsUiState
import me.calebjones.spacelaunchnow.ui.viewmodel.NewsEventsViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main screen for browsing news articles and space events.
 * 
 * Features two tabs:
 * - News: Articles from multiple space news sources
 * - Events: Upcoming and recent space events
 * 
 * Supports pagination, pull-to-refresh, and will support search and filtering.
 * 
 * @param initialTab Index of the tab to open on entry (0 = News, 1 = Events).
 * @param onEventClick Callback when an event is clicked (navigates to event detail)
 * @param onArticleClick Callback when an article is clicked (navigates to in-app news detail)
 * @param viewModel The ViewModel managing the screen state
 */
@Composable
fun NewsEventsScreen(
    initialTab: Int = 0,
    onEventClick: (Int) -> Unit = {},
    onArticleClick: (url: String, title: String) -> Unit = { _, _ -> },
    viewModel: NewsEventsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Deep-link entry (e.g. "See All" on Home) can request a specific tab.
    // Re-fires only when the requested tab index changes, so it never fights a
    // manual tab switch made within the screen.
    LaunchedEffect(initialTab) {
        val tabs = NewsEventsTab.entries
        val requested = tabs[initialTab.coerceIn(0, tabs.lastIndex)]
        if (requested != viewModel.uiState.value.selectedTab) {
            viewModel.selectTab(requested)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NewsEventsContent(
            uiState = uiState,
            onTabSelected = viewModel::selectTab,
            onRefreshNews = viewModel::refreshNews,
            onRefreshEvents = viewModel::refreshEvents,
            onLoadMoreNews = viewModel::loadMoreNews,
            onLoadMoreEvents = viewModel::loadMoreEvents,
            onEventClick = onEventClick,
            onArticleClick = { article ->
                viewModel.trackArticleClicked(article.id.toString(), article.newsSite, article.url)
                onArticleClick(article.url, article.title)
            },
            onSearchQueryChange = viewModel::updateSearchQuery,
            onClearSearch = viewModel::clearSearch,
            onNewsSiteToggled = viewModel::toggleNewsSiteFilter,
            onEventTypeToggled = viewModel::toggleEventTypeFilter,
            onClearFilters = viewModel::clearFilters
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NewsEventsContent(
    uiState: NewsEventsUiState,
    onTabSelected: (NewsEventsTab) -> Unit,
    onRefreshNews: () -> Unit,
    onRefreshEvents: () -> Unit,
    onLoadMoreNews: () -> Unit,
    onLoadMoreEvents: () -> Unit,
    onEventClick: (Int) -> Unit,
    onArticleClick: (me.calebjones.spacelaunchnow.api.snapi.models.Article) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onNewsSiteToggled: (String) -> Unit,
    onEventTypeToggled: (Int) -> Unit,
    onClearFilters: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val newsListState = rememberLazyListState()
    val eventsListState = rememberLazyListState()

    val pagerState = rememberPagerState(
        initialPage = uiState.selectedTab.ordinal,
        pageCount = { NewsEventsTab.entries.size }
    )

    // Sync pager with tab selection
    LaunchedEffect(uiState.selectedTab) {
        if (pagerState.currentPage != uiState.selectedTab.ordinal) {
            pagerState.animateScrollToPage(uiState.selectedTab.ordinal)
        }
    }

    // Sync tab selection with pager swipes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collectLatest { page ->
                val tab = NewsEventsTab.entries[page]
                if (tab != uiState.selectedTab) {
                    onTabSelected(tab)
                }
            }
    }

    // Detect scrolling near end for pagination - News tab
    val shouldLoadMoreNews by remember {
        derivedStateOf {
            val layoutInfo = newsListState.layoutInfo
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = layoutInfo.totalItemsCount
            totalItems > 0 && lastVisibleItem >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMoreNews) {
        if (shouldLoadMoreNews && !uiState.isLoadingMoreNews && uiState.newsHasMore) {
            onLoadMoreNews()
        }
    }

    // Detect scrolling near end for pagination - Events tab
    val shouldLoadMoreEvents by remember {
        derivedStateOf {
            val layoutInfo = eventsListState.layoutInfo
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = layoutInfo.totalItemsCount
            totalItems > 0 && lastVisibleItem >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMoreEvents) {
        if (shouldLoadMoreEvents && !uiState.isLoadingMoreEvents && uiState.eventsHasMore) {
            onLoadMoreEvents()
        }
    }

    // Pull-to-refresh states (one per tab — swapped based on the selected tab below)
    val newsRefreshState = rememberPullToRefreshState()
    val eventsRefreshState = rememberPullToRefreshState()
    val isNewsTab = uiState.selectedTab == NewsEventsTab.NEWS
    val activeRefreshState = if (isNewsTab) newsRefreshState else eventsRefreshState
    val isActiveRefreshing = if (isNewsTab) {
        uiState.isLoadingNews && uiState.news.isNotEmpty()
    } else {
        uiState.isLoadingEvents && uiState.events.isNotEmpty()
    }
    val onActiveRefresh: () -> Unit = if (isNewsTab) onRefreshNews else onRefreshEvents

    // Filter bottom sheet state
    var showFilterSheet by remember { mutableStateOf(false) }

    // Filter bottom sheet
    NewsEventsFilterBottomSheet(
        isOpen = showFilterSheet,
        selectedTab = uiState.selectedTab,
        availableNewsSites = uiState.availableNewsSites,
        selectedNewsSites = uiState.selectedNewsSites,
        availableEventTypes = uiState.availableEventTypes,
        selectedEventTypeIds = uiState.selectedEventTypeIds,
        onNewsSiteToggled = onNewsSiteToggled,
        onEventTypeToggled = onEventTypeToggled,
        onClearFilters = onClearFilters,
        onDismiss = { showFilterSheet = false }
    )

    PullToRefreshBox(
        isRefreshing = isActiveRefreshing,
        onRefresh = onActiveRefresh,
        state = activeRefreshState,
        modifier = Modifier.fillMaxSize(),
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = activeRefreshState,
                isRefreshing = isActiveRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header with title and filter button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "News & Events",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                BadgedBox(
                    badge = {
                        if (uiState.activeFilterCount > 0) {
                            Badge { Text("${uiState.activeFilterCount}") }
                        }
                    }
                ) {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                }
            }

            // Search bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = if (uiState.selectedTab == NewsEventsTab.NEWS)
                    "Search news articles..."
                else
                    "Search events...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Tabs
            val selectedTabIndex = uiState.selectedTab.ordinal
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                NewsEventsTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            onTabSelected(tab)
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(tab.displayName) }
                    )
                }
            }

            HorizontalDivider()

            // Loading indicator for refreshing
            if (uiState.selectedTab == NewsEventsTab.NEWS && uiState.isLoadingNews && uiState.news.isNotEmpty()) {
                LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else if (uiState.selectedTab == NewsEventsTab.EVENTS && uiState.isLoadingEvents && uiState.events.isNotEmpty()) {
                LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Pager with both tabs
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (NewsEventsTab.entries[page]) {
                    NewsEventsTab.NEWS -> NewsTabContent(
                        news = uiState.news,
                        isLoading = uiState.isLoadingNews,
                        isLoadingMore = uiState.isLoadingMoreNews,
                        error = uiState.newsError,
                        searchQuery = uiState.searchQuery,
                        onArticleClick = onArticleClick,
                        listState = newsListState
                    )
                    NewsEventsTab.EVENTS -> EventsTabContent(
                        events = uiState.events,
                        isLoading = uiState.isLoadingEvents,
                        isLoadingMore = uiState.isLoadingMoreEvents,
                        error = uiState.eventsError,
                        searchQuery = uiState.searchQuery,
                        onEventClick = onEventClick,
                        listState = eventsListState
                    )
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NewsTabContent(
    news: List<me.calebjones.spacelaunchnow.api.snapi.models.Article>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    error: String?,
    searchQuery: String,
    onArticleClick: (me.calebjones.spacelaunchnow.api.snapi.models.Article) -> Unit = {},
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Error state with items
        if (error != null && news.isEmpty()) {
            item {
                ErrorState(
                    message = error,
                    onRetry = null // Will add retry in future
                )
            }
        }

        // Initial loading state
        if (isLoading && news.isEmpty() && error == null) {
            item {
                LoadingState()
            }
        }

        // News items
        items(
            items = news,
            key = { it.id }
        ) { article ->
            NewsListItem(article = article, onClick = { onArticleClick(article) })
        }

        // Loading more indicator
        if (isLoadingMore && news.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
        }

        // Empty state
        if (news.isEmpty() && !isLoading && error == null) {
            item {
                EmptyState(
                    message = if (searchQuery.isNotBlank())
                        "No articles found for \"$searchQuery\""
                    else
                        "No news articles found"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EventsTabContent(
    events: List<me.calebjones.spacelaunchnow.domain.model.Event>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    error: String?,
    searchQuery: String,
    onEventClick: (Int) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Error state with items
        if (error != null && events.isEmpty()) {
            item {
                ErrorState(
                    message = error,
                    onRetry = null
                )
            }
        }

        // Initial loading state
        if (isLoading && events.isEmpty() && error == null) {
            item {
                LoadingState()
            }
        }

        // Events items
        items(
            items = events,
            key = { it.id }
        ) { event ->
            EventListItem(
                event = event,
                onClick = { onEventClick(event.id) }
            )
        }

        // Loading more indicator
        if (isLoadingMore && events.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
        }

        // Empty state
        if (events.isEmpty() && !isLoading && error == null) {
            item {
                EmptyState(
                    message = if (searchQuery.isNotBlank())
                        "No events found for \"$searchQuery\""
                    else
                        "No events found"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Error loading content",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ========== Previews ==========

@Preview
@Composable
private fun NewsEventsScreenPreview() {
    SpaceLaunchNowPreviewTheme {
        Surface {
            Text(
                text = "News & Events Screen Preview",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
private fun NewsEventsScreenDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Surface {
            Text(
                text = "News & Events Screen Preview (Dark)",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

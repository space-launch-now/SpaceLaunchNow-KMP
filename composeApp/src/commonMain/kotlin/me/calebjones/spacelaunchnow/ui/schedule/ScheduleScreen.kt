@file:OptIn(ExperimentalMaterialApi::class)

package me.calebjones.spacelaunchnow.ui.schedule

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.util.DateTimeUtil
import org.koin.compose.koinInject

@Composable
fun ScheduleScreen(
    onLaunchClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ScheduleContent(onLaunchClick = onLaunchClick)
    }
}

private enum class ScheduleTab { Upcoming, Previous }

private data class TabCache(
    val query: String,
    val results: List<LaunchBasic>,
    val endReached: Boolean,
    val timestamp: Long,
)

private data class TabUiState(
    val items: MutableList<LaunchBasic>,
    val listState: LazyListState,
    val isLoading: MutableState<Boolean>,
    val error: MutableState<String?>,
    val endReached: MutableState<Boolean>,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScheduleContent(onLaunchClick: (String) -> Unit) {
    val launchesApi: LaunchesApi = koinInject()

    var selectedTab by remember { mutableStateOf(ScheduleTab.Upcoming) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val upcomingItems = remember { mutableStateListOf<LaunchBasic>() }
    val previousItems = remember { mutableStateListOf<LaunchBasic>() }

    val upcomingListState = rememberLazyListState()
    val previousListState = rememberLazyListState()

    val upcomingLoading = remember { mutableStateOf(false) }
    val previousLoading = remember { mutableStateOf(false) }

    val upcomingError = remember { mutableStateOf<String?>(null) }
    val previousError = remember { mutableStateOf<String?>(null) }

    val upcomingEndReached = remember { mutableStateOf(false) }
    val previousEndReached = remember { mutableStateOf(false) }

    var hasAttemptedInitialLoad by remember { mutableStateOf(false) }

    fun uiFor(tab: ScheduleTab) = when (tab) {
        ScheduleTab.Upcoming -> TabUiState(
            upcomingItems,
            upcomingListState,
            upcomingLoading,
            upcomingError,
            upcomingEndReached
        )

        ScheduleTab.Previous -> TabUiState(
            previousItems,
            previousListState,
            previousLoading,
            previousError,
            previousEndReached
        )
    }

    val scope = rememberCoroutineScope()
    // Used to invalidate in-flight requests when search changes
    var queryVersion by remember { mutableStateOf(0) }

    // Simple per-tab cache with TTL
    val cacheTtlMillis = 900_000L // 15 minute TTL
    var upcomingCache by remember { mutableStateOf<TabCache?>(null) }
    var previousCache by remember { mutableStateOf<TabCache?>(null) }

    fun now() = Clock.System.now().toEpochMilliseconds()

    fun cacheFor(tab: ScheduleTab) = when (tab) {
        ScheduleTab.Upcoming -> upcomingCache
        ScheduleTab.Previous -> previousCache
    }

    fun setCache(tab: ScheduleTab, cache: TabCache?) {
        when (tab) {
            ScheduleTab.Upcoming -> upcomingCache = cache
            ScheduleTab.Previous -> previousCache = cache
        }
    }

    fun hasValidCache(tab: ScheduleTab): Boolean {
        val cache = cacheFor(tab) ?: return false
        val fresh = (now() - cache.timestamp) < cacheTtlMillis
        // Cache is valid if fresh and query matches (empty search matches empty cache query)
        val queryMatches =
            cache.query == searchQuery || (cache.query.isEmpty() && searchQuery.isEmpty())
        return fresh && queryMatches && cache.results.isNotEmpty()
    }

    fun restoreFromCache(tab: ScheduleTab) {
        val cache = cacheFor(tab) ?: return
        val ui = uiFor(tab)
        ui.items.clear()
        ui.items.addAll(cache.results)
        ui.endReached.value = cache.endReached
        ui.isLoading.value = false
        ui.error.value = null
    }

    suspend fun loadNextPage(reset: Boolean = false, tab: ScheduleTab) {
        val ui = uiFor(tab)
        // Allow a reset load to proceed even if a previous load is running
        if (!reset && (ui.isLoading.value || ui.endReached.value)) return
        ui.isLoading.value = true
        val requestVersion = queryVersion
        try {
            val limit = 25
            val offset = if (reset) 0 else ui.items.size
            val isUpcoming = tab == ScheduleTab.Upcoming
            val isPrevious = tab == ScheduleTab.Previous
            val ordering = if (isUpcoming) "net" else "-net"

            val page: PaginatedLaunchBasicList = if (isPrevious) {
                launchesApi.launchesMiniList(
                    limit = limit,
                    offset = offset,
                    previous = true,
                    ordering = ordering,
                    search = searchQuery.takeIf { it.isNotBlank() }
                ).body()
            } else {
                launchesApi.launchesMiniList(
                    limit = limit,
                    offset = offset,
                    upcoming = true,
                    ordering = ordering,
                    search = searchQuery.takeIf { it.isNotBlank() }
                ).body()
            }

            // If the query changed while this request was in flight, ignore these results
            if (requestVersion != queryVersion) return

            if (reset) ui.items.clear()
            ui.items.addAll(page.results)
            ui.endReached.value = page.next == null || page.results.isEmpty()

            // Update cache after successful load
            setCache(
                tab,
                TabCache(
                    query = searchQuery,
                    results = ui.items.toList(),
                    endReached = ui.endReached.value,
                    timestamp = now()
                )
            )
            ui.error.value = null
        } catch (t: Throwable) {
            ui.error.value = t.message ?: t.toString()
        } finally {
            ui.isLoading.value = false
        }
    }

    fun resetAndLoad(tab: ScheduleTab) {
        val ui = uiFor(tab)
        ui.items.clear()
        ui.endReached.value = false
        ui.error.value = null
        scope.launch { loadNextPage(reset = true, tab = tab) }
    }

    // Native pull-to-refresh (same style as HomeScreen)
    var isRefreshing by remember { mutableStateOf(false) }
    fun bustCacheAndRefresh(tab: ScheduleTab = selectedTab) {
        // Clear cache for the current tab only if searching, otherwise clear both tabs
        if (searchQuery.isNotBlank()) {
            setCache(tab, null)
        } else {
            // For general refresh, clear both caches to get fresh data
            setCache(ScheduleTab.Upcoming, null)
            setCache(ScheduleTab.Previous, null)
        }
        isRefreshing = true
        scope.launch {
            if (searchQuery.isNotBlank()) {
                // Only refresh current tab if searching
                resetAndLoad(tab)
            } else {
                // Refresh both tabs for general refresh
                resetAndLoad(ScheduleTab.Upcoming)
                resetAndLoad(ScheduleTab.Previous)
            }
            isRefreshing = false
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { bustCacheAndRefresh() }
    )

    val pagerState = rememberPagerState(
        initialPage = if (selectedTab == ScheduleTab.Upcoming) 0 else 1,
        pageCount = { 2 }
    )

    // Sync tab selection when user swipes pages
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        // Only update selectedTab when the page actually changes and we're not in the middle of scrolling
        if (!pagerState.isScrollInProgress) {
            val newTab =
                if (pagerState.currentPage == 0) ScheduleTab.Upcoming else ScheduleTab.Previous
            if (newTab != selectedTab) {
                selectedTab = newTab
                if (hasValidCache(newTab)) {
                    restoreFromCache(newTab)
                } else if (uiFor(newTab).items.isEmpty()) {
                    resetAndLoad(newTab)
                }
            }
        }
    }

    // When user taps the tab, animate pager to page
    fun onTabSelected(tab: ScheduleTab) {
        val target = if (tab == ScheduleTab.Upcoming) 0 else 1
        if (pagerState.currentPage != target) {
            selectedTab = tab // Update immediately for responsive UI
            scope.launch {
                pagerState.animateScrollToPage(
                    target,
                    animationSpec = tween(durationMillis = 550)
                )
            }
        }
    }

    // Debounce search; clear caches on query change and refetch both tabs (preload)
    LaunchedEffect(searchQuery) {
        delay(400)
        // Bump version so any in-flight requests from the previous query are ignored
        queryVersion++
        // Only clear caches if search query is not empty or has actually changed
        if (searchQuery.isNotBlank()) {
            // Clear caches only for search queries
            setCache(ScheduleTab.Upcoming, null)
            setCache(ScheduleTab.Previous, null)
            resetAndLoad(ScheduleTab.Upcoming)
            resetAndLoad(ScheduleTab.Previous)
        } else {
            // For empty search, check if we need to reload from cache or fetch fresh data
            val currentUi = uiFor(selectedTab)
            if (!hasValidCache(selectedTab) && currentUi.items.isEmpty()) {
                resetAndLoad(selectedTab)
            }
            val otherTab =
                if (selectedTab == ScheduleTab.Upcoming) ScheduleTab.Previous else ScheduleTab.Upcoming
            val otherUi = uiFor(otherTab)
            if (!hasValidCache(otherTab) && otherUi.items.isEmpty()) {
                resetAndLoad(otherTab)
            }
        }
    }

    // Infinite scroll for each tab
    LaunchedEffect(upcomingListState, searchQuery) {
        snapshotFlow { upcomingListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filter { it != null }
            .map { it!! }
            .distinctUntilChanged()
            .collectLatest { lastIndex ->
                val ui = uiFor(ScheduleTab.Upcoming)
                if (lastIndex >= ui.items.size - 8) {
                    loadNextPage(tab = ScheduleTab.Upcoming)
                }
            }
    }
    LaunchedEffect(previousListState, searchQuery) {
        snapshotFlow { previousListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filter { it != null }
            .map { it!! }
            .distinctUntilChanged()
            .collectLatest { lastIndex ->
                val ui = uiFor(ScheduleTab.Previous)
                if (lastIndex >= ui.items.size - 8) {
                    loadNextPage(tab = ScheduleTab.Previous)
                }
            }
    }

    // Initial load + preload other page
    LaunchedEffect(Unit) {
        val current = selectedTab
        // Ensure loading is marked to true immediately to avoid UI flicker.
        val currentUi = uiFor(current)
        if (!hasValidCache(current) && currentUi.items.isEmpty()) {
            currentUi.isLoading.value = true
        }
        if (hasValidCache(current)) {
            restoreFromCache(current)
        } else if (currentUi.items.isEmpty()) {
            resetAndLoad(current)
        }
        val other =
            if (current == ScheduleTab.Upcoming) ScheduleTab.Previous else ScheduleTab.Upcoming
        val otherUi = uiFor(other)
        if (!hasValidCache(other) && otherUi.items.isEmpty()) {
            otherUi.isLoading.value = true
        }
        if (hasValidCache(other)) {
            restoreFromCache(other)
        } else if (otherUi.items.isEmpty()) {
            resetAndLoad(other)
        }
        // Mark after first load attempt is done
        hasAttemptedInitialLoad = true
    }

    // Clear search when collapsed
    LaunchedEffect(isSearchExpanded) {
        if (!isSearchExpanded && searchQuery.isNotBlank()) {
            // Don't immediately trigger the search debounce by clearing query
            // Instead, set to empty without triggering cache clear
            searchQuery = ""
        }
    }

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Title bar with expandable search
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
            ) {
                if (isSearchExpanded) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = {
                                isSearchExpanded = false
                                searchQuery = ""
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close search")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text("Search launches") },
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        )
                    )
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Launch Schedule",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { isSearchExpanded = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                }
            }

            // Tabs
            val selectedTabIndex = if (selectedTab == ScheduleTab.Upcoming) 0 else 1
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
//                divider = { HorizontalDivider() }
            ) {
                Tab(
                    selected = selectedTab == ScheduleTab.Upcoming,
                    onClick = { onTabSelected(ScheduleTab.Upcoming) },
                    text = { Text("Upcoming") }
                )
                Tab(
                    selected = selectedTab == ScheduleTab.Previous,
                    onClick = { onTabSelected(ScheduleTab.Previous) },
                    text = { Text("Previous") }
                )
            }

            Divider()

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val tab = if (page == 0) ScheduleTab.Upcoming else ScheduleTab.Previous
                val ui = uiFor(tab)
                // Content list for the tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp),
                    state = ui.listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (ui.error.value != null && ui.items.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Error loading launches",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    ui.error.value!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                TextButton(onClick = { resetAndLoad(tab) }) { Text("Retry") }
                            }
                        }
                    }

                    // Only show initial loading indicator if no error and not refreshing
                    if (ui.items.isEmpty() && ui.isLoading.value && ui.error.value == null && !isRefreshing) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    itemsIndexed(ui.items) { index, launch ->
                        ScheduleLaunchCard(
                            launch = launch,
                            onClick = { onLaunchClick(launch.id) }
                        )
                    }

                    if (ui.isLoading.value && ui.items.isNotEmpty()) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    // Only show empty state if not loading and either not currently refreshing or finished refreshing.
                    // Prevents flicker during initial load.
                    if (hasAttemptedInitialLoad && !ui.isLoading.value && ui.items.isEmpty() && ui.error.value == null && !isRefreshing) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No launches found")
                            }
                        }
                    }
                }

            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun ScheduleLaunchCard(
    launch: LaunchBasic,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Launch Image/Icon
            val imageUrl = launch.image?.thumbnailUrl ?: launch.image?.imageUrl
            val dateText = DateTimeUtil.formatDateWithPrecisionFallback(launch)
            val title = launch.name ?: "Unknown Launch"
            val location = launch.locationName ?: "Unknown Location"
            val mission = launch.launchServiceProvider.name

            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    placeholder = rememberVectorPainter(Icons.Filled.RocketLaunch),
                    error = rememberVectorPainter(Icons.Filled.RocketLaunch),
                    fallback = rememberVectorPainter(Icons.Filled.RocketLaunch),
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.RocketLaunch,
                        contentDescription = "placeholder",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    )
                }
            }

            // Main Content Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Launch Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = mission,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Launch Service Provider
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Date Column (Right Side)
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
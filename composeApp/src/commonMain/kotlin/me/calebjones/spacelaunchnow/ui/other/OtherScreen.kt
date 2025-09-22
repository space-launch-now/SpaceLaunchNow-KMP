@file:OptIn(ExperimentalMaterialApi::class)

package me.calebjones.spacelaunchnow.ui.other

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.ktor.client.call.body
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.ui.compose.LaunchCardHeader
import me.calebjones.spacelaunchnow.ui.compose.toLaunchCardData
import me.calebjones.spacelaunchnow.util.DateTimeUtil
import me.calebjones.spacelaunchnow.util.LaunchFormatUtil
import me.calebjones.spacelaunchnow.util.StatusColorUtil
import org.koin.compose.koinInject
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.MutableState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Rocket
import androidx.compose.animation.core.tween

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
    val cacheTtlMillis = 600_000L // 10 minute TTL
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
        return fresh && cache.query == searchQuery && cache.results.isNotEmpty()
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
        setCache(tab, null)
        isRefreshing = true
        scope.launch {
            resetAndLoad(tab)
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
        // Always refresh both tabs so search is shared
        setCache(ScheduleTab.Upcoming, null)
        setCache(ScheduleTab.Previous, null)
        resetAndLoad(ScheduleTab.Upcoming)
        resetAndLoad(ScheduleTab.Previous)
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
        if (hasValidCache(current)) restoreFromCache(current) else if (uiFor(current).items.isEmpty()) resetAndLoad(
            current
        )
        val other =
            if (current == ScheduleTab.Upcoming) ScheduleTab.Previous else ScheduleTab.Upcoming
        if (hasValidCache(other)) restoreFromCache(other) else if (uiFor(other).items.isEmpty()) resetAndLoad(
            other
        )
    }

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search bar moved above tabs and made rounder
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text("Search launches") },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                )
            )

            // Tabs
            val selectedTabIndex = if (selectedTab == ScheduleTab.Upcoming) 0 else 1
            SecondaryTabRow(
                selectedTabIndex = selectedTabIndex,
                divider = { HorizontalDivider() }
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
                    contentPadding = PaddingValues(bottom = 24.dp),
                    state = ui.listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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

                    if (ui.items.isEmpty() && ui.isLoading.value) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    items(ui.items) { launch ->
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

                    if (!ui.isLoading.value && ui.items.isEmpty() && ui.error.value == null) {
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
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageUrl = launch.image?.thumbnailUrl ?: launch.image?.imageUrl

            if (imageUrl != null) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier
                        .size(64.dp)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        placeholder = rememberVectorPainter(FontAwesomeIcons.Solid.Rocket),
                        error = rememberVectorPainter(FontAwesomeIcons.Solid.Rocket),
                        fallback = rememberVectorPainter(FontAwesomeIcons.Solid.Rocket),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                            .clip(CircleShape),
//                            .border(
//                                width = 2.dp,
//                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
//                                shape = CircleShape
//                            ),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🚀",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = LaunchFormatUtil.formatLaunchTitle(launch),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                val dateText = formatDateWithPrecisionFallback(launch)
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.End
            ) {
                launch.status?.let { status ->
                    InfoChip(
                        text = status.abbrev ?: status.name,
                        background = StatusColorUtil.getLaunchStatusColor(status.id)
                    )
                }

                launch.netPrecision?.abbrev?.let { abbrev ->
                    InfoChip(text = abbrev)
                }
            }
        }
    }
}

@Composable
private fun InfoChip(text: String, background: Color = MaterialTheme.colorScheme.surfaceVariant) {
    Surface(
        color = background,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (background == MaterialTheme.colorScheme.surfaceVariant) MaterialTheme.colorScheme.onSurface else Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatDateWithPrecisionFallback(launch: LaunchBasic): String {
    val net = launch.net ?: return "TBD"
    val precisionId = launch.netPrecision?.id

    return when (precisionId) {
        2 -> "NET ${DateTimeUtil.formatLaunchDate(net)}"
        7 -> formatMonthYear(net)
        8, 9, 10, 11 -> "${formatQuarter(net)} ${formatYear(net)}"
        12 -> "H1 ${formatYear(net)}"
        13 -> "H2 ${formatYear(net)}"
        14 -> "NET ${formatYear(net)}"
        15 -> "FY ${formatYear(net)}"
        16 -> "Decade ${formatYear(net)}"
        else -> DateTimeUtil.formatLaunchDate(net)
    }
}

private fun formatYear(instant: kotlinx.datetime.Instant): String {
    return try {
        val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        ldt.year.toString()
    } catch (e: Throwable) {
        ""
    }
}

private fun formatMonthYear(instant: kotlinx.datetime.Instant): String {
    return try {
        val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val monthYear = DateTimeUtil.formatLaunchDate(instant)
        val year = ldt.year.toString()
        val idx = monthYear.indexOf(year)
        if (idx > 0) {
            val beforeYear = monthYear.substring(0, idx).trim().trimEnd(',')
            "$beforeYear $year"
        } else monthYear
    } catch (_: Throwable) {
        DateTimeUtil.formatLaunchDate(instant)
    }
}

private fun formatQuarter(instant: kotlinx.datetime.Instant): String {
    return try {
        val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = ldt.monthNumber
        val quarter = when (month) {
            in 1..3 -> "Q1"
            in 4..6 -> "Q2"
            in 7..9 -> "Q3"
            else -> "Q4"
        }
        quarter
    } catch (_: Throwable) {
        "Q?"
    }
}
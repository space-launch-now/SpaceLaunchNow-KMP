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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.isTabletOrDesktop
import me.calebjones.spacelaunchnow.ui.icons.CustomIcons
import me.calebjones.spacelaunchnow.ui.icons.RocketLaunch
import me.calebjones.spacelaunchnow.ui.viewmodel.ScheduleTab
import me.calebjones.spacelaunchnow.ui.viewmodel.ScheduleViewModel
import me.calebjones.spacelaunchnow.util.DateTimeUtil
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScheduleScreen(
    onLaunchClick: (String) -> Unit,
    viewModel: ScheduleViewModel = koinViewModel()
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ScheduleContent(
            viewModel = viewModel,
            onLaunchClick = onLaunchClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScheduleContent(
    viewModel: ScheduleViewModel,
    onLaunchClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val isTablet = isTabletOrDesktop()

    val upcomingListState = rememberLazyListState()
    val previousListState = rememberLazyListState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = {
            viewModel.refresh()
        }
    )

    val pagerState = rememberPagerState(
        initialPage = if (uiState.selectedTab == ScheduleTab.Upcoming) 0 else 1,
        pageCount = { 2 }
    )

    // Sync tab selection when user swipes pages
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            val newTab =
                if (pagerState.currentPage == 0) ScheduleTab.Upcoming else ScheduleTab.Previous
            if (newTab != uiState.selectedTab) {
                viewModel.selectTab(newTab)
            }
        }
    }

    // When user taps the tab, animate pager to page
    fun onTabSelected(tab: ScheduleTab) {
        val target = if (tab == ScheduleTab.Upcoming) 0 else 1
        if (pagerState.currentPage != target) {
            viewModel.selectTab(tab)
            scope.launch {
                pagerState.animateScrollToPage(
                    target,
                    animationSpec = tween(durationMillis = 550)
                )
            }
        }
    }

    // Focus search field when expanded
    LaunchedEffect(uiState.isSearchExpanded) {
        if (uiState.isSearchExpanded) {
            focusRequester.requestFocus()
        }
    }

    // Infinite scroll for each tab
    LaunchedEffect(upcomingListState) {
        snapshotFlow { upcomingListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filter { it != null }
            .map { it!! }
            .distinctUntilChanged()
            .collectLatest { lastIndex ->
                if (lastIndex >= uiState.upcomingTab.items.size - 8) {
                    viewModel.loadNextPage(ScheduleTab.Upcoming)
                }
            }
    }

    LaunchedEffect(previousListState) {
        snapshotFlow { previousListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filter { it != null }
            .map { it!! }
            .distinctUntilChanged()
            .collectLatest { lastIndex ->
                if (lastIndex >= uiState.previousTab.items.size - 8) {
                    viewModel.loadNextPage(ScheduleTab.Previous)
                }
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
                if (uiState.isSearchExpanded) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleSearchExpanded(false) }) {
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
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Launch Schedule",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            modifier = Modifier.weight(1f)
                        )
                        // Filter button with badge
                        if (uiState.filterState.hasActiveFilters()) {
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text("${uiState.filterState.activeFilterCount()}")
                                    }
                                }
                            ) {
                                IconButton(onClick = { viewModel.openFilterSheet() }) {
                                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                                }
                            }
                        } else {
                            IconButton(onClick = { viewModel.openFilterSheet() }) {
                                Icon(Icons.Default.FilterList, contentDescription = "Filter")
                            }
                        }
                        IconButton(onClick = { viewModel.toggleSearchExpanded(true) }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                }
            }

            // Tabs
            val selectedTabIndex = if (uiState.selectedTab == ScheduleTab.Upcoming) 0 else 1
            val hasActiveFilters = uiState.filterState.hasActiveFilters()
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = uiState.selectedTab == ScheduleTab.Upcoming,
                    onClick = { onTabSelected(ScheduleTab.Upcoming) },
                    text = {
                        val count = uiState.upcomingTab.totalCount
                        val isLoading = uiState.upcomingTab.isLoading
                        if (hasActiveFilters && count != null && count > 1 && !isLoading) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Upcoming")
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        } else {
                            Text("Upcoming")
                        }
                    }
                )
                Tab(
                    selected = uiState.selectedTab == ScheduleTab.Previous,
                    onClick = { onTabSelected(ScheduleTab.Previous) },
                    text = {
                        val count = uiState.previousTab.totalCount
                        val isLoading = uiState.previousTab.isLoading
                        if (hasActiveFilters && count != null && count > 1 && !isLoading) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Previous")
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        } else {
                            Text("Previous")
                        }
                    }
                )
            }

            Divider()

            // Loading indicator for refreshing (filter changes, pull-to-refresh)
            if (uiState.selectedTab == ScheduleTab.Upcoming && uiState.upcomingTab.isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else if (uiState.selectedTab == ScheduleTab.Previous && uiState.previousTab.isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val tab = if (page == 0) ScheduleTab.Upcoming else ScheduleTab.Previous
                val tabState =
                    if (tab == ScheduleTab.Upcoming) uiState.upcomingTab else uiState.previousTab
                val listState =
                    if (tab == ScheduleTab.Upcoming) upcomingListState else previousListState

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (tabState.error != null && tabState.items.isEmpty()) {
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
                                    tabState.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                TextButton(onClick = { viewModel.loadNextPage(tab) }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    if (tabState.items.isEmpty() && tabState.isLoading && tabState.error == null && !tabState.isRefreshing) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    itemsIndexed(tabState.items) { index, launch ->
                        ScheduleLaunchCard(
                            launch = launch,
                            onClick = { onLaunchClick(launch.id) }
                        )

                        // // 🚀 PERFORMANCE BOOST: Show inline banner ad every 5 items for maximum visibility
                        // // This dramatically improves show rate by embedding ads in the content feed
                        // if ((index + 1) % 25 == 0 && index < tabState.items.size - 1) {
                        //     SmartBannerAd(
                        //         placementType = AdPlacementType.CONTENT,
                        //         showRemoveAdsButton = false,
                        //         showCard = true,
                        //         modifier = Modifier.padding(vertical = 4.dp)
                        //     )
                        // }
                    }

                    if (tabState.isLoading && tabState.items.isNotEmpty()) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    if (tabState.items.isEmpty() && !tabState.isLoading && tabState.error == null && !uiState.isRefreshing) {
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
            refreshing = uiState.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Filter UI - responsive: bottom sheet for phone, side sheet for tablet/desktop
        if (isTablet) {
            ScheduleFilterSideSheet(
                isOpen = uiState.isFilterSheetOpen,
                currentFilterState = uiState.filterState,
                agencies = uiState.filterOptions.agencies,
                programs = uiState.filterOptions.programs,
                rockets = uiState.filterOptions.rockets,
                locations = uiState.filterOptions.locations,
                statuses = uiState.filterOptions.statuses,
                orbits = uiState.filterOptions.orbits,
                missionTypes = uiState.filterOptions.missionTypes,
                launcherConfigFamilies = uiState.filterOptions.launcherConfigFamilies,
                isLoading = uiState.isLoadingFilterOptions,
                onApplyFilters = { newFilterState ->
                    viewModel.applyFilters(newFilterState)
                },
                onReloadOptions = { viewModel.reloadFilterOptions() },
                onDismiss = { viewModel.closeFilterSheet() },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        } else {
            ScheduleFilterBottomSheet(
                isOpen = uiState.isFilterSheetOpen,
                currentFilterState = uiState.filterState,
                agencies = uiState.filterOptions.agencies,
                programs = uiState.filterOptions.programs,
                rockets = uiState.filterOptions.rockets,
                locations = uiState.filterOptions.locations,
                statuses = uiState.filterOptions.statuses,
                orbits = uiState.filterOptions.orbits,
                missionTypes = uiState.filterOptions.missionTypes,
                launcherConfigFamilies = uiState.filterOptions.launcherConfigFamilies,
                isLoading = uiState.isLoadingFilterOptions,
                onApplyFilters = { newFilterState ->
                    viewModel.applyFilters(newFilterState)
                },
                onReloadOptions = { viewModel.reloadFilterOptions() },
                onDismiss = { viewModel.closeFilterSheet() }
            )
        }
    }
}

@Composable
private fun RocketIconPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = CustomIcons.RocketLaunch,
            contentDescription = "placeholder",
            modifier = Modifier.size(48.dp).padding(4.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
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
            val imageUrl = launch.image?.thumbnailUrl ?: launch.image?.imageUrl
            val useUtc = LocalUseUtc.current
            val dateText = DateTimeUtil.formatDateWithPrecisionFallback(launch, useUtc)
            val title = launch.name ?: "Unknown Launch"
            val location = launch.locationName ?: "Unknown Location"
            val mission = launch.launchServiceProvider.name

            if (imageUrl != null) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    loading = { RocketIconPlaceholder() },
                    error = { RocketIconPlaceholder() },
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
                        imageVector = CustomIcons.RocketLaunch,
                        contentDescription = "placeholder",
                        modifier = Modifier.size(48.dp).padding(4.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
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
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

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

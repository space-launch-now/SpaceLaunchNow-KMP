package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.UpdateEndpoint
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.navigation.Schedule
import me.calebjones.spacelaunchnow.navigation.SupportUs
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.components.OfflineBanner
import me.calebjones.spacelaunchnow.ui.layout.rememberAdaptiveLayoutState
import me.calebjones.spacelaunchnow.ui.preview.PreviewData
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.viewmodel.HistoryViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.ViewState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ResponsiveHomeContent(
    navController: NavController,
    additionalFeaturedLaunchesState: ViewState<List<LaunchNormal>>,
    previousLaunchesState: ViewState<List<LaunchNormal>>,
    historyState: ViewState<HistoryViewModel.HistoryData>,
    featuredLaunchState: ViewState<LaunchNormal?>,
    updatesState: ViewState<List<UpdateEndpoint>>,
    articlesState: ViewState<List<Article>>,
    eventsState: ViewState<List<EventEndpointNormal>>,
    next24HoursCount: Int,
    nextWeekCount: Int,
    nextMonthCount: Int,
    combinedLaunches: List<LaunchNormal>,
    upcomingStartIndex: Int,
    carouselError: String?,
    isCarouselLoading: Boolean,
    onCarouselRetry: () -> Unit,
    isAnyViewLoading: Boolean,
    hasAdFree: Boolean,
    onShareLaunch: (LaunchNormal) -> Unit = {},
    modifier: Modifier = Modifier,
    isOffline: Boolean = false,
    oldestCacheTimestamp: Long? = null,
    onRetry: () -> Unit = {}
) {
    val layoutState = rememberAdaptiveLayoutState()
    val isTabletOrDesktop = layoutState.isExpanded

    BoxWithConstraints(modifier = if (isTabletOrDesktop) modifier.fillMaxSize() else modifier) {
    val useWideHeroLayout = isTabletOrDesktop && maxWidth >= 1000.dp

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "top_bar") { HomeTopBar(navController = navController) }

        // Single offline banner when any data is stale
        if (isOffline) {
            item(key = "offline_banner") {
                OfflineBanner(
                    modifier = Modifier.padding(bottom = 16.dp),
                    dataSource = DataSource.STALE_CACHE,
                    cacheTimestamp = oldestCacheTimestamp,
                    onRetry = onRetry
                )
            }
        }

        // Show loading indicator when any view is loading
        if (isAnyViewLoading) {
            item(key = "loading_indicator") {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }
        }

        if (isTabletOrDesktop) {
            // Desktop/Tablet: Hero cards row
            if (useWideHeroLayout) {
                // Wide layout: Last Launch + History (left) + Next Up (right)
                item(key = "hero_cards_row") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(0.4f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LastLaunchCard(
                                previousLaunch = previousLaunchesState.data.firstOrNull(),
                                navController = navController
                            )

                            TabletHistoryCard(
                                historyState = historyState,
                                navController = navController
                            )
                        }

                        Box(modifier = Modifier.weight(0.6f)) {
                            NextLaunchView(
                                state = featuredLaunchState,
                                navController = navController,
                                onShare = onShareLaunch
                            )
                        }
                    }
                }
            } else {
                // Medium expanded (e.g. foldable): Single column, full width
                item(key = "next_launch_view") {
                    NextLaunchView(
                        state = featuredLaunchState,
                        navController = navController,
                        onShare = onShareLaunch
                    )
                }
                item(key = "last_launch_card") {
                    LastLaunchCard(
                        previousLaunch = previousLaunchesState.data.firstOrNull(),
                        navController = navController,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        } else {
            // Phone: Next launch hero
            item(key = "next_launch_view") {
                NextLaunchView(
                    state = featuredLaunchState,
                    navController = navController,
                    onShare = onShareLaunch
                )
            }
        }

        // Featured Launches Row
        item(key = "featured_launches_row") {
            FeaturedLaunchesRow(
                launchesState = additionalFeaturedLaunchesState,
                navController = navController
            )
        }

        // Quick Stats
        item(key = "quick_stats") {
            QuickStatsSection(
                next24HoursCount = next24HoursCount,
                nextWeekCount = nextWeekCount,
                nextMonthCount = nextMonthCount,
                historyCount = historyState.data.count,
                isTabletLayout = isTabletOrDesktop,
                modifier = Modifier.padding(
                    horizontal = if (isTabletOrDesktop) 8.dp else 16.dp,
                    vertical = 8.dp
                )
            )
        }

        // This Day in History (shown separately when not using wide hero layout)
        if (!useWideHeroLayout) {
            item(key = "history_section") {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "This Day in History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ThisDayInHistorySection(
                        historyState = historyState,
                        navController = navController
                    )
                }
            }
        }

        // Ad placement (non-premium users only)
        if (!hasAdFree) {
            item(key = "ad_banner") {
                SmartBannerAd(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    placementType = AdPlacementType.FEED,
                    showRemoveAdsButton = true,
                    onRemoveAdsClick = { navController.navigate(SupportUs) }
                )
            }
        }

        item(key = "launch_schedule_title") {
            SectionTitle(
                title = "Launch Schedule",
                hasAction = true,
                onActionClick = {
                    navController.navigate(Schedule) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        item(key = "launch_list") {
            LaunchListView(
                combinedLaunches = combinedLaunches,
                upcomingStartIndex = upcomingStartIndex,
                carouselError = carouselError,
                isCarouselLoading = isCarouselLoading,
                onRetry = onCarouselRetry,
                navController = navController
            )
        }

        item(key = "updates_title") { SectionTitle(title = "Latest Updates", hasAction = false) }
        item(key = "updates_view") {
            LatestUpdatesView(
                state = updatesState,
                navController = navController
            )
        }
        item(key = "news_title") { SectionTitle(title = "Latest News", hasAction = false) }
        item(key = "news_view") { ArticlesView(state = articlesState) }
        item(key = "events_title") { SectionTitle(title = "Upcoming Events", hasAction = false) }
        item(key = "events_view") { EventsView(state = eventsState, navController = navController) }
        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(if (isTabletOrDesktop) 64.dp else 32.dp))
        }
    }
    } // BoxWithConstraints
}

/**
 * Tablet-specific "This Day in History" card with a header row,
 * wrapping the shared [ThisDayInHistorySection].
 */
@Composable
private fun TabletHistoryCard(
    historyState: ViewState<HistoryViewModel.HistoryData>,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = "This Day in History",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "This Day in History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            ThisDayInHistorySection(
                historyState = historyState,
                navController = navController
            )
        }
    }
}

// ========================================
// Previews
// ========================================

@Preview
@Composable
private fun ResponsiveHomeContentPreview() {
    SpaceLaunchNowPreviewTheme {
        val launches = listOf(
            PreviewData.launchNormalSpaceX,
            PreviewData.launchNormalULA,
            PreviewData.launchNormalCrewMission
        )
        ResponsiveHomeContent(
            navController = rememberNavController(),
            additionalFeaturedLaunchesState = ViewState(data = launches),
            previousLaunchesState = ViewState(data = launches),
            historyState = ViewState(
                data = HistoryViewModel.HistoryData(
                    count = 5,
                    launches = launches
                )
            ),
            featuredLaunchState = ViewState(data = launches.first()),
            updatesState = ViewState(data = emptyList()),
            articlesState = ViewState(data = emptyList()),
            eventsState = ViewState(data = emptyList()),
            next24HoursCount = 2,
            nextWeekCount = 8,
            nextMonthCount = 24,
            combinedLaunches = launches,
            upcomingStartIndex = 1,
            carouselError = null,
            isCarouselLoading = false,
            onCarouselRetry = {},
            isAnyViewLoading = false,
            hasAdFree = true
        )
    }
}

@Preview
@Composable
private fun ResponsiveHomeContentDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        val launches = listOf(
            PreviewData.launchNormalSpaceX,
            PreviewData.launchNormalULA,
            PreviewData.launchNormalCrewMission
        )
        ResponsiveHomeContent(
            navController = rememberNavController(),
            additionalFeaturedLaunchesState = ViewState(data = launches),
            previousLaunchesState = ViewState(data = launches),
            historyState = ViewState(
                data = HistoryViewModel.HistoryData(
                    count = 5,
                    launches = launches
                )
            ),
            featuredLaunchState = ViewState(data = launches.first()),
            updatesState = ViewState(data = emptyList()),
            articlesState = ViewState(data = emptyList()),
            eventsState = ViewState(data = emptyList()),
            next24HoursCount = 2,
            nextWeekCount = 8,
            nextMonthCount = 24,
            combinedLaunches = launches,
            upcomingStartIndex = 1,
            carouselError = null,
            isCarouselLoading = false,
            onCarouselRetry = {},
            isAnyViewLoading = false,
            hasAdFree = true
        )
    }
}

@Preview
@Composable
private fun ResponsiveHomeContentLoadingPreview() {
    SpaceLaunchNowPreviewTheme {
        ResponsiveHomeContent(
            navController = rememberNavController(),
            additionalFeaturedLaunchesState = ViewState(data = emptyList(), isLoading = true),
            previousLaunchesState = ViewState(data = emptyList(), isLoading = true),
            historyState = ViewState(
                data = HistoryViewModel.HistoryData(count = 0, launches = emptyList()),
                isLoading = true
            ),
            featuredLaunchState = ViewState(data = null, isLoading = true),
            updatesState = ViewState(data = emptyList(), isLoading = true),
            articlesState = ViewState(data = emptyList(), isLoading = true),
            eventsState = ViewState(data = emptyList(), isLoading = true),
            next24HoursCount = 0,
            nextWeekCount = 0,
            nextMonthCount = 0,
            combinedLaunches = emptyList(),
            upcomingStartIndex = 0,
            carouselError = null,
            isCarouselLoading = true,
            onCarouselRetry = {},
            isAnyViewLoading = true,
            hasAdFree = true
        )
    }
}

@Preview
@Composable
private fun ResponsiveHomeContentOfflinePreview() {
    SpaceLaunchNowPreviewTheme {
        val launches = listOf(PreviewData.launchNormalSpaceX)
        ResponsiveHomeContent(
            navController = rememberNavController(),
            additionalFeaturedLaunchesState = ViewState(
                data = launches,
                dataSource = DataSource.STALE_CACHE,
                error = "Network unavailable"
            ),
            previousLaunchesState = ViewState(data = launches),
            historyState = ViewState(
                data = HistoryViewModel.HistoryData(count = 1, launches = launches)
            ),
            featuredLaunchState = ViewState(data = launches.first()),
            updatesState = ViewState(data = emptyList()),
            articlesState = ViewState(data = emptyList()),
            eventsState = ViewState(data = emptyList()),
            next24HoursCount = 1,
            nextWeekCount = 3,
            nextMonthCount = 10,
            combinedLaunches = launches,
            upcomingStartIndex = 0,
            carouselError = null,
            isCarouselLoading = false,
            onCarouselRetry = {},
            isAnyViewLoading = false,
            hasAdFree = true,
            isOffline = true,
            oldestCacheTimestamp = 1710000000000L
        )
    }
}

@Preview
@Composable
private fun ResponsiveHomeContentErrorPreview() {
    SpaceLaunchNowPreviewTheme {
        ResponsiveHomeContent(
            navController = rememberNavController(),
            additionalFeaturedLaunchesState = ViewState(data = emptyList()),
            previousLaunchesState = ViewState(data = emptyList()),
            historyState = ViewState(
                data = HistoryViewModel.HistoryData(count = 0, launches = emptyList())
            ),
            featuredLaunchState = ViewState(data = null),
            updatesState = ViewState(data = emptyList()),
            articlesState = ViewState(data = emptyList()),
            eventsState = ViewState(data = emptyList()),
            next24HoursCount = 0,
            nextWeekCount = 0,
            nextMonthCount = 0,
            combinedLaunches = emptyList(),
            upcomingStartIndex = 0,
            carouselError = "Failed to connect to server",
            isCarouselLoading = false,
            onCarouselRetry = {},
            isAnyViewLoading = false,
            hasAdFree = true
        )
    }
}

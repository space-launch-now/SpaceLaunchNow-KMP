package me.calebjones.spacelaunchnow.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.ui.home.components.ResponsiveHomeContent
import me.calebjones.spacelaunchnow.ui.viewmodel.EventsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.FeaturedLaunchViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.FeedViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.HistoryViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchCarouselViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchesViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.StatsViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    // Phase 2: Inject domain-specific ViewModels
    val launchesViewModel = koinViewModel<LaunchesViewModel>()
    val featuredLaunchViewModel = koinViewModel<FeaturedLaunchViewModel>()
    val launchCarouselViewModel = koinViewModel<LaunchCarouselViewModel>()
    val feedViewModel = koinViewModel<FeedViewModel>()
    val eventsViewModel = koinViewModel<EventsViewModel>()
    val historyViewModel = koinViewModel<HistoryViewModel>()
    val statsViewModel = koinViewModel<StatsViewModel>()

    // Collect all ViewStates to check for offline data
    val featuredLaunchState by featuredLaunchViewModel.featuredLaunchState.collectAsStateWithLifecycle()
    val upcomingLaunchesState by launchCarouselViewModel.upcomingLaunchesState.collectAsStateWithLifecycle()
    val updatesState by feedViewModel.updatesState.collectAsStateWithLifecycle()
    val articlesState by feedViewModel.articlesState.collectAsStateWithLifecycle()
    val eventsState by eventsViewModel.eventsState.collectAsStateWithLifecycle()
    val historyState by historyViewModel.historyState.collectAsStateWithLifecycle()

    // Check if ANY data has an error AND is from stale cache (indicating network failure)
    val isOffline = remember(
        featuredLaunchState,
        upcomingLaunchesState,
        updatesState,
        articlesState,
        eventsState,
        historyState
    ) {
        listOf(
            featuredLaunchState,
            upcomingLaunchesState,
            updatesState,
            articlesState,
            eventsState,
            historyState
        ).any { it.error != null && it.dataSource == DataSource.STALE_CACHE }
    }

    // Get the oldest cache timestamp from all stale data that has errors
    val oldestCacheTimestamp = remember(
        featuredLaunchState,
        upcomingLaunchesState,
        updatesState,
        articlesState,
        eventsState,
        historyState
    ) {
        listOfNotNull(
            featuredLaunchState.takeIf { it.error != null && it.dataSource == DataSource.STALE_CACHE }?.cacheTimestamp,
            upcomingLaunchesState.takeIf { it.error != null && it.dataSource == DataSource.STALE_CACHE }?.cacheTimestamp,
            updatesState.takeIf { it.error != null && it.dataSource == DataSource.STALE_CACHE }?.cacheTimestamp,
            articlesState.takeIf { it.error != null && it.dataSource == DataSource.STALE_CACHE }?.cacheTimestamp,
            eventsState.takeIf { it.error != null && it.dataSource == DataSource.STALE_CACHE }?.cacheTimestamp,
            historyState.takeIf { it.error != null && it.dataSource == DataSource.STALE_CACHE }?.cacheTimestamp
        ).minOrNull()
    }

    // Simplified pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            // Refresh all ViewModels
            featuredLaunchViewModel.refresh()
            launchCarouselViewModel.refresh()
            feedViewModel.refreshAll()
            eventsViewModel.refresh()
            statsViewModel.refresh()
            isRefreshing = false
        }
    )

    // Initial load of all sections
    LaunchedEffect(Unit) {
        featuredLaunchViewModel.loadFeaturedLaunch()
        launchCarouselViewModel.loadLaunches()
        feedViewModel.loadUpdates()
        feedViewModel.loadArticles()
        eventsViewModel.loadEvents()
        statsViewModel.loadAllStats()
    }

    // Simplified UI - single offline banner at top of content, each view handles its own loading/error states
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        ResponsiveHomeContent(
            navController = navController,
            launchesViewModel = launchesViewModel,
            featuredLaunchViewModel = featuredLaunchViewModel,
            launchCarouselViewModel = launchCarouselViewModel,
            feedViewModel = feedViewModel,
            eventsViewModel = eventsViewModel,
            historyViewModel = historyViewModel,
            statsViewModel = statsViewModel,
            modifier = Modifier.fillMaxSize(),
            isOffline = isOffline,
            oldestCacheTimestamp = oldestCacheTimestamp,
            onRetry = {
                isRefreshing = true
                launchesViewModel.refresh()
                feedViewModel.refreshAll()
                eventsViewModel.refresh()
                statsViewModel.refresh()
                isRefreshing = false
            }
        )

        // Pull-to-refresh indicator
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import me.calebjones.spacelaunchnow.api.extensions.launchUrl
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.ui.home.components.ResponsiveHomeContent
import me.calebjones.spacelaunchnow.ui.subscription.rememberHasFeature
import me.calebjones.spacelaunchnow.ui.viewmodel.EventsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.FeaturedLaunchViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.FeedViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.HistoryViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchCarouselViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchesViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.StatsViewModel
import me.calebjones.spacelaunchnow.util.LaunchSharingService
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    // Inject domain-specific ViewModels
    val launchesViewModel = koinViewModel<LaunchesViewModel>()
    val featuredLaunchViewModel = koinViewModel<FeaturedLaunchViewModel>()
    val launchCarouselViewModel = koinViewModel<LaunchCarouselViewModel>()
    val feedViewModel = koinViewModel<FeedViewModel>()
    val eventsViewModel = koinViewModel<EventsViewModel>()
    val historyViewModel = koinViewModel<HistoryViewModel>()
    val statsViewModel = koinViewModel<StatsViewModel>()
    val sharingService = koinInject<LaunchSharingService>()
    val coroutineScope = rememberCoroutineScope()

    // Collect all ViewStates for offline detection AND to pass down as hoisted state
    val featuredLaunchState by featuredLaunchViewModel.featuredLaunchState.collectAsStateWithLifecycle()
    val additionalFeaturedLaunchesState by featuredLaunchViewModel.additionalFeaturedLaunches.collectAsStateWithLifecycle()
    val upcomingLaunchesState by launchCarouselViewModel.upcomingLaunchesState.collectAsStateWithLifecycle()
    val previousLaunchesState by launchCarouselViewModel.previousLaunchesState.collectAsStateWithLifecycle()
    val updatesState by feedViewModel.updatesState.collectAsStateWithLifecycle()
    val articlesState by feedViewModel.articlesState.collectAsStateWithLifecycle()
    val eventsState by eventsViewModel.eventsState.collectAsStateWithLifecycle()
    val historyState by historyViewModel.historyState.collectAsStateWithLifecycle()

    // Collect stats
    val next24HoursCount by statsViewModel.next24HoursCount.collectAsStateWithLifecycle()
    val nextWeekCount by statsViewModel.nextWeekCount.collectAsStateWithLifecycle()
    val nextMonthCount by statsViewModel.nextMonthCount.collectAsStateWithLifecycle()

    // Collect carousel derived state
    val combinedLaunches by launchCarouselViewModel.combinedLaunches.collectAsStateWithLifecycle()
    val upcomingStartIndex by launchCarouselViewModel.upcomingStartIndex.collectAsStateWithLifecycle()
    val carouselError by launchCarouselViewModel.carouselError.collectAsStateWithLifecycle()
    val isCarouselLoading by launchCarouselViewModel.isCarouselLoading.collectAsStateWithLifecycle()

    // Check if user has ad-free premium feature
    val hasAdFree by rememberHasFeature(PremiumFeature.AD_FREE)

    // Check if ANY view is loading
    val isAnyViewLoading = remember(
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
        ).any { it.isLoading }
    }

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

    // Get current day/month for This Day in History refresh
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val currentDay = currentDate.day
    val currentMonth = currentDate.month.number

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
            historyViewModel.refresh(day = currentDay, month = currentMonth)
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

    // Load history launches based on current date
    LaunchedEffect(currentDay, currentMonth) {
        if (historyState.data.count == 0 && !historyState.isLoading && historyState.error == null) {
            historyViewModel.loadHistoryLaunches(day = currentDay, month = currentMonth)
        }
    }

    val onRetry: () -> Unit = {
        isRefreshing = true
        launchesViewModel.refresh()
        featuredLaunchViewModel.refresh()
        launchCarouselViewModel.refresh()
        feedViewModel.refreshAll()
        eventsViewModel.refresh()
        statsViewModel.refresh()
        historyViewModel.refresh(day = currentDay, month = currentMonth)
        isRefreshing = false
    }

    // Simplified UI - single offline banner at top of content, each view handles its own loading/error states
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        ResponsiveHomeContent(
            navController = navController,
            additionalFeaturedLaunchesState = additionalFeaturedLaunchesState,
            previousLaunchesState = previousLaunchesState,
            historyState = historyState,
            featuredLaunchState = featuredLaunchState,
            updatesState = updatesState,
            articlesState = articlesState,
            eventsState = eventsState,
            next24HoursCount = next24HoursCount,
            nextWeekCount = nextWeekCount,
            nextMonthCount = nextMonthCount,
            combinedLaunches = combinedLaunches,
            upcomingStartIndex = upcomingStartIndex,
            carouselError = carouselError,
            isCarouselLoading = isCarouselLoading,
            onCarouselRetry = { launchCarouselViewModel.loadLaunches(upcomingLimit = 10, forceRefresh = true) },
            isAnyViewLoading = isAnyViewLoading,
            hasAdFree = hasAdFree,
            onShareLaunch = { launchToShare ->
                coroutineScope.launch {
                    sharingService.shareUrl(launchToShare.launchUrl)
                }
            },
            modifier = Modifier.fillMaxSize(),
            isOffline = isOffline,
            oldestCacheTimestamp = oldestCacheTimestamp,
            onRetry = onRetry
        )

        // Pull-to-refresh indicator
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

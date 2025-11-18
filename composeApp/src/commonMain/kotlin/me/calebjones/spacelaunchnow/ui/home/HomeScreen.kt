package me.calebjones.spacelaunchnow.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import me.calebjones.spacelaunchnow.ui.components.OfflineBanner
import me.calebjones.spacelaunchnow.ui.home.components.ResponsiveHomeContent
import me.calebjones.spacelaunchnow.ui.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    val homeViewModel = koinViewModel<HomeViewModel>()
    
    // Collect all ViewStates to check for offline data
    val featuredLaunchState by homeViewModel.featuredLaunchState.collectAsStateWithLifecycle()
    val upcomingLaunchesState by homeViewModel.upcomingLaunchesState.collectAsStateWithLifecycle()
    val updatesState by homeViewModel.updatesState.collectAsStateWithLifecycle()
    val articlesState by homeViewModel.articlesState.collectAsStateWithLifecycle()
    val eventsState by homeViewModel.eventsState.collectAsStateWithLifecycle()
    val historyState by homeViewModel.historyState.collectAsStateWithLifecycle()
    
    // Check if ANY data has an error AND is from stale cache (indicating network failure)
    val isOffline = remember(featuredLaunchState, upcomingLaunchesState, updatesState, articlesState, eventsState, historyState) {
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
    val oldestCacheTimestamp = remember(featuredLaunchState, upcomingLaunchesState, updatesState, articlesState, eventsState, historyState) {
        listOf(
            featuredLaunchState.takeIf { it.error != null && it.dataSource == DataSource.STALE_CACHE }?.cacheTimestamp,
            upcomingLaunchesState.takeIf { it.error != null && it.dataSource == DataSource.STALE_CACHE }?.cacheTimestamp,
            updatesState.takeIf { it.error != null && it.dataSource == DataSource.STALE_CACHE }?.cacheTimestamp,
            articlesState.takeIf { it.error != null && it.dataSource == DataSource.STALE_CACHE }?.cacheTimestamp,
            eventsState.takeIf { it.error != null && it.dataSource == DataSource.STALE_CACHE }?.cacheTimestamp,
            historyState.takeIf { it.error != null && it.dataSource == DataSource.STALE_CACHE }?.cacheTimestamp
        ).filterNotNull().minOrNull()
    }
    
    // Simplified pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            // Refresh all sections with forceRefresh = true
            homeViewModel.refreshAll(
                onComplete = { isRefreshing = false }
            )
        }
    )

    // Initial load of all sections
    LaunchedEffect(Unit) {
        homeViewModel.loadHomeScreenData()
    }

    // Simplified UI - single offline banner at top of content, each view handles its own loading/error states
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        ResponsiveHomeContent(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
            isOffline = isOffline,
            oldestCacheTimestamp = oldestCacheTimestamp,
            onRetry = {
                isRefreshing = true
                homeViewModel.refreshAll(
                    onComplete = { isRefreshing = false }
                )
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

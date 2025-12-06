package me.calebjones.spacelaunchnow.ui.starship

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.ui.starship.components.StarshipEventsTab
import me.calebjones.spacelaunchnow.ui.starship.components.StarshipOverviewTab
import me.calebjones.spacelaunchnow.ui.starship.components.StarshipVehiclesTab
import me.calebjones.spacelaunchnow.ui.viewmodel.StarshipTab
import me.calebjones.spacelaunchnow.ui.viewmodel.StarshipViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main Starship Dashboard screen with three tabs: Overview, Events, and Vehicles
 *
 * Displays Starship program-specific content including next launch, status updates,
 * news/events, and spacecraft vehicles.
 *
 * Features:
 * - Per-section ViewState for independent loading/error states
 * - Shimmer loading skeletons
 * - Pull-to-refresh support
 * - Stale data indicators
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun StarshipScreen(
    navController: NavController,
    viewModel: StarshipViewModel = koinViewModel()
) {
    // Collect per-section states
    val programState by viewModel.programState.collectAsStateWithLifecycle()
    val nextLaunchState by viewModel.nextLaunchState.collectAsStateWithLifecycle()
    val updatesState by viewModel.updatesState.collectAsStateWithLifecycle()
    val eventsState by viewModel.eventsState.collectAsStateWithLifecycle()
    val newsState by viewModel.newsState.collectAsStateWithLifecycle()

    // Vehicles tab states - hierarchical navigation
    val vehicleNavigationLevel by viewModel.vehicleNavigationLevel.collectAsStateWithLifecycle()
    val selectedVehicleType by viewModel.selectedVehicleType.collectAsStateWithLifecycle()
    val selectedLauncherConfig by viewModel.selectedLauncherConfig.collectAsStateWithLifecycle()
    val selectedSpacecraftConfig by viewModel.selectedSpacecraftConfig.collectAsStateWithLifecycle()

    // Config states (level 1)
    val launcherConfigsState by viewModel.launcherConfigsState.collectAsStateWithLifecycle()
    val spacecraftConfigsState by viewModel.spacecraftConfigsState.collectAsStateWithLifecycle()

    // Vehicle states (level 2)
    val spacecraftState by viewModel.spacecraftState.collectAsStateWithLifecycle()
    val spacecraftHasMore by viewModel.spacecraftHasMore.collectAsStateWithLifecycle()
    val spacecraftLoadingMore by viewModel.spacecraftLoadingMore.collectAsStateWithLifecycle()
    val launchersState by viewModel.launchersState.collectAsStateWithLifecycle()
    val launchersHasMore by viewModel.launchersHasMore.collectAsStateWithLifecycle()
    val launchersLoadingMore by viewModel.launchersLoadingMore.collectAsStateWithLifecycle()

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val videoPlayerState by viewModel.videoPlayerState.collectAsStateWithLifecycle()
    val isAnyLoading by viewModel.isAnyLoading.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    // Pull-to-refresh state (using Material, not Material3)
    var isRefreshing by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.refresh()
        }
    )

    // End refresh when loading completes
    LaunchedEffect(isAnyLoading) {
        if (!isAnyLoading && isRefreshing) {
            isRefreshing = false
        }
    }

    // Sync pager state with ViewModel tab state
    LaunchedEffect(pagerState.currentPage) {
        val tab = when (pagerState.currentPage) {
            0 -> StarshipTab.Overview
            1 -> StarshipTab.Events
            2 -> StarshipTab.Vehicles
            else -> StarshipTab.Overview
        }
        viewModel.switchTab(tab)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Starship",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        // Don't add extra content padding - PhoneLayout already handles bottom bar padding
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Tab Row
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        text = { Text("Overview") }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        text = { Text("Events") }
                    )
                    Tab(
                        selected = pagerState.currentPage == 2,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        },
                        text = { Text("Vehicles") }
                    )
                }

                // Horizontal Pager for tab content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> StarshipOverviewTab(
                            programState = programState,
                            nextLaunchState = nextLaunchState,
                            updatesState = updatesState,
                            videoPlayerState = videoPlayerState,
                            navController = navController,
                            onRefresh = { viewModel.refresh() },
                            onSetPlayerVisible = { viewModel.setPlayerVisible(it) },
                            onVideoSelected = { viewModel.setVideoIndex(it) },
                            onNavigateToFullscreen = { videoId, title ->
                                // TODO: Navigate to fullscreen video player if needed
                            }
                        )

                        1 -> StarshipEventsTab(
                            eventsState = eventsState,
                            newsState = newsState,
                            navController = navController,
                            onRefresh = { viewModel.refresh() }
                        )

                        2 -> StarshipVehiclesTab(
                            // Navigation state
                            navigationLevel = vehicleNavigationLevel,
                            selectedVehicleType = selectedVehicleType,
                            selectedLauncherConfig = selectedLauncherConfig,
                            selectedSpacecraftConfig = selectedSpacecraftConfig,
                            // Config states (level 1)
                            launcherConfigsState = launcherConfigsState,
                            spacecraftConfigsState = spacecraftConfigsState,
                            // Vehicle states (level 2)
                            spacecraftState = spacecraftState,
                            spacecraftHasMore = spacecraftHasMore,
                            spacecraftLoadingMore = spacecraftLoadingMore,
                            onLoadMoreSpacecraft = { viewModel.loadMoreSpacecraft() },
                            launchersState = launchersState,
                            launchersHasMore = launchersHasMore,
                            launchersLoadingMore = launchersLoadingMore,
                            onLoadMoreLaunchers = { viewModel.loadMoreLaunchers() },
                            // Navigation callbacks
                            onSelectLauncherConfig = { viewModel.selectLauncherConfig(it) },
                            onSelectSpacecraftConfig = { viewModel.selectSpacecraftConfig(it) },
                            onNavigateBack = { viewModel.navigateBackToConfigs() },
                            onRefresh = { viewModel.refresh() }
                        )
                    }
                }
            }

            // Pull-to-refresh indicator
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}


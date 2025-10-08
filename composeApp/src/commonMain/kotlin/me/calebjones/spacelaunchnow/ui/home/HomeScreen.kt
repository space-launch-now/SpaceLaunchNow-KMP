package me.calebjones.spacelaunchnow.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.ui.compose.LaunchListShimmer
import me.calebjones.spacelaunchnow.ui.compose.NextUpShimmerBox
import me.calebjones.spacelaunchnow.ui.compose.UpdatesShimmer
import me.calebjones.spacelaunchnow.ui.home.components.HomeTopBar
import me.calebjones.spacelaunchnow.ui.home.components.NewsItemShimmer
import me.calebjones.spacelaunchnow.ui.home.components.ResponsiveHomeContent
import me.calebjones.spacelaunchnow.ui.home.components.SectionTitle
import me.calebjones.spacelaunchnow.ui.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    val homeViewModel = koinViewModel<HomeViewModel>()
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Individual loading states to ensure ALL network calls are complete
    val isFeaturedLaunchLoading by homeViewModel.isFeaturedLaunchLoading.collectAsStateWithLifecycle()
    val isUpcomingLaunchesLoading by homeViewModel.isUpcomingLaunchesLoading.collectAsStateWithLifecycle()
    val isUpdatesLoading by homeViewModel.isUpdatesLoading.collectAsStateWithLifecycle()
    val isArticlesLoading by homeViewModel.isArticlesLoading.collectAsStateWithLifecycle()

    // Error states
    val featuredLaunchError by homeViewModel.featuredLaunchError.collectAsStateWithLifecycle()
    val upcomingLaunchesError by homeViewModel.upcomingLaunchesError.collectAsStateWithLifecycle()
    val updatesError by homeViewModel.updatesError.collectAsStateWithLifecycle()
    val articlesError by homeViewModel.articlesError.collectAsStateWithLifecycle()

    // Data states
    val featuredLaunch by homeViewModel.featuredLaunch.collectAsStateWithLifecycle()
    val upcomingLaunches by homeViewModel.upcomingLaunches.collectAsStateWithLifecycle()
    val updates by homeViewModel.updates.collectAsStateWithLifecycle()
    val articles by homeViewModel.articles.collectAsStateWithLifecycle()

    // Wait for ALL network calls to complete (success or error)
    val allNetworkCallsComplete = !isFeaturedLaunchLoading && !isUpcomingLaunchesLoading && !isUpdatesLoading && !isArticlesLoading

    // Check if we have any data or if all failed
    val hasAnyData =
        featuredLaunch != null || upcomingLaunches.isNotEmpty() || updates.isNotEmpty() || articles.isNotEmpty()
    val hasAnyErrors =
        featuredLaunchError != null || upcomingLaunchesError != null || updatesError != null || articlesError != null

    // Animation states
    var showContent by remember { mutableStateOf(false) }
    var hasInitiallyLoaded by remember { mutableStateOf(false) }
    val pulseScale = remember { Animatable(1f) }
    
    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            // Don't hide content on refresh - keep it visible
            homeViewModel.refreshHomeScreenData()
        }
    )

    // Load all home screen data when the screen is first displayed
    LaunchedEffect(Unit) {
        homeViewModel.loadHomeScreenData()
    }

    // Monitor lifecycle and retry failed requests when app comes back to foreground
    DisposableEffect(lifecycleOwner, hasAnyErrors, hasAnyData) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // App came back to foreground - retry any failed requests
                    if (hasAnyErrors && !hasAnyData) {
                        println("HomeScreen: App resumed with errors and no data - retrying requests")
                        homeViewModel.retryFailedRequests()
                    } else if (hasAnyErrors) {
                        println("HomeScreen: App resumed with some errors - retrying failed sections only")
                        homeViewModel.retryFailedRequests()
                    }
                }
                else -> { /* Handle other lifecycle events if needed */
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Handle completion of network calls
    LaunchedEffect(allNetworkCallsComplete) {
        if (allNetworkCallsComplete) {
            if (isRefreshing) {
                // Pull-to-refresh completed - trigger pulse animation
                isRefreshing = false
                
                // Pulse animation
                pulseScale.animateTo(
                    targetValue = 1.015f,
                    animationSpec = tween(durationMillis = 150)
                )
                pulseScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 150)
                )
            } else if (!hasInitiallyLoaded) {
                showContent = true
                hasInitiallyLoaded = true
            }
        }
    }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            // Show loading shimmer only on initial load (not during refresh)
            if (!allNetworkCallsComplete && !hasInitiallyLoaded) {
                // Master loading state - show shimmer for entire screen
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item { NextUpShimmerBox() }
                    item { SectionTitle(title = "Upcoming Launches", hasAction = true) }
                    item { LaunchListShimmer(cardCount = 3) }

                    item { SectionTitle(title = "Latest Updates", hasAction = false) }
                    item { UpdatesShimmer(cardCount = 4) }

                    item { SectionTitle(title = "News and Events", hasAction = false) }
                    items(3) { 
                        NewsItemShimmer()
                    }
                }
            } else if (hasInitiallyLoaded || showContent) {
                // Show content either after initial load or during refresh
                val contentModifier = if (hasInitiallyLoaded) {
                    // Apply pulse scale during refresh
                    Modifier
                        .fillMaxSize()
                        .scale(pulseScale.value)
                } else {
                    Modifier.fillMaxSize()
                }
                
                // Content with conditional animation wrapper
                if (hasInitiallyLoaded) {
                    // Already loaded - show content directly with pulse scale
                    ResponsiveHomeContent(
                        navController = navController,
                        modifier = contentModifier
                    )
                } else {
                    // Initial load - show with fade-in animation
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 800,
                            )
                        ) + slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialOffsetY = { it / 2 }
                        ),
                        exit = fadeOut(
                            animationSpec = tween(
                                durationMillis = 400,
                                delayMillis = 0
                            )
                        ) + slideOutVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            targetOffsetY = { it / 2 }
                        )
                    ) {
                        ResponsiveHomeContent(
                            navController = navController,
                            modifier = contentModifier
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

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavController
import kotlinx.coroutines.delay
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
    
    // Individual loading states to ensure ALL network calls are complete
    val isFeaturedLaunchLoading by homeViewModel.isFeaturedLaunchLoading.collectAsState()
    val isUpcomingLaunchesLoading by homeViewModel.isUpcomingLaunchesLoading.collectAsState()
    val isUpdatesLoading by homeViewModel.isUpdatesLoading.collectAsState()
    val isArticlesLoading by homeViewModel.isArticlesLoading.collectAsState()
    
    // Wait for ALL network calls to complete (success or error)
    val allNetworkCallsComplete = !isFeaturedLaunchLoading && !isUpcomingLaunchesLoading && !isUpdatesLoading && !isArticlesLoading
    
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
    
    // Handle completion of network calls
    LaunchedEffect(allNetworkCallsComplete) {
        if (allNetworkCallsComplete) {
            if (isRefreshing) {
                // Pull-to-refresh completed - trigger pulse animation
                delay(200) // Brief delay to show refresh completed
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
                // Initial load - show content with fade-in animation
                delay(100)
                showContent = true
                hasInitiallyLoaded = true
            }
        }
    }

    Scaffold {
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
                    item { HomeTopBar() }
                    
                    item {
                        NextUpShimmerBox()
                    }

                    item { SectionTitle(title = "Upcoming Launches", hasAction = true) }
                    item { LaunchListShimmer(cardCount = 3) }

                    item { SectionTitle(title = "Latest Updates", hasAction = true) }
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
                                durationMillis = 500,
                                delayMillis = 0
                            )
                        ) + slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            initialOffsetY = { it / 3 }
                        ),
                        exit = fadeOut(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = 0
                            )
                        ) + slideOutVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            targetOffsetY = { it / 3 }
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
}

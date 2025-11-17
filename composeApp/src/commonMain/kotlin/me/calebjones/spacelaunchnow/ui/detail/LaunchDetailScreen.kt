package me.calebjones.spacelaunchnow.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.navigation.FullscreenVideo
import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailView
import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailErrorView
import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailLoadingView
import me.calebjones.spacelaunchnow.ui.ads.InterstitialAdHandler
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LaunchDetailScreen(
    launchId: String,
    onNavigateBack: () -> Unit,
    navController: NavHostController? = null
) {
    val viewModel = koinViewModel<LaunchViewModel>()
    val launchCache = koinInject<LaunchCache>()
    
    // Check if we have pre-loaded detailed data in cache
    val cachedLaunchDetailed = remember(launchId) { launchCache.getCachedLaunchDetailed(launchId) }
    val launchDetails by viewModel.launchDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val videoPlayerState by viewModel.videoPlayerState.collectAsState()
    
    // Related news state
    val relatedNews by viewModel.relatedNews.collectAsState()
    val isNewsLoading by viewModel.isNewsLoading.collectAsState()
    val newsError by viewModel.newsError.collectAsState()
    
    // Determine current launch data
    val currentLaunch = cachedLaunchDetailed ?: launchDetails

    // Pull-to-refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.refreshLaunchDetails(launchId)
        }
    )
    
    // Stop refreshing when loading completes
    LaunchedEffect(isLoading) {
        if (!isLoading && isRefreshing) {
            isRefreshing = false
        }
    }

    // Handle loading logic
    LaunchedEffect(launchId) {
        if (cachedLaunchDetailed != null) {
            // We have preloaded data, set it immediately to avoid loading state
            viewModel.setLaunchDetails(cachedLaunchDetailed)
        } else if (launchDetails == null && !isLoading) {
            // No preloaded data and not currently loading, fetch from API
            viewModel.fetchLaunchDetails(launchId)
        }
        
        // Fetch related news for this launch
        viewModel.fetchRelatedNews(launchId)
    }

    // 🎯 INTERSTITIAL AD: Show every 4th detail view visit
    InterstitialAdHandler(
        onAdShown = {
            println("✅ LaunchDetail: Interstitial ad shown successfully")
        },
        onAdFailed = { error ->
            println("❌ LaunchDetail: Interstitial ad failed: $error")
        }
    )

    // Only render the view when we have launch data, show loading/error states otherwise
    val errorMessage = error
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        when {
            errorMessage != null -> {
                LaunchDetailErrorView(
                    errorMessage = errorMessage,
                    onRetry = { viewModel.fetchLaunchDetails(launchId) },
                    onNavigateBack = onNavigateBack
                )
            }

            currentLaunch != null -> {
                // We have launch data, pass non-null launch to view
                LaunchDetailView(
                    launch = currentLaunch,
                    videoPlayerState = videoPlayerState,
                    relatedNews = relatedNews,
                    isNewsLoading = isNewsLoading,
                    newsError = newsError,
                    onSelectVideo = viewModel::selectVideo,
                    onSetPlayerVisible = viewModel::setPlayerVisible,
                    onNavigateBack = onNavigateBack,
                    onNavigateToFullscreen = { videoUrl, launchName ->
                        navController?.navigate(
                            FullscreenVideo(
                                launchId = launchId,
                                videoUrl = videoUrl,
                                launchName = launchName
                            )
                        )
                    },
                    onVideoSelected = viewModel::selectVideo
                )
            }

            else -> {
                // Show loading state
                LaunchDetailLoadingView(onNavigateBack = onNavigateBack)
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



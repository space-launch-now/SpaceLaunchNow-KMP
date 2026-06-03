package me.calebjones.spacelaunchnow.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.navigation.AstronautDetail
import me.calebjones.spacelaunchnow.navigation.EventDetail
import me.calebjones.spacelaunchnow.navigation.FullscreenVideo
import me.calebjones.spacelaunchnow.navigation.NewsDetail
import me.calebjones.spacelaunchnow.ui.ads.InterstitialAdHandler
import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailErrorView
import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailLoadingView
import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailView
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LaunchDetailScreen(
    launchId: String,
    onNavigateBack: (() -> Unit)? = null,
    navController: NavHostController? = null,
    onOpenFullscreen: ((String) -> Unit)? = null,
    forcePhoneLayout: Boolean = false
) {
    val log = SpaceLogger.getLogger("LaunchDetailScreen")
    val viewModel = koinViewModel<LaunchViewModel>()
    val launchCache = koinInject<LaunchCache>()
    val uriHandler = LocalUriHandler.current

    // Check if we have pre-loaded detailed data in cache
    val cachedLaunchDetailed = remember(launchId) { launchCache.getCachedLaunch(launchId) }
    val launchDetails by viewModel.launchDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val videoPlayerState by viewModel.videoPlayerState.collectAsState()

    // Related news state
    val relatedNews by viewModel.relatedNews.collectAsState()
    val isNewsLoading by viewModel.isNewsLoading.collectAsState()
    val newsError by viewModel.newsError.collectAsState()

    // Related events state
    val relatedEvents by viewModel.relatedEvents.collectAsState()
    val isEventsLoading by viewModel.isEventsLoading.collectAsState()
    val eventsError by viewModel.eventsError.collectAsState()

    // Stale-while-revalidate state: shows progress bar when refreshing with stale data
    val isRefreshingWithStaleData by viewModel.isRefreshingWithStaleData.collectAsState()

    // Determine current launch data. Prefer the live ViewModel state — it carries
    // both the initial cached preview (set via setLaunchDetails on entry) and any
    // fresh data from a network refresh. Falling back to cachedLaunchDetailed only
    // covers the brief gap before LaunchedEffect(launchId) populates the ViewModel.
    // The previous order (cachedLaunchDetailed ?: launchDetails) caused pull-to-refresh
    // to render a stale snapshot frozen by remember(launchId).
    val currentLaunch = launchDetails ?: cachedLaunchDetailed

    // Pull-to-refresh state — driven by ViewModel.isRefreshing, which spans the full
    // refreshLaunchDetails+news+events flow and resets in a finally block.
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    // Handle loading logic
    LaunchedEffect(launchId) {
        if (cachedLaunchDetailed != null) {
            // We have preloaded data, set it immediately to avoid loading state
            viewModel.setLaunchDetails(cachedLaunchDetailed)
        } else {
            // Let ViewModel handle stale-while-revalidate pattern
            // It will show stale data with progress bar if available, or shimmer if not
            viewModel.fetchLaunchDetails(launchId)
        }

        // Fetch related news for this launch
        viewModel.fetchRelatedNews(launchId)

        // Fetch related events for this launch
        viewModel.fetchRelatedEvents(launchId)
    }

    // 🎯 INTERSTITIAL AD: Show every 4th detail view visit
    InterstitialAdHandler()

    // Only render the view when we have launch data, show loading/error states otherwise
    val errorMessage = error

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            log.i { "👇 Pull-to-refresh gesture fired for launch $launchId" }
            viewModel.refreshLaunchDetails(launchId)
        },
        state = pullRefreshState,
        modifier = Modifier.fillMaxSize(),
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = pullRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
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
                    relatedEvents = relatedEvents,
                    isEventsLoading = isEventsLoading,
                    eventsError = eventsError,
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
                    onVideoSelected = viewModel::selectVideo,
                    onNavigateToSettings = {
                        navController?.navigate(
                            me.calebjones.spacelaunchnow.navigation.SupportUs
                        )
                    },
                    onEventClick = { eventId ->
                        navController?.navigate(EventDetail(eventId = eventId))
                    },
                    onAstronautClick = { astronautId ->
                        navController?.navigate(AstronautDetail(astronautId = astronautId))
                    },
                    onArticleClick = { url, title ->
                        navController?.navigate(NewsDetail(url = url, title = title))
                    },
                    forcePhoneLayout = forcePhoneLayout,
                    onOpenUrl = { url ->
                        viewModel.trackLinkOpened(url, launchId)
                        try {
                            uriHandler.openUri(url)
                        } catch (_: Throwable) {
                        }
                    },
                    onExternalVideoOpened = { videoUrl, videoSource ->
                        viewModel.trackVideoOpened(videoUrl, videoSource)
                    }
                )
            }

            else -> {
                // Show loading state
                LaunchDetailLoadingView(onNavigateBack = onNavigateBack)
            }
        }

        // Show progress bar when refreshing with stale data displayed
        if (isRefreshingWithStaleData) {
            LinearWavyProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            )
        }

        // Fullscreen button when embedded in list-detail pane
        if (onOpenFullscreen != null) {
            SmallFloatingActionButton(
                onClick = { onOpenFullscreen(launchId) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Open fullscreen",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}



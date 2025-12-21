package me.calebjones.spacelaunchnow.ui.spacestation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import me.calebjones.spacelaunchnow.ui.ads.InterstitialAdHandler
import me.calebjones.spacelaunchnow.ui.viewmodel.SpaceStationViewModel
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import org.koin.compose.viewmodel.koinViewModel

/**
 * Space Station detail screen
 * Displays comprehensive information about a space station including:
 * - Station details and status
 * - Current crew and expedition
 * - ISS-specific: Live position map, orbit tracking, NASA live stream
 * - Related news reports
 */
@Composable
fun SpaceStationDetailScreen(
    stationId: Int,
    onNavigateBack: () -> Unit
) {
    val log = SpaceLogger.getLogger("SpaceStationDetailScreen")
    val viewModel = koinViewModel<SpaceStationViewModel>()
    
    val stationDetails by viewModel.stationDetails.collectAsState()
    val activeExpeditions by viewModel.activeExpeditions.collectAsState()
    val issPosition by viewModel.issPosition.collectAsState()
    val issPositionData by viewModel.issPositionData.collectAsState()
    val orbitPath by viewModel.orbitPath.collectAsState()
    val articles by viewModel.articles.collectAsState()
    val videoPlayerState by viewModel.videoPlayerState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(stationId) {
        if (stationDetails?.id != stationId && !isLoading) {
            log.d { "Fetching station details for ID: $stationId" }
            viewModel.fetchStationDetails(stationId)
        }
    }

    // Lifecycle observer to pause/resume ISS tracking
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    log.d { "App paused, stopping ISS tracking" }
                    viewModel.pauseTracking()
                }
                Lifecycle.Event.ON_RESUME -> {
                    log.d { "App resumed, resuming ISS tracking" }
                    viewModel.resumeTracking()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Interstitial ad handler
    InterstitialAdHandler()

    when {
        error != null -> {
            SpaceStationDetailErrorView(
                errorMessage = error!!,
                onRetry = { viewModel.fetchStationDetails(stationId) },
                onNavigateBack = onNavigateBack
            )
        }

        stationDetails != null -> {
            SpaceStationDetailView(
                station = stationDetails!!,
                activeExpeditions = activeExpeditions,
                issPosition = issPosition,
                issPositionData = issPositionData,
                orbitPath = orbitPath,
                articles = articles,
                videoPlayerState = videoPlayerState,
                onSetPlayerVisible = viewModel::setPlayerVisible,
                onVideoSelected = viewModel::selectVideo,
                onNavigateBack = onNavigateBack,
                onNavigateToFullscreen = { url, title -> 
                    // TODO: Navigate to fullscreen video screen
                }
            )
        }

        else -> {
            SpaceStationDetailLoadingView(onNavigateBack = onNavigateBack)
        }
    }
}

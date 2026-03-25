package me.calebjones.spacelaunchnow.ui.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import me.calebjones.spacelaunchnow.navigation.AgencyDetail
import me.calebjones.spacelaunchnow.navigation.AstronautDetail
import me.calebjones.spacelaunchnow.navigation.FullscreenVideo
import me.calebjones.spacelaunchnow.navigation.LaunchDetail
import me.calebjones.spacelaunchnow.navigation.SpaceStationDetail
import me.calebjones.spacelaunchnow.ui.ads.InterstitialAdHandler
import me.calebjones.spacelaunchnow.ui.viewmodel.EventViewModel
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EventDetailScreen(
    eventId: Int,
    onNavigateBack: () -> Unit,
    navController: NavController? = null
) {
    val log = SpaceLogger.getLogger("EventDetailScreen")
    val viewModel = koinViewModel<EventViewModel>()
    val eventDetails by viewModel.eventDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val videoPlayerState by viewModel.videoPlayerState.collectAsState()

    LaunchedEffect(eventId) {
        if (eventDetails?.id != eventId && !isLoading) {
            viewModel.fetchEventDetails(eventId)
        }
    }

    // 🎯 INTERSTITIAL AD: Show every 4th detail view visit
    InterstitialAdHandler()

    when {
        error != null -> {
            EventDetailErrorView(
                errorMessage = error!!,
                onRetry = { viewModel.fetchEventDetails(eventId) },
                onNavigateBack = onNavigateBack
            )
        }

        eventDetails != null -> {
            EventDetailView(
                event = eventDetails!!,
                onNavigateBack = onNavigateBack,
                videoPlayerState = videoPlayerState,
                onSelectVideo = viewModel::selectVideo,
                onSetPlayerVisible = viewModel::setPlayerVisible,
                onNavigateToFullscreen = { videoUrl, eventName ->
                    navController?.navigate(
                        FullscreenVideo(
                            launchId = eventId.toString(),
                            videoUrl = videoUrl,
                            launchName = eventName
                        )
                    )
                },
                onAgencyClick = navController?.let { nav ->
                    { agencyId: Int -> nav.navigate(AgencyDetail(agencyId)) }
                },
                onLaunchClick = navController?.let { nav ->
                    { launchId: String -> nav.navigate(LaunchDetail(launchId)) }
                },
                onAstronautClick = navController?.let { nav ->
                    { astronautId: Int -> nav.navigate(AstronautDetail(astronautId)) }
                },
                onSpaceStationClick = navController?.let { nav ->
                    { stationId: Int -> nav.navigate(SpaceStationDetail(stationId)) }
                },
            )
        }

        else -> {
            EventDetailLoadingView(onNavigateBack = onNavigateBack)
        }
    }
}

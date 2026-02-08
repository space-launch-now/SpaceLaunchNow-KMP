package me.calebjones.spacelaunchnow.ui.rockets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.calebjones.spacelaunchnow.ui.ads.InterstitialAdHandler
import me.calebjones.spacelaunchnow.ui.rockets.compose.RocketDetailErrorView
import me.calebjones.spacelaunchnow.ui.rockets.compose.RocketDetailLoadingView
import me.calebjones.spacelaunchnow.ui.rockets.compose.RocketDetailView
import me.calebjones.spacelaunchnow.ui.viewmodel.RocketViewModel
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RocketDetailScreen(
    rocketId: Int,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<RocketViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val rocketDetails by viewModel.rocketDetails.collectAsState()

    val log = SpaceLogger.getLogger("RocketDetailScreen")

    LaunchedEffect(rocketId) {
        if (rocketDetails?.id != rocketId) {
            viewModel.fetchRocketDetails(rocketId)
        }
    }

    // Interstitial ad handler (shows every 4th detail view visit)
    InterstitialAdHandler()

    when {
        uiState.error != null -> {
            RocketDetailErrorView(
                errorMessage = uiState.error!!,
                onRetry = { viewModel.fetchRocketDetails(rocketId) },
                onNavigateBack = onNavigateBack
            )
        }

        rocketDetails != null -> {
            RocketDetailView(
                rocket = rocketDetails!!,
                onNavigateBack = onNavigateBack
            )
        }

        else -> {
            RocketDetailLoadingView(onNavigateBack = onNavigateBack)
        }
    }
}

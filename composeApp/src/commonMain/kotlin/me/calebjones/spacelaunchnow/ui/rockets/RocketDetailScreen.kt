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
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RocketDetailScreen(
    rocketId: Int,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<RocketViewModel>()
    val rocketDetails by viewModel.rocketDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(rocketId) {
        if (rocketDetails?.id != rocketId && !isLoading) {
            viewModel.fetchRocketDetails(rocketId)
        }
    }

    // Interstitial ad handler (shows every 4th detail view visit)
    InterstitialAdHandler(
        onAdShown = {
            println("✅ RocketDetail: Interstitial ad shown successfully")
        },
        onAdFailed = { error ->
            println("❌ RocketDetail: Interstitial ad failed: $error")
        }
    )

    when {
        error != null -> {
            RocketDetailErrorView(
                errorMessage = error!!,
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

package me.calebjones.spacelaunchnow.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.calebjones.spacelaunchnow.ui.detail.compose.EventDetailErrorView
import me.calebjones.spacelaunchnow.ui.detail.compose.EventDetailLoadingView
import me.calebjones.spacelaunchnow.ui.detail.compose.EventDetailView
import me.calebjones.spacelaunchnow.ui.ads.InterstitialAdHandler
import me.calebjones.spacelaunchnow.ui.viewmodel.EventViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EventDetailScreen(
    eventId: Int,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<EventViewModel>()
    val eventDetails by viewModel.eventDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(eventId) {
        if (eventDetails?.id != eventId && !isLoading) {
            viewModel.fetchEventDetails(eventId)
        }
    }

    // 🎯 INTERSTITIAL AD: Show every 4th detail view visit
    InterstitialAdHandler(
        onAdShown = {
            println("✅ EventDetail: Interstitial ad shown successfully")
        },
        onAdFailed = { error ->
            println("❌ LaunchDetail: Interstitial ad failed: $error")
        }
    )

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
                onNavigateBack = onNavigateBack
            )
        }

        else -> {
            EventDetailLoadingView(onNavigateBack = onNavigateBack)
        }
    }
}

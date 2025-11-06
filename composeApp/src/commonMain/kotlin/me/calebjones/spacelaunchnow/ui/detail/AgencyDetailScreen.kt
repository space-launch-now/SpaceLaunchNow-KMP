package me.calebjones.spacelaunchnow.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.calebjones.spacelaunchnow.ui.detail.compose.AgencyDetailErrorView
import me.calebjones.spacelaunchnow.ui.detail.compose.AgencyDetailLoadingView
import me.calebjones.spacelaunchnow.ui.detail.compose.AgencyDetailView
import me.calebjones.spacelaunchnow.ui.ads.InterstitialAdHandler
import me.calebjones.spacelaunchnow.ui.viewmodel.AgencyViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AgencyDetailScreen(
    agencyId: Int,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<AgencyViewModel>()
    val agencyDetails by viewModel.agencyDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(agencyId) {
        if (agencyDetails?.id != agencyId && !isLoading) {
            viewModel.fetchAgencyDetails(agencyId)
        }
    }

    // 🎯 INTERSTITIAL AD: Show every 4th detail view visit
    InterstitialAdHandler(
        onAdShown = {
            println("✅ AgencyDetail: Interstitial ad shown successfully")
        },
        onAdFailed = { error ->
            println("❌ AgencyDetail: Interstitial ad failed: $error")
        }
    )

    when {
        error != null -> {
            AgencyDetailErrorView(
                errorMessage = error!!,
                onRetry = { viewModel.fetchAgencyDetails(agencyId) },
                onNavigateBack = onNavigateBack
            )
        }

        agencyDetails != null -> {
            AgencyDetailView(
                agency = agencyDetails!!,
                onNavigateBack = onNavigateBack
            )
        }

        else -> {
            AgencyDetailLoadingView(onNavigateBack = onNavigateBack)
        }
    }
}

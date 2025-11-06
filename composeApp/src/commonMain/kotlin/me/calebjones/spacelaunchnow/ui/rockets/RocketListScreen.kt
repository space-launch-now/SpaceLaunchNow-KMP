package me.calebjones.spacelaunchnow.ui.rockets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.calebjones.spacelaunchnow.ui.rockets.compose.RocketListView
import me.calebjones.spacelaunchnow.ui.viewmodel.RocketViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RocketListScreen(
    onNavigateToRocketDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<RocketViewModel>()
    val rockets by viewModel.rockets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        if (rockets.isEmpty() && !isLoading && error == null) {
            viewModel.fetchRockets(limit = 50)
        }
    }

    RocketListView(
        rockets = rockets,
        isLoading = isLoading,
        error = error,
        onRocketClick = onNavigateToRocketDetail,
        onNavigateBack = onNavigateBack,
        onRetry = { viewModel.fetchRockets(limit = 50) }
    )
}

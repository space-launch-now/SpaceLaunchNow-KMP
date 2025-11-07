package me.calebjones.spacelaunchnow.ui.agencies

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import me.calebjones.spacelaunchnow.ui.agencies.compose.AgencyListView
import me.calebjones.spacelaunchnow.ui.viewmodel.AgencyViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AgencyListScreen(
    onNavigateToAgencyDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<AgencyViewModel>()
    val agencies by viewModel.agencies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        if (agencies.isEmpty() && !isLoading && error == null) {
            viewModel.fetchAgencies(limit = 50)
        }
    }

    AgencyListView(
        agencies = agencies,
        isLoading = isLoading,
        error = error,
        onAgencyClick = onNavigateToAgencyDetail,
        onNavigateBack = onNavigateBack,
        onRetry = { viewModel.fetchAgencies(limit = 50) }
    )
}

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
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val error by viewModel.error.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filters by viewModel.filters.collectAsState()

    LaunchedEffect(Unit) {
        if (rockets.isEmpty() && !isLoading && error == null) {
            viewModel.loadRockets()
        }
    }

    RocketListView(
        rockets = rockets,
        isLoading = isLoading,
        isLoadingMore = isLoadingMore,
        hasMore = hasMore,
        error = error,
        searchQuery = searchQuery,
        currentSort = filters.sortField,
        activeFilterCount = filters.activeFilterCount(),
        hasActiveFilters = filters.hasActiveFilters(),
        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
        onSortSelected = { viewModel.setSortField(it) },
        onRocketClick = onNavigateToRocketDetail,
        onNavigateBack = onNavigateBack,
        onRetry = { viewModel.loadRockets() },
        onClearFilters = { viewModel.clearFilters() },
        onLoadMore = { viewModel.loadNextPage() }
    )
}

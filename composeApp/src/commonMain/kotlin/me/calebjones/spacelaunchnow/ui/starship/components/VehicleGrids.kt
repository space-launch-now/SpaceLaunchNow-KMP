package me.calebjones.spacelaunchnow.ui.starship.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftEndpointDetailed
import me.calebjones.spacelaunchnow.ui.compose.StarshipVehiclesShimmer
import me.calebjones.spacelaunchnow.ui.viewmodel.ViewState

/**
 * Grid displaying spacecraft (Starship ships) with pagination
 */
@Composable
internal fun SpacecraftGrid(
    spacecraftState: ViewState<List<SpacecraftEndpointDetailed>>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacecraft = spacecraftState.data ?: emptyList()
    val gridState = rememberLazyGridState()

    // Trigger load more when scrolled near the end
    val shouldLoadMore by remember(hasMore, isLoadingMore) {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            hasMore && !isLoadingMore && totalItems > 0 && lastVisibleItem >= totalItems - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            spacecraftState.isInitialLoading -> {
                StarshipVehiclesShimmer()
            }

            spacecraft.isNotEmpty() -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (spacecraftState.isStale) {
                        item(span = { GridItemSpan(2) }) {
                            StalenessIndicator(modifier = Modifier.padding(bottom = 8.dp))
                        }
                    }

                    val spacecraftCount = spacecraft.size
                    val showFullWidthLast = !hasMore && spacecraftCount % 2 == 1
                    itemsIndexed(
                        spacecraft,
                        key = { _, item -> "spacecraft_${item.id}" },
                        span = { index, _ ->
                            if (showFullWidthLast && index == spacecraftCount - 1) {
                                GridItemSpan(2)
                            } else {
                                GridItemSpan(1)
                            }
                        }
                    ) { index, item ->
                        SpacecraftGridCard(
                            spacecraft = item,
                            isFullWidth = showFullWidthLast && index == spacecraftCount - 1
                        )
                    }

                    if (isLoadingMore) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }

            spacecraftState.hasErrorWithNoData -> {
                VehicleErrorState(
                    message = "Failed to load spacecraft",
                    error = spacecraftState.error
                )
            }

            else -> {
                VehicleEmptyState(message = "No Starship spacecraft available")
            }
        }
    }
}

/**
 * Grid displaying launchers (Super Heavy boosters) with pagination
 */
@Composable
internal fun LaunchersGrid(
    launchersState: ViewState<List<LauncherDetailed>>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val launchers = launchersState.data ?: emptyList()
    val gridState = rememberLazyGridState()

    // Trigger load more when scrolled near the end
    val shouldLoadMore by remember(hasMore, isLoadingMore) {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            hasMore && !isLoadingMore && totalItems > 0 && lastVisibleItem >= totalItems - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            launchersState.isInitialLoading -> {
                StarshipVehiclesShimmer()
            }

            launchers.isNotEmpty() -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (launchersState.isStale) {
                        item(span = { GridItemSpan(2) }) {
                            StalenessIndicator(modifier = Modifier.padding(bottom = 8.dp))
                        }
                    }

                    val launcherCount = launchers.size
                    val showFullWidthLast = !hasMore && launcherCount % 2 == 1
                    itemsIndexed(
                        launchers,
                        key = { _, item -> "launcher_${item.id}" },
                        span = { index, _ ->
                            if (showFullWidthLast && index == launcherCount - 1) {
                                GridItemSpan(2)
                            } else {
                                GridItemSpan(1)
                            }
                        }
                    ) { index, item ->
                        LauncherGridCard(
                            launcher = item,
                            isFullWidth = showFullWidthLast && index == launcherCount - 1
                        )
                    }

                    if (isLoadingMore) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }

            launchersState.hasErrorWithNoData -> {
                VehicleErrorState(
                    message = "Failed to load boosters",
                    error = launchersState.error
                )
            }

            else -> {
                VehicleEmptyState(message = "No Super Heavy boosters available")
            }
        }
    }
}

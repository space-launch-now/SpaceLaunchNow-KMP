package me.calebjones.spacelaunchnow.ui.starship.components

import me.calebjones.spacelaunchnow.ui.components.PlatformBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftEndpointDetailed
import me.calebjones.spacelaunchnow.ui.compose.StarshipVehiclesShimmer
import me.calebjones.spacelaunchnow.ui.viewmodel.VehicleNavigationLevel
import me.calebjones.spacelaunchnow.ui.viewmodel.VehicleType
import me.calebjones.spacelaunchnow.ui.viewmodel.ViewState

/**
 * Vehicles tab for Starship Dashboard with hierarchical navigation.
 *
 * Level 1 (CONFIGS): Shows configuration categories for spacecraft and boosters
 * Level 2 (VEHICLES): Shows individual vehicles for a selected configuration
 */
@Composable
fun StarshipVehiclesTab(
    // Navigation state
    navigationLevel: VehicleNavigationLevel,
    selectedVehicleType: VehicleType,
    selectedLauncherConfig: LauncherConfigDetailed?,
    selectedSpacecraftConfig: SpacecraftConfigDetailed?,
    // Config states
    launcherConfigsState: ViewState<List<LauncherConfigDetailed>>,
    spacecraftConfigsState: ViewState<List<SpacecraftConfigDetailed>>,
    // Vehicle states (for level 2)
    spacecraftState: ViewState<List<SpacecraftEndpointDetailed>>,
    spacecraftHasMore: Boolean,
    spacecraftLoadingMore: Boolean,
    onLoadMoreSpacecraft: () -> Unit,
    launchersState: ViewState<List<LauncherDetailed>>,
    launchersHasMore: Boolean,
    launchersLoadingMore: Boolean,
    onLoadMoreLaunchers: () -> Unit,
    // Navigation callbacks
    onSelectLauncherConfig: (LauncherConfigDetailed) -> Unit,
    onSelectSpacecraftConfig: (SpacecraftConfigDetailed) -> Unit,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Handle Android back button when at VEHICLES level
    PlatformBackHandler(enabled = navigationLevel == VehicleNavigationLevel.VEHICLES) {
        onNavigateBack()
    }

    AnimatedContent(
        targetState = navigationLevel,
        label = "vehicle_navigation",
        transitionSpec = {
            if (targetState == VehicleNavigationLevel.VEHICLES) {
                // Navigating forward to vehicles
                (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it } + fadeOut())
            } else {
                // Navigating back to configs
                (slideInHorizontally { -it } + fadeIn()) togetherWith
                        (slideOutHorizontally { it } + fadeOut())
            }
        },
        modifier = modifier.fillMaxSize()
    ) { level ->
        when (level) {
            VehicleNavigationLevel.CONFIGS -> ConfigurationsView(
                launcherConfigsState = launcherConfigsState,
                spacecraftConfigsState = spacecraftConfigsState,
                onSelectLauncherConfig = onSelectLauncherConfig,
                onSelectSpacecraftConfig = onSelectSpacecraftConfig
            )

            VehicleNavigationLevel.VEHICLES -> VehiclesView(
                vehicleType = selectedVehicleType,
                selectedLauncherConfig = selectedLauncherConfig,
                selectedSpacecraftConfig = selectedSpacecraftConfig,
                spacecraftState = spacecraftState,
                spacecraftHasMore = spacecraftHasMore,
                spacecraftLoadingMore = spacecraftLoadingMore,
                onLoadMoreSpacecraft = onLoadMoreSpacecraft,
                launchersState = launchersState,
                launchersHasMore = launchersHasMore,
                launchersLoadingMore = launchersLoadingMore,
                onLoadMoreLaunchers = onLoadMoreLaunchers,
                onNavigateBack = onNavigateBack
            )
        }
    }
}

/**
 * Level 1: Configurations view showing both spacecraft and booster configs
 */
@Composable
private fun ConfigurationsView(
    launcherConfigsState: ViewState<List<LauncherConfigDetailed>>,
    spacecraftConfigsState: ViewState<List<SpacecraftConfigDetailed>>,
    onSelectLauncherConfig: (LauncherConfigDetailed) -> Unit,
    onSelectSpacecraftConfig: (SpacecraftConfigDetailed) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading = launcherConfigsState.isLoading || spacecraftConfigsState.isLoading
    val launcherConfigs = launcherConfigsState.data
    val spacecraftConfigs = spacecraftConfigsState.data

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading && launcherConfigs.isEmpty() && spacecraftConfigs.isEmpty() -> {
                StarshipVehiclesShimmer()
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Spacecraft Configurations Section
                    if (spacecraftConfigs.isNotEmpty() || spacecraftConfigsState.isLoading) {
                        item(span = { GridItemSpan(2) }) {
                            VehicleSectionHeader(
                                title = "Spacecraft",
                                subtitle = "Starship vehicles"
                            )
                        }

                        if (spacecraftConfigsState.isLoading && spacecraftConfigs.isEmpty()) {
                            items(2) {
                                ConfigCardShimmer()
                            }
                        } else {
                            val spacecraftCount = spacecraftConfigs.size
                            itemsIndexed(
                                spacecraftConfigs,
                                key = { _, item -> "spacecraft_config_${item.id}" },
                                span = { index, _ ->
                                    // Last item spans full width if odd count
                                    if (index == spacecraftCount - 1 && spacecraftCount % 2 == 1) {
                                        GridItemSpan(2)
                                    } else {
                                        GridItemSpan(1)
                                    }
                                }
                            ) { index, config ->
                                SpacecraftConfigCard(
                                    config = config,
                                    onClick = { onSelectSpacecraftConfig(config) },
                                    isFullWidth = index == spacecraftCount - 1 && spacecraftCount % 2 == 1
                                )
                            }
                        }
                    }

                    // Spacer between sections
                    if (spacecraftConfigs.isNotEmpty() && launcherConfigs.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Launcher Configurations Section
                    if (launcherConfigs.isNotEmpty() || launcherConfigsState.isLoading) {
                        item(span = { GridItemSpan(2) }) {
                            VehicleSectionHeader(
                                title = "Boosters",
                                subtitle = "Super Heavy first stages"
                            )
                        }

                        if (launcherConfigsState.isLoading && launcherConfigs.isEmpty()) {
                            items(2) {
                                ConfigCardShimmer()
                            }
                        } else {
                            val launcherCount = launcherConfigs.size
                            itemsIndexed(
                                launcherConfigs,
                                key = { _, item -> "launcher_config_${item.id}" },
                                span = { index, _ ->
                                    // Last item spans full width if odd count
                                    if (index == launcherCount - 1 && launcherCount % 2 == 1) {
                                        GridItemSpan(2)
                                    } else {
                                        GridItemSpan(1)
                                    }
                                }
                            ) { index, config ->
                                LauncherConfigCard(
                                    config = config,
                                    onClick = { onSelectLauncherConfig(config) },
                                    isFullWidth = index == launcherCount - 1 && launcherCount % 2 == 1
                                )
                            }
                        }
                    }

                    // Empty state if no configs
                    if (launcherConfigs.isEmpty() && spacecraftConfigs.isEmpty() &&
                        !launcherConfigsState.isLoading && !spacecraftConfigsState.isLoading
                    ) {
                        item(span = { GridItemSpan(2) }) {
                            VehicleEmptyState(message = "No vehicle configurations found")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Level 2: Individual vehicles view for a selected configuration
 */
@Composable
private fun VehiclesView(
    vehicleType: VehicleType,
    selectedLauncherConfig: LauncherConfigDetailed?,
    selectedSpacecraftConfig: SpacecraftConfigDetailed?,
    spacecraftState: ViewState<List<SpacecraftEndpointDetailed>>,
    spacecraftHasMore: Boolean,
    spacecraftLoadingMore: Boolean,
    onLoadMoreSpacecraft: () -> Unit,
    launchersState: ViewState<List<LauncherDetailed>>,
    launchersHasMore: Boolean,
    launchersLoadingMore: Boolean,
    onLoadMoreLaunchers: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configName = when (vehicleType) {
        VehicleType.SPACECRAFT -> selectedSpacecraftConfig?.name ?: "Spacecraft"
        VehicleType.LAUNCHERS -> selectedLauncherConfig?.name ?: "Boosters"
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Back navigation header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to configurations"
                )
            }
            Column {
                Text(
                    text = configName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Individual vehicles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Vehicle grid based on type
        when (vehicleType) {
            VehicleType.SPACECRAFT -> SpacecraftGrid(
                spacecraftState = spacecraftState,
                hasMore = spacecraftHasMore,
                isLoadingMore = spacecraftLoadingMore,
                onLoadMore = onLoadMoreSpacecraft,
                modifier = Modifier.weight(1f)
            )

            VehicleType.LAUNCHERS -> LaunchersGrid(
                launchersState = launchersState,
                hasMore = launchersHasMore,
                isLoadingMore = launchersLoadingMore,
                onLoadMore = onLoadMoreLaunchers,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


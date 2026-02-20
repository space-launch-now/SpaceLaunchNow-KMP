package me.calebjones.spacelaunchnow.ui.spacestation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import chaintech.videoplayer.model.VideoPlayerConfig
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ExpeditionDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationDetailedEndpoint
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.compose.SharedDetailScaffold
import me.calebjones.spacelaunchnow.ui.detail.compose.components.VideoPlayerCard
import me.calebjones.spacelaunchnow.ui.spacestation.components.DockingLocationsCard
import me.calebjones.spacelaunchnow.ui.spacestation.components.ExpeditionInfoCard
import me.calebjones.spacelaunchnow.ui.spacestation.components.IssMapView
import me.calebjones.spacelaunchnow.ui.spacestation.components.OwnerAgenciesCard
import me.calebjones.spacelaunchnow.ui.spacestation.components.StationReportsCard
import me.calebjones.spacelaunchnow.ui.spacestation.components.StationSpecsCard
import me.calebjones.spacelaunchnow.ui.state.VideoPlayerState
import me.calebjones.spacelaunchnow.ui.viewmodel.IssPositionData
import me.calebjones.spacelaunchnow.util.LatLng
import me.calebjones.spacelaunchnow.util.NumberFormatUtil

private val TitleHeight = 128.dp
private const val ISS_STATION_ID = 4

/**
 * Main view for Space Station details
 * Conditionally shows ISS-specific features (map, live video) when stationId == 4
 */
@Composable
fun SpaceStationDetailView(
    station: SpaceStationDetailedEndpoint,
    activeExpeditions: List<ExpeditionDetailed>,
    issPosition: LatLng?,
    issPositionData: IssPositionData?,
    orbitPath: List<LatLng>,
    articles: List<Article>,
    videoPlayerState: VideoPlayerState,
    onSetPlayerVisible: (Boolean) -> Unit,
    onVideoSelected: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit
) {
    val isIss = station.id == ISS_STATION_ID

    SharedDetailScaffold(
        titleText = station.name,
        taglineText = station.status?.name ?: "Space Station",
        imageUrl = station.image?.imageUrl,
        onNavigateBack = onNavigateBack,
        backgroundColors = listOf(
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        ),
    ) {
        SpaceStationDetailContent(
            station = station,
            activeExpeditions = activeExpeditions,
            isIss = isIss,
            issPosition = issPosition,
            issPositionData = issPositionData,
            orbitPath = orbitPath,
            articles = articles,
            videoPlayerState = videoPlayerState,
            onSetPlayerVisible = onSetPlayerVisible,
            onVideoSelected = onVideoSelected,
            onNavigateToFullscreen = onNavigateToFullscreen
        )
    }
}

@Composable
private fun SpaceStationDetailContent(
    station: SpaceStationDetailedEndpoint,
    activeExpeditions: List<ExpeditionDetailed>,
    isIss: Boolean,
    issPosition: LatLng?,
    issPositionData: IssPositionData?,
    orbitPath: List<LatLng>,
    articles: List<Article>,
    videoPlayerState: VideoPlayerState,
    onSetPlayerVisible: (Boolean) -> Unit,
    onVideoSelected: (Int) -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(TitleHeight))

        // ISS-specific: NASA live stream
        if (isIss && videoPlayerState.availableVideos.isNotEmpty()) {
            VideoPlayerCard(
                videoPlayerState = videoPlayerState,
                launchName = station.name,
                onSetPlayerVisible = onSetPlayerVisible,
                onNavigateToFullscreen = onNavigateToFullscreen,
                onVideoSelected = onVideoSelected,
                playerConfig = VideoPlayerConfig(
                    isSeekBarVisible = false,
                    isDurationVisible = false,
                    isFullScreenEnabled = false,
                    isLiveStream = true,
                    showControls = false,
                    isGestureVolumeControlEnabled = false,
                )
            )
            Spacer(Modifier.height(16.dp))
        }

        // Active expedition info - use detailed expeditions if available, fall back to station's mini list
        if (activeExpeditions.isNotEmpty()) {
            for (expedition in activeExpeditions) {
                ExpeditionInfoCard(expedition = expedition)
                Spacer(Modifier.height(16.dp))
            }
        } else {
            // Fallback to station's basic expedition list
            station.activeExpeditions.let { expeditions ->
                for (expedition in expeditions) {
                    ExpeditionInfoCard(expeditionMini = expedition)
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        // ISS-specific: Live map with orbit tracking and position info
        if (isIss) {
            var isMapFullscreen by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column {
                    // Map section
                    Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                        IssMapView(
                            currentPosition = issPosition,
                            orbitPath = orbitPath,
                            modifier = Modifier.fillMaxSize(),
                            isInteractive = false
                        )

                        // Fullscreen button
                        FloatingActionButton(
                            onClick = { isMapFullscreen = true },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen Map"
                            )
                        }
                    }

                    // Position info section (inside same card)
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Current Position",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (issPositionData != null) {
                            val position = issPositionData.position
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Location",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Column(modifier = Modifier.padding(start = 8.dp)) {
                                            Text(
                                                text = "Latitude",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${
                                                    NumberFormatUtil.formatDecimal(
                                                        position.latitude,
                                                        4
                                                    )
                                                }°",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Location",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Column(modifier = Modifier.padding(start = 8.dp)) {
                                            Text(
                                                text = "Longitude",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${
                                                    NumberFormatUtil.formatDecimal(
                                                        position.longitude,
                                                        4
                                                    )
                                                }°",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Additional ISS info: altitude, velocity, visibility
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Altitude",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                issPositionData?.altitude?.let {
                                    Text(
                                        text = "${
                                            NumberFormatUtil.formatDecimal(
                                                it,
                                                1
                                            )
                                        } km",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Velocity",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Column(modifier = Modifier.padding(start = 8.dp)) {
                                        Text(
                                            text = "Velocity",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        issPositionData?.velocity?.let {
                                            Text(
                                                text = "${
                                                    NumberFormatUtil.formatDecimal(
                                                        it,
                                                        0
                                                    )
                                                } km/h",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Visibility status
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Column {
                                Text(
                                    text = "Visibility",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                issPositionData?.visibility?.replaceFirstChar { it.uppercase() }
                                    ?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = if (issPositionData.visibility == "daylight")
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                            }
                        }

                        // Orbit info
                        station.orbit?.let { orbitInfo ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Column {
                                    Text(
                                        text = "Orbit",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = orbitInfo,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Fullscreen dialog
            if (isMapFullscreen) {
                Dialog(
                    onDismissRequest = { isMapFullscreen = false },
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        IssMapView(
                            currentPosition = issPosition,
                            orbitPath = orbitPath,
                            modifier = Modifier.fillMaxSize(),
                            isInteractive = true
                        )

                        // Close button
                        FloatingActionButton(
                            onClick = { isMapFullscreen = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit Fullscreen"
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Station statistics
        StationSpecsCard(station = station)
        Spacer(Modifier.height(16.dp))

        // Owner agencies
        station.owners?.let { owners ->
            if (owners.isNotEmpty()) {
                OwnerAgenciesCard(agencies = owners)
                Spacer(Modifier.height(16.dp))
            }
        }

        // Docking locations - show what's docked and where
        if (station.dockingLocation.isNotEmpty()) {
            DockingLocationsCard(dockingLocations = station.dockingLocation)
            Spacer(Modifier.height(16.dp))
        }

        // Banner ad
        SmartBannerAd(
            modifier = Modifier.fillMaxWidth(),
            placementType = AdPlacementType.CONTENT
        )
        Spacer(Modifier.height(16.dp))

        // Related news articles
        if (articles.isNotEmpty()) {
            StationReportsCard(articles = articles)
            Spacer(Modifier.height(16.dp))
        }

        // Bottom spacing
        Spacer(Modifier.height(32.dp))
    }
}

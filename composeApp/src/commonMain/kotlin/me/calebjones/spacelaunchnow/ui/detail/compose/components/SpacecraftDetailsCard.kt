package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.shimmer
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.domain.model.SpacecraftFlightSummary
import me.calebjones.spacelaunchnow.ui.components.InfoTile
import me.calebjones.spacelaunchnow.ui.components.InfoTileData
import me.calebjones.spacelaunchnow.ui.components.StatusChip
import me.calebjones.spacelaunchnow.ui.icons.CustomIcons
import me.calebjones.spacelaunchnow.ui.icons.RocketLaunch
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDateTime
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchTime
import me.calebjones.spacelaunchnow.util.parseIsoDurationToHumanReadable
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_landing_tile_landing_type
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_read_more
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_show_less_lower
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_spacecraft_departed
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_spacecraft_docked
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_spacecraft_docking_events
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_spacecraft_image_failed_description
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_spacecraft_in_space
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_spacecraft_serial
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_spacecraft_tile_completed_missions
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_spacecraft_tile_destination
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_spacecraft_tile_landing_location
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_spacecraft_tile_mission_duration
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_spacecraft_tile_mission_end
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_spacecraft_tile_time_docked
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_spacecraft_tile_time_in_space
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_spacecraft_tile_total_flights
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_spacecraft_tile_turnaround_time
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_vehicle_tile_fastest_turnaround

@Composable
fun SpacecraftDetailsCard(
    spacecraftStages: List<SpacecraftFlightSummary>
) {
    if (spacecraftStages.isEmpty()) return

    spacecraftStages.forEach { spacecraftFlight ->
        SpacecraftDetailCard(
            spacecraftFlight = spacecraftFlight
        )
        if (spacecraftFlight != spacecraftStages.last()) {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun SpacecraftDetailCard(
    spacecraftFlight: SpacecraftFlightSummary
) {
    val spacecraft = spacecraftFlight.spacecraft
    val useUtc = LocalUseUtc.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Spacecraft image
            spacecraft?.imageUrl?.let { imageUrl ->
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ) {
                        SubcomposeAsyncImage(
                            model = imageUrl,
                            contentDescription = spacecraft?.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit,
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .shimmer(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CustomIcons.RocketLaunch,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    )
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .shimmer(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CustomIcons.RocketLaunch,
                                        contentDescription = stringResource(Res.string.launch_detail_spacecraft_image_failed_description),
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        )
                    }
                }
            }
            // Header with spacecraft name
            Text(
                text = spacecraft?.name ?: "",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Status indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                spacecraft?.status?.name?.let { status ->
                    StatusChip(
                        text = status,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                spacecraft?.serialNumber?.let { serial ->
                    StatusChip(
                        text = stringResource(Res.string.launch_detail_spacecraft_serial, serial),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (spacecraft?.inSpace == true) {
                    StatusChip(
                        text = stringResource(Res.string.launch_detail_spacecraft_in_space),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Description (overflow-aware)
            spacecraft?.description?.takeIf { it.isNotBlank() }?.let { desc ->
                var expanded by remember { mutableStateOf(false) }
                var hasOverflow by remember { mutableStateOf(false) }
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { textLayoutResult ->
                        hasOverflow = textLayoutResult.hasVisualOverflow
                    }
                )
                if (hasOverflow || expanded) {
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) stringResource(Res.string.launch_detail_show_less_lower) else stringResource(Res.string.launch_detail_read_more))
                    }
                }
            }

            // Flight information tiles
            val infoTiles = buildList {
                spacecraftFlight.destination?.let {
                    add(InfoTileData(Icons.Filled.LocationOn, stringResource(Res.string.launch_detail_spacecraft_tile_destination), it))
                }

                if (spacecraft?.isPlaceholder != true) {
                    spacecraft?.flightsCount?.let {
                        add(InfoTileData(CustomIcons.RocketLaunch, stringResource(Res.string.launch_detail_spacecraft_tile_total_flights), "$it"))
                    }
                }

                if (spacecraft?.isPlaceholder != true) {
                    spacecraft?.missionEndsCount?.let {
                        add(InfoTileData(Icons.Filled.CheckCircle, stringResource(Res.string.launch_detail_spacecraft_tile_completed_missions), "$it"))
                    }
                }

                spacecraftFlight.missionEnd?.let {
                    add(
                        InfoTileData(
                            Icons.Filled.Schedule,
                            stringResource(Res.string.launch_detail_spacecraft_tile_mission_end),
                            formatLaunchTime(it, useUtc)
                        )
                    )
                }

                spacecraftFlight.duration?.let {
                    add(
                        InfoTileData(
                            Icons.Filled.Timelapse,
                            stringResource(Res.string.launch_detail_spacecraft_tile_mission_duration),
                            parseIsoDurationToHumanReadable(it)
                        )
                    )
                }

                if (spacecraft?.isPlaceholder != true) {
                    spacecraft?.timeInSpace?.let {
                        add(
                            InfoTileData(
                                Icons.Filled.Satellite,
                                stringResource(Res.string.launch_detail_spacecraft_tile_time_in_space),
                                parseIsoDurationToHumanReadable(it)
                            )
                        )
                    }
                }

                if (spacecraft?.isPlaceholder != true) {
                    spacecraft?.timeDocked?.let {
                        add(
                            InfoTileData(
                                Icons.Filled.SatelliteAlt,
                                stringResource(Res.string.launch_detail_spacecraft_tile_time_docked),
                                parseIsoDurationToHumanReadable(it)
                            )
                        )
                    }
                }

                if (spacecraft?.isPlaceholder != true) {
                    spacecraftFlight.turnAroundTime?.let {
                        add(
                            InfoTileData(
                                Icons.Filled.Refresh,
                                stringResource(Res.string.launch_detail_spacecraft_tile_turnaround_time),
                                parseIsoDurationToHumanReadable(it)
                            )
                        )
                    }
                }

                if (spacecraft?.isPlaceholder != true) {
                    spacecraft?.fastestTurnaround?.let {
                        add(
                            InfoTileData(
                                Icons.Filled.Speed,
                                stringResource(Res.string.launch_detail_vehicle_tile_fastest_turnaround),
                                parseIsoDurationToHumanReadable(it)
                            )
                        )
                    }
                }

                spacecraftFlight.landing?.type?.name?.let {
                    add(InfoTileData(Icons.Filled.FlightLand, stringResource(Res.string.launch_detail_landing_tile_landing_type), it))
                }

                spacecraftFlight.landing?.landingLocation?.name?.let {
                    add(InfoTileData(Icons.Filled.Place, stringResource(Res.string.launch_detail_spacecraft_tile_landing_location), it))
                }
            }


            if (infoTiles.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    infoTiles.chunked(2).forEach { rowTiles ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowTiles.forEach { tile ->
                                InfoTile(
                                    icon = tile.icon,
                                    label = tile.label,
                                    value = tile.value,
                                    modifier = Modifier.weight(1f),
                                    customComposable = tile.customComposable
                                )
                            }
                            // Fill remaining space if odd number
                            if (rowTiles.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }


            // Docking events
            if (spacecraftFlight.dockingEvents.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = stringResource(Res.string.launch_detail_spacecraft_docking_events, spacecraftFlight.dockingEvents.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                spacecraftFlight.dockingEvents.forEach { dockingEvent ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            dockingEvent.spaceStationTarget?.name?.let { stationName ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.SatelliteAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = stationName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Text(
                                text = dockingEvent.dockingLocation.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                dockingEvent.docking.let { dockingTime ->
                                    Column {
                                        Text(
                                            text = stringResource(Res.string.launch_detail_spacecraft_docked),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = formatLaunchDateTime(dockingTime, useUtc),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                dockingEvent.departure?.let { departureTime ->
                                    Column {
                                        Text(
                                            text = stringResource(Res.string.launch_detail_spacecraft_departed),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = formatLaunchDateTime(departureTime, useUtc),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
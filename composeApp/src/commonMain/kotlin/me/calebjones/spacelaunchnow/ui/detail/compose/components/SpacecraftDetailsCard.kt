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
import androidx.compose.material3.CircularProgressIndicator
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
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftFlightDetailedSerializerNoLaunch
import me.calebjones.spacelaunchnow.ui.components.InfoTile
import me.calebjones.spacelaunchnow.ui.components.InfoTileData
import me.calebjones.spacelaunchnow.ui.components.StatusChip
import me.calebjones.spacelaunchnow.ui.icons.CustomIcons
import me.calebjones.spacelaunchnow.ui.icons.RocketLaunch
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDateTime
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchTime
import me.calebjones.spacelaunchnow.util.parseIsoDurationToHumanReadable

@Composable
fun SpacecraftDetailsCard(
    spacecraftStages: List<SpacecraftFlightDetailedSerializerNoLaunch>
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
    spacecraftFlight: SpacecraftFlightDetailedSerializerNoLaunch
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
            spacecraft.image?.imageUrl?.let { imageUrl ->
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
                            contentDescription = spacecraft.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit,
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        color = MaterialTheme.colorScheme.primary
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
                                        contentDescription = "Spacecraft image failed to load",
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
                text = spacecraft.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Status indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                spacecraft.status.name.let { status ->
                    StatusChip(
                        text = status,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                spacecraft.serialNumber?.let { serial ->
                    StatusChip(
                        text = "S/N: $serial",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (spacecraft.inSpace == true) {
                    StatusChip(
                        text = "In Space",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Description (overflow-aware)
            spacecraft.description.takeIf { it.isNotBlank() }?.let { desc ->
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
                        Text(if (expanded) "Show less" else "Read more")
                    }
                }
            }

            // Flight information tiles
            val infoTiles = buildList {
                spacecraftFlight.destination?.let {
                    add(InfoTileData(Icons.Filled.LocationOn, "Destination", it))
                }

                if (spacecraft.isPlaceholder != true) {
                    spacecraft.flightsCount?.let {
                        add(InfoTileData(CustomIcons.RocketLaunch, "Total Flights", "$it"))
                    }
                }

                if (spacecraft.isPlaceholder != true) {
                    spacecraft.missionEndsCount?.let {
                        add(InfoTileData(Icons.Filled.CheckCircle, "Completed Missions", "$it"))
                    }
                }

                spacecraftFlight.missionEnd?.let {
                    add(
                        InfoTileData(
                            Icons.Filled.Schedule,
                            "Mission End",
                            formatLaunchTime(it, useUtc)
                        )
                    )
                }

                spacecraftFlight.duration?.let {
                    add(
                        InfoTileData(
                            Icons.Filled.Timelapse,
                            "Mission Duration",
                            parseIsoDurationToHumanReadable(it)
                        )
                    )
                }

                if (spacecraft.isPlaceholder != true) {
                    spacecraft.timeInSpace?.let {
                        add(
                            InfoTileData(
                                Icons.Filled.Satellite,
                                "Time in Space",
                                parseIsoDurationToHumanReadable(it)
                            )
                        )
                    }
                }

                if (spacecraft.isPlaceholder != true) {
                    spacecraft.timeDocked?.let {
                        add(
                            InfoTileData(
                                Icons.Filled.SatelliteAlt,
                                "Time Docked",
                                parseIsoDurationToHumanReadable(it)
                            )
                        )
                    }
                }

                if (spacecraft.isPlaceholder != true) {
                    spacecraftFlight.turnAroundTime?.let {
                        add(
                            InfoTileData(
                                Icons.Filled.Refresh,
                                "Turnaround Time",
                                parseIsoDurationToHumanReadable(it)
                            )
                        )
                    }
                }

                if (spacecraft.isPlaceholder != true) {
                    spacecraft.fastestTurnaround?.let {
                        add(
                            InfoTileData(
                                Icons.Filled.Speed,
                                "Fastest Turnaround",
                                parseIsoDurationToHumanReadable(it)
                            )
                        )
                    }
                }

                spacecraftFlight.landing?.type?.name?.let {
                    add(InfoTileData(Icons.Filled.FlightLand, "Landing Type", it))
                }

                spacecraftFlight.landing?.landingLocation?.name?.let {
                    add(InfoTileData(Icons.Filled.Place, "Landing Location", it))
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
                    text = "Docking Events (${spacecraftFlight.dockingEvents.size})",
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
                                            text = "Docked",
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
                                            text = "Departed",
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
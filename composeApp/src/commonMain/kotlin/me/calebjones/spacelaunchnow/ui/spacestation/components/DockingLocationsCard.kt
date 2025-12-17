package me.calebjones.spacelaunchnow.ui.spacestation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.SpaceShuttle
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.api.launchlibrary.models.DockingLocationSerializerForSpacestation
import me.calebjones.spacelaunchnow.util.DateTimeUtil

/**
 * Card displaying docked vehicles at the space station
 * Only shows ports that currently have a vehicle docked
 */
@Composable
fun DockingLocationsCard(
    dockingLocations: List<DockingLocationSerializerForSpacestation>,
    modifier: Modifier = Modifier
) {
    // Only show occupied docking locations
    val occupiedLocations = dockingLocations.filter { it.currentlyDocked != null }

    if (occupiedLocations.isEmpty()) return

    Text(
        text = "Docked Vehicles",
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )

    Column(modifier = Modifier.padding(vertical = 8.dp)) {

        // List each docked vehicle
        occupiedLocations.forEach { location ->
            Card(
                modifier = modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                DockedVehicleItem(location = location)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}


@Composable
private fun DockedVehicleItem(
    location: DockingLocationSerializerForSpacestation
) {
    val dockedEvent = location.currentlyDocked ?: return

    // Get vehicle image URL
    val imageUrl = dockedEvent.flightVehicleChaser?.spacecraft?.image?.thumbnailUrl
        ?: dockedEvent.flightVehicleChaser?.spacecraft?.spacecraftConfig?.image?.thumbnailUrl
        ?: dockedEvent.flightVehicleChaser?.spacecraft?.image?.imageUrl
        ?: dockedEvent.flightVehicleChaser?.spacecraft?.spacecraftConfig?.image?.imageUrl

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vehicle image
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = "Docked Vehicle",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    )
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.SpaceShuttle,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Vehicle name
                val vehicleName = dockedEvent.flightVehicleChaser?.spacecraft?.name
                    ?: dockedEvent.payloadFlightChaser?.payload?.name
                    ?: "Unknown Vehicle"

                Text(
                    text = vehicleName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Vehicle type/config
                val vehicleConfig =
                    dockedEvent.flightVehicleChaser?.spacecraft?.spacecraftConfig?.name
                        ?: dockedEvent.payloadFlightChaser?.payload?.type?.name
                        ?: ""

                if (vehicleConfig.isNotBlank()) {
                    Text(
                        text = vehicleConfig,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Docking port name
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Docking date
                val useUtc = LocalUseUtc.current
                Text(
                    text = "Since ${DateTimeUtil.formatLaunchDate(dockedEvent.docking, useUtc)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}


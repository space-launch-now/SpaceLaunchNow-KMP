package me.calebjones.spacelaunchnow.ui.spacestation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationDetailedEndpoint
import me.calebjones.spacelaunchnow.util.DateTimeUtil

/**
 * Card displaying station specifications and statistics
 */
@Composable
fun StationSpecsCard(
    station: SpaceStationDetailedEndpoint,
    modifier: Modifier = Modifier
) {
    val hasSpecs = station.height != null || station.width != null ||
            station.mass != null || station.volume != null ||
            station.onboardCrew != null || station.dockedVehicles != null

    if (!hasSpecs) return

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Station Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            // Crew and vehicles row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                station.onboardCrew?.let { crew ->
                    StatItem(
                        label = "Onboard Crew",
                        value = "$crew",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (station.onboardCrew != null && station.dockedVehicles != null) {
                    Spacer(Modifier.width(16.dp))
                }

                station.dockedVehicles?.let { vehicles ->
                    StatItem(
                        label = "Docked Vehicles",
                        value = "$vehicles",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (station.height != null || station.width != null) {
                Spacer(Modifier.height(12.dp))
            }

            // Dimensions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                station.height?.let { height ->
                    StatItem(
                        label = "Height",
                        value = "${String.format("%.1f", height)} m",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (station.height != null && station.width != null) {
                    Spacer(Modifier.width(16.dp))
                }

                station.width?.let { width ->
                    StatItem(
                        label = "Width",
                        value = "${String.format("%.1f", width)} m",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (station.mass != null || station.volume != null) {
                Spacer(Modifier.height(12.dp))
            }

            // Mass and volume row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                station.mass?.let { mass ->
                    StatItem(
                        label = "Mass",
                        value = "${String.format("%,.0f", mass)} kg",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (station.mass != null && station.volume != null) {
                    Spacer(Modifier.width(16.dp))
                }

                station.volume?.let { volume ->
                    StatItem(
                        label = "Volume",
                        value = "$volume m³",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Additional info
            if (station.founded != null || station.orbit != null) {
                Spacer(Modifier.height(12.dp))
            }

            station.founded?.let { founded ->
                val useUtc = LocalUseUtc.current
                StatItem(
                    label = "Founded",
                    value = DateTimeUtil.formatLaunchDate(founded, useUtc)
                )
            }

            station.orbit?.let { orbit ->
                if (station.founded != null) {
                    Spacer(Modifier.height(8.dp))
                }
                StatItem(
                    label = "Orbit",
                    value = orbit
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

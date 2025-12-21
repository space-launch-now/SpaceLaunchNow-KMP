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
import androidx.compose.ui.unit.sp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationDetailedEndpoint
import me.calebjones.spacelaunchnow.util.DateTimeUtil
import me.calebjones.spacelaunchnow.util.NumberFormatUtil

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

    Text(
        text = "Station Statistics",
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )

    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

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
                        value = "${NumberFormatUtil.formatDecimal(height, 1)} m",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (station.height != null && station.width != null) {
                    Spacer(Modifier.width(16.dp))
                }

                station.width?.let { width ->
                    StatItem(
                        label = "Width",
                        value = "${NumberFormatUtil.formatDecimal(width, 1)} m",
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
                        value = "${NumberFormatUtil.formatNumber(mass)} kg",
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

            station.founded?.let { founded ->
                val useUtc = LocalUseUtc.current
                val tz = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
                val foundedInstant = founded.atStartOfDayIn(tz)
                StatItem(
                    label = "Founded",
                    value = DateTimeUtil.formatLaunchDate(foundedInstant, useUtc)
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

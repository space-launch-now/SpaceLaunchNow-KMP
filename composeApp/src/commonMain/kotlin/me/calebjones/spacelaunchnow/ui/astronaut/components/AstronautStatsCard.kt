package me.calebjones.spacelaunchnow.ui.astronaut.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointDetailed
import me.calebjones.spacelaunchnow.ui.components.InfoTile
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowTheme
import me.calebjones.spacelaunchnow.util.DateTimeUtil
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Card displaying astronaut career statistics.
 *
 * Shows:
 * - Total flights
 * - Total time in space
 * - EVA count and time
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AstronautStatsCard(
    astronaut: AstronautEndpointDetailed,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Career Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Flights Count
                InfoTile(
                    icon = Icons.Default.FlightTakeoff,
                    label = "Flights",
                    value = astronaut.flightsCount?.toString() ?: "0",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Time in Space
                InfoTile(
                    icon = Icons.Default.Timer,
                    label = "Time in Space",
                    value = astronaut.timeInSpace?.let { 
                        DateTimeUtil.parseIsoDurationToHumanReadable(it)
                    } ?: "N/A",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // EVA Count
                InfoTile(
                    icon = Icons.Default.Recycling,
                    label = "EVAs",
                    value = astronaut.spacewalksCount?.toString() ?: "0",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // EVA Time
                InfoTile(
                    icon = Icons.Default.Timer,
                    label = "EVA Time",
                    value = astronaut.evaTime?.let {
                        DateTimeUtil.parseIsoDurationToHumanReadable(it)
                    } ?: "N/A",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Preview
@Composable
private fun AstronautStatsCardPreview() {
    SpaceLaunchNowPreviewTheme {
        AstronautStatsCard(
            astronaut = AstronautEndpointDetailed(
                id = 1,
                url = "https://test.example.com/astronaut/1",
                responseMode = "detailed",
                name = "Neil Armstrong",
                status = null,
                agency = null,
                image = null,
                age = null,
                bio = "",
                type = me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautType(
                    id = 1,
                    name = "Astronaut"
                ),
                nationality = emptyList(),
                inSpace = false,
                timeInSpace = "P8DT14H12M30S",
                evaTime = "PT2H31M40S",
                dateOfBirth = null,
                dateOfDeath = null,
                wiki = null,
                lastFlight = null,
                firstFlight = null,
                socialMediaLinks = null,
                flightsCount = 2,
                landingsCount = null,
                spacewalksCount = 1,
                flights = emptyList(),
                landings = emptyList(),
                spacewalks = emptyList()
            )
        )
    }
}

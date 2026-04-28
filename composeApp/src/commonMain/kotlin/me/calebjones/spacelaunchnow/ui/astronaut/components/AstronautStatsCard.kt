package me.calebjones.spacelaunchnow.ui.astronaut.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import me.calebjones.spacelaunchnow.domain.model.AstronautDetail
import me.calebjones.spacelaunchnow.domain.model.Country
import me.calebjones.spacelaunchnow.ui.components.CountryChip
import me.calebjones.spacelaunchnow.ui.components.InfoTile
import me.calebjones.spacelaunchnow.ui.components.StatusChip
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.util.parseIsoDurationToHumanReadable
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
    astronaut: AstronautDetail,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
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
                text = "Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Status and Age/Death Date Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Status
                astronaut.statusName?.let { statusName ->
                    InfoTile(
                        icon = Icons.Default.EmojiEvents,
                        label = "Status",
                        modifier = Modifier.weight(1f),
                        color = color,
                        customComposable = {
                            StatusChip(
                                text = statusName,
                                color = getStatusColor(statusName)
                            )
                        }
                    )
                }

                // Age or Death Date
                if (astronaut.dateOfDeath != null) {
                    InfoTile(
                        icon = Icons.Default.Cake,
                        label = "Died",
                        value = formatDate(astronaut.dateOfDeath),
                        color = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    )
                } else if (astronaut.age != null) {
                    InfoTile(
                        icon = Icons.Default.Cake,
                        label = "Age",
                        value = "${astronaut.age} years",
                        modifier = Modifier.weight(1f),
                        color = color
                    )
                }
            }

            // Nationality
            if (astronaut.nationality.isNotEmpty()) {
                InfoTile(
                    icon = Icons.Default.Public,
                    label = "Nationality",
                    color = color,
                    customComposable = {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            astronaut.nationality.forEach { country ->
                                CountryChip(country = country)
                            }
                        }
                    }
                )
            }

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
                    color = color
                )

                // Time in Space
                InfoTile(
                    icon = Icons.Default.Timer,
                    label = "Time in Space",
                    value = astronaut.timeInSpace?.let { 
                        parseIsoDurationToHumanReadable(it)
                    } ?: "N/A",
                    modifier = Modifier.weight(1f),
                    color = color
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
                    color = color
                )

                // EVA Time
                InfoTile(
                    icon = Icons.Default.Timer,
                    label = "EVA Time",
                    value = astronaut.evaTime?.let {
                        parseIsoDurationToHumanReadable(it)
                    } ?: "N/A",
                    modifier = Modifier.weight(1f),
                    color = color
                )
            }
        }
    }
}

@Composable
private fun getStatusColor(statusName: String): Color {
    return when (statusName.lowercase()) {
        "active" -> Color(0xFF4CAF50)
        "retired" -> Color(0xFF9E9E9E)
        "deceased", "lost in flight" -> Color(0xFFF44336)
        "management" -> Color(0xFF2196F3)
        else -> MaterialTheme.colorScheme.primary
    }
}

private fun formatDate(date: LocalDate): String {
    return "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}, ${date.year}"
}

@Preview
@Composable
private fun AstronautStatsCardPreview() {
    SpaceLaunchNowPreviewTheme {
        AstronautStatsCard(
            astronaut = AstronautDetail(
                id = 1,
                name = "Neil Armstrong",
                statusName = "Deceased",
                statusId = 11,
                agencyName = "NASA",
                agencyAbbrev = "NASA",
                agencyId = 44,
                imageUrl = null,
                thumbnailUrl = null,
                age = null,
                bio = "",
                typeName = "Government",
                nationality = listOf(
                    Country(
                        id = 1,
                        name = "United States",
                        alpha2Code = "US",
                        alpha3Code = "USA",
                        nationalityName = "American",
                        nationalityNameComposed = "American"
                    )
                ),
                inSpace = false,
                timeInSpace = "P8DT14H12M30S",
                evaTime = "PT2H31M40S",
                dateOfBirth = LocalDate.parse("1930-08-05"),
                dateOfDeath = LocalDate.parse("2012-08-25"),
                wikiUrl = null,
                lastFlight = null,
                firstFlight = null,
                socialMediaLinks = emptyList(),
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

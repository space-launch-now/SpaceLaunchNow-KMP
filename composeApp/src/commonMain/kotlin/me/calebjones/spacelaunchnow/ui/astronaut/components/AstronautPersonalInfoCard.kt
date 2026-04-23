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
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import me.calebjones.spacelaunchnow.domain.model.AstronautDetail
import me.calebjones.spacelaunchnow.domain.model.Country
import me.calebjones.spacelaunchnow.ui.components.CountryChip
import me.calebjones.spacelaunchnow.ui.components.InfoTile
import me.calebjones.spacelaunchnow.ui.components.StatusChip
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Card displaying astronaut personal information including status, age, nationality, and death date if applicable.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AstronautPersonalInfoCard(
    astronaut: AstronautDetail,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Status
            astronaut.statusName?.let { statusName ->
                InfoTile(
                    icon = Icons.Default.EmojiEvents,
                    label = "Status",
                    modifier = Modifier.weight(1f),
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
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Nationality
        if (astronaut.nationality.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoTile(
                    icon = Icons.Default.Public,
                    label = "Nationality",
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
private fun AstronautPersonalInfoCardPreview() {
    SpaceLaunchNowPreviewTheme {
        AstronautPersonalInfoCard(
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
                bio = "First person to walk on the Moon",
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
                evaTime = "P2H32M",
                dateOfBirth = LocalDate.parse("1930-08-05"),
                dateOfDeath = LocalDate.parse("2012-08-25"),
                wikiUrl = "https://en.wikipedia.org/wiki/Neil_Armstrong",
                lastFlight = null,
                firstFlight = null,
                socialMediaLinks = emptyList(),
                flightsCount = 2,
                landingsCount = 2,
                spacewalksCount = 1,
                flights = emptyList(),
                landings = emptyList(),
                spacewalks = emptyList()
            )
        )
    }
}

@Preview
@Composable
private fun AstronautPersonalInfoCardActivePreview() {
    SpaceLaunchNowPreviewTheme {
        AstronautPersonalInfoCard(
            astronaut = AstronautDetail(
                id = 2,
                name = "Jessica Watkins",
                statusName = "Active",
                statusId = 1,
                agencyName = "NASA",
                agencyAbbrev = "NASA",
                agencyId = 44,
                imageUrl = null,
                thumbnailUrl = null,
                age = 35,
                bio = "NASA astronaut",
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
                timeInSpace = "P170DT13H31M",
                evaTime = null,
                dateOfBirth = LocalDate.parse("1988-11-14"),
                dateOfDeath = null,
                wikiUrl = "https://en.wikipedia.org/wiki/Jessica_Watkins",
                lastFlight = null,
                firstFlight = null,
                socialMediaLinks = emptyList(),
                flightsCount = 1,
                landingsCount = 1,
                spacewalksCount = 0,
                flights = emptyList(),
                landings = emptyList(),
                spacewalks = emptyList()
            )
        )
    }
}

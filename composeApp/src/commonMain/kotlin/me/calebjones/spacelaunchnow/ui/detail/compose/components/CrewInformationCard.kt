package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.ui.astronaut.components.AstronautProfileCard
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Displays crew information for a launch, including launch crew, onboard crew, and landing crew.
 * 
 * This component extracts all crew information from all spacecraft stages in a launch
 * and displays them in a single organized card.
 */
@Composable
fun CrewInformationCard(
    launch: LaunchDetailed,
    onAstronautClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Collect all crew from all spacecraft stages
    val allLaunchCrew = launch.rocket?.spacecraftStage?.flatMap { it.launchCrew } ?: emptyList()
    val allOnboardCrew = launch.rocket?.spacecraftStage?.flatMap { it.onboardCrew } ?: emptyList()
    val allLandingCrew = launch.rocket?.spacecraftStage?.flatMap { it.landingCrew } ?: emptyList()

    // Check if launch crew and landing crew are the same (by astronaut IDs)
    val launchCrewIds = allLaunchCrew.map { it.astronaut.id }.toSet()
    val landingCrewIds = allLandingCrew.map { it.astronaut.id }.toSet()
    val crewsAreIdentical = launchCrewIds.isNotEmpty() && launchCrewIds == landingCrewIds

    // Only show card if there's crew information
    if (allLaunchCrew.isEmpty() && allOnboardCrew.isEmpty() && allLandingCrew.isEmpty()) {
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Crew Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // If launch and landing crew are identical, show as "Crew"
        if (crewsAreIdentical) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allLaunchCrew.forEach { crewMember ->
                    val astronaut = crewMember.astronaut
                    val astronautName = astronaut.name ?: "Unknown Astronaut"
                    val role = crewMember.role?.role ?: "Crew Member"

                    AstronautProfileCard(
                        astronautName = astronautName,
                        role = role,
                        profileImageUrl = astronaut.image?.imageUrl,
                        agencyImageUrl = null,
                        onClick = {
                            onAstronautClick?.invoke(astronaut.id)
                        }
                    )
                }
            }
        } else {
            // Show launch and landing crew separately
            // Launch Crew
            if (allLaunchCrew.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allLaunchCrew.forEach { crewMember ->
                        val astronaut = crewMember.astronaut
                        val astronautName = astronaut.name ?: "Unknown Astronaut"
                        val role = crewMember.role?.role ?: "Crew Member"

                        AstronautProfileCard(
                            astronautName = astronautName,
                            role = role,
                            profileImageUrl = astronaut.image?.imageUrl,
                            agencyImageUrl = null,
                            onClick = {
                                onAstronautClick?.invoke(astronaut.id)
                            }
                        )
                    }
                }
            }

            // Landing Crew (only show if different from launch crew)
            if (allLandingCrew.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allLandingCrew.forEach { crewMember ->
                        val astronaut = crewMember.astronaut
                        val astronautName = astronaut.name ?: "Unknown Astronaut"
                        val role = crewMember.role?.role ?: "Crew Member"

                        AstronautProfileCard(
                            astronautName = astronautName,
                            role = role,
                            profileImageUrl = astronaut.image?.imageUrl,
                            agencyImageUrl = null,
                            onClick = {
                                onAstronautClick?.invoke(astronaut.id)
                            }
                        )
                    }
                }
            }
        }

        // Onboard Crew (always shown separately)
        if (allOnboardCrew.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allOnboardCrew.forEach { crewMember ->
                    val astronaut = crewMember.astronaut
                    val astronautName = astronaut.name ?: "Unknown Astronaut"
                    val role = crewMember.role?.role ?: "Crew Member"

                    AstronautProfileCard(
                        astronautName = astronautName,
                        role = role,
                        profileImageUrl = astronaut.image?.imageUrl,
                        agencyImageUrl = null,
                        onClick = {
                            onAstronautClick?.invoke(astronaut.id)
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CrewInformationCardPreview() {
    SpaceLaunchNowPreviewTheme {
        // For preview, show the card structure
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Crew Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AstronautProfileCard(
                        astronautName = "Robert Behnken",
                        role = "Commander",
                        profileImageUrl = null,
                        agencyImageUrl = null,
                        onClick = {}
                    )
                    AstronautProfileCard(
                        astronautName = "Douglas Hurley",
                        role = "Pilot",
                        profileImageUrl = null,
                        agencyImageUrl = null,
                        onClick = {}
                    )
                }
            }
        }
    }
}

package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.domain.model.CrewMemberSummary
import me.calebjones.spacelaunchnow.ui.astronaut.components.AstronautProfileCard
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Displays crew information for a launch, organized by crew role (launch, onboard, landing).
 *
 * When launch and landing crew are identical they are shown under a single "Crew" heading.
 * Otherwise each group gets its own labelled section.
 */
@Composable
fun CrewInformationCard(
    launchCrew: List<CrewMemberSummary>,
    onboardCrew: List<CrewMemberSummary> = emptyList(),
    landingCrew: List<CrewMemberSummary> = emptyList(),
    onAstronautClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (launchCrew.isEmpty() && onboardCrew.isEmpty() && landingCrew.isEmpty()) return

    val launchIds = launchCrew.map { it.astronautId }.toSet()
    val landingIds = landingCrew.map { it.astronautId }.toSet()
    val crewsAreIdentical = launchIds.isNotEmpty() && launchIds == landingIds

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Crew Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (crewsAreIdentical) {
                    // Launch == Landing crew: single unlabelled section
                    CrewSection(crew = launchCrew, onAstronautClick = onAstronautClick)
                } else {
                    var needsDivider = false

                    if (launchCrew.isNotEmpty()) {
                        CrewSectionWithLabel(
                            label = "Launch Crew",
                            crew = launchCrew,
                            onAstronautClick = onAstronautClick
                        )
                        needsDivider = true
                    }

                    if (onboardCrew.isNotEmpty()) {
                        if (needsDivider) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        CrewSectionWithLabel(
                            label = "Onboard Crew",
                            crew = onboardCrew,
                            onAstronautClick = onAstronautClick
                        )
                        needsDivider = true
                    }

                    if (landingCrew.isNotEmpty()) {
                        if (needsDivider) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        CrewSectionWithLabel(
                            label = "Landing Crew",
                            crew = landingCrew,
                            onAstronautClick = onAstronautClick
                        )
                    }
                }

                // Onboard crew always gets its own section when crews differ
                if (crewsAreIdentical && onboardCrew.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    CrewSectionWithLabel(
                        label = "Onboard Crew",
                        crew = onboardCrew,
                        onAstronautClick = onAstronautClick
                    )
                }
            }
        }
    }
}

@Composable
private fun CrewSectionWithLabel(
    label: String,
    crew: List<CrewMemberSummary>,
    onAstronautClick: ((Int) -> Unit)?
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp)
    )
    CrewSection(crew = crew, onAstronautClick = onAstronautClick)
}

@Composable
private fun CrewSection(
    crew: List<CrewMemberSummary>,
    onAstronautClick: ((Int) -> Unit)?
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        crew.forEach { member ->
            AstronautProfileCard(
                astronautName = member.astronautName ?: "Unknown Astronaut",
                role = member.role ?: "Crew Member",
                profileImageUrl = member.imageUrl,
                agencyImageUrl = null,
                onClick = { onAstronautClick?.invoke(member.astronautId) }
            )
        }
    }
}

@Preview
@Composable
private fun CrewInformationCardPreview() {
    SpaceLaunchNowPreviewTheme {
        CrewInformationCard(
            launchCrew = listOf(
                CrewMemberSummary(astronautId = 1, astronautName = "Robert Behnken", imageUrl = null, role = "Commander"),
                CrewMemberSummary(astronautId = 2, astronautName = "Douglas Hurley", imageUrl = null, role = "Pilot")
            ),
            onboardCrew = listOf(
                CrewMemberSummary(astronautId = 3, astronautName = "Chris Cassidy", imageUrl = null, role = "Flight Engineer")
            )
        )
    }
}

@Preview
@Composable
private fun CrewInformationCardDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        CrewInformationCard(
            launchCrew = listOf(
                CrewMemberSummary(astronautId = 1, astronautName = "Robert Behnken", imageUrl = null, role = "Commander"),
                CrewMemberSummary(astronautId = 2, astronautName = "Douglas Hurley", imageUrl = null, role = "Pilot")
            )
        )
    }
}

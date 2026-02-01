package me.calebjones.spacelaunchnow.ui.astronaut.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointDetailed
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Card displaying astronaut biography.
 *
 * Features:
 * - Expandable text with "Show More" / "Show Less"
 * - Formatted biography text
 */
@Composable
fun AstronautInfoCard(
    astronaut: AstronautEndpointDetailed,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val bio = astronaut.bio ?: return

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
                text = "Biography",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (isExpanded) Int.MAX_VALUE else 5,
                overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis
            )

            // Show expand/collapse button only if text is long enough
            if (bio.length > 200 || bio.lines().size > 5) {
                TextButton(
                    onClick = { isExpanded = !isExpanded }
                ) {
                    Text(
                        text = if (isExpanded) "Show Less" else "Show More",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AstronautInfoCardPreview() {
    SpaceLaunchNowPreviewTheme {
        AstronautInfoCard(
            astronaut = AstronautEndpointDetailed(
                id = 1,
                url = "https://test.example.com/astronaut/1",
                responseMode = "detailed",
                name = "Neil Armstrong",
                status = null,
                agency = null,
                image = null,
                age = null,
                bio = "Neil Alden Armstrong was an American astronaut and aeronautical engineer, and the first person to walk on the Moon. He was also a naval aviator, test pilot, and university professor. A graduate of Purdue University, Armstrong studied aeronautical engineering with his college tuition paid for by the U.S. Navy under the Holloway Plan.",
                type = me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautType(
                    id = 1,
                    name = "Astronaut"
                ),
                nationality = emptyList(),
                inSpace = false,
                timeInSpace = null,
                evaTime = null,
                dateOfBirth = null,
                dateOfDeath = null,
                wiki = null,
                lastFlight = null,
                firstFlight = null,
                socialMediaLinks = null,
                flightsCount = null,
                landingsCount = null,
                spacewalksCount = null,
                flights = emptyList(),
                landings = emptyList(),
                spacewalks = emptyList()
            )
        )
    }
}

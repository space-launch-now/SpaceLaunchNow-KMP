package me.calebjones.spacelaunchnow.ui.spacestation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import me.calebjones.spacelaunchnow.domain.model.AstronautListItem
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Card displaying current crew members aboard the space station
 */
@Composable
fun CrewCard(
    crew: List<AstronautListItem>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Current Crew (${crew.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            crew.forEach { astronaut ->
                CrewMemberItem(astronaut = astronaut)
            }
        }
    }
}

@Composable
private fun CrewMemberItem(astronaut: AstronautListItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular astronaut profile image
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = astronaut.thumbnailUrl ?: astronaut.imageUrl,
                contentDescription = astronaut.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                loading = {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                error = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = astronaut.name ?: "Unknown",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview
@Composable
private fun CrewCardPreview() {
    SpaceLaunchNowPreviewTheme {
        CrewCard(
            crew = listOf(
                sampleCrewMember(id = 1, name = "Suni Williams"),
                sampleCrewMember(id = 2, name = "Oleg Kononenko"),
                sampleCrewMember(id = 3, name = "Tracy Caldwell Dyson")
            )
        )
    }
}

@Preview
@Composable
private fun CrewCardDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        CrewCard(
            crew = listOf(
                sampleCrewMember(id = 1, name = "Suni Williams"),
                sampleCrewMember(id = 2, name = "Oleg Kononenko"),
                sampleCrewMember(id = 3, name = "Tracy Caldwell Dyson")
            )
        )
    }
}

private fun sampleCrewMember(id: Int, name: String) = AstronautListItem(
    id = id,
    name = name,
    statusName = "Active",
    statusId = 1,
    agencyName = "NASA",
    agencyAbbrev = "NASA",
    agencyId = 44,
    imageUrl = null,
    thumbnailUrl = null,
    age = null,
    bio = null,
    typeName = "Government",
    nationality = emptyList()
)

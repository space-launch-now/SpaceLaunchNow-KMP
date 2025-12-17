package me.calebjones.spacelaunchnow.ui.spacestation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautNormal

/**
 * Card displaying current crew members aboard the space station
 */
@Composable
fun CrewCard(
    crew: List<AstronautNormal>,
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
private fun CrewMemberItem(astronaut: AstronautNormal) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Astronaut profile image
        AsyncImage(
            model = astronaut.image?.imageUrl,
            contentDescription = astronaut.name,
            modifier = Modifier
                .size(56.dp)
                .padding(end = 12.dp),
            contentScale = ContentScale.Crop
        )

        Column {
            astronaut.name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

package me.calebjones.spacelaunchnow.ui.astronaut.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyMini
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautStatus
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautType
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Country
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Image
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ImageLicense
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ImageVariant
import me.calebjones.spacelaunchnow.ui.components.StatusChip
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Card component displaying astronaut information in a list.
 * 
 * Shows circular avatar, name, agency, and current status.
 * Follows Material 3 design with proper spacing and accessibility.
 */
@Composable
fun AstronautCard(
    astronaut: AstronautEndpointNormal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentDesc = "Astronaut card for ${astronaut.name ?: "Unknown"}. " +
            "Status: ${astronaut.status?.name ?: "Unknown"}. " +
            "Agency: ${astronaut.agency?.name ?: "Unknown"}. " +
            "Tap to view details."
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = contentDesc },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circular Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = astronaut.image?.thumbnailUrl ?: astronaut.image?.imageUrl,
                    contentDescription = null, // Described in parent semantics
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
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
            
            // Details Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Name
                Text(
                    text = astronaut.name ?: "Unknown Astronaut",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Agency
                astronaut.agency?.name?.let { agencyName ->
                    Text(
                        text = agencyName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Status Chip
                astronaut.status?.let { status ->
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusChip(
                        text = status.name,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AstronautCardPreview() {
    SpaceLaunchNowPreviewTheme {
        AstronautCard(
            astronaut = AstronautEndpointNormal(
                responseMode = "normal",
                id = 1,
                url = "https://ll.thespacedevs.com/2.4.0/astronaut/1/",
                name = "Neil Armstrong",
                status = AstronautStatus(
                    id = 2,
                    name = "Retired"
                ),
                agency = AgencyMini(
                    responseMode = "list",
                    id = 44,
                    url = "https://ll.thespacedevs.com/2.4.0/agencies/44/",
                    name = "NASA",
                    type = null,
                    abbrev = "NASA"
                ),
                image = null,
                age = 93,
                bio = "First person to walk on the Moon",
                type = AstronautType(
                    id = 1,
                    name = "Government"
                ),
                nationality = listOf(
                    Country(
                        id = 1,
                        name = "United States",
                        alpha2Code = "US",
                        alpha3Code = "USA",
                        nationalityName = "American",
                        nationalityNameComposed = "American"
                    )
                )
            ),
            onClick = {}
        )
    }
}

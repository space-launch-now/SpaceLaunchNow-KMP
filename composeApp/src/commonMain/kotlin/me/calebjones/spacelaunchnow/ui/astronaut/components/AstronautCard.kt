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
import com.valentinilk.shimmer.shimmer
import me.calebjones.spacelaunchnow.domain.model.AstronautListItem
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
    astronaut: AstronautListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentDesc = "Astronaut card for ${astronaut.name?.ifBlank { "Unknown" } ?: "Unknown"}. " +
            "Status: ${astronaut.statusName ?: "Unknown"}. " +
            "Agency: ${astronaut.agencyName ?: "Unknown"}. " +
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
                    model = astronaut.thumbnailUrl ?: astronaut.imageUrl,
                    contentDescription = null, // Described in parent semantics
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize().shimmer(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }
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
                    text = astronaut.name?.ifBlank { "Unknown Astronaut" } ?: "Unknown Astronaut",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // Agency
                astronaut.agencyName?.let { agencyName ->
                    Text(
                        text = agencyName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Status Chip
                astronaut.statusName?.let { statusName ->
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusChip(
                        text = statusName,
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
            astronaut = AstronautListItem(
                id = 1,
                name = "Neil Armstrong",
                statusName = "Retired",
                statusId = 2,
                agencyName = "NASA",
                agencyAbbrev = "NASA",
                agencyId = 44,
                imageUrl = null,
                thumbnailUrl = null,
                age = 93,
                bio = "First person to walk on the Moon",
                typeName = "Government",
                nationality = emptyList()
            ),
            onClick = {}
        )
    }
}

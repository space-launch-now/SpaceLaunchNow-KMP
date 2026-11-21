package me.calebjones.spacelaunchnow.ui.astronaut.components

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.shimmer
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_profile_agency_logo_description
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_profile_click_label
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_profile_description
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_profile_photo_description

/**
 * A compact horizontal card displaying astronaut profile information.
 * Designed for use in spacecraft/launch detail screens to show crew members.
 * 
 * Features:
 * - 48dp circular avatar with loading/error states
 * - Name (primary) and role (secondary)
 * - Agency logo (if available)
 * - Clickable with full accessibility support
 * - Minimum 48dp height for touch targets
 * 
 * @param astronautName The full name of the astronaut
 * @param role The astronaut's role on this mission (e.g., "Commander", "Pilot")
 * @param profileImageUrl URL to the astronaut's profile photo
 * @param agencyImageUrl Optional URL to the agency logo
 * @param onClick Callback when the card is tapped
 * @param modifier Modifier for the card
 */
@Composable
fun AstronautProfileCard(
    astronautName: String,
    role: String,
    profileImageUrl: String?,
    agencyImageUrl: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clickLabel = stringResource(Res.string.astronaut_profile_click_label, astronautName)
    val cardDescription = stringResource(Res.string.astronaut_profile_description, astronautName, role)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(
                onClick = onClick,
                onClickLabel = clickLabel
            )
            .semantics {
                this.role = Role.Button
                contentDescription = cardDescription
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circular avatar
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                if (profileImageUrl != null) {
                    SubcomposeAsyncImage(
                        model = profileImageUrl,
                        contentDescription = stringResource(Res.string.astronaut_profile_photo_description, astronautName),
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
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    )
                } else {
                    // Placeholder icon when no image URL provided
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Name and role column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = astronautName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Agency logo (optional)
            if (agencyImageUrl != null) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    SubcomposeAsyncImage(
                        model = agencyImageUrl,
                        contentDescription = stringResource(Res.string.astronaut_profile_agency_logo_description),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shimmer()
                            )
                        },
                        error = {
                            // Silently fail - agency logo is optional decoration
                            Spacer(modifier = Modifier.size(32.dp))
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AstronautProfileCardPreview() {
    SpaceLaunchNowPreviewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // With agency logo
            AstronautProfileCard(
                astronautName = "Neil Armstrong",
                role = "Commander",
                profileImageUrl = null,
                agencyImageUrl = null,
                onClick = {}
            )
            
            // Without agency logo
            AstronautProfileCard(
                astronautName = "Buzz Aldrin",
                role = "Lunar Module Pilot",
                profileImageUrl = null,
                agencyImageUrl = null,
                onClick = {}
            )
            
            // Long names
            AstronautProfileCard(
                astronautName = "Katherine Johnson-Martinez",
                role = "Mission Specialist & Flight Engineer",
                profileImageUrl = null,
                agencyImageUrl = null,
                onClick = {}
            )
        }
    }
}

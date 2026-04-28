package me.calebjones.spacelaunchnow.ui.spacestation.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.shimmer
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.UserAstronaut
import me.calebjones.spacelaunchnow.domain.model.CrewMember
import me.calebjones.spacelaunchnow.domain.model.ExpeditionDetailItem
import me.calebjones.spacelaunchnow.domain.model.ExpeditionMiniItem
import me.calebjones.spacelaunchnow.util.DateTimeUtil

/**
 * Card displaying detailed expedition information with crew
 */
@Composable
fun ExpeditionInfoCard(
    expedition: ExpeditionDetailItem?,
    modifier: Modifier = Modifier
) {
    if (expedition == null) return

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Mission patch (centered at top if available)
            expedition.missionPatches.firstOrNull()?.imageUrl?.let { patchUrl ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SubcomposeAsyncImage(
                        model = patchUrl,
                        contentDescription = "Mission Patch",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit,
                        loading = {
                            // Use shimmer instead of CircularProgressIndicator for warm start perf
                            Box(
                                modifier = Modifier.fillMaxSize()
                                    .shimmer(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.UserAstronaut,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.UserAstronaut,
                                    contentDescription = "Expedition Icon",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }

            // Expedition name
            expedition.name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Date range
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    expedition.start?.let {
                        Text(
                            text = DateTimeUtil.formatLaunchDate(it),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "End",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = expedition.end?.let { DateTimeUtil.formatLaunchDate(it) }
                            ?: "Ongoing",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Crew section
            val crew = expedition.crew
            if (crew.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Crew (${crew.size})",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (crewMember in crew) {
                        CrewMemberRow(crewMember = crewMember)
                    }
                }
            }
        }
    }
}

/**
 * Card displaying basic expedition information (fallback when detailed not available)
 */
@Composable
fun ExpeditionInfoCard(
    expeditionMini: ExpeditionMiniItem?,
    modifier: Modifier = Modifier
) {
    if (expeditionMini == null) return

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            expeditionMini.name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    expeditionMini.start?.let {
                        Text(
                            text = DateTimeUtil.formatLaunchDate(it),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "End",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = expeditionMini.end?.let { DateTimeUtil.formatLaunchDate(it) }
                            ?: "Ongoing",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Row displaying a single crew member
 */
@Composable
private fun CrewMemberRow(
    crewMember: CrewMember,
    modifier: Modifier = Modifier
) {
    val astronaut = crewMember.astronaut

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Astronaut photo
        SubcomposeAsyncImage(
            model = astronaut.thumbnailUrl ?: astronaut.imageUrl,
            contentDescription = astronaut.name,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = {
                // Use shimmer instead of CircularProgressIndicator for warm start perf
                Box(
                    modifier = Modifier.fillMaxSize()
                        .shimmer(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.UserAstronaut,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.UserAstronaut,
                        contentDescription = "Crew placeholder",
                        modifier = Modifier.size(96.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Name, role, and agency
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = astronaut.name ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            crewMember.role?.let { role ->
                Text(
                    text = role,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            astronaut.agencyName?.let { agencyName ->
                Text(
                    text = agencyName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

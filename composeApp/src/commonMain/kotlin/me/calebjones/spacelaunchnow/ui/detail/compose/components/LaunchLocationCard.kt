package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.brands.WikipediaW
import compose.icons.fontawesomeicons.solid.InfoCircle
import compose.icons.fontawesomeicons.solid.Map
import me.calebjones.spacelaunchnow.domain.model.Location
import me.calebjones.spacelaunchnow.domain.model.Pad
import me.calebjones.spacelaunchnow.ui.components.StatCard
import me.calebjones.spacelaunchnow.util.parseIsoDurationToHumanReadable
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_info_description
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_link_website
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_link_wikipedia
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_link_wikipedia_description
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_location_flag_description
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_location_image_description
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_location_map_description
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_pad_coordinates
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_pad_fastest_turnaround
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_pad_image_description
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_pad_map_description
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_pad_map_icon_description
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_pad_open_map
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_pad_stat_launches
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_pad_stat_orbital_attempts

@Composable
fun LaunchLocationCard(
    location: Location?,
    pad: Pad?,
    openUrl: (String) -> Unit = { /* TODO: Implement for platform */ }
) {
    if (location == null) return
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Map image centered at the top (if exists)
                // Prefer location.imageUrl, fallback to mapImage if imageUrl is null
                location.imageUrl?.let { mapUrl ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = mapUrl,
                            contentDescription = stringResource(Res.string.launch_detail_location_image_description),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                } ?: location.mapImage?.let { mapUrl ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = mapUrl,
                            contentDescription = stringResource(Res.string.launch_detail_location_map_description),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                // Name
                location.name?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                location.countryAlpha2?.let { alpha2 ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = "https://flagcdn.com/w40/${alpha2.lowercase()}.png",
                            contentDescription = stringResource(Res.string.launch_detail_location_flag_description),
                            modifier = Modifier.width(24.dp).height(16.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = location.countryName ?: alpha2,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } ?: location.countryName?.let { name ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                location.celestialBodyName?.let { celestialBody ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(text = celestialBody, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                location.timezoneName?.let { tz ->
                    if (tz.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(text = tz, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                // Description
                location.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pad info (if available)
                pad?.let { it ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Map image centered at the top (if exists)
                        // Prefer pad.imageUrl, fallback to mapImage if imageUrl is null
                        it.imageUrl?.let { mapUrl ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = mapUrl,
                                    contentDescription = stringResource(Res.string.launch_detail_pad_image_description),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } ?: it.mapImage?.let { mapUrl ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = mapUrl,
                                    contentDescription = stringResource(Res.string.launch_detail_pad_map_description),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        it.name?.let { padName ->
                            Text(
                                text = padName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        it.description?.takeIf { it.isNotBlank() }?.let { desc ->
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Other details (coords, turnaround)
                        val otherStats = listOfNotNull(
                            it.latitude?.let { lat ->
                                it.longitude?.let { lon ->
                                    val latStr = ((lat * 100).toInt() / 100.0).toString()
                                    val lonStr = ((lon * 100).toInt() / 100.0).toString()
                                    stringResource(Res.string.launch_detail_pad_coordinates, latStr, lonStr)
                                }
                            },
                            it.fastestTurnaround?.takeIf { f -> f.isNotBlank() }
                                ?.let { f ->
                                    stringResource(Res.string.launch_detail_pad_fastest_turnaround, parseIsoDurationToHumanReadable(f))
                                }
                        )

                        if (otherStats.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                otherStats.forEach { stat ->
                                    Text(
                                        text = stat,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        // Optional: Pad map url (if exists)
                        it.mapUrl?.let { mapUrl ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { openUrl(mapUrl) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Solid.Map,
                                        contentDescription = stringResource(Res.string.launch_detail_pad_map_icon_description),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(Res.string.launch_detail_pad_open_map), color = MaterialTheme.colorScheme.onTertiary)
                                }
                            }
                        }
                        // Info & Wiki links
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            it.infoUrl?.let { url ->
                                Button(
                                    onClick = { openUrl(url) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Solid.InfoCircle,
                                        contentDescription = stringResource(Res.string.launch_detail_info_description),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(Res.string.launch_detail_link_website))
                                }
                            }
                            it.wikiUrl?.let { url ->
                                Button(
                                    onClick = { openUrl(url) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Brands.WikipediaW,
                                        contentDescription = stringResource(Res.string.launch_detail_link_wikipedia_description),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(Res.string.launch_detail_link_wikipedia), color = MaterialTheme.colorScheme.onSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PadQuickStatsRow(pad: Pad?) {
    if (pad == null) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (pad.totalLaunchCount != null) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Public,
                value = pad.totalLaunchCount.toString(),
                label = stringResource(Res.string.launch_detail_pad_stat_launches)
            )
        }
        if (pad.orbitalLaunchAttemptCount != null) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.TrendingUp,
                value = pad.orbitalLaunchAttemptCount.toString(),
                label = stringResource(Res.string.launch_detail_pad_stat_orbital_attempts)
            )
        }
    }
}
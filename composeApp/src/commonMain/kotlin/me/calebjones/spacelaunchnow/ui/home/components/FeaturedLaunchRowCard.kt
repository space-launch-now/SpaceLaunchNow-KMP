package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.shimmer
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.NetPrecision
import me.calebjones.spacelaunchnow.navigation.LaunchDetail
import me.calebjones.spacelaunchnow.ui.compose.CompactLaunchCountdown
import me.calebjones.spacelaunchnow.ui.icons.CustomIcons
import me.calebjones.spacelaunchnow.ui.icons.RocketLaunch
import me.calebjones.spacelaunchnow.ui.preview.PreviewData
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.util.DateTimeUtil
import me.calebjones.spacelaunchnow.util.StatusColorUtil.getLaunchStatusColor
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock.System
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * A compact, wide card for displaying featured launches in a horizontal row.
 * Shows: thumbnail image, launch title, a live countdown (falling back to the
 * date/time for far-out or imprecise launches), location + pad, and status chip.
 *
 * Height: 120dp, width adapts to available space (use with Modifier.weight(1f))
 */
@Composable
fun FeaturedLaunchRowCard(
    launch: Launch,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val useUtc = LocalUseUtc.current

    Card(
        modifier = modifier
            .height(120.dp)
            .clickable {
                navController.navigate(LaunchDetail(launch.id))
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left side: Thumbnail image
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            ) {
                launch.imageUrl?.let { url ->
                    SubcomposeAsyncImage(
                        model = url,
                        contentDescription = "Launch Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            // Use shimmer instead of CircularProgressIndicator for warm start perf
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shimmer()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CustomIcons.RocketLaunch,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
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
                                    imageVector = CustomIcons.RocketLaunch,
                                    contentDescription = "Launch placeholder",
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                            }
                        }
                    )
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CustomIcons.RocketLaunch,
                            contentDescription = "Launch placeholder",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Right side: Launch details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section: Title (split into rocket config and mission)
                Column {
                    val launchName = launch.name
                    val parts = launchName.split(" | ", limit = 2)

                    if (parts.size == 2) {
                        // Rocket Configuration (first line)
                        Text(
                            text = parts[0],
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                        // Mission Name (second line)
                        Text(
                            text = parts[1],
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                        )
                    } else {
                        // No separator, show entire name
                        Text(
                            text = launchName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                    }

                    // Location + Pad
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        launch.pad?.location?.name?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Middle section: live countdown, falling back to the date/time
                    // for launches that are far out or have an imprecise net time.
                    val net = launch.net
                    if (net != null) {
                        CompactLaunchCountdown(
                            launchTime = net,
                            precision = launch.netPrecision,
                            fallback = {
                                LaunchDateRow(
                                    text = DateTimeUtil.formatLaunchDateTime(net, useUtc)
                                )
                            }
                        )
                    } else {
                        LaunchDateRow(text = "TBD")
                    }

                }

                // Bottom section: Status chip pinned to the bottom-right.
                // Uses fillMaxWidth (not fillMaxSize) so it only claims its own height
                // and won't overlap the countdown/date above it.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Status chip
                    launch.status?.let { status ->
                        val statusColor = getLaunchStatusColor(status.id)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = statusColor,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = status.name.uppercase(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * The date/time row shown on the card: a Schedule clock icon followed by [text].
 * Used as the fallback when a live countdown isn't shown (far-out or imprecise launches).
 */
@Composable
private fun LaunchDateRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Formats location and pad name for display.
 * Shows "Location, Pad" or just location if pad name is similar.
 */
private fun formatLocationAndPad(launch: Launch): String {
    val pad = launch.pad
    val location = pad?.location
    val padName = pad?.name
    val locationName = location?.name

    return when {
        locationName != null && padName != null -> {
            // If pad name contains the location name, just show pad name
            if (padName.contains(locationName, ignoreCase = true)) {
                padName
            } else {
                "$padName"
            }
        }

        padName != null -> padName
        locationName != null -> locationName
        else -> "Unknown Location"
    }
}

// ========================================
// Previews
// ========================================

@Preview
@Composable
private fun FeaturedLaunchRowCardPreview() {
    SpaceLaunchNowPreviewTheme {
        FeaturedLaunchRowCard(
            launch = PreviewData.domainLaunchSpaceX,
            navController = rememberNavController()
        )
    }
}

@Preview
@Composable
private fun FeaturedLaunchRowCardDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        FeaturedLaunchRowCard(
            launch = PreviewData.domainLaunchSpaceX,
            navController = rememberNavController()
        )
    }
}

@Preview
@Composable
private fun FeaturedLaunchRowCardNoImagePreview() {
    SpaceLaunchNowPreviewTheme {
        FeaturedLaunchRowCard(
            launch = PreviewData.domainLaunchULA,
            navController = rememberNavController()
        )
    }
}

@Preview
@Composable
private fun FeaturedLaunchRowCardNoImageDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        FeaturedLaunchRowCard(
            launch = PreviewData.domainLaunchULA,
            navController = rememberNavController()
        )
    }
}

/**
 * Three small cards with live countdowns, mirroring the home page. The preview data
 * launches ([PreviewData]) have fixed past NET dates, so here we copy in near-future,
 * precise NET times to exercise the countdown path rather than the date fallback.
 */
@Composable
private fun FeaturedLaunchesCountdownStack() {
    val now = System.now()
    // precision id 0..4 is required for the live countdown to show (see CompactLaunchCountdown)
    val precise = NetPrecision(id = 0, name = "Second", abbrev = null, description = null)

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FeaturedLaunchRowCard(
            launch = PreviewData.domainLaunchSpaceX.copy(
                net = now.plus(1.days).plus(7.hours),
                netPrecision = precise
            ),
            navController = rememberNavController(),
            modifier = Modifier.fillMaxWidth()
        )
        FeaturedLaunchRowCard(
            launch = PreviewData.domainLaunchCrewMission.copy(
                net = now.plus(2.days).plus(3.hours),
                netPrecision = precise
            ),
            navController = rememberNavController(),
            modifier = Modifier.fillMaxWidth()
        )
        // No image + a further-out NET, still within the 30-day countdown window
        FeaturedLaunchRowCard(
            launch = PreviewData.domainLaunchULA.copy(
                net = now.plus(12.days),
                netPrecision = precise,
                status = PreviewData.domainStatusGo
            ),
            navController = rememberNavController(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun FeaturedLaunchesCountdownStackPreview() {
    SpaceLaunchNowPreviewTheme {
        FeaturedLaunchesCountdownStack()
    }
}

@Preview
@Composable
private fun FeaturedLaunchesCountdownStackDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        FeaturedLaunchesCountdownStack()
    }
}

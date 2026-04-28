package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.shimmer
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.navigation.LaunchDetail
import me.calebjones.spacelaunchnow.ui.compose.LiveIndicator
import me.calebjones.spacelaunchnow.ui.icons.CustomIcons
import me.calebjones.spacelaunchnow.ui.icons.RocketLaunch
import me.calebjones.spacelaunchnow.ui.preview.PreviewData
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.util.LaunchFormatUtil
import me.calebjones.spacelaunchnow.util.StatusColorUtil.getLaunchStatusColor
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * In Flight status color - Blue 500 (#1976D2)
 * Used for LIVE launch card visual distinction per StatusColorUtil standard.
 */
private val InFlightBlue = Color(0xFF1976D2)

/**
 * Card displaying a launch that is currently in flight.
 * 
 * Features:
 * - Blue 500 border (In Flight status color)
 * - Status chip showing launch status (e.g., "In Flight")
 * - Animated LIVE indicator badge (only when webcast is live)
 * - Clickable - navigates to launch detail page
 * - Medium card height (130dp) matching FeaturedLaunchRowCard
 */
@Composable
fun LiveLaunchCard(
    launch: Launch,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val title by remember(launch) {
        mutableStateOf(LaunchFormatUtil.formatLaunchTitle(launch))
    }
    val statusText = launch.status?.name ?: "In Flight"
    val statusColor = getLaunchStatusColor(launch.status?.id)
    val isWebcastLive = launch.webcastLive

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 16.dp)
            .clickable { navController.navigate(LaunchDetail(launch.id)) }
            .semantics {
                contentDescription = "In-flight launch: $title. Status: $statusText. Tap to view details."
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = InFlightBlue.copy(alpha = 0.08f)
        ),
        border = BorderStroke(2.dp, InFlightBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left side: Launch image thumbnail
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(130.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            ) {
                launch.imageUrl?.let { url ->
                    SubcomposeAsyncImage(
                        model = url,
                        contentDescription = "Launch image for $title",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
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
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                            }
                        }
                    )
                } ?: run {
                    // No image URL - show placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
                }
            }

            // Right side: Launch info column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Mission name or launch name
                    Text(
                        text = launch.mission?.name ?: launch.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Location
                    launch.pad?.location?.name?.let { location ->
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Bottom row: Status chip and optional LIVE indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status chip (e.g., "In Flight")
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusColor,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = statusText.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }

                    // LIVE indicator (only when webcast is live)
                    if (isWebcastLive) {
                        LiveIndicator()
                    }
                }
            }
        }
    }
}

/**
 * Composable to render the LIVE launch section on home screen.
 * Shows nothing if there are no in-flight launches.
 */
@Composable
fun LiveLaunchSection(
    launch: Launch?,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    if (launch != null) {
        Column(modifier = modifier) {
            LiveLaunchCard(
                launch = launch,
                navController = navController
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ========================================
// Previews (without NavController)
// ========================================

@Composable
private fun LiveLaunchCardPreviewContent(
    launch: Launch,
    isWebcastLive: Boolean = false
) {
    val title by remember(launch) {
        mutableStateOf(LaunchFormatUtil.formatLaunchTitle(launch))
    }
    val statusText = launch.status?.name ?: "In Flight"
    val statusColor = getLaunchStatusColor(launch.status?.id)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 16.dp)
            .semantics {
                contentDescription = "In-flight launch: $title. Status: $statusText. Tap to view details."
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = InFlightBlue.copy(alpha = 0.08f)
        ),
        border = BorderStroke(2.dp, InFlightBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left side: Launch image placeholder for preview
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(130.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
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

            // Right side: Launch info column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Mission name or launch name
                    Text(
                        text = launch.mission?.name ?: launch.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Location
                    launch.pad?.location?.name?.let { location ->
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Bottom row: Status chip and optional LIVE indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusColor,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = statusText.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }

                    // LIVE indicator (only when webcast is live)
                    if (isWebcastLive) {
                        LiveIndicator()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun LiveLaunchCardPreview() {
    SpaceLaunchNowPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LiveLaunchCardPreviewContent(
                launch = PreviewData.domainLaunchSpaceX.copy(
                    status = PreviewData.domainStatusInFlight
                ),
                isWebcastLive = false
            )
        }
    }
}

@Preview
@Composable
private fun LiveLaunchCardWithWebcastPreview() {
    SpaceLaunchNowPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LiveLaunchCardPreviewContent(
                launch = PreviewData.domainLaunchSpaceX.copy(
                    status = PreviewData.domainStatusInFlight,
                    webcastLive = true
                ),
                isWebcastLive = true
            )
        }
    }
}

@Preview
@Composable
private fun LiveLaunchCardDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            LiveLaunchCardPreviewContent(
                launch = PreviewData.domainLaunchSpaceX.copy(
                    status = PreviewData.domainStatusInFlight
                ),
                isWebcastLive = false
            )
        }
    }
}

@Preview
@Composable
private fun LiveLaunchCardWithWebcastDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            LiveLaunchCardPreviewContent(
                launch = PreviewData.domainLaunchSpaceX.copy(
                    status = PreviewData.domainStatusInFlight,
                    webcastLive = true
                ),
                isWebcastLive = true
            )
        }
    }
}

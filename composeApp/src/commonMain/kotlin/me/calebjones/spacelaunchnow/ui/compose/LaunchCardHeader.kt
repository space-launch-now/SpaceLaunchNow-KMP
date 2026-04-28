package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.ui.preview.PreviewData
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.util.DateTimeUtil
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun AgencyLogoShimmer(logoSize: Dp) {
    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
    val shimmerColors = listOf(baseColor, highlightColor, baseColor)
    val transition = rememberInfiniteTransition(label = "logo_shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "logo_shimmer_translate"
    )
    Box(
        modifier = Modifier
            .size(logoSize)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(translateAnim - 200f, translateAnim - 200f),
                    end = Offset(translateAnim, translateAnim)
                )
            )
    )
}

/**
 *
 * @param launch The launch data
 * @param showAgencyLogo Whether to show the circular agency logo on the left
 * @param logoSize Size of the agency logo
 * @param useRelativeTime Whether to use relative time formatting (e.g., "in 2 days") vs absolute time
 * @param contentPadding Padding around the content
 */
@Composable
fun LaunchCardHeaderOverlay(
    launch: Launch,
    showAgencyLogo: Boolean = true,
    logoSize: Dp = 56.dp,
    useRelativeTime: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    modifier: Modifier = Modifier
) {
    val useUtc = LocalUseUtc.current

    // Split launch name into rocket config and mission name
    val (rocketConfig, missionName) = remember(launch) {
        val title = launch.name
        // Split on " | " to separate rocket config from mission name
        val parts = title.split(" | ", limit = 2)
        if (parts.size == 2) {
            Pair(parts[0], parts[1])
        } else {
            // If no separator, show entire name on first line
            Pair(title, null)
        }
    }

    val formattedDate by remember(launch.net, useRelativeTime, useUtc) {
        mutableStateOf(
            launch.net?.let {
                if (useRelativeTime) {
                    DateTimeUtil.formatLaunchDateTimeRelative(it, useUtc)
                } else {
                    DateTimeUtil.formatLaunchDateTime(it, useUtc)
                }
            } ?: "TBD"
        )
    }

    Row(
        modifier = modifier.padding(contentPadding),
        verticalAlignment = Alignment.Top,
    ) {
        // Circular Agency Logo (if enabled and available) - always top-aligned
        if (showAgencyLogo && launch.provider.socialLogo != null) {
            SubcomposeAsyncImage(
                model = launch.provider.socialLogo,
                contentDescription = "Agency Logo",
                modifier = Modifier
                    .size(logoSize)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                loading = { AgencyLogoShimmer(logoSize = logoSize) },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = "Agency placeholder",
                            modifier = Modifier.size(logoSize / 2),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.width(10.dp))
        }

        // Launch Information Column with drop shadows
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Rocket Configuration (first line)
            Text(
                text = rocketConfig,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.8f),
                        offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                        blurRadius = 5f
                    )
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White
            )

            // Mission Name (second line, if available)
            missionName?.let { mission ->
                Text(
                    text = mission,
                    style = MaterialTheme.typography.titleMedium.copy(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.8f),
                            offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                            blurRadius = 5f
                        )
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }

            // Launch Location (if available) with icon and drop shadow
            launch.pad?.location?.name?.let { locationName ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Launch Location",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                blurRadius = 2f
                            )
                        ),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Human Readable Date with icon and drop shadow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.8f),
                            offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                            blurRadius = 2f
                        )
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

// ========================================
// Previews
// ========================================

@Preview
@Composable
private fun AgencyLogoLoadingShimmerPreview() {
    SpaceLaunchNowPreviewTheme {
        Box(modifier = Modifier.background(Color.DarkGray).padding(16.dp)) {
            AgencyLogoShimmer(logoSize = 56.dp)
        }
    }
}

@Preview
@Composable
private fun AgencyLogoLoadingShimmerDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Box(modifier = Modifier.background(Color.DarkGray).padding(16.dp)) {
            AgencyLogoShimmer(logoSize = 56.dp)
        }
    }
}

@Preview
@Composable
private fun LaunchCardHeaderOverlayPreview() {
    SpaceLaunchNowPreviewTheme {
        Box(modifier = Modifier.background(Color.DarkGray)) {
            LaunchCardHeaderOverlay(
                launch = PreviewData.domainLaunchSpaceX,
                showAgencyLogo = true,
                useRelativeTime = true
            )
        }
    }
}

@Preview
@Composable
private fun LaunchCardHeaderOverlayDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Box(modifier = Modifier.background(Color.DarkGray)) {
            LaunchCardHeaderOverlay(
                launch = PreviewData.domainLaunchSpaceX,
                showAgencyLogo = true,
                useRelativeTime = true
            )
        }
    }
}

@Preview
@Composable
private fun LaunchCardHeaderOverlayNoLogoPreview() {
    SpaceLaunchNowPreviewTheme {
        Box(modifier = Modifier.background(Color.DarkGray)) {
            LaunchCardHeaderOverlay(
                launch = PreviewData.domainLaunchULA,
                showAgencyLogo = false,
                useRelativeTime = false
            )
        }
    }
}

@Preview
@Composable
private fun LaunchCardHeaderOverlayNoLogoDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Box(modifier = Modifier.background(Color.DarkGray)) {
            LaunchCardHeaderOverlay(
                launch = PreviewData.domainLaunchULA,
                showAgencyLogo = false,
                useRelativeTime = false
            )
        }
    }
}

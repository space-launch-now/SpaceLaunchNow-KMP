package me.calebjones.spacelaunchnow.ui.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.ui.preview.PreviewData
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.ui.icons.CustomIcons
import me.calebjones.spacelaunchnow.ui.icons.RocketLaunch
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.util.DateTimeUtil
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Reusable launch card component displaying a compact launch summary.
 *
 * Shows:
 * - Circular thumbnail/image
 * - Launch name
 * - Launch service provider
 * - Location
 * - Date with precision formatting
 *
 * Used in Schedule screen and Astronaut flight history.
 */
@Composable
fun ScheduleLaunchView(
    launch: Launch,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val imageUrl = launch.thumbnailUrl ?: launch.imageUrl
        val useUtc = LocalUseUtc.current
        val dateText = DateTimeUtil.formatDateWithPrecisionFallback(launch, useUtc)
        val title = launch.name ?: "Unknown Launch"
        val location = launch.pad?.location?.name ?: "Unknown Location"
        val mission = launch.provider.name

        if (imageUrl != null) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = null,
                loading = { RocketIconPlaceholder() },
                error = { RocketIconPlaceholder() },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CustomIcons.RocketLaunch,
                    contentDescription = "placeholder",
                    modifier = Modifier.size(48.dp).padding(4.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = mission,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = location,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun RocketIconPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = CustomIcons.RocketLaunch,
            contentDescription = "placeholder",
            modifier = Modifier.size(48.dp).padding(4.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
    }
}

@Preview
@Composable
private fun ScheduleLaunchCardPreview() {
    SpaceLaunchNowPreviewTheme {
        ScheduleLaunchView(
            launch = PreviewData.domainLaunchSpaceX,
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun ScheduleLaunchCardNoImagePreview() {
    SpaceLaunchNowPreviewTheme {
        ScheduleLaunchView(
            launch = PreviewData.domainLaunchULA.copy(imageUrl = null, thumbnailUrl = null),
            onClick = {}
        )
    }
}

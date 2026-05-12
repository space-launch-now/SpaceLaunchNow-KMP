package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import coil3.compose.AsyncImage
import me.calebjones.spacelaunchnow.domain.model.VideoLink
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.util.VideoUtil
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Card list of alternate videos for a launch/event. Shown below the embedded
 * video player. Each card opens its video externally — there is no inline
 * switching of the embedded player from this surface.
 */
@Composable
fun AlternateVideosCard(
    videos: List<VideoLink>,
    currentlyPlayingIndex: Int,
    launchName: String,
    onOpenExternal: (VideoLink) -> Unit,
    modifier: Modifier = Modifier,
) {
    val alternates = videos.filterIndexed { index, _ -> index != currentlyPlayingIndex }
    if (alternates.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "More Videos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        alternates.forEach { video ->
            AlternateVideoRow(
                video = video,
                launchName = launchName,
                onClick = { onOpenExternal(video) },
            )
        }
    }
}

@Composable
private fun AlternateVideoRow(
    video: VideoLink,
    launchName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
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
            VideoThumbnail(
                video = video,
                isLive = video.live == true,
                modifier = Modifier
                    .width(96.dp)
                    .height(54.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = video.title?.takeIf { it.isNotBlank() } ?: launchName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = buildSubtitle(video),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(
                onClick = onClick,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Open video in app",
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun VideoThumbnail(
    video: VideoLink,
    isLive: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val imageUrl = video.featureImage?.takeIf { it.isNotBlank() }
        ?: youTubeThumbnailUrl(video.url)

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        if (isLive) {
            Surface(
                color = Color.Red,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Text(
                    text = "LIVE",
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun youTubeThumbnailUrl(videoUrl: String): String? {
    if (!VideoUtil.isYouTubeUrl(videoUrl)) return null
    val id = VideoUtil.extractYouTubeVideoId(videoUrl) ?: return null
    return "https://img.youtube.com/vi/$id/mqdefault.jpg"
}

private fun buildSubtitle(video: VideoLink): String {
    val publisher = video.publisher?.takeIf { it.isNotBlank() }
    val domain = extractDomain(video.url)
    return when {
        publisher != null && domain.isNotBlank() -> "$publisher · $domain"
        publisher != null -> publisher
        domain.isNotBlank() -> domain
        else -> VideoUtil.getVideoSourceName(video)
    }
}

private fun extractDomain(url: String): String {
    val withoutProtocol = url.substringAfter("://", url)
    val host = withoutProtocol
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
    return host.removePrefix("www.")
}

// region Previews

private val previewVideos = listOf(
    VideoLink(
        url = "https://www.youtube.com/watch?v=abc123",
        title = "SpaceX NROL-172",
        source = "youtube",
        publisher = "The Space Devs",
        description = null,
        featureImage = null,
        live = null,
        priority = 1,
    ),
    VideoLink(
        url = "https://www.youtube.com/watch?v=def456",
        title = "Live: SpaceX Falcon 9 rocket launches from California on a mission for U.S. spy satellites",
        source = "youtube",
        publisher = "Spaceflight Now",
        description = null,
        featureImage = null,
        live = true,
        priority = 2,
    ),
    VideoLink(
        url = "https://x.com/SpaceX/status/12345",
        title = "NROL-172 Mission",
        source = "x",
        publisher = "SpaceX",
        description = null,
        featureImage = null,
        live = null,
        priority = 3,
    ),
)

@Preview
@Composable
private fun AlternateVideosCardPreview_Light() {
    SpaceLaunchNowPreviewTheme {
        AlternateVideosCard(
            videos = previewVideos,
            currentlyPlayingIndex = 0,
            launchName = "Falcon 9 Block 5 | SpaceX",
            onOpenExternal = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun AlternateVideosCardPreview_Dark() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        AlternateVideosCard(
            videos = previewVideos,
            currentlyPlayingIndex = 0,
            launchName = "Falcon 9 Block 5 | SpaceX",
            onOpenExternal = {},
        )
    }
}

// endregion

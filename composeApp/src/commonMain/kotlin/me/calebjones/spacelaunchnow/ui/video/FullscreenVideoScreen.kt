package me.calebjones.spacelaunchnow.ui.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import me.calebjones.spacelaunchnow.api.launchlibrary.models.VidURL
import me.calebjones.spacelaunchnow.util.VideoUtil
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.ui.video.VideoPlayerComposable
import chaintech.videoplayer.model.VideoPlayerConfig

@Composable
fun FullscreenVideoScreen(
    vidUrl: VidURL,
    launchName: String,
    availableVideos: List<VidURL> = emptyList(),
    selectedVideoIndex: Int = 0,
    onNavigateBack: () -> Unit,
    onVideoSelected: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding() // Add system bars padding
    ) {
        // Main video player
        val playerHost = remember(vidUrl.url) {
            MediaPlayerHost(mediaUrl = vidUrl.url)
        }

        VideoPlayerComposable(
            modifier = Modifier.fillMaxSize(),
            playerHost = playerHost,
            playerConfig = VideoPlayerConfig(
                isFullScreenEnabled = false
            )
        )

        // Top bar with back button and video info
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .background(
                    Color.Black.copy(alpha = 0.7f)
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ), // Increased padding for better spacing
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(4.dp) // Extra padding for the button itself
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Exit fullscreen",
                    tint = Color.White
                )
            }

            // Video title
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = VideoUtil.getVideoTitle(vidUrl, launchName),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = VideoUtil.getVideoSourceName(vidUrl),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Live indicator
            if (vidUrl.live == true) {
                Surface(
                    color = Color.Red,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "LIVE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Bottom controls for multiple videos
        if (availableVideos.size > 1 && onVideoSelected != null) {
            VideoSelectionControls(
                videos = availableVideos,
                selectedIndex = selectedVideoIndex,
                launchName = launchName,
                onVideoSelected = onVideoSelected,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(alpha = 0.7f)
                    )
                    .padding(
                        horizontal = 16.dp,
                        vertical = 24.dp  // Increased bottom padding for system navigation
                    )
            )
        }
    }
}

@Composable
private fun VideoSelectionControls(
    videos: List<VidURL>,
    selectedIndex: Int,
    launchName: String,
    onVideoSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Current video info
        Text(
            text = "Video ${selectedIndex + 1} of ${videos.size}",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f)
        )

        // Video selection buttons (horizontal scroll)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            videos.forEachIndexed { index, video ->
                VideoSelectionButton(
                    video = video,
                    launchName = launchName,
                    isSelected = index == selectedIndex,
                    onClick = { onVideoSelected(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun VideoSelectionButton(
    video: VidURL,
    launchName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                Color.White.copy(alpha = 0.2f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = VideoUtil.getVideoTitle(video, launchName),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (video.live == true) {
                Text(
                    text = "LIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Simplified version for single video with URL and launch name
@Composable
fun FullscreenVideoScreen(
    videoUrl: String,
    launchName: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Create a simple VidURL object for the single video
    val vidUrl = remember {
        VidURL(
            url = videoUrl,
            type = null,  // VidURLType is optional
            language = null,  // Language is optional
            title = launchName,
            description = null,
            source = null,
            publisher = null,
            featureImage = null,
            live = null
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding() // Add system bars padding
    ) {
        // Main video player
        val playerHost = remember(videoUrl) {
            MediaPlayerHost(mediaUrl = videoUrl)
        }

        VideoPlayerComposable(
            modifier = Modifier.fillMaxSize(),
            playerHost = playerHost,
            playerConfig = VideoPlayerConfig(
                isFullScreenEnabled = false
            )
        )

        // Top bar with back button and video info
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .background(
                    Color.Black.copy(alpha = 0.7f)
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ), // Increased padding for better spacing
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(4.dp) // Extra padding for the button itself
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Exit fullscreen",
                    tint = Color.White
                )
            }

            // Video title
            Text(
                text = launchName,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )
        }
    }
}
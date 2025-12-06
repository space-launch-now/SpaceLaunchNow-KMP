package me.calebjones.spacelaunchnow.ui.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.model.VideoPlayerConfig
import chaintech.videoplayer.ui.video.VideoPlayerComposable
import kotlinx.coroutines.delay
import me.calebjones.spacelaunchnow.api.launchlibrary.models.VidURL
import me.calebjones.spacelaunchnow.api.launchlibrary.models.VidURLType
import me.calebjones.spacelaunchnow.util.VideoUtil
import kotlin.time.Clock.System

private val log by lazy { SpaceLogger.getLogger("FullscreenVideoScreen") }

@OptIn(ExperimentalMaterial3Api::class)
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
    val uriHandler = LocalUriHandler.current

    // Auto-hiding controls state
    var showControls by rememberSaveable { mutableStateOf(true) }
    var lastTapTime by remember { mutableLongStateOf(System.now().toEpochMilliseconds()) }

    // Auto-hide controls after 3 seconds
    LaunchedEffect(showControls, lastTapTime) {
        log.v { "LaunchedEffect triggered - showControls: $showControls, lastTapTime: $lastTapTime" }
        if (showControls) {
            delay(3000)
            if (showControls) { // Check again in case user interacted during delay
                log.v { "Auto-hiding controls after 3 seconds" }
                showControls = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
    ) {
        // Video content (always visible)
        val playerHost = remember("fullscreen_${vidUrl.url}") {
            MediaPlayerHost(mediaUrl = vidUrl.url)
        }

        VideoPlayerComposable(
            modifier = Modifier.fillMaxSize(),
            playerHost = playerHost,
            playerConfig = VideoPlayerConfig(
                isFullScreenEnabled = false
            )
        )

        // Touch overlay - only active when controls are hidden
        if (!showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                showControls = true
                                lastTapTime = System.now().toEpochMilliseconds()
                            }
                        )
                    }
            )
        }

        // Animated header bar
        log.v { "Rendering header bar (detailed) - showControls: $showControls" }
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            log.v { "AnimatedVisibility content block executing (detailed) - showControls: $showControls" }
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Title section - takes available space
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = VideoUtil.getVideoTitle(vidUrl, launchName),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = VideoUtil.getVideoSourceName(vidUrl),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    // Actions row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Live indicator
                        if (vidUrl.live == true) {
                            Surface(
                                color = Color.Red,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "LIVE",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Open external icon
                        IconButton(
                            onClick = {
                                try {
                                    uriHandler.openUri(vidUrl.url)
                                } catch (e: Exception) {
                                    log.e("Failed to open external URL: ${e.message}")
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open in external app",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Invisible clickable area below the header to hide controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Give some space for easy tapping
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showControls = false
                        }
                )
            }
        }

        // Animated bottom controls for multiple videos
        if (availableVideos.size > 1 && onVideoSelected != null) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                VideoSelectionControls(
                    videos = availableVideos,
                    selectedIndex = selectedVideoIndex,
                    launchName = launchName,
                    onVideoSelected = onVideoSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.Black.copy(alpha = 0.7f)
                        )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 24.dp
                        )
                )
            }
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
        modifier = modifier.height(24.dp),
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullscreenVideoScreen(
    videoUrl: String,
    launchName: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    // Auto-hiding controls state
    var showControls by rememberSaveable { mutableStateOf(true) }
    var lastTapTime by remember { mutableLongStateOf(System.now().toEpochMilliseconds()) }

    // Auto-hide controls after 3 seconds
    LaunchedEffect(showControls, lastTapTime) {
        log.v { "LaunchedEffect (simplified) triggered - showControls: $showControls, lastTapTime: $lastTapTime" }
        if (showControls) {
            delay(3000)
            if (showControls) { // Check again in case user interacted during delay
                log.v { "Auto-hiding controls after 3 seconds (simplified)" }
                showControls = false
            }
        }
    }

    // Create a simple VidURL object for the single video
    val vidUrl = remember(videoUrl) {
        VidURL(
            url = videoUrl,
            type = VidURLType(1, "Local"),  // VidURLType is optional
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
            .systemBarsPadding()
    ) {
        // Video content (always visible)
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

        // Touch overlay - only active when controls are hidden
        if (!showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                log.v { "Touch overlay tapped - showing controls" }
                                showControls = true
                                lastTapTime = System.now().toEpochMilliseconds()
                            }
                        )
                    }
            )
        }

        // Animated header bar
        log.v { "Rendering header bar (simplified) - showControls: $showControls" }
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            log.v { "AnimatedVisibility content block executing (simplified) - showControls: $showControls" }
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Title section - takes available space
                    Text(
                        text = launchName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )

                    // Open external icon
                    IconButton(
                        onClick = {
                            try {
                                uriHandler.openUri(videoUrl)
                            } catch (e: Exception) {
                                log.e("Failed to open external URL: ${e.message}")
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open in external app",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Invisible clickable area below the header to hide controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Give some space for easy tapping
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            log.v { "Control area tapped - hiding controls" }
                            showControls = false
                        }
                )
            }
        }
    }
}
package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import chaintech.videoplayer.model.VideoPlayerConfig
import me.calebjones.spacelaunchnow.ui.compose.LaunchVideoPlayer
import me.calebjones.spacelaunchnow.ui.state.VideoPlayerState
import me.calebjones.spacelaunchnow.util.VideoUtil

/**
 * Video player component without card wrapper
 * Use this for edge-to-edge layouts
 */
@Composable
fun VideoPlayer(
    videoPlayerState: VideoPlayerState,
    launchName: String,
    onSetPlayerVisible: (Boolean) -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit,
    @Suppress("UNUSED_PARAMETER") onVideoSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    playerConfig: VideoPlayerConfig = VideoPlayerConfig(isFullScreenEnabled = false),
    showVideoPicker: Boolean = true,
    onExternalVideoOpened: ((String, String) -> Unit)? = null
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Embedded player for the currently-selected video
        val selectedVideo =
            videoPlayerState.availableVideos[videoPlayerState.selectedVideoIndex]
        LaunchVideoPlayer(
            vidUrl = selectedVideo,
            launchName = launchName,
            isPlayerVisible = videoPlayerState.isPlayerVisible,
            onSetPlayerVisible = onSetPlayerVisible,
            onNavigateToFullscreen = onNavigateToFullscreen,
            modifier = Modifier.fillMaxWidth(),
            playerConfig = playerConfig,
            onExternalVideoOpened = onExternalVideoOpened
        )

        // Alternate videos list (only show if there are multiple videos and picker is enabled)
        if (showVideoPicker && videoPlayerState.availableVideos.size > 1) {
            AlternateVideosCard(
                videos = videoPlayerState.availableVideos,
                currentlyPlayingIndex = videoPlayerState.selectedVideoIndex,
                launchName = launchName,
                onOpenExternal = { video ->
                    onExternalVideoOpened?.invoke(
                        video.url,
                        VideoUtil.getVideoSourceName(video)
                    )
                    uriHandler.openUri(video.url)
                }
            )
        }
    }
}

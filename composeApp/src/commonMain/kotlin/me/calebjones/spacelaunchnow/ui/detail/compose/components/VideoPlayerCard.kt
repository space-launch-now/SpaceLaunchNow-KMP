package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chaintech.videoplayer.model.VideoPlayerConfig
import me.calebjones.spacelaunchnow.ui.state.VideoPlayerState

/**
 * Video player wrapped in a Card for use in Launch Detail view
 */
@Composable
fun VideoPlayerCard(
    videoPlayerState: VideoPlayerState,
    launchName: String,
    onSetPlayerVisible: (Boolean) -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit,
    onVideoSelected: (Int) -> Unit,
    playerConfig: VideoPlayerConfig = VideoPlayerConfig(isFullScreenEnabled = false),
    showVideoPicker: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        VideoPlayer(
            videoPlayerState = videoPlayerState,
            launchName = launchName,
            onSetPlayerVisible = onSetPlayerVisible,
            onNavigateToFullscreen = onNavigateToFullscreen,
            onVideoSelected = onVideoSelected,
            playerConfig = playerConfig,
            showVideoPicker = showVideoPicker
        )
    }
}

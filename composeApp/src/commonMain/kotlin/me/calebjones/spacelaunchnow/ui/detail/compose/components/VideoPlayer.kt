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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import chaintech.videoplayer.model.VideoPlayerConfig
import me.calebjones.spacelaunchnow.api.launchlibrary.models.VidURL
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
    onVideoSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    playerConfig: VideoPlayerConfig = VideoPlayerConfig(isFullScreenEnabled = false),
    showVideoPicker: Boolean = true,
    onExternalVideoOpened: ((String, String) -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Video player
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

        // Video picker (only show if there are multiple videos and showVideoPicker is true)
        if (showVideoPicker && videoPlayerState.availableVideos.size > 1) {
            VideoPicker(
                videos = videoPlayerState.availableVideos,
                selectedIndex = videoPlayerState.selectedVideoIndex,
                launchName = launchName,
                onVideoSelected = onVideoSelected,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun VideoPicker(
    videos: List<VidURL>,
    selectedIndex: Int,
    launchName: String,
    onVideoSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Select Video",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        VideoPickerDropdown(
            videos = videos,
            selectedIndex = selectedIndex,
            launchName = launchName,
            onVideoSelected = onVideoSelected
        )
    }
}

@Composable
private fun VideoPickerDropdown(
    videos: List<VidURL>,
    selectedIndex: Int,
    launchName: String,
    onVideoSelected: (Int) -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val selectedVideo = videos[selectedIndex]

    Box(modifier = Modifier.fillMaxWidth()) {
        // Material 3 Expressive style button
        Button(
            onClick = { isDropdownExpanded = !isDropdownExpanded },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), // Material 3 expressive height
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(16.dp) // Expressive corner radius
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = VideoUtil.getVideoTitle(selectedVideo, launchName),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.width(8.dp))

                Icon(
                    imageVector = if (isDropdownExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isDropdownExpanded) "Close menu" else "Open menu",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Material 3 Expressive dropdown menu
        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
            offset = DpOffset(0.dp, 8.dp), // Expressive spacing from anchor
            shape = RoundedCornerShape(16.dp), // Expressive corner radius
            modifier = Modifier.fillMaxWidth()
        ) {
            videos.forEachIndexed { index, video ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = VideoUtil.getVideoTitle(video, launchName),
                                modifier = Modifier.weight(1f),
                                maxLines = 2, // Allow more lines for better readability
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            if (index == selectedIndex) {
                                Spacer(Modifier.width(12.dp))
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        onVideoSelected(index)
                        isDropdownExpanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = if (index == selectedIndex) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    ),
                    contentPadding = MenuDefaults.DropdownMenuItemContentPadding
                )
            }
        }
    }
}

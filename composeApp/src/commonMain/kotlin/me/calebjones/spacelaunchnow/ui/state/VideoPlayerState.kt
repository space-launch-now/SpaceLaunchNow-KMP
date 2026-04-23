package me.calebjones.spacelaunchnow.ui.state

import me.calebjones.spacelaunchnow.domain.model.VideoLink

/**
 * Video player state for managing video playback across the app
 */
data class VideoPlayerState(
    val selectedVideoIndex: Int = 0,
    val isPlayerVisible: Boolean = false,
    val isFullscreen: Boolean = false,
    val availableVideos: List<VideoLink> = emptyList()
)

package me.calebjones.spacelaunchnow.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import me.calebjones.spacelaunchnow.i18n.ScreenLayoutHarness
import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailView
import me.calebjones.spacelaunchnow.ui.state.VideoPlayerState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LaunchDetailScreenLayoutTest {
    @Test
    fun launchDetailLayout() = ScreenLayoutHarness.assertLayout("launch_detail", koinModules = { listOf(ScreenKoin.common) }) {
        LaunchDetailView(
            launch = ScreenFixtures.launch,
            videoPlayerState = VideoPlayerState(),
            relatedNews = emptyList(),
            isNewsLoading = false,
            newsError = null,
            onSelectVideo = {},
            onSetPlayerVisible = {},
            onNavigateToFullscreen = { _, _ -> },
            onVideoSelected = {},
            forcePhoneLayout = true,
        )
    }
}

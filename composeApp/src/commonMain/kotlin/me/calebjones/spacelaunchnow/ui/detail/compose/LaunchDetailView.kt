package me.calebjones.spacelaunchnow.ui.detail.compose

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.isLargeScreen
import me.calebjones.spacelaunchnow.ui.compose.SharedDetailScaffold
import me.calebjones.spacelaunchnow.ui.detail.compose.phone.PhoneLaunchDetailContent
import me.calebjones.spacelaunchnow.ui.detail.compose.tablet.TabletLaunchDetailContent
import me.calebjones.spacelaunchnow.ui.state.VideoPlayerState
import me.calebjones.spacelaunchnow.util.StatusColorUtil.getLaunchStatusColor

// Height constants for spacing adjustments
private val TitleHeight = 120.dp
private val CompactHeight = 40.dp

/**
 * Shared element transition configuration for launch detail animations.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val snackDetailBoundsTransform = BoundsTransform { _, _ ->
    spring(dampingRatio = 0.8f, stiffness = 380f)
}

/**
 * Main launch detail view composable.
 *
 * This is the entry point for displaying detailed information about a launch.
 * It wraps the content in a [SharedDetailScaffold] and delegates the actual
 * content rendering to either [PhoneLaunchDetailContent] (tabbed layout) or
 * [TabletLaunchDetailContent] (two-column layout) based on screen size.
 *
 * @param launch The detailed launch information to display
 * @param videoPlayerState State for the embedded video player
 * @param relatedNews List of related news articles
 * @param isNewsLoading Whether news is currently loading
 * @param newsError Error message if news loading failed
 * @param relatedEvents List of related events
 * @param isEventsLoading Whether events are currently loading
 * @param eventsError Error message if events loading failed
 * @param onSelectVideo Callback when a video is selected
 * @param onSetPlayerVisible Callback to show/hide the video player
 * @param onNavigateBack Callback when the back button is pressed
 * @param onNavigateToFullscreen Callback to navigate to fullscreen video
 * @param onVideoSelected Callback when a video is selected from the list
 * @param onNavigateToSettings Optional callback to navigate to settings
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class
)
@Composable
fun LaunchDetailView(
    launch: LaunchDetailed,
    videoPlayerState: VideoPlayerState,
    relatedNews: List<Article>,
    isNewsLoading: Boolean,
    newsError: String?,
    relatedEvents: List<EventEndpointNormal> = emptyList(),
    isEventsLoading: Boolean = false,
    eventsError: String? = null,
    onSelectVideo: (Int) -> Unit,
    onSetPlayerVisible: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit,
    onVideoSelected: (Int) -> Unit,
    onNavigateToSettings: (() -> Unit)? = null,
    onEventClick: ((Int) -> Unit)? = null,
    onAstronautClick: ((Int) -> Unit)? = null
) {
    val isLargeScreen = isLargeScreen()

    // Completely separate architecture for phone vs tablet
    if (isLargeScreen) {
        // TABLET: Scrollable scaffold with two-column layout
        TabletLaunchDetailView(
            launch = launch,
            videoPlayerState = videoPlayerState,
            relatedNews = relatedNews,
            isNewsLoading = isNewsLoading,
            newsError = newsError,
            relatedEvents = relatedEvents,
            isEventsLoading = isEventsLoading,
            eventsError = eventsError,
            onSelectVideo = onSelectVideo,
            onSetPlayerVisible = onSetPlayerVisible,
            onNavigateBack = onNavigateBack,
            onNavigateToFullscreen = onNavigateToFullscreen,
            onVideoSelected = onVideoSelected,
            onNavigateToSettings = onNavigateToSettings,
            onEventClick = onEventClick,
            onAstronautClick = onAstronautClick
        )
    } else {
        // PHONE: Non-scrollable scaffold with tabbed layout (tabs handle their own scrolling)
        PhoneLaunchDetailView(
            launch = launch,
            videoPlayerState = videoPlayerState,
            relatedNews = relatedNews,
            isNewsLoading = isNewsLoading,
            newsError = newsError,
            relatedEvents = relatedEvents,
            isEventsLoading = isEventsLoading,
            eventsError = eventsError,
            onSelectVideo = onSelectVideo,
            onSetPlayerVisible = onSetPlayerVisible,
            onNavigateBack = onNavigateBack,
            onNavigateToFullscreen = onNavigateToFullscreen,
            onVideoSelected = onVideoSelected,
            onNavigateToSettings = onNavigateToSettings,
            onEventClick = onEventClick,
            onAstronautClick = onAstronautClick
        )
    }
}

/**
 * Tablet view with scrollable scaffold and two-column layout
 */
@Composable
private fun TabletLaunchDetailView(
    launch: LaunchDetailed,
    videoPlayerState: VideoPlayerState,
    relatedNews: List<Article>,
    isNewsLoading: Boolean,
    newsError: String?,
    relatedEvents: List<EventEndpointNormal>,
    isEventsLoading: Boolean,
    eventsError: String?,
    onSelectVideo: (Int) -> Unit,
    onSetPlayerVisible: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit,
    onVideoSelected: (Int) -> Unit,
    onNavigateToSettings: (() -> Unit)? = null,
    onEventClick: ((Int) -> Unit)? = null,
    onAstronautClick: ((Int) -> Unit)? = null
) {
    SharedDetailScaffold(
        titleText = launch.name ?: "Unknown Launch",
        taglineText = launch.launchServiceProvider.name,
        imageUrl = launch.image?.imageUrl,
        onNavigateBack = onNavigateBack,
        scrollEnabled = true, // Tablet uses scrollable layout
        backgroundColors = listOf(
            getLaunchStatusColor(launch.status?.id),
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        val uriHandler = LocalUriHandler.current
        val openUrl: (String) -> Unit = { url ->
            try {
                uriHandler.openUri(url)
            } catch (_: Throwable) {
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(CompactHeight))

            TabletLaunchDetailContent(
                launch = launch,
                videoPlayerState = videoPlayerState,
                relatedNews = relatedNews,
                isNewsLoading = isNewsLoading,
                newsError = newsError,
                relatedEvents = relatedEvents,
                isEventsLoading = isEventsLoading,
                eventsError = eventsError,
                onSelectVideo = onSelectVideo,
                onSetPlayerVisible = onSetPlayerVisible,
                onNavigateToFullscreen = onNavigateToFullscreen,
                onVideoSelected = onVideoSelected,
                onNavigateToSettings = onNavigateToSettings,
                onEventClick = onEventClick,
                onAstronautClick = onAstronautClick,
                openUrl = openUrl
            )

            Spacer(Modifier.height(200.dp))
        }
    }
}

/**
 * Phone view with scrollable scaffold and tabbed layout
 * The scaffold handles scrolling and header collapse
 */
@Composable
private fun PhoneLaunchDetailView(
    launch: LaunchDetailed,
    videoPlayerState: VideoPlayerState,
    relatedNews: List<Article>,
    isNewsLoading: Boolean,
    newsError: String?,
    relatedEvents: List<EventEndpointNormal>,
    isEventsLoading: Boolean,
    eventsError: String?,
    onSelectVideo: (Int) -> Unit,
    onSetPlayerVisible: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit,
    onVideoSelected: (Int) -> Unit,
    onNavigateToSettings: (() -> Unit)? = null,
    onEventClick: ((Int) -> Unit)? = null,
    onAstronautClick: ((Int) -> Unit)? = null
) {
    SharedDetailScaffold(
        titleText = launch.name ?: "Unknown Launch",
        taglineText = launch.launchServiceProvider.name,
        imageUrl = launch.image?.imageUrl,
        onNavigateBack = onNavigateBack,
        scrollEnabled = true, // Parent scrolling enabled for header collapse
        backgroundColors = listOf(
            getLaunchStatusColor(launch.status?.id),
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        val uriHandler = LocalUriHandler.current
        val openUrl: (String) -> Unit = { url ->
            try {
                uriHandler.openUri(url)
            } catch (_: Throwable) {
            }
        }

        // Column provides layout for content, parent scaffold handles scrolling
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(TitleHeight))

            PhoneLaunchDetailContent(
                launch = launch,
                videoPlayerState = videoPlayerState,
                relatedNews = relatedNews,
                isNewsLoading = isNewsLoading,
                newsError = newsError,
                relatedEvents = relatedEvents,
                isEventsLoading = isEventsLoading,
                eventsError = eventsError,
                onSelectVideo = onSelectVideo,
                onSetPlayerVisible = onSetPlayerVisible,
                onNavigateToFullscreen = onNavigateToFullscreen,
                onVideoSelected = onVideoSelected,
                onNavigateToSettings = onNavigateToSettings,
                onEventClick = onEventClick,
                onAstronautClick = onAstronautClick,
                openUrl = openUrl
            )

            Spacer(Modifier.height(200.dp))
        }
    }
}
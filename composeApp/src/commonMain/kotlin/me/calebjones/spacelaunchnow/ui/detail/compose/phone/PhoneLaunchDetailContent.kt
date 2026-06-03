package me.calebjones.spacelaunchnow.ui.detail.compose.phone

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailTab
import me.calebjones.spacelaunchnow.ui.detail.compose.components.AlternateVideosCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.CombinedLaunchOverviewCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.CrewInformationCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.VideoPlayerCard
import me.calebjones.spacelaunchnow.ui.detail.compose.tabs.AgencyTabContent
import me.calebjones.spacelaunchnow.ui.detail.compose.tabs.MissionTabContent
import me.calebjones.spacelaunchnow.ui.detail.compose.tabs.OverviewTabContent
import me.calebjones.spacelaunchnow.ui.detail.compose.tabs.RocketTabContent
import me.calebjones.spacelaunchnow.ui.state.VideoPlayerState
import me.calebjones.spacelaunchnow.util.VideoUtil
import kotlin.time.Clock

/**
 * Phone-specific layout for launch detail view.
 *
 * Displays a tabbed interface with tab content switching.
 * Content is laid out vertically - parent scaffold handles scrolling.
 *
 * The CombinedLaunchOverviewCard and video player (if available) are
 * always visible above the tabs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneLaunchDetailContent(
    launch: Launch,
    videoPlayerState: VideoPlayerState,
    relatedNews: List<Article>,
    isNewsLoading: Boolean,
    newsError: String?,
    relatedEvents: List<Event> = emptyList(),
    isEventsLoading: Boolean = false,
    eventsError: String? = null,
    onSelectVideo: (Int) -> Unit,
    onSetPlayerVisible: (Boolean) -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit,
    onVideoSelected: (Int) -> Unit,
    onNavigateToSettings: (() -> Unit)? = null,
    onEventClick: ((Int) -> Unit)? = null,
    onAstronautClick: ((Int) -> Unit)? = null,
    onArticleClick: ((url: String, title: String) -> Unit)? = null,
    openUrl: (String) -> Unit,
    onExternalVideoOpened: ((String, String) -> Unit)? = null
) {
    val tabs = LaunchDetailTab.values()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val uriHandler = LocalUriHandler.current

    Column {
        // Combined Launch Overview Card at the top (always visible above tabs)
        CombinedLaunchOverviewCard(launch = launch)
        Spacer(Modifier.height(16.dp))

        // Video Player Card (above tabs, only shown if videos are available)
        if (videoPlayerState.availableVideos.isNotEmpty()) {
            val videoTitle = launch.net?.let { net ->
                val now = Clock.System.now()
                if (net > now) "Watch Live" else "Watch Replay"
            } ?: "Watch Launch"

            Text(
                text = videoTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            VideoPlayerCard(
                videoPlayerState = videoPlayerState,
                launchName = launch.mission?.name ?: "Space Launch",
                onSetPlayerVisible = onSetPlayerVisible,
                onNavigateToFullscreen = onNavigateToFullscreen,
                onVideoSelected = { /* Alternate videos open externally via AlternateVideosCard below */ },
                showVideoPicker = false,
                onExternalVideoOpened = onExternalVideoOpened
            )
            Spacer(Modifier.height(16.dp))

            // Alternate videos as cards (only show if there are multiple videos)
            if (videoPlayerState.availableVideos.size > 1) {
                AlternateVideosCard(
                    videos = videoPlayerState.availableVideos,
                    currentlyPlayingIndex = videoPlayerState.selectedVideoIndex,
                    launchName = launch.mission?.name ?: "Space Launch",
                    onOpenExternal = { video ->
                        onExternalVideoOpened?.invoke(
                            video.url,
                            VideoUtil.getVideoSourceName(video)
                        )
                        uriHandler.openUri(video.url)
                    }
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        // Crew Information Card (if crewed launch)
        CrewInformationCard(
            launchCrew = launch.rocketDetail?.spacecraftFlights?.flatMap { it.launchCrew } ?: emptyList(),
            onboardCrew = launch.rocketDetail?.spacecraftFlights?.flatMap { it.onboardCrew } ?: emptyList(),
            landingCrew = launch.rocketDetail?.spacecraftFlights?.flatMap { it.landingCrew } ?: emptyList(),
            onAstronautClick = onAstronautClick
        )
        Spacer(Modifier.height(16.dp))

        // Ad Banner above tabs
        SmartBannerAd(
            modifier = Modifier.fillMaxWidth(),
            placementType = AdPlacementType.INTERSTITIAL,
            showRemoveAdsButton = true,
            onRemoveAdsClick = onNavigateToSettings
        )
        Spacer(Modifier.height(16.dp))

        // Tab Row for tab selection
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(tab.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Tab content based on selected tab
        when (LaunchDetailTab.fromIndex(selectedTabIndex)) {
            LaunchDetailTab.Overview -> {
                OverviewTabContent(
                    launch = launch,
                    videoPlayerState = videoPlayerState,
                    relatedNews = relatedNews,
                    isNewsLoading = isNewsLoading,
                    newsError = newsError,
                    relatedEvents = relatedEvents,
                    isEventsLoading = isEventsLoading,
                    eventsError = eventsError,
                    onSetPlayerVisible = onSetPlayerVisible,
                    onNavigateToFullscreen = onNavigateToFullscreen,
                    onVideoSelected = onVideoSelected,
                    onNavigateToSettings = onNavigateToSettings,
                    onEventClick = onEventClick,
                    onArticleClick = onArticleClick
                )
            }

            LaunchDetailTab.Mission -> {
                MissionTabContent(
                    launch = launch,
                    openUrl = openUrl
                )
            }

            LaunchDetailTab.Agency -> {
                AgencyTabContent(
                    launch = launch,
                    openUrl = openUrl
                )
            }

            LaunchDetailTab.Rocket -> {
                RocketTabContent(
                    launch = launch,
                    openUrl = openUrl,
                    onAstronautClick = onAstronautClick
                )
            }
        }
    }
}

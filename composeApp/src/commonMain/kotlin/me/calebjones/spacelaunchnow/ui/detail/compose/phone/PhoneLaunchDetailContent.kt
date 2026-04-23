package me.calebjones.spacelaunchnow.ui.detail.compose.phone

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.VideoLink
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailTab
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
    openUrl: (String) -> Unit,
    onExternalVideoOpened: ((String, String) -> Unit)? = null
) {
    val tabs = LaunchDetailTab.values()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

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
                onVideoSelected = { /* Video selection handled below */ },
                showVideoPicker = false,
                onExternalVideoOpened = onExternalVideoOpened
            )
            Spacer(Modifier.height(16.dp))

            // Separate Video Picker Card (only show if there are multiple videos)
            if (videoPlayerState.availableVideos.size > 1) {
                VideoPickerCardSeparate(
                    videos = videoPlayerState.availableVideos,
                    selectedIndex = videoPlayerState.selectedVideoIndex,
                    launchName = launch.mission?.name ?: "Space Launch",
                    onVideoSelected = onVideoSelected
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
                    onEventClick = onEventClick
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

@Composable
private fun VideoPickerCardSeparate(
    videos: List<VideoLink>,
    selectedIndex: Int,
    launchName: String,
    onVideoSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val selectedVideo = videos[selectedIndex]

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Select Video",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
            )

            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 0.dp)) {
                // Material 3 Expressive style button
                Button(
                    onClick = { isDropdownExpanded = !isDropdownExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(16.dp)
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
                    offset = DpOffset(0.dp, 8.dp),
                    shape = RoundedCornerShape(16.dp),
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
                                        maxLines = 2,
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
            Spacer(Modifier.height(16.dp))
        }
    }
}


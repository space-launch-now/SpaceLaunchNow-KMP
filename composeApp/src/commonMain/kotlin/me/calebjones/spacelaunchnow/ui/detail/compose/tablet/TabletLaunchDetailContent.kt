package me.calebjones.spacelaunchnow.ui.detail.compose.tablet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.ui.detail.compose.components.CombinedLaunchOverviewCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.AgencyDetailsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.AgencyLaunchStatistics
import me.calebjones.spacelaunchnow.ui.detail.compose.components.FlightClubCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LandingDetailsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LaunchLocationCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LaunchUpdatesSection
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LaunchVehicleDetailedStatistics
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LaunchVehicleDetailsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.MissionDetailsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.PadQuickStatsRow
import me.calebjones.spacelaunchnow.ui.detail.compose.components.QuickStatsGrid
import me.calebjones.spacelaunchnow.ui.detail.compose.components.RelatedEventsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.RelatedNewsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.SpacecraftDetailsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.TimelineCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.VideoPlayerCard
import me.calebjones.spacelaunchnow.ui.state.VideoPlayerState
import kotlin.time.Clock.System

/**
 * Tablet/Desktop-specific layout for launch detail view.
 *
 * Displays a two-column layout:
 * - Left Column: Overview, Timeline, Updates, Events, News, Mission, Location, Spacecraft, Agency
 * - Right Column: Video player, Rocket details, Landing details, Agency statistics
 *
 * No tabs are shown on tablet/desktop - all content is visible in a scrollable layout.
 */
@Composable
fun TabletLaunchDetailContent(
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
    onNavigateToFullscreen: (String, String) -> Unit,
    onVideoSelected: (Int) -> Unit,
    onNavigateToSettings: (() -> Unit)? = null,
    onEventClick: ((Int) -> Unit)? = null,
    onAstronautClick: ((Int) -> Unit)? = null,
    openUrl: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Combined Launch Overview Card
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            CombinedLaunchOverviewCard(launch = launch)
            Spacer(Modifier.height(16.dp))

            // 2. Quick Stats Grid
            QuickStatsGrid(launch = launch)
            Spacer(Modifier.height(8.dp))

            SmartBannerAd(
                modifier = Modifier.fillMaxWidth(),
                placementType = AdPlacementType.INTERSTITIAL,
                showRemoveAdsButton = true,
                onRemoveAdsClick = onNavigateToSettings
            )

            Spacer(Modifier.height(8.dp))

            // Timeline Card
            if (launch.timeline.isNotEmpty()) {
                Text(
                    text = "Timeline",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TimelineCard(timeline = launch.timeline)

                // Flight Club link
                launch.flightclubUrl?.let { url ->
                    Spacer(Modifier.height(8.dp))
                    FlightClubCard(flightClubUrl = url)
                }
            }

            // Updates Section
            if (launch.updates.isNotEmpty()) {
                Text(
                    text = "Updates",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                LaunchUpdatesSection(updates = launch.updates)
            }

            // Related Events Section
            if (relatedEvents.isNotEmpty() || isEventsLoading || eventsError != null) {
                Text(
                    text = "Related Events",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                RelatedEventsCard(
                    events = relatedEvents,
                    isLoading = isEventsLoading,
                    error = eventsError,
                    onEventClick = { event -> onEventClick?.invoke(event.id) }
                )
            }

            // Related News Section
            if (relatedNews.isNotEmpty() || isNewsLoading || newsError != null) {
                Text(
                    text = "Related News",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                RelatedNewsCard(
                    articles = relatedNews,
                    isLoading = isNewsLoading,
                    error = newsError
                )
            }

            // Mission Details Card
            launch.mission?.let { mission ->
                Text(
                    text = "Mission Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                MissionDetailsCard(
                    mission = mission,
                    missionPatchUrl = launch.missionPatches.firstOrNull()?.imageUrl
                )
            }

            // Launch Location
            launch.pad?.let { pad ->
                Text(
                    text = "Launch Location",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                LaunchLocationCard(
                    location = pad.location,
                    pad = pad,
                    openUrl = openUrl
                )
                Spacer(Modifier.height(8.dp))
                PadQuickStatsRow(pad)
                Spacer(Modifier.height(16.dp))
            }

            // Spacecraft Details Card
            if (!launch.rocket?.spacecraftStage.isNullOrEmpty()) {
                Text(
                    text = "Spacecraft Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                SpacecraftDetailsCard(
                    spacecraftStages = launch.rocket.spacecraftStage
                )
            }

            // Agency Card
            launch.launchServiceProvider.let { agency ->
                Text(
                    text = "Launch Service Provider",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                AgencyDetailsCard(agency = agency, openUrl = openUrl)
            }
        }

        // Right Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Video Player Card
            if (videoPlayerState.availableVideos.isNotEmpty()) {
                val videoTitle = launch.net?.let { net ->
                    val now = System.now()
                    if (net > now) "Watch Live" else "Watch Replay"
                } ?: "Watch Launch"

                Text(
                    text = videoTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                VideoPlayerCard(
                    videoPlayerState = videoPlayerState,
                    launchName = launch.mission?.name ?: "Space Launch",
                    onSetPlayerVisible = onSetPlayerVisible,
                    onNavigateToFullscreen = onNavigateToFullscreen,
                    onVideoSelected = onVideoSelected
                )
            }

            // Launch Vehicle Details Card
            launch.rocket?.configuration?.let { rocketConfig ->
                Text(
                    text = "Launch Vehicle Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                LaunchVehicleDetailsCard(rocketConfig = rocketConfig, openUrl = openUrl)
                LaunchVehicleDetailedStatistics(rocketConfig = rocketConfig)
            }

            // Landing Details Card
            run {
                val landingStages = launch.rocket?.launcherStage ?: emptyList()
                if (landingStages.any { it.landing != null }) {
                    Text(
                        text = "Landing Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    LandingDetailsCard(launcherStages = landingStages)
                }
            }

            // Agency Statistics
            launch.launchServiceProvider.let { agency ->
                Text(
                    text = "Launch Service Provider Statistics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                AgencyLaunchStatistics(agency = agency)
            }
        }
    }
}

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
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.ui.ads.AdPlacementType
import me.calebjones.spacelaunchnow.ui.ads.SmartBannerAd
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
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
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_launch_fallback_name
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_section_landing_details
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_section_launch_location
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_section_launch_service_provider
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_section_launch_service_provider_statistics
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_section_launch_vehicle_details
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_section_mission_details
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_section_overview
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_section_related_events
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_section_related_news
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_section_spacecraft_details
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_section_timeline
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_section_updates
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_watch_launch
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_watch_live
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_watch_replay

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
                text = stringResource(Res.string.launch_detail_section_overview),
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
                onRemoveAdsClick = onNavigateToSettings,
                refreshKey = launch.id
            )

            Spacer(Modifier.height(8.dp))

            // Timeline Card
            if (launch.timeline.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.launch_detail_section_timeline),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TimelineCard(timeline = launch.timeline, net = launch.net)

                // Flight Club link
                launch.flightclubUrl?.let { url ->
                    val analyticsManager: AnalyticsManager = koinInject()
                    Spacer(Modifier.height(8.dp))
                    FlightClubCard(
                        flightClubUrl = url,
                        onReferralTracked = { referralUrl ->
                            analyticsManager.track(AnalyticsEvent.ThirdPartyReferral(
                                provider = "flightclub",
                                url = referralUrl,
                                contentType = "flight_club",
                                contentId = launch.id.toString()
                            ))
                        }
                    )
                }
            }

            // Updates Section
            if (launch.updates.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.launch_detail_section_updates),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                LaunchUpdatesSection(updates = launch.updates)
            }

            // Related Events Section
            if (relatedEvents.isNotEmpty() || isEventsLoading || eventsError != null) {
                Text(
                    text = stringResource(Res.string.launch_detail_section_related_events),
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
                    text = stringResource(Res.string.launch_detail_section_related_news),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                RelatedNewsCard(
                    articles = relatedNews,
                    isLoading = isNewsLoading,
                    error = newsError,
                    onArticleClick = onArticleClick
                )
            }

            // Mission Details Card
            launch.mission?.let { mission ->
                Text(
                    text = stringResource(Res.string.launch_detail_section_mission_details),
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
                    text = stringResource(Res.string.launch_detail_section_launch_location),
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
            if (!launch.rocketDetail?.spacecraftFlights.isNullOrEmpty()) {
                Text(
                    text = stringResource(Res.string.launch_detail_section_spacecraft_details),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                SpacecraftDetailsCard(
                    spacecraftStages = launch.rocketDetail!!.spacecraftFlights
                )
            }

            // Agency Card
            launch.provider.let { provider ->
                Text(
                    text = stringResource(Res.string.launch_detail_section_launch_service_provider),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                AgencyDetailsCard(provider = provider, providerDetail = launch.providerDetail, openUrl = openUrl)
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
                    if (net > now) stringResource(Res.string.launch_detail_watch_live) else stringResource(Res.string.launch_detail_watch_replay)
                } ?: stringResource(Res.string.launch_detail_watch_launch)

                Text(
                    text = videoTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                VideoPlayerCard(
                    videoPlayerState = videoPlayerState,
                    launchName = launch.mission?.name ?: stringResource(Res.string.launch_detail_launch_fallback_name),
                    onSetPlayerVisible = onSetPlayerVisible,
                    onNavigateToFullscreen = onNavigateToFullscreen,
                    onVideoSelected = onVideoSelected,
                    onExternalVideoOpened = onExternalVideoOpened
                )
            }

            // Launch Vehicle Details Card
            launch.rocket?.let { rocketConfig ->
                Text(
                    text = stringResource(Res.string.launch_detail_section_launch_vehicle_details),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                LaunchVehicleDetailsCard(rocketConfig = rocketConfig, openUrl = openUrl)
                LaunchVehicleDetailedStatistics(rocketConfig = rocketConfig)
            }

            // Landing Details Card
            run {
                val landingStages = launch.rocketDetail?.stages ?: emptyList()
                if (landingStages.any { it.landingAttempt != null }) {
                    Text(
                        text = stringResource(Res.string.launch_detail_section_landing_details),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    LandingDetailsCard(launcherStages = landingStages)
                }
            }

            // Agency Statistics
            launch.providerDetail.let { providerDetail ->
                Text(
                    text = stringResource(Res.string.launch_detail_section_launch_service_provider_statistics),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                AgencyLaunchStatistics(providerDetail = providerDetail)
            }
        }
    }
}

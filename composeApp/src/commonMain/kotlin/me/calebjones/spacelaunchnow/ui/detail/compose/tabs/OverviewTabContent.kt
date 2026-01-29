package me.calebjones.spacelaunchnow.ui.detail.compose.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import me.calebjones.spacelaunchnow.ui.detail.compose.components.FlightClubCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.LaunchUpdatesSection
import me.calebjones.spacelaunchnow.ui.detail.compose.components.QuickStatsGrid
import me.calebjones.spacelaunchnow.ui.detail.compose.components.RelatedEventsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.RelatedNewsCard
import me.calebjones.spacelaunchnow.ui.detail.compose.components.TimelineCard
import me.calebjones.spacelaunchnow.ui.state.VideoPlayerState

/**
 * Overview tab content for launch detail view.
 *
 * Displays:
 * - Quick stats grid
 * - Timeline (if available)
 * - Flight Club link (if available)
 * - Launch updates (if available)
 * - Related events (if available)
 * - Related news articles (if available)
 *
 * Note: Video player is now displayed above the tabs in PhoneLaunchDetailContent.
 *
 * Parent scaffold handles scrolling.
 */
@Composable
fun OverviewTabContent(
    launch: LaunchDetailed,
    videoPlayerState: VideoPlayerState,
    relatedNews: List<Article> = emptyList(),
    isNewsLoading: Boolean = false,
    newsError: String? = null,
    relatedEvents: List<EventEndpointNormal> = emptyList(),
    isEventsLoading: Boolean = false,
    eventsError: String? = null,
    onSetPlayerVisible: (Boolean) -> Unit,
    onNavigateToFullscreen: (String, String) -> Unit,
    onVideoSelected: (Int) -> Unit,
    onNavigateToSettings: (() -> Unit)? = null,
    onEventClick: ((Int) -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Quick Stats Grid
        QuickStatsGrid(launch = launch)
        Spacer(Modifier.height(16.dp))

        // Timeline Card
        if (launch.timeline.isNotEmpty()) {
            Text(
                text = "Timeline",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            TimelineCard(timeline = launch.timeline)

            // Flight Club link
            launch.flightclubUrl?.let { url ->
                Spacer(Modifier.height(8.dp))
                FlightClubCard(flightClubUrl = url)
            }
            Spacer(Modifier.height(16.dp))
        }

        // Launch Updates Section
        if (launch.updates.isNotEmpty()) {
            Text(
                text = "Updates",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            LaunchUpdatesSection(updates = launch.updates)
            Spacer(Modifier.height(16.dp))
        }

        // Related Events Section
        if (relatedEvents.isNotEmpty() || isEventsLoading || eventsError != null) {
            Text(
                text = "Related Events",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            RelatedEventsCard(
                events = relatedEvents,
                isLoading = isEventsLoading,
                error = eventsError,
                onEventClick = { event -> onEventClick?.invoke(event.id) }
            )
            Spacer(Modifier.height(16.dp))
        }

        // Related News Section
        if (relatedNews.isNotEmpty() || isNewsLoading || newsError != null) {
            Text(
                text = "Related News",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            RelatedNewsCard(
                articles = relatedNews,
                isLoading = isNewsLoading,
                error = newsError
            )
            Spacer(Modifier.height(16.dp))
        }

        // Bottom spacing for better scrolling
        Spacer(Modifier.height(100.dp))
    }
}


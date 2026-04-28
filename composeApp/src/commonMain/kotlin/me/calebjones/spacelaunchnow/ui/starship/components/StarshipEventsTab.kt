package me.calebjones.spacelaunchnow.ui.starship.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.SubcomposeAsyncImage
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.navigation.EventDetail
import me.calebjones.spacelaunchnow.ui.compose.StarshipEventsShimmer
import me.calebjones.spacelaunchnow.ui.detail.compose.components.RelatedNewsCard
import me.calebjones.spacelaunchnow.ui.home.components.SectionTitle
import me.calebjones.spacelaunchnow.ui.viewmodel.ViewState
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDateTime

/**
 * Events tab for Starship Dashboard
 *
 * Displays a list of Starship program events, updates, and news
 * with per-section ViewState for independent loading/error states.
 */
@Composable
fun StarshipEventsTab(
    eventsState: ViewState<List<Event>>,
    newsState: ViewState<List<Article>>,
    navController: NavController,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine overall initial loading state (all sections loading with no data)
    val isInitialLoading = eventsState.isInitialLoading &&
            newsState.isInitialLoading

    // Check if we have any content to show
    val hasAnyContent = !eventsState.isEmpty || !newsState.isEmpty

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isInitialLoading -> {
                // Show shimmer loading skeleton
                StarshipEventsShimmer()
            }

            hasAnyContent -> {
                // Content list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // Events Section
                    val events = eventsState.data
                    if (events.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                            ) {
                                SectionTitle(
                                    title = "Events",
                                    hasAction = false
                                )
                                // Staleness indicator for events
                                if (eventsState.isStale) {
                                    StalenessIndicator()
                                }
                            }
                        }

                        items(events) { event ->
                            EventListCard(
                                event = event,
                                onClick = {
                                    navController.navigate(EventDetail(event.id))
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // News Section
                    val news = newsState.data
                    if (news.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
                            ) {
                                SectionTitle(
                                    title = "News & Articles",
                                    hasAction = false
                                )
                                // Staleness indicator for news
                                if (newsState.isStale) {
                                    StalenessIndicator()
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                RelatedNewsCard(
                                    articles = news,
                                    isLoading = newsState.isLoading,
                                    error = null
                                )
                            }
                        }
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            else -> {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Starship events available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Error state (show first error encountered)
        val error = eventsState.error ?: newsState.error
        if (error != null && !hasAnyContent) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Card displaying a single event in a list
 */
@Composable
private fun EventListCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val useUtc = LocalUseUtc.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Event Image
            event.imageUrl?.let { imageUrl ->
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = event.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                )
            }

            // Event Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                event.date?.let { date ->
                    Text(
                        text = formatLaunchDateTime(date, useUtc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = event.type.name ?: "Unknown Type",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}


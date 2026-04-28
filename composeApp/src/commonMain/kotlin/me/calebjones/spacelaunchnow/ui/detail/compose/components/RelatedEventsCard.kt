package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.shimmer
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.CalendarAlt
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.util.DateTimeUtil

/**
 * Card component for displaying related events to a launch.
 *
 * Shows a list of events that are related to the current launch,
 * with options to expand/collapse if there are more than 5 events.
 *
 * @param events List of related events to display
 * @param isLoading Whether events are currently loading
 * @param error Error message if loading failed
 * @param onEventClick Optional callback when an event is clicked
 */
@Composable
fun RelatedEventsCard(
    events: List<Event>,
    isLoading: Boolean,
    error: String?,
    onEventClick: ((Event) -> Unit)? = null
) {
    var showAll by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        when {
            isLoading && events.isEmpty() -> {
                RelatedEventsLoadingState()
            }

            error != null && events.isEmpty() -> {
                RelatedEventsErrorState(error = error)
            }

            events.isNotEmpty() -> {
                val displayedEvents = if (showAll) events else events.take(5)

                displayedEvents.forEach { event ->
                    RelatedEventItem(
                        event = event,
                        onClick = { onEventClick?.invoke(event) }
                    )
                }

                // Show "Load More" button if there are more than 5 events
                if (events.size > 5 && !showAll) {
                    TextButton(
                        onClick = { showAll = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Load More (${events.size - 5} more)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                // Show "Show Less" button if currently showing all
                if (showAll && events.size > 5) {
                    TextButton(
                        onClick = { showAll = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Show Less",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual event item in the related events list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatedEventItem(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val imageUrl = event.imageUrl
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = "Event Image",
                modifier = Modifier
                    .size(96.dp)
                    .clip(shape = RoundedCornerShape(2.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize().shimmer(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.CalendarAlt,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    }
                },
                error = {
                    // Show Font Awesome calendar icon as fallback
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.CalendarAlt,
                            contentDescription = "Event Icon",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = event.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.description?.replace(".\n", " ")?.replace("\n", "") ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        maxLines = 3,
                        minLines = 3,
                        overflow = TextOverflow.Visible,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    event.type.name?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    val useUtc = LocalUseUtc.current
                    event.date?.let { date ->
                        Text(
                            text = DateTimeUtil.formatLaunchDate(date, useUtc),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Loading state for related events section.
 */
@Composable
fun RelatedEventsLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Loading related events...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * Error state for related events section.
 */
@Composable
fun RelatedEventsErrorState(error: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "No related events available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

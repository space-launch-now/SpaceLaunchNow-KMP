package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Instant
import me.calebjones.spacelaunchnow.api.launchlibrary.models.TimelineEvent
import me.calebjones.spacelaunchnow.api.launchlibrary.models.TimelineEventType
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.util.DateTimeUtil
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatTimelineRelativeTime
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TimelineCard(timeline: List<TimelineEvent>, net: Instant? = null) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Timeline vertical
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                val displayItems = if (expanded) timeline else timeline.take(4)
                displayItems.forEachIndexed { idx, event ->
                    val hasPassed = remember(net, event.relativeTime) {
                        net?.let { DateTimeUtil.isTimelineEventPassed(event.relativeTime, it) } ?: false
                    }
                    // Check if the next event has also passed (to color the bottom connector)
                    val nextHasPassed = remember(net, displayItems) {
                        if (idx < displayItems.lastIndex) {
                            net?.let { DateTimeUtil.isTimelineEventPassed(displayItems[idx + 1].relativeTime, it) } ?: false
                        } else false
                    }
                    TimelineEventRow(
                        event = event,
                        isFirst = idx == 0,
                        isLast = idx == displayItems.lastIndex,
                        hasPassed = hasPassed,
                        nextHasPassed = nextHasPassed
                    )
                }
            }

            // Show more button centered at bottom (if there are more than 4 items)
            if (timeline.size > 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = { expanded = !expanded }
                    ) {
                        Text(if (expanded) "Show less" else "Show more")
                    }
                }
            }
        }
    }
}


@Composable
fun TimelineEventRow(
    event: TimelineEvent,
    isFirst: Boolean,
    isLast: Boolean,
    hasPassed: Boolean = false,
    nextHasPassed: Boolean = false
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // Timeline indicator column (dot and connecting lines)
        // Dot is 12dp tall, starts at 4dp from top → spans 4dp to 16dp
        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Top line segment (from top edge to top of dot)
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(4.dp)
                        .align(Alignment.TopCenter)
                        .background(if (hasPassed) primaryColor else outlineColor)
                )
            }
            
            // Bottom line segment (from bottom of dot to bottom edge)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .padding(top = 16.dp) // 4dp padding + 12dp dot
                        .align(Alignment.TopCenter)
                        .background(if (nextHasPassed) primaryColor else outlineColor)
                )
            }
            
            // Timeline dot - filled if passed, hollow outline if future
            if (hasPassed) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(12.dp)
                        .background(primaryColor, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(12.dp)
                        .border(2.dp, primaryColor, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content column with event details
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (!isLast) 12.dp else 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Title abbreviation/name
                    Text(
                        text = event.type?.abbrev ?: "Event",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Description
                    if (!event.type?.description.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.type.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Relative time at the end of the line
                Text(
                    text = formatTimelineRelativeTime(event.relativeTime),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

// region Sample Data

private val sampleTimelineEvents = listOf(
    TimelineEvent(
        type = TimelineEventType(
            id = 1,
            abbrev = "Launch Team On Stations",
            description = "Launch team arrives at stations"
        ),
        relativeTime = "-P1DT14H"
    ),
    TimelineEvent(
        type = TimelineEventType(
            id = 2,
            abbrev = "Countdown Start",
            description = "Countdown clock begins"
        ),
        relativeTime = "-PT4H"
    ),
    TimelineEvent(
        type = TimelineEventType(
            id = 3,
            abbrev = "Prop Load",
            description = "Propellant loading begins"
        ),
        relativeTime = "-PT2H30M"
    ),
    TimelineEvent(
        type = TimelineEventType(
            id = 4,
            abbrev = "T-0",
            description = "Liftoff"
        ),
        relativeTime = "PT0S"
    ),
    TimelineEvent(
        type = TimelineEventType(
            id = 5,
            abbrev = "Max-Q",
            description = "Maximum dynamic pressure"
        ),
        relativeTime = "PT1M12S"
    ),
    TimelineEvent(
        type = TimelineEventType(
            id = 6,
            abbrev = "MECO",
            description = "Main engine cutoff"
        ),
        relativeTime = "PT2M33S"
    )
)

// endregion

// region Previews

// NET set far in the past so all events show as passed
private val allPassedNet = Instant.fromEpochMilliseconds(0L)

// NET set far in the future so all events show as upcoming
private val allFutureNet = Instant.fromEpochMilliseconds(32503680000000L) // year 3000

// NET set to epoch + 3 hours so first 3 events (before T-0) are passed, rest are future
private val partiallyPassedNet = Instant.fromEpochMilliseconds(
    // Current time minus 1 hour — so events at T-4h and T-2h30m are passed, T-0 onward are future
    // Using a fixed value for stable previews: epoch + enough offset that negative events pass
    10800000L // 3 hours after epoch
)

@Preview
@Composable
private fun TimelineCardPreview() {
    SpaceLaunchNowPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TimelineCard(
                timeline = sampleTimelineEvents.take(4)
            )
        }
    }
}

@Preview
@Composable
private fun TimelineCardDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            TimelineCard(
                timeline = sampleTimelineEvents.take(4)
            )
        }
    }
}

@Preview
@Composable
private fun TimelineCardAllPassedPreview() {
    SpaceLaunchNowPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TimelineCard(
                timeline = sampleTimelineEvents,
                net = allPassedNet
            )
        }
    }
}

@Preview
@Composable
private fun TimelineCardAllPassedDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            TimelineCard(
                timeline = sampleTimelineEvents,
                net = allPassedNet
            )
        }
    }
}

@Preview
@Composable
private fun TimelineCardAllFuturePreview() {
    SpaceLaunchNowPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TimelineCard(
                timeline = sampleTimelineEvents,
                net = allFutureNet
            )
        }
    }
}

@Preview
@Composable
private fun TimelineCardAllFutureDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            TimelineCard(
                timeline = sampleTimelineEvents,
                net = allFutureNet
            )
        }
    }
}

@Preview
@Composable
private fun TimelineCardPartiallyPassedPreview() {
    SpaceLaunchNowPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TimelineCard(
                timeline = sampleTimelineEvents,
                net = partiallyPassedNet
            )
        }
    }
}

@Preview
@Composable
private fun TimelineCardPartiallyPassedDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            TimelineCard(
                timeline = sampleTimelineEvents,
                net = partiallyPassedNet
            )
        }
    }
}

// endregion
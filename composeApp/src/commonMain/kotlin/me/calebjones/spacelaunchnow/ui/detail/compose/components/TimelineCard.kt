package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.background
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
import me.calebjones.spacelaunchnow.api.launchlibrary.models.TimelineEvent
import me.calebjones.spacelaunchnow.api.launchlibrary.models.TimelineEventType
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatTimelineRelativeTime
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TimelineCard(timeline: List<TimelineEvent>) {
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
                    TimelineEventRow(
                        event = event,
                        isFirst = idx == 0,
                        isLast = idx == displayItems.lastIndex
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
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // Timeline indicator column (dot and connecting lines)
        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Top line segment (from top to dot center)
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(10.dp)
                        .align(Alignment.TopCenter)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
            
            // Bottom line segment (from dot center to bottom)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .padding(top = 10.dp)
                        .align(Alignment.TopCenter)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
            
            // Timeline dot - positioned to align with title center
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(12.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
            )
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
            abbrev = "T-0",
            description = "Liftoff"
        ),
        relativeTime = "00:00:00"
    ),
    TimelineEvent(
        type = TimelineEventType(
            id = 2,
            abbrev = "Max-Q",
            description = "Maximum dynamic pressure"
        ),
        relativeTime = "00:01:12"
    ),
    TimelineEvent(
        type = TimelineEventType(
            id = 3,
            abbrev = "MECO",
            description = "Main engine cutoff"
        ),
        relativeTime = "00:02:33"
    ),
    TimelineEvent(
        type = TimelineEventType(
            id = 4,
            abbrev = "Stage Sep",
            description = "First stage separation"
        ),
        relativeTime = "00:02:36"
    ),
    TimelineEvent(
        type = TimelineEventType(
            id = 5,
            abbrev = "SES-1",
            description = "Second engine start 1"
        ),
        relativeTime = "00:02:44"
    ),
    TimelineEvent(
        type = TimelineEventType(
            id = 6,
            abbrev = "Fairing Sep",
            description = "Fairing separation"
        ),
        relativeTime = "00:03:15"
    )
)

// endregion

// region Previews

@Preview
@Composable
private fun TimelineCardPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TimelineCard(
                timeline = sampleTimelineEvents.take(4)
            )
        }
    }
}

@Preview
@Composable
private fun TimelineCardExpandedPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TimelineCard(
                timeline = sampleTimelineEvents
            )
        }
    }
}

@Preview
@Composable
private fun TimelineCardSingleItemPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TimelineCard(
                timeline = sampleTimelineEvents.take(1)
            )
        }
    }
}

@Preview
@Composable
private fun TimelineEventRowPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            TimelineEventRow(
                event = sampleTimelineEvents.first(),
                isFirst = true,
                isLast = true
            )
        }
    }
}
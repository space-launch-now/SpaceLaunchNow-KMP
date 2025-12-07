package me.calebjones.spacelaunchnow.ui.starship.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.ui.components.StatusChip
import me.calebjones.spacelaunchnow.ui.viewmodel.ViewState
import me.calebjones.spacelaunchnow.util.DateTimeUtil.formatLaunchDate
import me.calebjones.spacelaunchnow.util.LaunchFormatUtil
import me.calebjones.spacelaunchnow.util.StatusColorUtil.getLaunchStatusColor

/**
 * Timeline view displaying the complete Starship program history
 * Shows next launch (highlighted) at top, then previous launches in reverse chronological order
 */
@Composable
fun StarshipHistoryTimeline(
    nextLaunch: LaunchNormal?,
    historyLaunchesState: ViewState<List<LaunchNormal>>,
    onLaunchClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Section header
        Text(
            text = "Program Timeline",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Show next launch card (highlighted) if available
        if (nextLaunch != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NextLaunchTimelineCard(
                    launch = nextLaunch,
                    onClick = { onLaunchClick(nextLaunch.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Loading state
        if (historyLaunchesState.isLoading && historyLaunchesState.data.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Error state
        if (historyLaunchesState.error != null && historyLaunchesState.data.isEmpty()) {
            Text(
                text = "Error loading history: ${historyLaunchesState.error}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        // History launches grouped by year
        if (historyLaunchesState.data.isNotEmpty()) {
            val launchesByYear = historyLaunchesState.data.groupBy { launch ->
                launch.net?.toLocalDateTime(TimeZone.UTC)?.year ?: 0
            }

            val yearKeys = launchesByYear.keys.toList()
            launchesByYear.entries.forEachIndexed { yearIndex, (year, launches) ->
                val isFirstYear = yearIndex == 0
                val isLastYear = yearIndex == yearKeys.size - 1

                // Year divider with timeline connector
                YearDivider(
                    year = year,
                    isFirst = isFirstYear,
                    isLast = isLastYear && launches.isEmpty()
                )

                // Timeline cards for this year
                launches.forEachIndexed { index, launch ->
                    val isLastInYear = index == launches.size - 1
                    val isLastOverall = isLastInYear && isLastYear

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Timeline connector outside card - fills full height of row
                        TimelineConnector(
                            showTopLine = true,
                            showBottomLine = !isLastOverall
                        )

                        HistoryTimelineCard(
                            launch = launch,
                            onClick = { onLaunchClick(launch.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Next launch card with highlighted "UPCOMING" badge and accent border
 */
@Composable
private fun NextLaunchTimelineCard(
    launch: LaunchNormal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "UPCOMING" badge
            StatusChip(
                text = "UPCOMING",
                color = MaterialTheme.colorScheme.primary
            )

            // Launch image
            if (launch.image?.imageUrl != null) {
                AsyncImage(
                    model = launch.image.imageUrl,
                    contentDescription = "Launch image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Launch name
            launch.name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Launch date
            launch.net?.let { net ->
                Text(
                    text = formatLaunchDate(net),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status
            launch.status?.let { status ->
                StatusChip(
                    text = status.name,
                    color = getLaunchStatusColor(status.id)
                )
            }

            // Mission description (if available)
            if (!launch.mission?.description.isNullOrBlank()) {
                Text(
                    text = launch.mission.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


/**
 * History launch card with image, status, and failure reason
 */
@Composable
private fun HistoryTimelineCard(
    launch: LaunchNormal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = remember(launch) {
        LaunchFormatUtil.formatLaunchTitle(launch)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Launch image
            if (launch.image?.imageUrl != null) {
                AsyncImage(
                    model = launch.image.imageUrl,
                    contentDescription = "Launch image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Launch name
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Launch date
            launch.net?.let { net ->
                Text(
                    text = formatLaunchDate(net),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status chip
            launch.status?.let { status ->
                StatusChip(
                    text = status.name,
                    color = getLaunchStatusColor(status.id)
                )
            }

            // Failure reason (if applicable)
            if (!launch.failreason.isNullOrBlank()) {
                Text(
                    text = "⚠ ${launch.failreason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Mission description (if available)
            if (!launch.mission?.description.isNullOrBlank()) {
                Text(
                    text = launch.mission.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Year divider label with integrated timeline connector
 */
@Composable
private fun YearDivider(
    year: Int,
    isFirst: Boolean,
    isLast: Boolean
) {
    // Dot is centered vertically in the row
    val dotSize = 16.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timeline connector column - same width as card connectors for alignment
        Box(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            // Top line - from top to center (above dot)
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .width(3.dp)
                        .fillMaxHeight(0.5f)
                        .background(
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(1.5.dp)
                        )
                )
            }

            // Bottom line - from center to bottom (below dot)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .width(3.dp)
                        .fillMaxHeight(0.5f)
                        .background(
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(1.5.dp)
                        )
                )
            }

            // Year dot - larger and primary colored, on top of lines
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        }

        // Year text
        Text(
            text = year.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Divider line
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(1.dp)
                )
        )
    }
}

/**
 * Timeline connector with vertical line and dot - positioned outside cards
 * Dot is at the top, line extends to connect to adjacent cards
 */
@Composable
private fun TimelineConnector(
    showTopLine: Boolean,
    showBottomLine: Boolean
) {
    // Dot position: 12dp from top, 14dp diameter, so center is at 19dp, bottom at 26dp
    val dotTopPadding = 12.dp
    val dotSize = 14.dp

    Box(
        modifier = Modifier
            .width(32.dp)
            .fillMaxHeight(),
        contentAlignment = Alignment.TopCenter
    ) {
        // Top line - from top of container to top of dot
        if (showTopLine) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(3.dp)
                    .height(dotTopPadding)
                    .background(
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(1.5.dp)
                    )
            )
        }

        // Bottom line - from bottom of dot to bottom of container
        if (showBottomLine) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = dotTopPadding + dotSize)
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(1.5.dp)
                    )
            )
        }

        // Dot - positioned at top, aligned with card content
        Box(
            modifier = Modifier
                .padding(top = dotTopPadding)
                .size(dotSize)
                .background(
                    color = MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
        )
    }
}

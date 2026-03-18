package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Quick Stats section showing launch count summaries in a grid.
 *
 * @param next24HoursCount Number of launches in the next 24 hours
 * @param nextWeekCount Number of launches in the next 7 days
 * @param nextMonthCount Number of launches in the next 30 days
 * @param historyCount Number of launches on this day in history
 * @param isTabletLayout When true, displays all 4 stats in a single row; otherwise 2x2 grid
 */
@Composable
fun QuickStatsSection(
    next24HoursCount: Int,
    nextWeekCount: Int,
    nextMonthCount: Int,
    historyCount: Int,
    isTabletLayout: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Quick Stats",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (isTabletLayout) {
            // Tablet: single row of 4
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatItem(
                    icon = Icons.Default.Today,
                    label = "Next 24 Hours",
                    value = if (next24HoursCount > 0) next24HoursCount.toString() else "-",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    icon = Icons.Default.DateRange,
                    label = "Next 7 Days",
                    value = if (nextWeekCount > 0) nextWeekCount.toString() else "-",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    icon = Icons.Default.Schedule,
                    label = "Next 30 Days",
                    value = if (nextMonthCount > 0) nextMonthCount.toString() else "-",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    icon = Icons.Default.History,
                    label = "This Day in History",
                    value = if (historyCount > 0) historyCount.toString() else "-",
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // Phone: 2x2 grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatItem(
                    icon = Icons.Default.Today,
                    label = "Next 24 Hours",
                    value = if (next24HoursCount > 0) next24HoursCount.toString() else "-",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    icon = Icons.Default.DateRange,
                    label = "Next 7 Days",
                    value = if (nextWeekCount > 0) nextWeekCount.toString() else "-",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatItem(
                    icon = Icons.Default.Schedule,
                    label = "Next 30 Days",
                    value = if (nextMonthCount > 0) nextMonthCount.toString() else "-",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    icon = Icons.Default.History,
                    label = "This Day in History",
                    value = if (historyCount > 0) historyCount.toString() else "-",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual stat item card showing an icon, numeric value, and label.
 */
@Composable
fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 96.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

// region Previews

@Preview
@Composable
private fun QuickStatsSectionPhonePreview() {
    SpaceLaunchNowPreviewTheme {
        QuickStatsSection(
            next24HoursCount = 2,
            nextWeekCount = 5,
            nextMonthCount = 12,
            historyCount = 3,
            isTabletLayout = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun QuickStatsSectionPhoneDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        QuickStatsSection(
            next24HoursCount = 2,
            nextWeekCount = 5,
            nextMonthCount = 12,
            historyCount = 3,
            isTabletLayout = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun QuickStatsSectionTabletPreview() {
    SpaceLaunchNowPreviewTheme {
        QuickStatsSection(
            next24HoursCount = 2,
            nextWeekCount = 5,
            nextMonthCount = 12,
            historyCount = 3,
            isTabletLayout = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun QuickStatsSectionEmptyPreview() {
    SpaceLaunchNowPreviewTheme {
        QuickStatsSection(
            next24HoursCount = 0,
            nextWeekCount = 0,
            nextMonthCount = 0,
            historyCount = 0,
            isTabletLayout = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun StatItemPreview() {
    SpaceLaunchNowPreviewTheme {
        StatItem(
            icon = Icons.Default.Today,
            label = "Next 24 Hours",
            value = "3",
            modifier = Modifier.width(200.dp)
        )
    }
}

@Preview
@Composable
private fun StatItemDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        StatItem(
            icon = Icons.Default.Today,
            label = "Next 24 Hours",
            value = "3",
            modifier = Modifier.width(200.dp)
        )
    }
}

// endregion

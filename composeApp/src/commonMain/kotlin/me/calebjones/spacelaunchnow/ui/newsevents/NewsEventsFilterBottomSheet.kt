package me.calebjones.spacelaunchnow.ui.newsevents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventType
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.viewmodel.NewsEventsTab
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Bottom sheet for filtering news articles by source or events by type.
 * Content changes based on which tab is selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsEventsFilterBottomSheet(
    isOpen: Boolean,
    selectedTab: NewsEventsTab,
    availableNewsSites: List<String>,
    selectedNewsSites: List<String>,
    availableEventTypes: List<EventType>,
    selectedEventTypeIds: List<Int>,
    onNewsSiteToggled: (String) -> Unit,
    onEventTypeToggled: (Int) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    if (isOpen) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedTab == NewsEventsTab.NEWS) "Filter by Source" else "Filter by Event Type",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        NewsEventsTab.NEWS -> NewsSiteFilterContent(
                            availableNewsSites = availableNewsSites,
                            selectedNewsSites = selectedNewsSites,
                            onNewsSiteToggled = onNewsSiteToggled
                        )
                        NewsEventsTab.EVENTS -> EventTypeFilterContent(
                            availableEventTypes = availableEventTypes,
                            selectedEventTypeIds = selectedEventTypeIds,
                            onEventTypeToggled = onEventTypeToggled
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Clear filters button
                    val hasActiveFilters = when (selectedTab) {
                        NewsEventsTab.NEWS -> selectedNewsSites.isNotEmpty()
                        NewsEventsTab.EVENTS -> selectedEventTypeIds.isNotEmpty()
                    }
                    if (hasActiveFilters) {
                        TextButton(
                            onClick = {
                                onClearFilters()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear All Filters")
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NewsSiteFilterContent(
    availableNewsSites: List<String>,
    selectedNewsSites: List<String>,
    onNewsSiteToggled: (String) -> Unit
) {
    Text(
        text = "Select sources",
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        availableNewsSites.forEach { site ->
            val isSelected = site in selectedNewsSites
            FilterChip(
                selected = isSelected,
                onClick = { onNewsSiteToggled(site) },
                label = { Text(site) },
                trailingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove filter",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EventTypeFilterContent(
    availableEventTypes: List<EventType>,
    selectedEventTypeIds: List<Int>,
    onEventTypeToggled: (Int) -> Unit
) {
    Text(
        text = "Select event types",
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    if (availableEventTypes.isEmpty()) {
        Text(
            text = "Loading event types...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableEventTypes.forEach { type ->
                val isSelected = type.id in selectedEventTypeIds
                FilterChip(
                    selected = isSelected,
                    onClick = { onEventTypeToggled(type.id) },
                    label = { Text(type.name ?: "Unknown") },
                    trailingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove filter",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

// ========== Previews ==========

@Preview
@Composable
private fun NewsFilterContentPreview() {
    SpaceLaunchNowPreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            NewsSiteFilterContent(
                availableNewsSites = listOf("SpaceNews", "NASASpaceflight", "Teslarati", "Spaceflight Now"),
                selectedNewsSites = listOf("SpaceNews", "Teslarati"),
                onNewsSiteToggled = {}
            )
        }
    }
}

@Preview
@Composable
private fun NewsFilterContentDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Column(modifier = Modifier.padding(16.dp)) {
            NewsSiteFilterContent(
                availableNewsSites = listOf("SpaceNews", "NASASpaceflight", "Teslarati", "Spaceflight Now"),
                selectedNewsSites = emptyList(),
                onNewsSiteToggled = {}
            )
        }
    }
}

@Preview
@Composable
private fun EventTypeFilterContentPreview() {
    SpaceLaunchNowPreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            EventTypeFilterContent(
                availableEventTypes = listOf(
                    EventType(id = 1, name = "Spacewalk"),
                    EventType(id = 2, name = "Docking"),
                    EventType(id = 3, name = "Undocking"),
                    EventType(id = 4, name = "Landing"),
                    EventType(id = 5, name = "Press Event")
                ),
                selectedEventTypeIds = listOf(1, 3),
                onEventTypeToggled = {}
            )
        }
    }
}

@Preview
@Composable
private fun EventTypeFilterContentDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Column(modifier = Modifier.padding(16.dp)) {
            EventTypeFilterContent(
                availableEventTypes = listOf(
                    EventType(id = 1, name = "Spacewalk"),
                    EventType(id = 2, name = "Docking"),
                    EventType(id = 3, name = "Landing")
                ),
                selectedEventTypeIds = listOf(2),
                onEventTypeToggled = {}
            )
        }
    }
}

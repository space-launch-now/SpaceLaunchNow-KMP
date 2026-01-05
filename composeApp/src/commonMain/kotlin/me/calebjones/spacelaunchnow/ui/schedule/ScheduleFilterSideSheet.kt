package me.calebjones.spacelaunchnow.ui.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import me.calebjones.spacelaunchnow.data.model.FilterOption

/**
 * Side sheet for tablet/desktop layout
 * Slides in from the right with filter options and Apply/Close buttons
 */
@Composable
fun ScheduleFilterSideSheet(
    isOpen: Boolean,
    currentFilterState: ScheduleFilterState,
    agencies: List<FilterOption>,
    programs: List<FilterOption>,
    rockets: List<FilterOption>,
    locations: List<FilterOption>,
    statuses: List<FilterOption>,
    orbits: List<FilterOption>,
    missionTypes: List<FilterOption>,
    launcherConfigFamilies: List<FilterOption>,
    isLoading: Boolean,
    onApplyFilters: (ScheduleFilterState) -> Unit,
    onReloadOptions: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingFilterState by remember(currentFilterState) { mutableStateOf(currentFilterState) }

    // Handler for clearing all filters and auto-applying
    val onClearAll: () -> Unit = {
        val emptyState = ScheduleFilterState()
        pendingFilterState = emptyState
        onApplyFilters(emptyState)
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it }),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(400.dp),
            shadowElevation = 8.dp,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Launches",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }

                HorizontalDivider()

                // Filter content (scrollable)
                ScheduleFilterContent(
                    currentFilterState = currentFilterState,
                    agencies = agencies,
                    programs = programs,
                    rockets = rockets,
                    locations = locations,
                    statuses = statuses,
                    orbits = orbits,
                    missionTypes = missionTypes,
                    launcherConfigFamilies = launcherConfigFamilies,
                    isLoading = isLoading,
                    onFilterStateChange = { newState ->
                        pendingFilterState = newState
                    },
                    onReloadOptions = onReloadOptions,
                    onClearAll = onClearAll,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // Apply button (sticky at bottom)
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onApplyFilters(pendingFilterState)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (pendingFilterState.hasActiveFilters())
                                "Apply (${pendingFilterState.activeFilterCount()})"
                            else
                                "Apply"
                        )
                    }
                }
            }
        }
    }
}

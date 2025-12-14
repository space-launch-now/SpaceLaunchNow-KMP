package me.calebjones.spacelaunchnow.ui.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
 * Bottom sheet for phone layout
 * Displays filter options with Apply and Close buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleFilterBottomSheet(
    isOpen: Boolean,
    currentFilterState: ScheduleFilterState,
    agencies: List<FilterOption>,
    programs: List<FilterOption>,
    rockets: List<FilterOption>,
    locations: List<FilterOption>,
    statuses: List<FilterOption>,
    isLoading: Boolean,
    onApplyFilters: (ScheduleFilterState) -> Unit,
    onReloadOptions: () -> Unit,
    onDismiss: () -> Unit
) {
    var pendingFilterState by remember(currentFilterState) { mutableStateOf(currentFilterState) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

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
                    isLoading = isLoading,
                    onFilterStateChange = { newState ->
                        pendingFilterState = newState
                    },
                    onReloadOptions = onReloadOptions,
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
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onApplyFilters(pendingFilterState)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (pendingFilterState.hasActiveFilters())
                                "Apply ${pendingFilterState.activeFilterCount()} Filters"
                            else
                                "Apply"
                        )
                    }
                }
            }
        }
    }
}

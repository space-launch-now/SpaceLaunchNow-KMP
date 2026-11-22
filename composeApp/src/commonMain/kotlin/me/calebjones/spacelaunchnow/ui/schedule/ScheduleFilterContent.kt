package me.calebjones.spacelaunchnow.ui.schedule

import me.calebjones.spacelaunchnow.ui.ads.AdCoveringAlertDialog
import me.calebjones.spacelaunchnow.ui.ads.AdCoveringDropdownMenu
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.data.model.FilterOption
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.common_agencies
import spacelaunchnow_kmp.composeapp.generated.resources.common_cancel
import spacelaunchnow_kmp.composeapp.generated.resources.common_clear_all
import spacelaunchnow_kmp.composeapp.generated.resources.common_collapse
import spacelaunchnow_kmp.composeapp.generated.resources.common_expand
import spacelaunchnow_kmp.composeapp.generated.resources.common_locations
import spacelaunchnow_kmp.composeapp.generated.resources.common_no_results_found
import spacelaunchnow_kmp.composeapp.generated.resources.common_programs
import spacelaunchnow_kmp.composeapp.generated.resources.common_reload
import spacelaunchnow_kmp.composeapp.generated.resources.common_reload_filter_options
import spacelaunchnow_kmp.composeapp.generated.resources.common_reload_filter_options_title
import spacelaunchnow_kmp.composeapp.generated.resources.common_remove_filter
import spacelaunchnow_kmp.composeapp.generated.resources.common_rockets
import spacelaunchnow_kmp.composeapp.generated.resources.common_select_options
import spacelaunchnow_kmp.composeapp.generated.resources.common_selected
import spacelaunchnow_kmp.composeapp.generated.resources.common_selected_count
import spacelaunchnow_kmp.composeapp.generated.resources.schedule_filter_active_count
import spacelaunchnow_kmp.composeapp.generated.resources.schedule_filter_crewed_only
import spacelaunchnow_kmp.composeapp.generated.resources.schedule_filter_launch_type_heading
import spacelaunchnow_kmp.composeapp.generated.resources.schedule_filter_launcher_families
import spacelaunchnow_kmp.composeapp.generated.resources.schedule_filter_mission_types
import spacelaunchnow_kmp.composeapp.generated.resources.schedule_filter_orbits
import spacelaunchnow_kmp.composeapp.generated.resources.schedule_filter_reload_message
import spacelaunchnow_kmp.composeapp.generated.resources.schedule_filter_rocket_limit_note
import spacelaunchnow_kmp.composeapp.generated.resources.schedule_filter_scope_note
import spacelaunchnow_kmp.composeapp.generated.resources.schedule_filter_statuses
import spacelaunchnow_kmp.composeapp.generated.resources.schedule_filter_work_in_progress

/**
 * Filter content composable - displays filter options with selectable chips
 * Used in both bottom sheet (phone) and side sheet (tablet/desktop)
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScheduleFilterContent(
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
    onFilterStateChange: (ScheduleFilterState) -> Unit,
    onReloadOptions: (() -> Unit)? = null,
    onClearAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var filterState by remember(currentFilterState) { mutableStateOf(currentFilterState) }
    var showReloadConfirmDialog by remember { mutableStateOf(false) }

    // Update parent when internal state changes
    LaunchedEffect(filterState) {
        onFilterStateChange(filterState)
    }

    // Reload confirmation dialog
    if (showReloadConfirmDialog) {
        AdCoveringAlertDialog(
            onDismissRequest = { showReloadConfirmDialog = false },
            title = { Text(stringResource(Res.string.common_reload_filter_options_title)) },
            text = {
                Text(stringResource(Res.string.schedule_filter_reload_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReloadConfirmDialog = false
                        onReloadOptions?.invoke()
                    }
                ) {
                    Text(stringResource(Res.string.common_reload))
                }
            },
            dismissButton = {
                TextButton(onClick = { showReloadConfirmDialog = false }) {
                    Text(stringResource(Res.string.common_cancel))
                }
            }
        )
    }

    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator()
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active filters count
            if (filterState.hasActiveFilters()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.schedule_filter_active_count, filterState.activeFilterCount()),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            OutlinedButton(
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                onClick = {
                                    // Use the onClearAll handler if provided, otherwise just update local state
                                    if (onClearAll != null) {
                                        onClearAll()
                                    } else {
                                        filterState = ScheduleFilterState()
                                    }
                                }
                            ) {
                                Text(
                                    text = stringResource(Res.string.common_clear_all),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
            }

            // Info notes
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Scope note
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ℹ️",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(Res.string.schedule_filter_scope_note),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Work In Progress indicator
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.schedule_filter_work_in_progress),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Boolean filters section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.schedule_filter_launch_type_heading),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Is Crewed toggle
                    BooleanFilterRow(
                        label = stringResource(Res.string.schedule_filter_crewed_only),
                        value = filterState.isCrewed,
                        onValueChange = { newValue ->
                            filterState = filterState.copy(isCrewed = newValue)
                        }
                    )
                }
            }

            // Agencies filter
            if (agencies.isNotEmpty()) {
                item {
                    FilterSection(
                        title = stringResource(Res.string.common_agencies),
                        options = agencies,
                        selectedIds = filterState.selectedAgencyIds.toList(),
                        onSelectionChange = { newSelection ->
                            filterState = filterState.copy(selectedAgencyIds = newSelection.toSet())
                        }
                    )
                }
            }

            // Programs filter
            if (programs.isNotEmpty()) {
                item {
                    FilterSection(
                        title = stringResource(Res.string.common_programs),
                        options = programs,
                        selectedIds = filterState.selectedProgramIds.toList(),
                        onSelectionChange = { newSelection ->
                            filterState =
                                filterState.copy(selectedProgramIds = newSelection.toSet())
                        }
                    )
                }
            }

            // Rockets filter
            if (rockets.isNotEmpty()) {
                item {
                    FilterSection(
                        title = stringResource(Res.string.common_rockets),
                        options = rockets,
                        selectedIds = filterState.selectedRocketIds.toList(),
                        onSelectionChange = { newSelection ->
                            filterState = filterState.copy(selectedRocketIds = newSelection.toSet())
                        },
                        note = if (filterState.selectedRocketIds.size > 1)
                            stringResource(Res.string.schedule_filter_rocket_limit_note)
                        else null
                    )
                }
            }

            // Locations filter
            if (locations.isNotEmpty()) {
                item {
                    FilterSection(
                        title = stringResource(Res.string.common_locations),
                        options = locations,
                        selectedIds = filterState.selectedLocationIds.toList(),
                        onSelectionChange = { newSelection ->
                            filterState =
                                filterState.copy(selectedLocationIds = newSelection.toSet())
                        }
                    )
                }
            }

            // Statuses filter
            if (statuses.isNotEmpty()) {
                item {
                    FilterSection(
                        title = stringResource(Res.string.schedule_filter_statuses),
                        options = statuses,
                        selectedIds = filterState.selectedStatusIds.toList(),
                        onSelectionChange = { newSelection ->
                            filterState =
                                filterState.copy(selectedStatusIds = newSelection.toSet())
                        }
                    )
                }
            }

            // Orbits filter
            if (orbits.isNotEmpty()) {
                item {
                    FilterSection(
                        title = stringResource(Res.string.schedule_filter_orbits),
                        options = orbits,
                        selectedIds = filterState.selectedOrbitIds.toList(),
                        onSelectionChange = { newSelection ->
                            filterState =
                                filterState.copy(selectedOrbitIds = newSelection.toSet())
                        }
                    )
                }
            }

            // Mission Types filter
            if (missionTypes.isNotEmpty()) {
                item {
                    FilterSection(
                        title = stringResource(Res.string.schedule_filter_mission_types),
                        options = missionTypes,
                        selectedIds = filterState.selectedMissionTypeIds.toList(),
                        onSelectionChange = { newSelection ->
                            filterState =
                                filterState.copy(selectedMissionTypeIds = newSelection.toSet())
                        }
                    )
                }
            }

            // Launcher Config Families filter
            if (launcherConfigFamilies.isNotEmpty()) {
                item {
                    FilterSection(
                        title = stringResource(Res.string.schedule_filter_launcher_families),
                        options = launcherConfigFamilies,
                        selectedIds = filterState.selectedLauncherConfigFamilyIds.toList(),
                        onSelectionChange = { newSelection ->
                            filterState =
                                filterState.copy(selectedLauncherConfigFamilyIds = newSelection.toSet())
                        }
                    )
                }
            }

            // Reload button at bottom
            if (onReloadOptions != null) {
                item {
                    TextButton(
                        onClick = { showReloadConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.common_reload_filter_options))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    options: List<FilterOption>,
    selectedIds: List<Int>,
    onSelectionChange: (List<Int>) -> Unit,
    note: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var debouncedSearchQuery by remember { mutableStateOf("") }

    // Debounce search query - only update after 300ms of no typing
    LaunchedEffect(searchQuery) {
        kotlinx.coroutines.delay(300)
        debouncedSearchQuery = searchQuery
        // Auto-expand when user stops typing and has search text
        if (searchQuery.isNotBlank()) {
            expanded = true
        }
    }

    val filteredOptions = remember(debouncedSearchQuery, options) {
        if (debouncedSearchQuery.isBlank()) {
            options
        } else {
            options.filter { it.displayName.contains(debouncedSearchQuery, ignoreCase = true) }
        }
    }

    val selectedOptions = remember(selectedIds, options) {
        options.filter { selectedIds.contains(it.id) }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (note != null) {
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Selected items display
        if (selectedOptions.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedOptions.forEach { option ->
                    FilterChip(
                        selected = true,
                        onClick = {
                            onSelectionChange(selectedIds - option.id)
                        },
                        label = {
                            Text(
                                text = option.displayName,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.common_remove_filter),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        // Dropdown field
        Box {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        if (selectedIds.isEmpty()) stringResource(Res.string.common_select_options)
                        else stringResource(Res.string.common_selected_count, selectedIds.size)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = if (expanded) stringResource(Res.string.common_collapse) else stringResource(Res.string.common_expand),
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                },
                singleLine = true
            )

            AdCoveringDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    searchQuery = ""
                    debouncedSearchQuery = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (filteredOptions.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.common_no_results_found)) },
                        onClick = { },
                        enabled = false
                    )
                } else {
                    filteredOptions.forEach { option ->
                        val isSelected = selectedIds.contains(option.id)
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option.displayName,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = stringResource(Res.string.common_selected),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                val newSelection = if (isSelected) {
                                    selectedIds - option.id
                                } else {
                                    selectedIds + option.id
                                }
                                onSelectionChange(newSelection)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Boolean filter row with Switch
 * null = disabled (no filter sent to API), true = enabled (filter sent to API)
 */
@Composable
private fun BooleanFilterRow(
    label: String,
    value: Boolean?,
    onValueChange: (Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = value == true,
            onCheckedChange = { enabled ->
                onValueChange(if (enabled) true else null)
            }
        )
    }
}

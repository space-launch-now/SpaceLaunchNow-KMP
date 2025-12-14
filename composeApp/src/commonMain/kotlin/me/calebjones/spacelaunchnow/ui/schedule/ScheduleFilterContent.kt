package me.calebjones.spacelaunchnow.ui.schedule

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
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

/**
 * Filter content composable - displays filter options with selectable chips
 * Used in both bottom sheet (phone) and side sheet (tablet/desktop)
 */
@Composable
fun ScheduleFilterContent(
    currentFilterState: ScheduleFilterState,
    agencies: List<FilterOption>,
    programs: List<FilterOption>,
    rockets: List<FilterOption>,
    locations: List<FilterOption>,
    statuses: List<FilterOption>,
    isLoading: Boolean,
    onFilterStateChange: (ScheduleFilterState) -> Unit,
    onReloadOptions: (() -> Unit)? = null,
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
        AlertDialog(
            onDismissRequest = { showReloadConfirmDialog = false },
            title = { Text("Reload Filter Options?") },
            text = { 
                Text("This will re-download all filter options from the server. This may take a few moments and will refresh the list of agencies, programs, rockets, locations, and statuses.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReloadConfirmDialog = false
                        onReloadOptions?.invoke()
                    }
                ) {
                    Text("Reload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReloadConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
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
                                text = "${filterState.activeFilterCount()} active filter(s)",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            OutlinedButton(
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                onClick = {
                                    filterState = ScheduleFilterState()
                                }
                            ) {
                                Text(
                                    text = "Clear All",
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
                                text = "These filters only affect the Schedule screen. They won't change what appears on the Home page or in notifications.",
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
                                text = "🚧 Work In Progress",
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
                        text = "Launch Type Filters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Is Crewed toggle
                    BooleanFilterRow(
                        label = "Crewed Launches Only",
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
                        title = "Agencies",
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
                        title = "Programs",
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
                        title = "Rockets",
                        options = rockets,
                        selectedIds = filterState.selectedRocketIds.toList(),
                        onSelectionChange = { newSelection ->
                            filterState = filterState.copy(selectedRocketIds = newSelection.toSet())
                        },
                        note = if (filterState.selectedRocketIds.size > 1)
                            "Note: API supports only 1 rocket filter. Only the first selection will be applied."
                        else null
                    )
                }
            }

            // Locations filter
            if (locations.isNotEmpty()) {
                item {
                    FilterSection(
                        title = "Locations",
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
                        title = "Statuses",
                        options = statuses,
                        selectedIds = filterState.selectedStatusIds.toList(),
                        onSelectionChange = { newSelection ->
                            filterState =
                                filterState.copy(selectedStatusIds = newSelection.toSet())
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
                        Text("Reload Filter Options")
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
                                contentDescription = "Remove filter",
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
                        if (selectedIds.isEmpty()) "Select options..."
                        else "${selectedIds.size} selected"
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                },
                singleLine = true
            )

            DropdownMenu(
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
                        text = { Text("No results found") },
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
                                            contentDescription = "Selected",
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

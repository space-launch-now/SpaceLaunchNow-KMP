package me.calebjones.spacelaunchnow.ui.rockets.compose

import me.calebjones.spacelaunchnow.ui.ads.AdCoveringDropdownMenu
import me.calebjones.spacelaunchnow.ui.ads.AdCoveringExposedDropdownMenu
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import kotlinx.coroutines.delay
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.ui.viewmodel.RocketSortOrder
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.common_apply
import spacelaunchnow_kmp.composeapp.generated.resources.common_clear
import spacelaunchnow_kmp.composeapp.generated.resources.common_clear_all
import spacelaunchnow_kmp.composeapp.generated.resources.common_close
import spacelaunchnow_kmp.composeapp.generated.resources.common_collapse
import spacelaunchnow_kmp.composeapp.generated.resources.common_expand
import spacelaunchnow_kmp.composeapp.generated.resources.common_expendable
import spacelaunchnow_kmp.composeapp.generated.resources.common_families
import spacelaunchnow_kmp.composeapp.generated.resources.common_filter_and_sort
import spacelaunchnow_kmp.composeapp.generated.resources.common_filters
import spacelaunchnow_kmp.composeapp.generated.resources.common_no_results_found
import spacelaunchnow_kmp.composeapp.generated.resources.common_programs
import spacelaunchnow_kmp.composeapp.generated.resources.common_remove_filter
import spacelaunchnow_kmp.composeapp.generated.resources.common_reusable
import spacelaunchnow_kmp.composeapp.generated.resources.common_search_title
import spacelaunchnow_kmp.composeapp.generated.resources.common_select_options
import spacelaunchnow_kmp.composeapp.generated.resources.common_selected
import spacelaunchnow_kmp.composeapp.generated.resources.common_selected_count
import spacelaunchnow_kmp.composeapp.generated.resources.common_sort_by
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_filter_active_only
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_filter_active_title
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_filter_loading
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_filter_none_loaded
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_filter_option_all
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_filter_refresh
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_filter_reload
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_filter_retired
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_filter_reusable_title
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_filter_search_label
import spacelaunchnow_kmp.composeapp.generated.resources.rockets_filter_search_placeholder

/**
 * Filter and sort bottom sheet content for rocket list.
 * 
 * Features:
 * - Sort order dropdown
 * - Search by name
 * - Clear all filters button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RocketFilterSheet(
    searchQuery: String,
    selectedSortOrder: RocketSortOrder,
    selectedProgramIds: List<Int>,
    selectedFamilies: List<String>,
    activeFilter: Boolean?,
    reusableFilter: Boolean?,
    programOptions: List<FilterOption>,
    familyOptions: List<FilterOption>,
    isLoadingFilterOptions: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (RocketSortOrder) -> Unit,
    onProgramsChange: (List<Int>) -> Unit,
    onFamiliesChange: (List<String>) -> Unit,
    onActiveChange: (Boolean?) -> Unit,
    onReusableChange: (Boolean?) -> Unit,
    onReloadOptions: () -> Unit,
    onClearAll: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sortExpanded by remember { mutableStateOf(false) }
    var localSearchQuery by remember(searchQuery) { mutableStateOf(searchQuery) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.common_filter_and_sort),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClearAll) {
                    Text(stringResource(Res.string.common_clear_all))
                }
            }
        }

        item {
            // Search Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.common_search_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = localSearchQuery,
                        onValueChange = { localSearchQuery = it },
                        label = { Text(stringResource(Res.string.rockets_filter_search_label)) },
                        placeholder = { Text(stringResource(Res.string.rockets_filter_search_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                localSearchQuery = ""
                                onSearchQueryChange("")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(Res.string.common_clear))
                        }
                        Button(
                            onClick = { onSearchQueryChange(localSearchQuery) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(Res.string.common_apply))
                        }
                    }
                }
            }
        }

        item {
            // Sort Order Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.common_sort_by),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = sortExpanded,
                        onExpandedChange = { sortExpanded = !sortExpanded }
                    ) {
                        OutlinedTextField(
                            value = stringResource(selectedSortOrder.label),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(12.dp)
                        )
                        AdCoveringExposedDropdownMenu(
                            expanded = sortExpanded,
                            onDismissRequest = { sortExpanded = false }
                        ) {
                            RocketSortOrder.entries.forEach { sortOrder ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(sortOrder.label)) },
                                    onClick = {
                                        onSortOrderChange(sortOrder)
                                        sortExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            // Filters Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.common_filters),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Active Filter
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(Res.string.rockets_filter_active_title),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(
                                selected = activeFilter == null,
                                onClick = { onActiveChange(null) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                                label = { Text(stringResource(Res.string.rockets_filter_option_all)) }
                            )
                            SegmentedButton(
                                selected = activeFilter == true,
                                onClick = { onActiveChange(true) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                                label = { Text(stringResource(Res.string.rockets_filter_active_only)) }
                            )
                            SegmentedButton(
                                selected = activeFilter == false,
                                onClick = { onActiveChange(false) },
                                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                                label = { Text(stringResource(Res.string.rockets_filter_retired)) }
                            )
                        }
                    }

                    // Reusable Filter
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(Res.string.rockets_filter_reusable_title),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(
                                selected = reusableFilter == null,
                                onClick = { onReusableChange(null) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                                label = { Text(stringResource(Res.string.rockets_filter_option_all)) }
                            )
                            SegmentedButton(
                                selected = reusableFilter == true,
                                onClick = { onReusableChange(true) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                                label = { Text(stringResource(Res.string.common_reusable)) }
                            )
                            SegmentedButton(
                                selected = reusableFilter == false,
                                onClick = { onReusableChange(false) },
                                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                                label = { Text(stringResource(Res.string.common_expendable)) }
                            )
                        }
                    }

                    // Program Filter
                    if (programOptions.isNotEmpty()) {
                        FilterSection(
                            title = stringResource(Res.string.common_programs),
                            options = programOptions,
                            selectedIds = selectedProgramIds,
                            onSelectionChange = onProgramsChange
                        )
                    }

                    // Family Filter
                    if (familyOptions.isNotEmpty()) {
                        FilterSectionByName(
                            title = stringResource(Res.string.common_families),
                            options = familyOptions,
                            selectedNames = selectedFamilies,
                            onSelectionChange = onFamiliesChange
                        )
                    }

                    if (isLoadingFilterOptions) {
                        Text(
                            text = stringResource(Res.string.rockets_filter_loading),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (programOptions.isEmpty() && familyOptions.isEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.rockets_filter_none_loaded),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = onReloadOptions
                            ) {
                                Text(stringResource(Res.string.rockets_filter_reload))
                            }
                        }
                    } else {
                        TextButton(
                            onClick = onReloadOptions
                        ) {
                            Text(stringResource(Res.string.rockets_filter_refresh))
                        }
                    }
                }
            }
        }

        item {
            // Close button
            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.common_close))
            }
        }
    }
}

/**
 * Searchable filter section for FilterOptions (by ID).
 * Shows selected items as chips and provides autocomplete text field.
 */
@Composable
private fun FilterSection(
    title: String,
    options: List<FilterOption>,
    selectedIds: List<Int>,
    onSelectionChange: (List<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var debouncedSearchQuery by remember { mutableStateOf("") }

    // Debounce search query - only update after 300ms of no typing
    LaunchedEffect(searchQuery) {
        delay(300)
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

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
                onValueChange = { searchQuery = it },
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
 * Searchable filter section for FilterOptions (by name).
 * Shows selected items as chips and provides autocomplete text field.
 */
@Composable
private fun FilterSectionByName(
    title: String,
    options: List<FilterOption>,
    selectedNames: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var debouncedSearchQuery by remember { mutableStateOf("") }

    // Debounce search query - only update after 300ms of no typing
    LaunchedEffect(searchQuery) {
        delay(300)
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

    val selectedOptions = remember(selectedNames, options) {
        options.filter { selectedNames.contains(it.name) }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

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
                            onSelectionChange(selectedNames - option.name)
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
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        if (selectedNames.isEmpty()) stringResource(Res.string.common_select_options)
                        else stringResource(Res.string.common_selected_count, selectedNames.size)
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
                        val isSelected = selectedNames.contains(option.name)
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
                                    selectedNames - option.name
                                } else {
                                    selectedNames + option.name
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

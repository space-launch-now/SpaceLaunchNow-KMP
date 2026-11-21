package me.calebjones.spacelaunchnow.ui.astronaut

import me.calebjones.spacelaunchnow.ui.ads.AdCoveringAlertDialog
import me.calebjones.spacelaunchnow.ui.ads.AdCoveringExposedDropdownMenu
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.ui.viewmodel.AstronautSortOrder
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_clear_all
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_done
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_filters_title
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_has_flown_title
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_in_space_title
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_name_label
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_name_placeholder
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_option_all
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_option_no
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_option_yes
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_order_label
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_reload_button
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_reload_dialog_cancel
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_reload_dialog_confirm
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_reload_dialog_message
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_reload_dialog_title
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_search_apply
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_search_clear
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_search_title
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_selected_description
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_sort_title
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_status_add_more
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_status_all
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_status_hint
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_status_remove_description
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_status_title
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_title
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_type_human
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_type_non_human
import spacelaunchnow_kmp.composeapp.generated.resources.astronaut_filter_type_title

/**
 * Filter and sort bottom sheet content for astronaut list.
 * 
 * Features:
 * - Sort order dropdown
 * - Search by name
 * - Status filters: Multi-select from available statuses
 * - Boolean filters: Has Flown, In Space, Is Human
 * - Clear all filters button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstronautFilterSheet(
    searchQuery: String,
    selectedSortOrder: AstronautSortOrder,
    selectedStatusIds: List<Int>,
    statusOptions: List<FilterOption>,
    hasFlownFilter: Boolean?,
    inSpaceFilter: Boolean?,
    isHumanFilter: Boolean?,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (AstronautSortOrder) -> Unit,
    onStatusSelectionChange: (List<Int>) -> Unit,
    onHasFlownChange: (Boolean?) -> Unit,
    onInSpaceChange: (Boolean?) -> Unit,
    onIsHumanChange: (Boolean?) -> Unit,
    onClearAll: () -> Unit,
    onReloadOptions: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sortExpanded by remember { mutableStateOf(false) }
    var localSearchQuery by remember(searchQuery) { mutableStateOf(searchQuery) }
    var showReloadConfirmDialog by remember { mutableStateOf(false) }

    // Reload confirmation dialog
    if (showReloadConfirmDialog) {
        AdCoveringAlertDialog(
            onDismissRequest = { showReloadConfirmDialog = false },
            title = { Text(stringResource(Res.string.astronaut_filter_reload_dialog_title)) },
            text = {
                Text(stringResource(Res.string.astronaut_filter_reload_dialog_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReloadConfirmDialog = false
                        onReloadOptions?.invoke()
                    }
                ) {
                    Text(stringResource(Res.string.astronaut_filter_reload_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showReloadConfirmDialog = false }) {
                    Text(stringResource(Res.string.astronaut_filter_reload_dialog_cancel))
                }
            }
        )
    }

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
                    text = stringResource(Res.string.astronaut_filter_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClearAll) {
                    Text(stringResource(Res.string.astronaut_filter_clear_all))
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
                        text = stringResource(Res.string.astronaut_filter_search_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = localSearchQuery,
                        onValueChange = { localSearchQuery = it },
                        label = { Text(stringResource(Res.string.astronaut_filter_name_label)) },
                        placeholder = { Text(stringResource(Res.string.astronaut_filter_name_placeholder)) },
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
                            Text(stringResource(Res.string.astronaut_filter_search_clear))
                        }
                        Button(
                            onClick = { onSearchQueryChange(localSearchQuery) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(Res.string.astronaut_filter_search_apply))
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
                        text = stringResource(Res.string.astronaut_filter_sort_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = sortExpanded,
                        onExpandedChange = { sortExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSortOrder.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(Res.string.astronaut_filter_order_label)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        AdCoveringExposedDropdownMenu(
                            expanded = sortExpanded,
                            onDismissRequest = { sortExpanded = false }
                        ) {
                            AstronautSortOrder.entries.forEach { sortOrder ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(sortOrder.displayName)
                                            if (sortOrder == selectedSortOrder) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = stringResource(Res.string.astronaut_filter_selected_description),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
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
            // Boolean Filters Section
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
                        text = stringResource(Res.string.astronaut_filter_filters_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Has Flown Filter
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(Res.string.astronaut_filter_has_flown_title),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(
                                selected = hasFlownFilter == null,
                                onClick = { onHasFlownChange(null) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                                label = { Text(stringResource(Res.string.astronaut_filter_option_all)) }
                            )
                            SegmentedButton(
                                selected = hasFlownFilter == true,
                                onClick = { onHasFlownChange(true) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                                label = { Text(stringResource(Res.string.astronaut_filter_option_yes)) }
                            )
                            SegmentedButton(
                                selected = hasFlownFilter == false,
                                onClick = { onHasFlownChange(false) },
                                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                                label = { Text(stringResource(Res.string.astronaut_filter_option_no)) }
                            )
                        }
                    }

                    // In Space Filter
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(Res.string.astronaut_filter_in_space_title),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(
                                selected = inSpaceFilter == null,
                                onClick = { onInSpaceChange(null) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                                label = { Text(stringResource(Res.string.astronaut_filter_option_all)) }
                            )
                            SegmentedButton(
                                selected = inSpaceFilter == true,
                                onClick = { onInSpaceChange(true) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                                label = { Text(stringResource(Res.string.astronaut_filter_option_yes)) }
                            )
                            SegmentedButton(
                                selected = inSpaceFilter == false,
                                onClick = { onInSpaceChange(false) },
                                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                                label = { Text(stringResource(Res.string.astronaut_filter_option_no)) }
                            )
                        }
                    }

                    // Is Human Filter
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(Res.string.astronaut_filter_type_title),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(
                                selected = isHumanFilter == null,
                                onClick = { onIsHumanChange(null) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                                label = { Text(stringResource(Res.string.astronaut_filter_option_all)) }
                            )
                            SegmentedButton(
                                selected = isHumanFilter == true,
                                onClick = { onIsHumanChange(true) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                                label = { Text(stringResource(Res.string.astronaut_filter_type_human)) }
                            )
                            SegmentedButton(
                                selected = isHumanFilter == false,
                                onClick = { onIsHumanChange(false) },
                                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                                label = { Text(stringResource(Res.string.astronaut_filter_type_non_human)) }
                            )
                        }
                    }
                }
            }
        }

        // Status Filter Section
        if (statusOptions.isNotEmpty()) {
            item {
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
                            text = stringResource(Res.string.astronaut_filter_status_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = stringResource(Res.string.astronaut_filter_status_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Selected status chips
                        val selectedStatuses = remember(selectedStatusIds, statusOptions) {
                            statusOptions.filter { selectedStatusIds.contains(it.id) }
                        }

                        if (selectedStatuses.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedStatuses.forEach { status ->
                                    FilterChip(
                                        selected = true,
                                        onClick = {
                                            onStatusSelectionChange(selectedStatusIds - status.id)
                                        },
                                        label = {
                                            Text(
                                                text = status.displayName,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(Res.string.astronaut_filter_status_remove_description),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // All available status chips
                        Text(
                            text = if (selectedStatusIds.isEmpty()) stringResource(Res.string.astronaut_filter_status_all) else stringResource(Res.string.astronaut_filter_status_add_more),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            statusOptions.forEach { status ->
                                val isSelected = selectedStatusIds.contains(status.id)
                                if (!isSelected) {
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            onStatusSelectionChange(selectedStatusIds + status.id)
                                        },
                                        label = {
                                            Text(
                                                text = status.displayName,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Reload button
        if (onReloadOptions != null) {
            item {
                TextButton(
                    onClick = { showReloadConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.astronaut_filter_reload_button))
                }
            }
        }

        item {
            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(Res.string.astronaut_filter_done))
                }
            }
        }
    }
}

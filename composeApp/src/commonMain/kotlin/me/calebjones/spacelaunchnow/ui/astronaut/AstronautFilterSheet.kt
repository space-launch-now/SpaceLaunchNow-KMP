package me.calebjones.spacelaunchnow.ui.astronaut

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
import androidx.compose.material3.AlertDialog
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
        AlertDialog(
            onDismissRequest = { showReloadConfirmDialog = false },
            title = { Text("Reload Filter Options?") },
            text = {
                Text("This will re-download status options from the server. This may take a few moments.")
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
                    text = "Filter & Sort",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClearAll) {
                    Text("Clear All")
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
                        text = "Search",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = localSearchQuery,
                        onValueChange = { localSearchQuery = it },
                        label = { Text("Astronaut name") },
                        placeholder = { Text("e.g., Armstrong, Aldrin") },
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
                            Text("Clear")
                        }
                        Button(
                            onClick = { onSearchQueryChange(localSearchQuery) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apply")
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
                        text = "Sort By",
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
                            label = { Text("Order") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
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
                                                    contentDescription = "Selected",
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
                        text = "Filters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Has Flown Filter
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Has Flown to Space",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = hasFlownFilter == null,
                                onClick = { onHasFlownChange(null) },
                                label = { Text("All") }
                            )
                            FilterChip(
                                selected = hasFlownFilter == true,
                                onClick = { onHasFlownChange(true) },
                                label = { Text("Yes") }
                            )
                            FilterChip(
                                selected = hasFlownFilter == false,
                                onClick = { onHasFlownChange(false) },
                                label = { Text("No") }
                            )
                        }
                    }

                    // In Space Filter
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Currently in Space",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = inSpaceFilter == null,
                                onClick = { onInSpaceChange(null) },
                                label = { Text("All") }
                            )
                            FilterChip(
                                selected = inSpaceFilter == true,
                                onClick = { onInSpaceChange(true) },
                                label = { Text("Yes") }
                            )
                            FilterChip(
                                selected = inSpaceFilter == false,
                                onClick = { onInSpaceChange(false) },
                                label = { Text("No") }
                            )
                        }
                    }

                    // Is Human Filter
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Type",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = isHumanFilter == null,
                                onClick = { onIsHumanChange(null) },
                                label = { Text("All") }
                            )
                            FilterChip(
                                selected = isHumanFilter == true,
                                onClick = { onIsHumanChange(true) },
                                label = { Text("Human") }
                            )
                            FilterChip(
                                selected = isHumanFilter == false,
                                onClick = { onIsHumanChange(false) },
                                label = { Text("Non-Human") }
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
                            text = "Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Select one or more statuses to filter by",
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
                                                contentDescription = "Remove filter",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // All available status chips
                        Text(
                            text = if (selectedStatusIds.isEmpty()) "All Statuses" else "Add More",
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
                    Text("Reload Filter Options")
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
                    Text("Done")
                }
            }
        }
    }
}

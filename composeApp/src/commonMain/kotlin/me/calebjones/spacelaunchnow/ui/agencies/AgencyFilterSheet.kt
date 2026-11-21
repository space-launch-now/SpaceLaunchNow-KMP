package me.calebjones.spacelaunchnow.ui.agencies

import me.calebjones.spacelaunchnow.ui.ads.AdCoveringExposedDropdownMenu
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import me.calebjones.spacelaunchnow.ui.viewmodel.AgencySortOrder
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.agency_filter_clear_all
import spacelaunchnow_kmp.composeapp.generated.resources.agency_filter_done
import spacelaunchnow_kmp.composeapp.generated.resources.agency_filter_featured_all
import spacelaunchnow_kmp.composeapp.generated.resources.agency_filter_featured_none
import spacelaunchnow_kmp.composeapp.generated.resources.agency_filter_featured_only
import spacelaunchnow_kmp.composeapp.generated.resources.agency_filter_featured_title
import spacelaunchnow_kmp.composeapp.generated.resources.agency_filter_filters_title
import spacelaunchnow_kmp.composeapp.generated.resources.agency_filter_name_label
import spacelaunchnow_kmp.composeapp.generated.resources.agency_filter_name_placeholder
import spacelaunchnow_kmp.composeapp.generated.resources.agency_filter_order_label
import spacelaunchnow_kmp.composeapp.generated.resources.agency_filter_search_apply
import spacelaunchnow_kmp.composeapp.generated.resources.agency_filter_search_clear
import spacelaunchnow_kmp.composeapp.generated.resources.agency_filter_search_title
import spacelaunchnow_kmp.composeapp.generated.resources.agency_filter_selected_description
import spacelaunchnow_kmp.composeapp.generated.resources.agency_filter_sort_title
import spacelaunchnow_kmp.composeapp.generated.resources.agency_filter_title
import spacelaunchnow_kmp.composeapp.generated.resources.*

/**
 * Filter and sort bottom sheet content for agency list.
 * 
 * Features:
 * - Sort order dropdown
 * - Search by name
 * - Featured filter
 * - Clear all filters button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyFilterSheet(
    searchQuery: String,
    selectedSortOrder: AgencySortOrder,
    featuredFilter: Boolean?,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (AgencySortOrder) -> Unit,
    onFeaturedChange: (Boolean?) -> Unit,
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
                    text = stringResource(Res.string.agency_filter_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClearAll) {
                    Text(stringResource(Res.string.agency_filter_clear_all))
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
                        text = stringResource(Res.string.agency_filter_search_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = localSearchQuery,
                        onValueChange = { localSearchQuery = it },
                        label = { Text(stringResource(Res.string.agency_filter_name_label)) },
                        placeholder = { Text(stringResource(Res.string.agency_filter_name_placeholder)) },
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
                            Text(stringResource(Res.string.agency_filter_search_clear))
                        }
                        Button(
                            onClick = { onSearchQueryChange(localSearchQuery) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(Res.string.agency_filter_search_apply))
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
                        text = stringResource(Res.string.agency_filter_sort_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = sortExpanded,
                        onExpandedChange = { sortExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = stringResource(selectedSortOrder.label),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(Res.string.agency_filter_order_label)) },
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
                            AgencySortOrder.entries.forEach { sortOrder ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(stringResource(sortOrder.label))
                                            if (sortOrder == selectedSortOrder) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = stringResource(Res.string.agency_filter_selected_description),
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
            // Featured Filter Section
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
                        text = stringResource(Res.string.agency_filter_filters_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Featured Filter
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(Res.string.agency_filter_featured_title),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(
                                selected = featuredFilter == null,
                                onClick = { onFeaturedChange(null) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                                label = { Text(stringResource(Res.string.agency_filter_featured_all)) }
                            )
                            SegmentedButton(
                                selected = featuredFilter == true,
                                onClick = { onFeaturedChange(true) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                                label = { Text(stringResource(Res.string.agency_filter_featured_only)) }
                            )
                            SegmentedButton(
                                selected = featuredFilter == false,
                                onClick = { onFeaturedChange(false) },
                                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                                label = { Text(stringResource(Res.string.agency_filter_featured_none)) }
                            )
                        }
                    }
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
                    Text(stringResource(Res.string.agency_filter_done))
                }
            }
        }
    }
}

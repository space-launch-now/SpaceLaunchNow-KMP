package me.calebjones.spacelaunchnow.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.data.model.SortField
import me.calebjones.spacelaunchnow.getPlatform
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Sort menu component that adapts to platform:
 * - Desktop/Web: DropdownMenu
 * - Mobile: ModalBottomSheet
 *
 * @param currentSort Currently selected sort field
 * @param onSortSelected Callback when a sort option is selected
 * @param onDismiss Callback when the menu is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortMenu(
    currentSort: SortField,
    onSortSelected: (SortField) -> Unit,
    onDismiss: () -> Unit,
    anchorView: @Composable () -> Unit
) {
    val platform = getPlatform().name
    val isDesktop = platform.contains("JVM", ignoreCase = true) || 
                    platform.contains("Desktop", ignoreCase = true) ||
                    platform.contains("JS", ignoreCase = true)

    if (isDesktop) {
        // Desktop: Dropdown menu
        var expanded by remember { mutableStateOf(true) }
        
        Column {
            anchorView()
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    onDismiss()
                }
            ) {
                SortField.entries.forEach { sortField ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentSort == sortField,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(sortField.displayName)
                            }
                        },
                        onClick = {
                            onSortSelected(sortField)
                            expanded = false
                            onDismiss()
                        }
                    )
                }
            }
        }
    } else {
        // Mobile: Bottom sheet
        ModalBottomSheet(
            onDismissRequest = onDismiss
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Sort by",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SortField.entries.forEach { sortField ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSortSelected(sortField)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSort == sortField,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = sortField.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SortMenuPreview() {
    MaterialTheme {
        Surface {
            SortMenu(
                currentSort = SortField.NAME_ASC,
                onSortSelected = {},
                onDismiss = {},
                anchorView = { Text("Sort") }
            )
        }
    }
}

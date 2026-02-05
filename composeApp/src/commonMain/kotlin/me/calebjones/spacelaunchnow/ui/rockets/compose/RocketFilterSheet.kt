package me.calebjones.spacelaunchnow.ui.rockets.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.data.model.ManufacturerFilter
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Bottom sheet for filtering rockets by manufacturer.
 *
 * @param manufacturers List of available manufacturers with rocket counts
 * @param selectedManufacturerIds Currently selected manufacturer IDs
 * @param onManufacturerToggle Callback when a manufacturer checkbox is toggled
 * @param onDismiss Callback when the sheet is dismissed
 * @param onClearAll Callback when "Clear All" is clicked
 * @param onApply Callback when "Apply" is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RocketFilterSheet(
    manufacturers: List<ManufacturerFilter>,
    selectedManufacturerIds: Set<Int>,
    onManufacturerToggle: (Int) -> Unit,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter by Manufacturer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClearAll) {
                    Text("Clear All")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Manufacturer list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(manufacturers) { manufacturer ->
                    ManufacturerFilterItem(
                        manufacturer = manufacturer,
                        isSelected = selectedManufacturerIds.contains(manufacturer.id),
                        onToggle = { onManufacturerToggle(manufacturer.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onApply()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

/**
 * Individual manufacturer filter item with checkbox.
 */
@Composable
private fun ManufacturerFilterItem(
    manufacturer: ManufacturerFilter,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
            Column(
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = manufacturer.displayLabel,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview
@Composable
private fun RocketFilterSheetPreview() {
    MaterialTheme {
        Surface {
            RocketFilterSheet(
                manufacturers = listOf(
                    ManufacturerFilter(
                        id = 1,
                        name = "SpaceX",
                        abbreviation = "SpX",
                        rocketCount = 12
                    ),
                    ManufacturerFilter(
                        id = 2,
                        name = "United Launch Alliance",
                        abbreviation = "ULA",
                        rocketCount = 8
                    ),
                    ManufacturerFilter(
                        id = 3,
                        name = "Rocket Lab",
                        abbreviation = null,
                        rocketCount = 3
                    )
                ),
                selectedManufacturerIds = setOf(1),
                onManufacturerToggle = {},
                onDismiss = {},
                onClearAll = {},
                onApply = {}
            )
        }
    }
}

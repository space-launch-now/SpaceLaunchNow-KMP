package me.calebjones.spacelaunchnow.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_filter_chip_remove_description
import org.jetbrains.compose.resources.stringResource

/**
 * Filter chip component displaying active filter with optional remove action.
 *
 * @param label The text to display on the chip
 * @param onRemove Callback when the remove icon is clicked. If null, no remove icon is shown.
 */
@Composable
fun FilterChip(
    label: String,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = { onRemove?.invoke() },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        },
        trailingIcon = onRemove?.let {
            {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.app_components_filter_chip_remove_description, label),
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier
    )
}

@Preview
@Composable
private fun FilterChipPreview() {
    MaterialTheme {
        Surface {
            FilterChip(
                label = "SpaceX", // i18n-ignore: preview sample data
                onRemove = {}
            )
        }
    }
}

@Preview
@Composable
private fun FilterChipWithoutRemovePreview() {
    MaterialTheme {
        Surface {
            FilterChip(
                label = "Active Only" // i18n-ignore: preview sample data
            )
        }
    }
}

package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Data class for InfoTile content
 */
data class InfoTileData(
    val icon: ImageVector,
    val label: String,
    val value: String? = null,
    val customComposable: (@Composable () -> Unit)? = null,
)

/**
 * Reusable Info Tile component for displaying labeled data with an icon
 * 
 * @param icon The icon to display at the top of the tile
 * @param label The label text describing the data
 * @param value The value text to display (optional if customComposable is provided)
 * @param modifier Modifier for the tile
 * @param customComposable Optional custom composable to display instead of value text
 */
@Composable
fun InfoTile(
    icon: ImageVector,
    label: String,
    value: String? = null,
    modifier: Modifier = Modifier,
    customComposable: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (customComposable != null) {
                customComposable()
            } else if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview
@Composable
private fun InfoTilePreview() {
    MaterialTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoTile(
                    icon = Icons.Filled.CalendarToday,
                    label = "Founded",
                    value = "1958",
                    modifier = Modifier.weight(1f)
                )
                InfoTile(
                    icon = Icons.Filled.CalendarToday,
                    label = "Type",
                    value = "Government",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

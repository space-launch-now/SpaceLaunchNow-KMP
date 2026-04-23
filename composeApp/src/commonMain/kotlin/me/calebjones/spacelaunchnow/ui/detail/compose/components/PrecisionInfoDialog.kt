package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.domain.model.NetPrecision

@Composable
fun PrecisionInfoDialog(
    netPrecision: NetPrecision?,
    onDismiss: () -> Unit
) {
    val ui = remember(netPrecision) { mapNetPrecisionUi(netPrecision) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Launch Time Precision", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        ui.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = ui.primary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                ui.secondary?.let { secondary ->
                    Text(
                        text = secondary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "The launch NET (No Earlier Than) value is supplied by the provider and precision may vary. NET can represent a fully scheduled time, an approximate window, only a year, only a month, etc. Some launches only have rough or placeholder NET values until closer to launch.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
    )
}

private data class PrecisionUi(
    val icon: ImageVector,
    val primary: String,
    val secondary: String?
)

private fun mapNetPrecisionUi(netPrecision: NetPrecision?): PrecisionUi {
    // Defaults
    val defaultPrimary = netPrecision?.name?.takeIf { it.isNotBlank() }
        ?: netPrecision?.abbrev?.takeIf { it.isNotBlank() }
        ?: "Unknown"
    val fallbackSecondary = netPrecision?.description?.takeIf { it.isNotBlank() }

    // Choose icon and concise secondary by id when available
    return when (netPrecision?.id) {
        0 -> PrecisionUi(
            icon = Icons.Filled.Schedule,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )

        1 -> PrecisionUi(
            icon = Icons.Filled.Schedule,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )

        2 -> PrecisionUi(
            icon = Icons.Filled.Schedule,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )

        3 -> PrecisionUi(
            icon = Icons.Filled.Schedule,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )

        4 -> PrecisionUi(
            icon = Icons.Filled.Schedule,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )

        5 -> PrecisionUi(
            icon = Icons.Filled.CalendarToday,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )

        7 -> PrecisionUi(
            icon = Icons.Filled.CalendarToday,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )

        8, 9, 10 -> PrecisionUi(
            icon = Icons.Filled.CalendarToday,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )

        else -> PrecisionUi(
            icon = Icons.Filled.CalendarToday,
            primary = defaultPrimary,
            secondary = fallbackSecondary
        )
    }
}

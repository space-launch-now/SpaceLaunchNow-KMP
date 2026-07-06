package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.util.logging.DiagnosticLevel
import me.calebjones.spacelaunchnow.util.logging.DiagnosticLevelController
import me.calebjones.spacelaunchnow.util.logging.LoggingPreferences
import me.calebjones.spacelaunchnow.util.logging.displayLabel

/**
 * Single diagnostic-logging control (replaces the old enable toggle + console
 * severity + Datadog severity + sample-rate knobs).
 *
 * Off      — nothing uploads (Datadog consent NOT_GRANTED); local file log stays on.
 * Standard — warnings, delivery decisions, and structured events upload.
 * Verbose  — detailed debug logs upload; console goes to Debug too.
 */
@Composable
fun LoggingSettingsSection(
    loggingPreferences: LoggingPreferences,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val level by loggingPreferences.getDiagnosticLevel()
        .collectAsState(initial = DiagnosticLevel.OFF)
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Diagnostic Logging", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Help us debug issues by sharing diagnostic logs. Standard sends warnings " +
                    "and notification delivery decisions; Verbose sends detailed logs. " +
                    "Off keeps logs on this device only." +
                    " Verbose switches back automatically after 72 hours.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiagnosticLevel.entries.forEach { option ->
                    FilterChip(
                        selected = level == option,
                        onClick = {
                            coroutineScope.launch {
                                loggingPreferences.setDiagnosticLevel(option)
                                // Apply immediately; the startup observer also converges.
                                DiagnosticLevelController.apply(option)
                            }
                        },
                        label = { Text(option.displayLabel()) }
                    )
                }
            }
            TextButton(onClick = onOpenDiagnostics) {
                Text("Open Diagnostics")
            }
        }
    }
}

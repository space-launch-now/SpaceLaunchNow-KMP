package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Severity
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.util.logging.LoggingPreferences

/**
 * Debug menu section for developer logging controls
 * 
 * Provides granular logging level selectors for Console and DataDog destinations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugLoggingSettings(
    loggingPreferences: LoggingPreferences,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // Get current console severity based on debug build and preferences
    val consoleSeverity by loggingPreferences.getConsoleSeverity(BuildConfig.IS_DEBUG)
        .collectAsState(initial = Severity.Warn)
    val dataDogSeverity by loggingPreferences.getDataDogSeverity()
        .collectAsState(initial = Severity.Warn)

    // Available severity levels
    val severityLevels = listOf(
        Severity.Verbose to "VERBOSE (All logs)",
        Severity.Debug to "DEBUG (Detailed)",
        Severity.Info to "INFO (General)",
        Severity.Warn to "WARN (Warnings only)",
        Severity.Error to "ERROR (Errors only)"
    )

    var consoleExpanded by remember { mutableStateOf(false) }
    var dataDogExpanded by remember { mutableStateOf(false) }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Developer Logging",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Control log verbosity for console output and remote DataDog logging. " +
                "Higher verbosity levels may impact performance.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Console Log Level Selector
            Text(
                "Console (Logcat/Xcode)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = consoleExpanded,
                onExpandedChange = { consoleExpanded = it }
            ) {
                OutlinedTextField(
                    value = severityLevels.find { it.first == consoleSeverity }?.second
                        ?: "DEBUG (Detailed)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Console Log Level") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = consoleExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = consoleExpanded,
                    onDismissRequest = { consoleExpanded = false }
                ) {
                    severityLevels.forEach { (severity, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                coroutineScope.launch {
                                    // Set the appropriate preference based on severity
                                    when (severity) {
                                        Severity.Verbose -> {
                                            loggingPreferences.setDebugModeEnabled(true)
                                        }
                                        Severity.Debug -> {
                                            loggingPreferences.setDebugModeEnabled(false)
                                            loggingPreferences.setUserLoggingEnabled(false)
                                        }
                                        Severity.Info -> {
                                            loggingPreferences.setDebugModeEnabled(false)
                                            loggingPreferences.setUserLoggingEnabled(true)
                                        }
                                        else -> { // Warn, Error
                                            loggingPreferences.setDebugModeEnabled(false)
                                            loggingPreferences.setUserLoggingEnabled(false)
                                        }
                                    }
                                }
                                consoleExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DataDog Log Level Selector
            Text(
                "DataDog (Remote Logging)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = dataDogExpanded,
                onExpandedChange = { dataDogExpanded = it }
            ) {
                OutlinedTextField(
                    value = severityLevels.find { it.first == dataDogSeverity }?.second
                        ?: "WARN (Warnings only)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("DataDog Log Level") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dataDogExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = dataDogExpanded,
                    onDismissRequest = { dataDogExpanded = false }
                ) {
                    severityLevels.forEach { (severity, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                coroutineScope.launch {
                                    // DataDog severity is controlled separately
                                    // This would require extending LoggingPreferences
                                    // For now, it follows the same rules as console
                                    when (severity) {
                                        Severity.Verbose, Severity.Debug -> {
                                            loggingPreferences.setDebugModeEnabled(true)
                                        }
                                        Severity.Info -> {
                                            loggingPreferences.setDebugModeEnabled(false)
                                            loggingPreferences.setUserLoggingEnabled(true)
                                        }
                                        else -> { // Warn, Error
                                            loggingPreferences.setDebugModeEnabled(false)
                                            loggingPreferences.setUserLoggingEnabled(false)
                                        }
                                    }
                                }
                                dataDogExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Warning when verbose/debug mode is on
            if (consoleSeverity <= Severity.Debug) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    color = if (consoleSeverity == Severity.Verbose)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (consoleSeverity == Severity.Verbose) "⚠️" else "ℹ️",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (consoleSeverity == Severity.Verbose)
                                "VERBOSE logs ALL app activity. May significantly impact performance and use data."
                            else
                                "DEBUG logging is active. May impact performance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (consoleSeverity == Severity.Verbose)
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            // Current Log Levels Display (kept for reference)
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Current Settings",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LogLevelRow(
                destination = "Console Output",
                severity = consoleSeverity
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LogLevelRow(
                destination = "DataDog Remote",
                severity = dataDogSeverity
            )
        }
    }
}

@Composable
private fun LogLevelRow(
    destination: String,
    severity: Severity
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            destination,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        SeverityChip(severity)
    }
}

@Composable
private fun SeverityChip(severity: Severity) {
    val (color, text) = when (severity) {
        Severity.Verbose -> MaterialTheme.colorScheme.surfaceVariant to "VERBOSE"
        Severity.Debug -> MaterialTheme.colorScheme.tertiary to "DEBUG+"
        Severity.Info -> MaterialTheme.colorScheme.primary to "INFO+"
        Severity.Warn -> MaterialTheme.colorScheme.secondary to "WARN+"
        Severity.Error, Severity.Assert -> MaterialTheme.colorScheme.error to "ERROR+"
    }
    
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

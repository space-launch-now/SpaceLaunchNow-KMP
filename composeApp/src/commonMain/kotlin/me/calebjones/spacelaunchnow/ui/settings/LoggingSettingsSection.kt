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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.util.logging.LoggingPreferences

/**
 * Settings section for user-controlled diagnostic logging
 * 
 * Allows users to opt-in to INFO+ logging to help with troubleshooting
 */
@Composable
fun LoggingSettingsSection(
    loggingPreferences: LoggingPreferences,
    modifier: Modifier = Modifier
) {
    val isUserLoggingEnabled by loggingPreferences.isUserLoggingEnabled
        .collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Diagnostic Logging",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Help us improve by sharing diagnostic logs. " +
                "Only errors are logged by default.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Enable Diagnostic Logging",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        if (isUserLoggingEnabled) "Logging app activity" 
                        else "Only logging errors",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = isUserLoggingEnabled,
                    onCheckedChange = { enabled ->
                        coroutineScope.launch {
                            loggingPreferences.setUserLoggingEnabled(enabled)
                        }
                    }
                )
            }
        }
    }
}

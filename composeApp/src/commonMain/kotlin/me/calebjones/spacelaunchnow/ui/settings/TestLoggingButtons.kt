package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

/**
 * Test Logging Buttons Section
 * Allows testing Datadog logging at different severity levels to verify configuration
 */
@Composable
fun TestLoggingButtons() {
    val log = remember { SpaceLogger.getLogger("TestLogging") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Generate test log messages at different severity levels to verify Datadog logging configuration.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // VERBOSE button
                OutlinedButton(
                    onClick = {
                        log.v { "🔍 VERBOSE: Test message at VERBOSE level" }
                        scope.launch {
                            snackbarHostState.showSnackbar("Verbose log sent")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                ) {
                    Text("VERBOSE", fontSize = 11.sp)
                }
                
                // DEBUG button
                OutlinedButton(
                    onClick = {
                        log.d { "🐛 DEBUG: Test message at DEBUG level" }
                        scope.launch {
                            snackbarHostState.showSnackbar("Debug log sent")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("DEBUG", fontSize = 11.sp)
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // INFO button
                Button(
                    onClick = {
                        log.i { "ℹ️ INFO: Test message at INFO level" }
                        scope.launch {
                            snackbarHostState.showSnackbar("Info log sent")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("INFO", fontSize = 11.sp)
                }
                
                // WARN button
                Button(
                    onClick = {
                        log.w { "⚠️ WARN: Test message at WARN level" }
                        scope.launch {
                            snackbarHostState.showSnackbar("Warning log sent")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("WARN", fontSize = 11.sp)
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ERROR button
                Button(
                    onClick = {
                        log.e { "❌ ERROR: Test message at ERROR level" }
                        scope.launch {
                            snackbarHostState.showSnackbar("Error log sent")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("ERROR", fontSize = 11.sp)
                }
                
                // ERROR with Exception button
                Button(
                    onClick = {
                        val testException = Exception("Test exception for debugging")
                        log.e(testException) { "💥 ERROR: Test error with exception attached" }
                        scope.launch {
                            snackbarHostState.showSnackbar("Error with exception sent")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("ERROR+EX", fontSize = 10.sp)
                }
            }
            
            Text(
                text = "💡 Tip: Check your Datadog dashboard or logcat/Xcode console to verify which levels are being logged based on your current configuration.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
    
    SnackbarHost(hostState = snackbarHostState)
}

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
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

/**
 * Datadog Sample Rate Control
 * Allows adjusting the percentage of logs sent to Datadog servers
 */
@Composable
fun DatadogSampleRateControl(
    currentRate: Float,
    onRateChange: (Float) -> Unit
) {
    var sliderPosition by remember(currentRate) { mutableStateOf(currentRate) }

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
                text = "Control what percentage of logs are sent to Datadog remote servers. Lower values reduce costs.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sample Rate:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${sliderPosition.toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        sliderPosition <= 5f -> MaterialTheme.colorScheme.primary
                        sliderPosition <= 25f -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }

            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                onValueChangeFinished = {
                    onRateChange(sliderPosition)
                },
                valueRange = 0f..100f,
                steps = 19, // 0, 5, 10, 15, ..., 100
                modifier = Modifier.fillMaxWidth()
            )

            // Quick preset buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        sliderPosition = 1f
                        onRateChange(1f)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("1%", fontSize = 10.sp, maxLines = 1)
                }
                OutlinedButton(
                    onClick = {
                        sliderPosition = 5f
                        onRateChange(5f)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("5%", fontSize = 10.sp, maxLines = 1)
                }
                OutlinedButton(
                    onClick = {
                        sliderPosition = 10f
                        onRateChange(10f)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("10%", fontSize = 10.sp, maxLines = 1)
                }
                OutlinedButton(
                    onClick = {
                        sliderPosition = 25f
                        onRateChange(25f)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("25%", fontSize = 10.sp, maxLines = 1)
                }
                OutlinedButton(
                    onClick = {
                        sliderPosition = 100f
                        onRateChange(100f)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("100%", fontSize = 10.sp, maxLines = 1)
                }
            }

            Text(
                text = "⚠️ Changes require app restart to take effect",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

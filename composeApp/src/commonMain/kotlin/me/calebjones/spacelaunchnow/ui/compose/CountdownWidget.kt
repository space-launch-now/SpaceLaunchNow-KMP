package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.calebjones.spacelaunchnow.domain.model.LaunchStatus
import me.calebjones.spacelaunchnow.domain.model.NetPrecision
import me.calebjones.spacelaunchnow.util.StatusColorUtil
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs
import kotlin.time.Clock.System
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Preview
@Composable
fun BuildLaunchCountdown() {
    Card {
        LaunchCountdown(
            launchTime = System.now().plus(120.days),
            status = LaunchStatus(1, "Go for Launch", abbrev = "Go", description = "The launch is a go!"),
            precision = NetPrecision(id = 7, name = "Minute", abbrev = null, description = null)
        )
    }
}


@Composable
fun LaunchCountdown(
    launchTime: Instant,
    status: LaunchStatus?,
    precision: NetPrecision?
) {
    var showStatusDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Status button with divider overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            // HorizontalDivider with higher contrast
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .align(Alignment.Center)
            )

            // Pill-shaped Status Button
            Button(
                onClick = { showStatusDialog = true },
                shape = RoundedCornerShape(25),
                modifier = Modifier
                    .align(Alignment.Center)
                    .shadow(8.dp, RoundedCornerShape(25.dp)),
                colors = StatusColorUtil.getLaunchStatusButtonColors(status?.id)
            ) {
                status?.name?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Countdown display
        CountdownDisplay(launchTime = launchTime, precision = precision)
    }

    // Status Information Dialog
    if (showStatusDialog) {
        LaunchStatusDialog(
            statusName = status?.name,
            statusDescription = status?.description,
            onDismiss = { showStatusDialog = false }
        )
    }
}

@Composable
fun LaunchStatusDialog(
    statusName: String?,
    statusDescription: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Launch Status", style = MaterialTheme.typography.titleLarge)
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
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Status information"
                    )
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (statusDescription != null) {
                    Text(
                        text = statusDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "OK")
            }
        }
    )
}

@Composable
private fun CountdownDisplay(launchTime: Instant, precision: NetPrecision?) {
    val remainingTime = remember { mutableStateOf(launchTime - System.now()) }

    LaunchedEffect(launchTime) {
        while (true) {
            remainingTime.value = launchTime - System.now()
            delay(1.seconds)
        }
    }

    val totalSeconds = remainingTime.value.inWholeSeconds
    val isPast = totalSeconds < 0 // Check if the target date is in the past
    val absoluteSeconds = abs(totalSeconds) // Get the absolute value of seconds

    val days = (absoluteSeconds / 86400).toInt()
    val hours = ((absoluteSeconds % 86400) / 3600).toInt()
    val minutes = ((absoluteSeconds % 3600) / 60).toInt()
    val seconds = (absoluteSeconds % 60).toInt()

    // Show friendly date format if more than 90 days away (past or future)
    val precisionAllowed = precision?.id in 0..4
    val showCountdown = (days < 30) && precisionAllowed

    if (showCountdown ) {
        // Use BoxWithConstraints at the top level to determine sizing for all elements
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            // Calculate uniform font sizes based on available width
            val containerWidth = maxWidth.value
            val digitFontSize = (containerWidth * 0.07f).coerceIn(15f, 64f).sp
            val labelFontSize = (containerWidth * 0.025f).coerceIn(10f, 22f).sp

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    if (isPast) {
                        Text(
                            text = "+",
                            fontSize = digitFontSize,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(end = 2.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    CountdownItem(
                        label = "DAYS",
                        value = days,
                        digitFontSize = digitFontSize,
                        labelFontSize = labelFontSize
                    )
                    CountdownSeparator(fontSize = digitFontSize)
                    CountdownItem(
                        label = "HOURS",
                        value = hours,
                        digitFontSize = digitFontSize,
                        labelFontSize = labelFontSize
                    )
                    CountdownSeparator(fontSize = digitFontSize)
                    CountdownItem(
                        label = "MINUTES",
                        value = minutes,
                        digitFontSize = digitFontSize,
                        labelFontSize = labelFontSize
                    )
                    CountdownSeparator(fontSize = digitFontSize)
                    CountdownItem(
                        label = "SECONDS",
                        value = seconds,
                        digitFontSize = digitFontSize,
                        labelFontSize = labelFontSize
                    )
                }
            }
        }
    }

    if (showCountdown) {
        // Divider after countdown
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        )
    }

}

@Composable
fun CountdownSeparator(fontSize: androidx.compose.ui.unit.TextUnit) {
    Text(
        text = ":",
        fontSize = fontSize,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 2.dp)
    )
}

@Composable
fun CountdownItem(
    label: String,
    value: Int,
    digitFontSize: androidx.compose.ui.unit.TextUnit,
    labelFontSize: androidx.compose.ui.unit.TextUnit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .padding(horizontal = 2.dp)
    ) {
        Text(
            text = value.toString().padStart(2, '0'),
            fontSize = digitFontSize,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = labelFontSize,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
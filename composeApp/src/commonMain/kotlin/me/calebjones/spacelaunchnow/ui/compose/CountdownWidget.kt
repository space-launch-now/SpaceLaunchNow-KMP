package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.util.StatusColorUtil
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs
import kotlin.time.Clock.System
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Preview
@Composable
fun BuildLaunchCountdown() {
    SpaceLaunchNowPreviewTheme {
        Card {
            LaunchCountdown(
                // Near + precise so the countdown digits actually render
                // (showCountdown needs days < 30 and precision id in 0..4).
                launchTime = System.now().plus(2.days).plus(6.hours).plus(34.minutes),
                status = LaunchStatus(
                    1,
                    "Go for Launch",
                    abbrev = "Go",
                    description = "The launch is a go!"
                ),
                precision = NetPrecision(id = 0, name = "Second", abbrev = null, description = null)
            )
        }
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
    val state = rememberCountdownState(launchTime, precision)

    if (state.showCountdown) {
        CountdownDigits(state)

        // Divider after countdown
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth()
        )
    }
}

/**
 * A compact live countdown for tight spaces such as the small home-page launch cards.
 * Renders a single-line "T- 1d 06h 59m 59s" row — a small timer icon plus monospace text
 * styled to match the sibling location row (same onSurfaceVariant color) so it sits neatly
 * in the card. Monospace keeps the digits from jittering as they tick. When the launch is
 * too far out or its date is too imprecise to count down, [fallback] is shown instead —
 * typically the formatted launch date.
 */
@Composable
fun CompactLaunchCountdown(
    launchTime: Instant,
    precision: NetPrecision?,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit
) {
    val state = rememberCountdownState(launchTime, precision)

    if (state.showCountdown) {
        val countdownText = buildString {
            append(if (state.isPast) "T+ " else "T- ")
            append(state.days).append("d ")
            append(state.hours.toString().padStart(2, '0')).append("h ")
            append(state.minutes.toString().padStart(2, '0')).append("m ")
            append(state.seconds.toString().padStart(2, '0')).append("s")
        }

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Timer,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = countdownText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    } else {
        fallback()
    }
}

/**
 * Decomposed time remaining until a launch, recomputed once per second.
 * Shared by the hero countdown and the compact card countdown so both stay in sync.
 */
private data class CountdownState(
    val days: Int,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val isPast: Boolean,
    val showCountdown: Boolean
)

/**
 * Ticks once per second and returns the current [CountdownState] for [launchTime].
 * A live countdown is only shown when the launch is within 30 days and its net time
 * is precise enough (precision id 0..4); otherwise callers fall back to a date.
 */
@Composable
private fun rememberCountdownState(launchTime: Instant, precision: NetPrecision?): CountdownState {
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

    val precisionAllowed = precision?.id in 0..4
    val showCountdown = (days < 30) && precisionAllowed

    return CountdownState(days, hours, minutes, seconds, isPast, showCountdown)
}

/**
 * The DAYS : HOURS : MINUTES : SECONDS digit row, sized to the available width.
 * Prefixes a "+" when the launch time is in the past (T+).
 */
@Composable
private fun CountdownDigits(state: CountdownState, modifier: Modifier = Modifier) {
    // Use BoxWithConstraints at the top level to determine sizing for all elements
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        // Calculate uniform font sizes based on available width
        val containerWidth = maxWidth.value
        val digitFontSize = (containerWidth * 0.07f).coerceIn(15f, 64f).sp
        val labelFontSize = (containerWidth * 0.03f).coerceIn(10f, 22f).sp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                if (state.isPast) {
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
                    value = state.days,
                    digitFontSize = digitFontSize,
                    labelFontSize = labelFontSize
                )
                CountdownSeparator(fontSize = digitFontSize)
                CountdownItem(
                    label = "HOURS",
                    value = state.hours,
                    digitFontSize = digitFontSize,
                    labelFontSize = labelFontSize
                )
                CountdownSeparator(fontSize = digitFontSize)
                CountdownItem(
                    label = "MINUTES",
                    value = state.minutes,
                    digitFontSize = digitFontSize,
                    labelFontSize = labelFontSize
                )
                CountdownSeparator(fontSize = digitFontSize)
                CountdownItem(
                    label = "SECONDS",
                    value = state.seconds,
                    digitFontSize = digitFontSize,
                    labelFontSize = labelFontSize
                )
            }
        }
    }
}

@Composable
fun CountdownSeparator(fontSize: androidx.compose.ui.unit.TextUnit) {
    Text(
        text = ":",
        fontSize = fontSize,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
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
//            .width(IntrinsicSize.Min)
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
        Text(
            text = label,
            fontSize = labelFontSize,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// ========================================
// CompactLaunchCountdown previews
// ========================================

/**
 * Both states of [CompactLaunchCountdown] on a card-like surface: the live pill (near +
 * precise) and the date fallback (too far out to count down).
 */
@Composable
private fun CompactCountdownPreviewContent() {
    val precise = NetPrecision(id = 0, name = "Second", abbrev = null, description = null)

    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Live countdown pill
            CompactLaunchCountdown(
                launchTime = System.now().plus(2.days).plus(6.hours).plus(34.minutes),
                precision = precise,
                fallback = { Text("fallback") }
            )
            // Fallback: 120 days out is past the 30-day countdown window
            CompactLaunchCountdown(
                launchTime = System.now().plus(120.days),
                precision = precise,
                fallback = {
                    Text(
                        text = "NET Feb 15, 2026",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        }
    }
}

@Preview
@Composable
private fun CompactLaunchCountdownPreview() {
    SpaceLaunchNowPreviewTheme {
        CompactCountdownPreviewContent()
    }
}

@Preview
@Composable
private fun CompactLaunchCountdownDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        CompactCountdownPreviewContent()
    }
}
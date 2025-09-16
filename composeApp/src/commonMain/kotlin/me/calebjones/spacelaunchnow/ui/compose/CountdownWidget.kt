package me.calebjones.spacelaunchnow.ui.compose


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.coroutines.delay
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowTheme
import me.calebjones.spacelaunchnow.util.StatusColorUtil
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.abs
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

@Preview
@Composable
fun BuildLaunchCountdown() {
    SpaceLaunchNowTheme {
        Card {
            LaunchCountdown(
                launchTime = Clock.System.now().plus(6.hours),
            )
        }
    }
}


@Composable
fun LaunchCountdown(
    launchTime: Instant,
    statusId: Int? = null,
    statusName: String? = null
) {
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
                onClick = { /* Handle click */ },
                shape = RoundedCornerShape(25),
                modifier = Modifier
                    .align(Alignment.Center)
                    .shadow(8.dp, RoundedCornerShape(25.dp)),
                colors = StatusColorUtil.getLaunchStatusButtonColors(statusId)
            ) {
                Text(
                    text = statusName ?: "Unknown Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Countdown display
        CountdownDisplay(launchTime = launchTime)

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
private fun CountdownDisplay(launchTime: Instant) {
    val remainingTime = remember { mutableStateOf(launchTime - Clock.System.now()) }

    LaunchedEffect(launchTime) {
        while (true) {
            remainingTime.value = launchTime - Clock.System.now()
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
                CountdownItem(label = "DAYS", value = days, digitFontSize = digitFontSize, labelFontSize = labelFontSize)
                CountdownSeparator(fontSize = digitFontSize)
                CountdownItem(label = "HOURS", value = hours, digitFontSize = digitFontSize, labelFontSize = labelFontSize)
                CountdownSeparator(fontSize = digitFontSize)
                CountdownItem(label = "MINUTES", value = minutes, digitFontSize = digitFontSize, labelFontSize = labelFontSize)
                CountdownSeparator(fontSize = digitFontSize)
                CountdownItem(label = "SECONDS", value = seconds, digitFontSize = digitFontSize, labelFontSize = labelFontSize)
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
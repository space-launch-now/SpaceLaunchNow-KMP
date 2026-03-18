package me.calebjones.spacelaunchnow.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.calebjones.spacelaunchnow.PlatformType
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock.System

/**
 * Visual style for the device frame bezel.
 * Selected at runtime via [detectDeviceFrameStyle] — no expect/actual needed.
 */
sealed class DeviceFrameStyle {
    /** Android-style frame: rounded corners, pill-shaped camera cutout at top-center */
    data object Android : DeviceFrameStyle()

    /** iPhone-style frame: Dynamic Island notch at top-center */
    data object IPhone : DeviceFrameStyle()

    /** Generic rectangular frame for desktop previews */
    data object Generic : DeviceFrameStyle()
}

/** Maps the current runtime platform to the appropriate [DeviceFrameStyle]. */
fun detectDeviceFrameStyle(): DeviceFrameStyle = when (getPlatform().type) {
    PlatformType.ANDROID -> DeviceFrameStyle.Android
    PlatformType.IOS -> DeviceFrameStyle.IPhone
    PlatformType.DESKTOP -> DeviceFrameStyle.Generic
}

private val bezelColor = Color(0xFF1A1A1A)
private val statusBarColor = Color(0xFF111111)
private val statusBarTextColor = Color.White.copy(alpha = 0.8f)

/**
 * A composable device frame that wraps [content] inside a dark bezel with a status bar.
 *
 * The frame renders platform-specific details:
 * - **Android**: pill-shaped camera cutout centered at top
 * - **iPhone**: Dynamic Island notch centered at top
 * - **Generic**: plain rounded rectangle
 *
 * A live clock is displayed in the status bar, updated every 60 seconds.
 */
@Composable
fun DeviceFrame(
    modifier: Modifier = Modifier,
    style: DeviceFrameStyle = detectDeviceFrameStyle(),
    content: @Composable () -> Unit
) {
    // Live clock
    var currentTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val now = System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            currentTime =
                "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
            delay(60_000)
        }
    }

    val outerCornerRadius = when (style) {
        is DeviceFrameStyle.Android -> 32.dp
        is DeviceFrameStyle.IPhone -> 40.dp
        is DeviceFrameStyle.Generic -> 24.dp
    }
    val innerCornerRadius = when (style) {
        is DeviceFrameStyle.Android -> 24.dp
        is DeviceFrameStyle.IPhone -> 32.dp
        is DeviceFrameStyle.Generic -> 16.dp
    }

    // Outer bezel
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(outerCornerRadius))
            .background(bezelColor)
            .padding(6.dp)
    ) {
        // Inner screen area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(innerCornerRadius))
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Status bar
            StatusBar(
                time = currentTime,
                style = style,
                modifier = Modifier.fillMaxWidth()
            )

            // Content area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun StatusBar(
    time: String,
    style: DeviceFrameStyle,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(statusBarColor)
            .height(28.dp)
    ) {
        // Time on left
        Text(
            text = time,
            color = statusBarTextColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )

        // Platform-specific cutout at center
        when (style) {
            is DeviceFrameStyle.Android -> {
                // Pill camera cutout
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 6.dp)
                        .size(width = 28.dp, height = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(bezelColor)
                )
            }

            is DeviceFrameStyle.IPhone -> {
                // Dynamic Island
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 5.dp)
                        .size(width = 72.dp, height = 18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(bezelColor)
                )
            }

            is DeviceFrameStyle.Generic -> {
                // No cutout
            }
        }

        // Status indicators on right (battery/signal icons simplified as text)
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Signal bars (simplified)
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                for (i in 0 until 4) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height((4 + i * 2).dp)
                            .background(statusBarTextColor, RoundedCornerShape(1.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            // Battery indicator (simplified)
            Box(
                modifier = Modifier
                    .size(width = 18.dp, height = 9.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Transparent)
                    .padding(1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(1.dp))
                        .background(statusBarTextColor)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview
@Composable
private fun DeviceFrameAndroidPreview() {
    SpaceLaunchNowPreviewTheme {
        DeviceFrame(
            style = DeviceFrameStyle.Android,
            modifier = Modifier.size(width = 200.dp, height = 400.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text("Android Frame", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview
@Composable
private fun DeviceFrameAndroidDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        DeviceFrame(
            style = DeviceFrameStyle.Android,
            modifier = Modifier.size(width = 200.dp, height = 400.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text("Android Frame", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview
@Composable
private fun DeviceFrameIPhonePreview() {
    SpaceLaunchNowPreviewTheme {
        DeviceFrame(
            style = DeviceFrameStyle.IPhone,
            modifier = Modifier.size(width = 200.dp, height = 400.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text("iPhone Frame", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview
@Composable
private fun DeviceFrameIPhoneDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        DeviceFrame(
            style = DeviceFrameStyle.IPhone,
            modifier = Modifier.size(width = 200.dp, height = 400.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text("iPhone Frame", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

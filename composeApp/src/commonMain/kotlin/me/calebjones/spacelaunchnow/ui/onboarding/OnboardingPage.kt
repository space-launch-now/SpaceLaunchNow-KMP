package me.calebjones.spacelaunchnow.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.ui.layout.rememberAdaptiveLayoutState
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Data class for each onboarding carousel page's metadata.
 */
data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val pageType: OnboardingPageType
)

/**
 * Identifies the content for each onboarding page.
 */
enum class OnboardingPageType {
    WELCOME,
    LAUNCH_CARD,
    SCHEDULE,
    NEWS_EVENTS,
    EXPLORE,
    WIDGETS,
    NOTIFICATION_PERMISSION
}

private val spaceGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0A0E2A), Color(0xFF1A1040), Color(0xFF2A1060))
)

/**
 * A single onboarding page with a centered device frame, bold title, and lighter subtitle.
 *
 * The device frame occupies ~55% of the available height. Content rendered inside
 * the frame is scaled via [Modifier.graphicsLayer] and touch-disabled via
 * [Modifier.pointerInput] so the user swipes the carousel rather than interacting
 * with the embedded composable.
 *
 * @param title               Bold heading text displayed below the device frame.
 * @param subtitle            Lighter description text below the title.
 * @param icon                Optional icon displayed between the device frame and title.
 * @param modifier            Modifier applied to the outer container.
 * @param deviceFrameStyle    The style of the device frame bezel.
 * @param contentScale        Scale factor applied to the inner device frame content.
 * @param allowInteraction    When true, touch events pass through to child content (e.g. tabs).
 * @param deviceFrameContent  Composable lambda rendered inside the device frame.
 */
@Composable
fun OnboardingPage(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    deviceFrameStyle: DeviceFrameStyle = detectDeviceFrameStyle(),
    contentScale: Float = 0.85f,
    allowInteraction: Boolean = false,
    deviceFrameContent: @Composable () -> Unit
) {
    // Use wide (side-by-side) layout only when width is EXPANDED (≥840dp).
    // MEDIUM (600-840dp, e.g. tablet portrait) is too narrow for side-by-side.
    val useWideLayout = rememberAdaptiveLayoutState().isExpanded

    if (useWideLayout) {
        OnboardingPageWide(
            title = title,
            subtitle = subtitle,
            modifier = modifier,
            icon = icon,
            deviceFrameStyle = deviceFrameStyle,
            contentScale = contentScale,
            allowInteraction = allowInteraction,
            deviceFrameContent = deviceFrameContent
        )
    } else {
        OnboardingPageCompact(
            title = title,
            subtitle = subtitle,
            modifier = modifier,
            icon = icon,
            deviceFrameStyle = deviceFrameStyle,
            contentScale = contentScale,
            allowInteraction = allowInteraction,
            deviceFrameContent = deviceFrameContent
        )
    }
}

@Composable
private fun OnboardingPageWide(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    deviceFrameStyle: DeviceFrameStyle,
    contentScale: Float,
    allowInteraction: Boolean,
    deviceFrameContent: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(spaceGradient)
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Left: Device frame
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            DeviceFrame(
                style = deviceFrameStyle,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight(0.9f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = contentScale, scaleY = contentScale)
                        .then(
                            if (!allowInteraction) {
                                Modifier.pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            awaitPointerEvent()
                                        }
                                    }
                                }
                            } else Modifier
                        )
                ) {
                    deviceFrameContent()
                }
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Right: Text content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun OnboardingPageCompact(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    deviceFrameStyle: DeviceFrameStyle,
    contentScale: Float,
    allowInteraction: Boolean,
    deviceFrameContent: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(spaceGradient),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Device frame area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.50f),
            contentAlignment = Alignment.TopCenter
        ) {
            DeviceFrame(
                style = deviceFrameStyle,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = contentScale, scaleY = contentScale)
                        .then(
                            if (!allowInteraction) {
                                Modifier.pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            awaitPointerEvent()
                                        }
                                    }
                                }
                            } else Modifier
                        )
                ) {
                    deviceFrameContent()
                }
            }

            // Gradient overlay — covers the bottom third to hide bottom bezel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.5f to Color(0xFF1A1040).copy(alpha = 0.7f),
                                1.0f to Color(0xFF1A1040),
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Scrollable text section
        Column(
            modifier = Modifier
                .weight(0.1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icon
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview
@Composable
private fun OnboardingPagePreview() {
    SpaceLaunchNowPreviewTheme {
        OnboardingPage(
            title = "Track Every Launch",
            subtitle = "Get real-time updates on upcoming rocket launches from agencies worldwide.",
            deviceFrameStyle = DeviceFrameStyle.Android
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text("Launch Card Preview", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Preview
@Composable
private fun OnboardingPageDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        OnboardingPage(
            title = "Your Launch Schedule",
            subtitle = "Browse upcoming and previous launches in a clean, organized timeline.",
            deviceFrameStyle = DeviceFrameStyle.Android
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text("Schedule Preview", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

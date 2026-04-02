package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * LIVE indicator badge with pulsing animation.
 * 
 * Used to indicate that a launch is currently in flight.
 * The badge pulses with alpha animation to draw attention.
 */
@Composable
fun LiveIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_indicator")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_alpha"
    )

    // LIVE indicator red color
    val liveRed = Color(0xFFE53935)

    Surface(
        modifier = modifier.semantics {
            contentDescription = "Live broadcast indicator"
        },
        shape = RoundedCornerShape(4.dp),
        color = liveRed.copy(alpha = alpha)
    ) {
        Text(
            text = "LIVE",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// ========================================
// Previews
// ========================================

@Preview
@Composable
private fun LiveIndicatorPreview() {
    SpaceLaunchNowPreviewTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            LiveIndicator(modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview
@Composable
private fun LiveIndicatorDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            LiveIndicator(modifier = Modifier.padding(16.dp))
        }
    }
}

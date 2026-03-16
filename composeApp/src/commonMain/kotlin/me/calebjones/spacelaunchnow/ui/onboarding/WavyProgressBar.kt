package me.calebjones.spacelaunchnow.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.sin

/**
 * A wavy progress bar that animates as the user swipes through onboarding pages.
 *
 * - A **track line** spans the full width in a muted white.
 * - A **progress wave** fills from the left up to the current progress, drawn as a sine wave
 *   whose animated offset creates a gentle motion effect.
 *
 * @param currentPage      The zero-based index of the current page.
 * @param pageCount        Total number of pages.
 * @param pageOffsetFraction  The fractional offset from HorizontalPager (0f..1f between pages).
 * @param modifier         Modifier applied to the Canvas.
 * @param amplitude        Peak amplitude of the sine wave.
 * @param strokeWidth      Stroke width for both the track and the wave.
 * @param trackColor       Color of the full-width background track.
 * @param waveColor        Color of the filled progress wave.
 */
@Composable
fun WavyProgressBar(
    currentPage: Int,
    pageCount: Int,
    pageOffsetFraction: Float,
    modifier: Modifier = Modifier,
    amplitude: Dp = 6.dp,
    strokeWidth: Dp = 3.dp,
    trackColor: Color = Color.White.copy(alpha = 0.3f),
    waveColor: Color = Color.White
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(amplitude * 2 + strokeWidth)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2f
        val ampPx = amplitude.toPx()
        val strokePx = strokeWidth.toPx()

        // Progress as fraction of full width
        val progress = if (pageCount <= 1) 1f
        else ((currentPage + pageOffsetFraction) / (pageCount - 1)).coerceIn(0f, 1f)

        // Full-width straight track line
        drawLine(
            color = trackColor,
            start = Offset(0f, centerY),
            end = Offset(canvasWidth, centerY),
            strokeWidth = strokePx,
            cap = StrokeCap.Round
        )

        // Wavy progress fill
        if (progress > 0f) {
            val endX = canvasWidth * progress
            val wavePath = Path().apply {
                val step = 2f // pixel step for smooth curve
                var x = 0f
                // Frequency: ~2 full waves across the entire bar
                val frequency = (2f * PI.toFloat() * 2f) / canvasWidth

                moveTo(0f, centerY)
                while (x <= endX) {
                    val y = centerY + ampPx * sin(frequency * x).toFloat()
                    lineTo(x, y)
                    x += step
                }
            }

            drawPath(
                path = wavePath,
                color = waveColor,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview
@Composable
private fun WavyProgressBarPreview() {
    SpaceLaunchNowPreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0E2A))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            WavyProgressBar(
                currentPage = 1,
                pageCount = 4,
                pageOffsetFraction = 0.3f
            )
        }
    }
}

@Preview
@Composable
private fun WavyProgressBarDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0E2A))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            WavyProgressBar(
                currentPage = 2,
                pageCount = 4,
                pageOffsetFraction = 0f
            )
        }
    }
}

package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.util.DateTimeUtil

@Composable
fun LaunchWindowIndicator(
    launchTime: Instant,
    windowStart: Instant,
    windowEnd: Instant,
    modifier: Modifier = Modifier,
    baseBarColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
    windowBarColor: Color = MaterialTheme.colorScheme.primary,
    dotColor: Color = MaterialTheme.colorScheme.inversePrimary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    barThickness: Float = 8f
) {
    Column(
        modifier = modifier
    ) {
        // Title and duration row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Launch Window",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Window duration info aligned to the right
            val durationMinutes = (windowEnd.epochSeconds - windowStart.epochSeconds) / 60
            Text(
                text = if (durationMinutes > 0) "${durationMinutes} min" else "Instantaneous",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Timeline indicator
        LaunchWindowTimeline(
            launchTime = launchTime,
            windowStart = windowStart,
            windowEnd = windowEnd,
            baseBarColor = baseBarColor,
            windowBarColor = windowBarColor,
            dotColor = dotColor,
            textColor = textColor,
            barThickness = barThickness,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        )
    }
}

@Composable
private fun LaunchWindowTimeline(
    launchTime: Instant,
    windowStart: Instant,
    windowEnd: Instant,
    baseBarColor: Color,
    windowBarColor: Color,
    dotColor: Color,
    textColor: Color,
    barThickness: Float,
    modifier: Modifier = Modifier
) {    
    val textMeasurer = rememberTextMeasurer()
    val fontSize = MaterialTheme.typography.labelSmall.fontSize

    Canvas(modifier = modifier.padding(horizontal = 20.dp)) {
        val width = size.width
        val height = size.height
        val barY = height / 2
        
        // Calculate time range for display (extend beyond window by 20% on each side)
        val totalWindowDuration = windowEnd.epochSeconds - windowStart.epochSeconds
        val timelineStart = windowStart.epochSeconds - (totalWindowDuration * 0.2f)
        val timelineEnd = windowEnd.epochSeconds + (totalWindowDuration * 0.2f)
        
        // Calculate position ratios relative to extended timeline
        val startRatio = (windowStart.epochSeconds - timelineStart) / (timelineEnd - timelineStart)
        val endRatio = (windowEnd.epochSeconds - timelineStart) / (timelineEnd - timelineStart)  
        val launchRatio = (launchTime.epochSeconds - timelineStart) / (timelineEnd - timelineStart)        // Draw base bar with rounded ends (full timeline)
        drawLine(
            color = baseBarColor,
            start = Offset(0f, barY),
            end = Offset(width, barY),
            strokeWidth = barThickness,
            cap = StrokeCap.Round
        )

        // Draw window bar with enhanced thickness and gradient effect (active launch window)
        val windowStartX = startRatio.coerceIn(0f, 1f) * width
        val windowEndX = endRatio.coerceIn(0f, 1f) * width
        
        // Draw window bar with subtle enhancement
        drawLine(
            color = windowBarColor,
            start = Offset(windowStartX, barY),
            end = Offset(windowEndX, barY),
            strokeWidth = barThickness * 1.2f,
            cap = StrokeCap.Round
        )

        // Enhanced launch time indicator - more subtle but still prominent
        val dotX = launchRatio.coerceIn(0f, 1f) * width
        
        // Subtle outer glow
        drawCircle(
            color = dotColor.copy(alpha = 0.15f),
            radius = barThickness * 1.6f,
            center = Offset(dotX, barY)
        )
        
        // White highlight ring for contrast
        drawCircle(
            color = Color.White,
            radius = barThickness * 1.0f,
            center = Offset(dotX, barY)
        )
        
        // Main dot - slightly larger
        drawCircle(
            color = dotColor,
            radius = barThickness * 0.75f,
            center = Offset(dotX, barY)
        )        

        // Format times as HOUR:MIN per users locale
        val startText = DateTimeUtil.formatLaunchTime(windowStart)
        val endText = DateTimeUtil.formatLaunchTime(windowEnd)
        val launchText = DateTimeUtil.formatLaunchTime(launchTime)

        val textStyle = TextStyle(color = textColor, fontSize = fontSize)// Draw window start text
        val startTextLayout = textMeasurer.measure(startText, style = textStyle)
        // Ensure we have a valid range before coercing
        val startTextMaxX = (width - startTextLayout.size.width).coerceAtLeast(0f)
        val startX = (startRatio * width).coerceIn(0f, startTextMaxX)
        drawText(
            textLayoutResult = startTextLayout,
            topLeft = Offset(startX, barY + barThickness * 2f)
        )

        // Draw window end text
        val endTextLayout = textMeasurer.measure(endText, style = textStyle)
        // Ensure we have a valid range before coercing
        val endTextMaxX = (width - endTextLayout.size.width).coerceAtLeast(0f)
        val endX = (endRatio * width - endTextLayout.size.width).coerceIn(0f, endTextMaxX)
        drawText(
            textLayoutResult = endTextLayout,
            topLeft = Offset(endX, barY + barThickness * 2f)
        )
        
        // Draw expected launch time text if it's not too close to start/end
        val launchTextLayout = textMeasurer.measure(launchText, style = textStyle)
        // Ensure we have a valid range before coercing
        val launchTextMaxX = (width - launchTextLayout.size.width).coerceAtLeast(0f)
        val launchX = (launchRatio * width - launchTextLayout.size.width / 2).coerceIn(0f, launchTextMaxX)
        
        // Only draw if there's enough space between texts
        val minLaunchTextSpace = launchTextLayout.size.width * 1.5f
        val distanceToStart = kotlin.math.abs(launchX - startX)
        val distanceToEnd = kotlin.math.abs(launchX - endX)
        
        if (distanceToStart > minLaunchTextSpace && distanceToEnd > minLaunchTextSpace) {
            drawText(
                textLayoutResult = launchTextLayout,
                topLeft = Offset(launchX, barY + barThickness * 2f)
            )
        }
    }
}
package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import formatTimeForUser
import kotlinx.datetime.Instant

@Composable
fun LaunchWindowIndicator(
    launchTime: Instant,
    windowStart: Instant,
    windowEnd: Instant,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(80.dp),
    baseBarColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    windowBarColor: Color = MaterialTheme.colorScheme.primary,
    dotColor: Color = MaterialTheme.colorScheme.inversePrimary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    barThickness: Float = 12f
) {    
    val textMeasurer = rememberTextMeasurer()
    val fontSize = MaterialTheme.typography.labelMedium.fontSize

    Canvas(modifier = modifier.padding(horizontal = 16.dp)) {
        val width = size.width
        val height = size.height
        val barY = height / 2
        
        // Calculate time range for display (extend beyond window by 20% on each side)
        val totalWindowDuration = windowEnd.epochSeconds - windowStart.epochSeconds
        val extendedDuration = totalWindowDuration * 1.4f // Add 20% margin on each side
        val timelineStart = windowStart.epochSeconds - (totalWindowDuration * 0.2f)
        val timelineEnd = windowEnd.epochSeconds + (totalWindowDuration * 0.2f)
        
        // Calculate position ratios relative to extended timeline
        val startRatio = (windowStart.epochSeconds - timelineStart) / (timelineEnd - timelineStart)
        val endRatio = (windowEnd.epochSeconds - timelineStart) / (timelineEnd - timelineStart)  
        val launchRatio = (launchTime.epochSeconds - timelineStart) / (timelineEnd - timelineStart)        // Draw base bar (full timeline)
        drawLine(
            color = baseBarColor,
            start = Offset(0f, barY),
            end = Offset(width, barY),
            strokeWidth = barThickness,
            cap = StrokeCap.Round
        )

        // Draw window bar (active launch window)
        drawLine(
            color = windowBarColor,
            start = Offset(startRatio.coerceIn(0f, 1f) * width, barY),
            end = Offset(endRatio.coerceIn(0f, 1f) * width, barY),
            strokeWidth = barThickness,
            cap = StrokeCap.Round
        )

        // Draw launch time indicator
        val dotX = launchRatio.coerceIn(0f, 1f) * width
        // Draw a white highlight circle around the dot
        drawCircle(
            color = Color.White,
            radius = barThickness * 1.2f,
            center = Offset(dotX, barY)
        )
        // Draw the center dot
        drawCircle(
            color = dotColor,
            radius = barThickness * 0.8f,
            center = Offset(dotX, barY)
        )        

        // Format times as HOUR:MIN per users locale
        val startText = formatTimeForUser(windowStart)
        val endText = formatTimeForUser(windowEnd)
        val launchText = formatTimeForUser(launchTime)

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
                topLeft = Offset(launchX, barY - barThickness * 3f - launchTextLayout.size.height)
            )
        }
    }
}
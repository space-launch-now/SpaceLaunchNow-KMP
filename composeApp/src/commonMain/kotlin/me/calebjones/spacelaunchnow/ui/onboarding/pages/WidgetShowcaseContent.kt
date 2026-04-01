package me.calebjones.spacelaunchnow.ui.onboarding.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

private val WidgetDark = Color(0xFF1C1C1E)
private val WidgetSurface = Color(0xFF2C2C2E)
private val WidgetAccent = Color(0xFFFF9500)
private val WidgetTextPrimary = Color.White
private val WidgetTextSecondary = Color.White.copy(alpha = 0.7f)
private val PremiumGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF9500), Color(0xFFFF6B00))
)

/**
 * iOS widget showcase — single medium home screen widget mockup.
 */
@Composable
fun IOSWidgetShowcaseContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        HomeScreenWidgetMediumMockup()
    }
}

/**
 * Android widget showcase — launch list widget mockup matching the actual Glance widget.
 */
@Composable
fun AndroidWidgetShowcaseContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidLaunchListWidgetMockup()
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = WidgetTextSecondary,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp
    )
}

/**
 * Mockup of the iOS accessory rectangular lock screen widget.
 * Shows launch name, countdown, and agency in a compact horizontal layout.
 */
@Composable
fun LockScreenWidgetMockup(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WidgetSurface.copy(alpha = 0.8f))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Falcon 9 Block 5",
                style = MaterialTheme.typography.bodySmall,
                color = WidgetTextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = WidgetAccent
                )
                Text(
                    text = "2d 5hr",
                    style = MaterialTheme.typography.labelSmall,
                    color = WidgetAccent,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "SpaceX",
                style = MaterialTheme.typography.labelSmall,
                color = WidgetTextSecondary
            )
        }
    }
}

/**
 * Mockup of the iOS system small home screen widget.
 * Shows a compact tile with status indicator, launch name, and countdown.
 */
@Composable
fun HomeScreenWidgetSmallMockup(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(150.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(WidgetDark)
            .padding(14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.matchParentSize()
        ) {
            // Status row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = WidgetAccent
                )
                Text(
                    text = "Go",
                    style = MaterialTheme.typography.labelSmall,
                    color = WidgetAccent,
                    fontWeight = FontWeight.Medium
                )
            }

            // Launch name
            Text(
                text = "Starlink Group 12-5",
                style = MaterialTheme.typography.bodySmall,
                color = WidgetTextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Countdown
            Text(
                text = "2d 5hr",
                style = MaterialTheme.typography.titleMedium,
                color = WidgetTextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Mockup of the iOS system medium home screen widget.
 * Matches the actual SpaceLaunchNow iOS "Next Launch" widget:
 * image on left, title + status badge + countdown + agency on right.
 */
@Composable
fun HomeScreenWidgetMediumMockup(modifier: Modifier = Modifier) {
    val statusGreen = Color(0xFF34C759)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(WidgetDark)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Text(
                text = "NEXT LAUNCH",
                style = MaterialTheme.typography.labelSmall,
                color = WidgetTextSecondary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                fontSize = 9.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Image placeholder
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(WidgetSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = WidgetAccent.copy(alpha = 0.6f)
                    )
                }

                // Details
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Launch name
                    Text(
                        text = "SLS Block 1 | Artemis II",
                        style = MaterialTheme.typography.bodySmall,
                        color = WidgetTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Status badge (pill)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusGreen)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "GO FOR LAUNCH",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp
                        )
                    }

                    // Countdown
                    Text(
                        text = "1 hr, 34 min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WidgetAccent,
                        fontWeight = FontWeight.Bold
                    )

                    // Agency + Location
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = WidgetTextSecondary
                        )
                        Text(
                            text = "NASA \u00B7 Kennedy Space Ce\u2026",
                            style = MaterialTheme.typography.labelSmall,
                            color = WidgetTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Android Launch List Widget Mockup
// ---------------------------------------------------------------------------

/**
 * Mockup of the Android Glance launch list widget.
 * Shows a header and a short list of upcoming launches with agency, location,
 * countdown, and status — matching the real widget's appearance.
 */
@Composable
fun AndroidLaunchListWidgetMockup(modifier: Modifier = Modifier) {
    val widgetGreen = Color(0xFF4CAF50)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(WidgetDark)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Upcoming Launches",
            style = MaterialTheme.typography.bodyMedium,
            color = widgetGreen,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Launch items
        AndroidWidgetLaunchItem(
            name = "SLS Block 1 | Artemis II",
            agency = "National Aeronautics and Space Administration",
            location = "Kennedy Space Center, FL, USA",
            countdown = "1h 24m",
            status = "Go for Launch",
            accentColor = widgetGreen
        )
        AndroidWidgetLaunchItem(
            name = "Falcon 9 Block 5 | Starlink Group 10-58",
            agency = "SpaceX",
            location = "Cape Canaveral SFS, FL, USA",
            countdown = "14h 52m",
            status = "Go for Launch",
            accentColor = widgetGreen
        )
        AndroidWidgetLaunchItem(
            name = "Falcon 9 Block 5 | Starlink Group 17-35",
            agency = "SpaceX",
            location = "Vandenberg SFB, CA, USA",
            countdown = "1d 2h",
            status = "Go for Launch",
            accentColor = widgetGreen
        )
        AndroidWidgetLaunchItem(
            name = "Tianlong-3 | Demo Flight",
            agency = "Space Pioneer",
            location = "Jiuquan Satellite Launch Center, People's Republic of China",
            countdown = "1d 7h",
            status = "Go for Launch",
            accentColor = widgetGreen
        )
    }
}

@Composable
private fun AndroidWidgetLaunchItem(
    name: String,
    agency: String,
    location: String,
    countdown: String,
    status: String,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = WidgetTextPrimary,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = agency,
            style = MaterialTheme.typography.labelSmall,
            color = WidgetTextSecondary,
            maxLines = 1
        )
        Text(
            text = location,
            style = MaterialTheme.typography.labelSmall,
            color = WidgetTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = countdown,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview
@Composable
private fun IOSWidgetShowcaseContentPreview() {
    SpaceLaunchNowPreviewTheme {
        IOSWidgetShowcaseContent()
    }
}

@Preview
@Composable
private fun IOSWidgetShowcaseContentDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        IOSWidgetShowcaseContent()
    }
}

@Preview
@Composable
private fun AndroidWidgetShowcaseContentPreview() {
    SpaceLaunchNowPreviewTheme {
        AndroidWidgetShowcaseContent()
    }
}

@Preview
@Composable
private fun AndroidWidgetShowcaseContentDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        AndroidWidgetShowcaseContent()
    }
}

package me.calebjones.spacelaunchnow.ui.onboarding.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
 * Full widget showcase layout displaying lock screen and home screen widget mockups.
 * Uses static mock data to approximate the appearance of iOS WidgetKit widgets.
 */
@Composable
fun WidgetShowcaseContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF000000))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Lock Screen section
        SectionLabel(text = "Lock Screen")
        LockScreenWidgetMockup()

        Spacer(modifier = Modifier.height(4.dp))

        // Home Screen section
        SectionLabel(text = "Home Screen")
        HomeScreenWidgetSmallMockup()

        Spacer(modifier = Modifier.height(4.dp))

        HomeScreenWidgetMediumMockup()
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
 * Shows image placeholder on left, launch details on right.
 */
@Composable
fun HomeScreenWidgetMediumMockup(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(WidgetDark)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
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
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    // Header
                    Text(
                        text = "NEXT LAUNCH",
                        style = MaterialTheme.typography.labelSmall,
                        color = WidgetTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp,
                        fontSize = 9.sp
                    )

                    // Launch name
                    Text(
                        text = "SpaceX | Falcon 9 Block 5",
                        style = MaterialTheme.typography.bodySmall,
                        color = WidgetTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status + Countdown
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(WidgetAccent)
                        )
                        Text(
                            text = "Go",
                            style = MaterialTheme.typography.labelSmall,
                            color = WidgetAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Countdown
                    Text(
                        text = "2d 5hr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WidgetTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview
@Composable
private fun WidgetShowcaseContentPreview() {
    SpaceLaunchNowPreviewTheme {
        WidgetShowcaseContent()
    }
}

@Preview
@Composable
private fun WidgetShowcaseContentDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        WidgetShowcaseContent()
    }
}

@Preview
@Composable
private fun LockScreenWidgetMockupPreview() {
    SpaceLaunchNowPreviewTheme {
        LockScreenWidgetMockup(modifier = Modifier.width(200.dp))
    }
}

@Preview
@Composable
private fun HomeScreenWidgetSmallMockupPreview() {
    SpaceLaunchNowPreviewTheme {
        HomeScreenWidgetSmallMockup()
    }
}

@Preview
@Composable
private fun HomeScreenWidgetMediumMockupPreview() {
    SpaceLaunchNowPreviewTheme {
        HomeScreenWidgetMediumMockup(modifier = Modifier.width(320.dp))
    }
}

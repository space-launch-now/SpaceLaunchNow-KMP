package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import me.calebjones.spacelaunchnow.navigation.NotificationSettings
import me.calebjones.spacelaunchnow.navigation.SupportUs
import me.calebjones.spacelaunchnow.ui.platformShadowGlow
import me.calebjones.spacelaunchnow.ui.subscription.rememberIsPremium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(navController: NavController) {
    val isPremium by rememberIsPremium()

    TopAppBar(
        windowInsets = WindowInsets(0, 0, 0, 0),
        title = {
            Text(
                text = "Space Launch Now",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            if (!isPremium) {
                SupportUsButton(onClick = { navController.navigate(SupportUs) })
            }
            IconButton(
                onClick = {
                    navController.navigate(NotificationSettings)
                },
            ) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "Filters",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}

/**
 * Heart icon that beats on a "lub-dub" rhythm with a long rest, mimicking an actual
 * heartbeat. Layered with a primary-colored breathing glow (compose-ShadowGlow on
 * Android, no-op elsewhere) on the same 5600ms period so the two rhythms share a
 * cadence rather than drifting in and out of phase.
 */
@Composable
private fun SupportUsButton(onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "support-heartbeat")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 30000
                1f at 0 using FastOutSlowInEasing
                1.22f at 140 using FastOutSlowInEasing   // lub
                1f at 280 using FastOutSlowInEasing
                1.14f at 400 using FastOutSlowInEasing   // dub
                1f at 540 using FastOutSlowInEasing
                1f at 30000                               // long rest
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "support-heartbeat-scale"
    )

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Box(contentAlignment = Alignment.Center) {
        // Breathing glow disc behind the heart. Clipped to a circle so the
        // shadowGlow draws around a round shape that matches the IconButton's
        // ripple footprint.
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .platformShadowGlow(
                    gradientColors = listOf(
                        primary.copy(alpha = 0.45f),
                        tertiary.copy(alpha = 0.35f)
                    ),
                    borderRadius = 18.dp,
                    blurRadius = 14.dp,
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                    spread = 2.dp,
                    enableBreathingEffect = true,
                    breathingEffectIntensity = 6.dp,
                    breathingDurationMillis = 5600
                )
        )

        IconButton(onClick = onClick) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = "Support Me",
                tint = primary,
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale)
            )
        }
    }
}

package me.calebjones.spacelaunchnow.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Platform-specific shadow glow modifier.
 * On Android, uses the compose-ShadowGlow library for advanced drop shadows.
 * On other platforms, returns the modifier unchanged.
 */
expect fun Modifier.platformShadowGlow(
    gradientColors: List<Color>,
    borderRadius: Dp = 0.dp,
    blurRadius: Dp = 8.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 4.dp,
    spread: Dp = 0.dp,
    enableBreathingEffect: Boolean = false,
    breathingEffectIntensity: Dp = 4.dp,
    breathingDurationMillis: Int = 1500
): Modifier

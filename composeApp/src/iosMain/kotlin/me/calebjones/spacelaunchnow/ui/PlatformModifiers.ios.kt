package me.calebjones.spacelaunchnow.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * iOS implementation of platform-specific shadow glow modifier.
 * Currently returns the modifier unchanged as shadowGlow is Android-specific.
 */
actual fun Modifier.platformShadowGlow(
    gradientColors: List<Color>,
    borderRadius: Dp,
    blurRadius: Dp,
    offsetX: Dp,
    offsetY: Dp,
    spread: Dp,
    enableBreathingEffect: Boolean,
    breathingEffectIntensity: Dp,
    breathingDurationMillis: Int
): Modifier = this

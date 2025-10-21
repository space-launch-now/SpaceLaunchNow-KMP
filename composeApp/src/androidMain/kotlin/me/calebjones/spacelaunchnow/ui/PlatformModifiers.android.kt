package me.calebjones.spacelaunchnow.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.trishiraj.shadowglow.shadowGlow

/**
 * Android implementation of platform-specific shadow glow modifier.
 * Uses the compose-ShadowGlow library for advanced drop shadows with breathing effect.
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
): Modifier = this.shadowGlow(
    gradientColors = gradientColors,
    borderRadius = borderRadius,
    blurRadius = blurRadius,
    offsetX = offsetX,
    offsetY = offsetY,
    spread = spread,
    enableBreathingEffect = enableBreathingEffect,
    breathingEffectIntensity = breathingEffectIntensity,
    breathingDurationMillis = breathingDurationMillis
)

package me.calebjones.spacelaunchnow.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

// Space Launch Now brand palette – high-contrast for small AMOLED screens
private val SLNPrimary = Color(0xFF90CAF9)          // Light blue – primary actions & countdown
private val SLNOnPrimary = Color(0xFF003258)
private val SLNPrimaryContainer = Color(0xFF004A77)
private val SLNOnPrimaryContainer = Color(0xFFCDE5FF)

private val SLNSecondary = Color(0xFFFFB74D)         // Amber – status badges
private val SLNOnSecondary = Color(0xFF452B00)
private val SLNSecondaryContainer = Color(0xFF633F00)
private val SLNOnSecondaryContainer = Color(0xFFFFDDB3)

private val SLNTertiary = Color(0xFF81C784)          // Green – "Go" status
private val SLNOnTertiary = Color(0xFF003919)
private val SLNTertiaryContainer = Color(0xFF005227)
private val SLNOnTertiaryContainer = Color(0xFFA5F0A8)

private val SLNBackground = Color(0xFF0E1114)        // Near-black
private val SLNOnBackground = Color(0xFFE2E2E6)
private val SLNOnSurface = Color(0xFFE2E2E6)
private val SLNSurfaceContainerLow = Color(0xFF1A1C20)
private val SLNSurfaceContainer = Color(0xFF1E2125)
private val SLNSurfaceContainerHigh = Color(0xFF282B30)
private val SLNOnSurfaceVariant = Color(0xFFC3C6CF)
private val SLNOutline = Color(0xFF8D9199)
private val SLNOutlineVariant = Color(0xFF43474E)
private val SLNError = Color(0xFFFFB4AB)
private val SLNOnError = Color(0xFF690005)

private val SpaceLaunchNowColorScheme = ColorScheme(
    primary = SLNPrimary,
    onPrimary = SLNOnPrimary,
    primaryContainer = SLNPrimaryContainer,
    onPrimaryContainer = SLNOnPrimaryContainer,
    secondary = SLNSecondary,
    onSecondary = SLNOnSecondary,
    secondaryContainer = SLNSecondaryContainer,
    onSecondaryContainer = SLNOnSecondaryContainer,
    tertiary = SLNTertiary,
    onTertiary = SLNOnTertiary,
    tertiaryContainer = SLNTertiaryContainer,
    onTertiaryContainer = SLNOnTertiaryContainer,
    background = SLNBackground,
    onBackground = SLNOnBackground,
    onSurface = SLNOnSurface,
    surfaceContainerLow = SLNSurfaceContainerLow,
    surfaceContainer = SLNSurfaceContainer,
    surfaceContainerHigh = SLNSurfaceContainerHigh,
    onSurfaceVariant = SLNOnSurfaceVariant,
    outline = SLNOutline,
    outlineVariant = SLNOutlineVariant,
    error = SLNError,
    onError = SLNOnError,
)

@Composable
fun WearTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SpaceLaunchNowColorScheme,
        content = content,
    )
}

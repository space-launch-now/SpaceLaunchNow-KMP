package me.calebjones.spacelaunchnow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import com.moriatsushi.insetsx.rememberWindowInsetsController

// Define your color schemes
private val DarkColorScheme = darkColorScheme(
//    primary = Color(0xFFBB86FC),
//    onPrimary = Color.Black,
//    secondary = Color(0xFF03DAC6),
//    onSecondary = Color.Black,
//    background = Color(0xFF121212),
//    onBackground = Color.White,
//    surface = Color(0xFF1F1B24),
//    onSurface = Color.White,
    // Add other color roles as needed
)

private val LightColorScheme = lightColorScheme(
//    primary = Color(0xFF6200EE),
//    onPrimary = Color.White,
//    secondary = Color(0xFF03DAC6),
//    onSecondary = Color.Black,
//    background = Color(0xFFFFFFFF),
//    onBackground = Color.Black,
//    surface = Color(0xFFFFFFFF),
//    onSurface = Color.Black,
    // Add other color roles as needed
)

// Theme Composable
@Composable
fun SpaceLaunchNowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Optionally, add dynamic color support for Android 12+
    dynamicColorImage: String? = null,
    content: @Composable () -> Unit
) {
    // Base color scheme based on dark/light theme
    val baseColorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
    
    val windowInsetsController = rememberWindowInsetsController()

    LaunchedEffect(darkTheme) {
        // The status bars icon + content will change to a light color
        windowInsetsController?.setStatusBarContentColor(dark = !darkTheme)
        // The navigation bars icons will change to a light color (android only)
        windowInsetsController?.setNavigationBarsContentColor(dark = !darkTheme)
    }

    // Apply the Material3 Theme
    MaterialTheme(
        colorScheme = baseColorScheme,
        typography = Typography, // Define your typography
        shapes = Shapes,         // Define your shapes
        content = content
    )
}
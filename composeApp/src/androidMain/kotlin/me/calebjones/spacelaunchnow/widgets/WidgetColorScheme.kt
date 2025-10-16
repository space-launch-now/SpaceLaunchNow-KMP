package me.calebjones.spacelaunchnow.widgets

import android.content.Context
import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProviders
import androidx.glance.material3.ColorProviders
import me.calebjones.spacelaunchnow.Primary

/**
 * Default light color scheme for widgets.
 * Uses Material 3 baseline colors with app's primary color.
 */
val WidgetLightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7E0EC),
    onPrimaryContainer = Color(0xFF1C1B1F),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1E192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
)

/**
 * Default dark color scheme for widgets.
 * Uses Material 3 baseline dark colors with app's primary color.
 */
val WidgetDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
)

/**
 * Glance ColorProviders for widgets.
 * Provides both light and dark color schemes that adapt to system theme.
 */
object WidgetGlanceColorScheme {

    /**
     * Default color providers using app's color scheme.
     */
    val colors = ColorProviders(
        light = WidgetLightColorScheme,
        dark = WidgetDarkColorScheme
    )

    /**
     * Get dynamic color providers based on theme source.
     * For DYNAMIC_COLORS, uses Material You colors on Android 12+.
     */
    fun getColorProviders(context: Context, useDynamicColors: Boolean): ColorProviders {
        return if (useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Use Material You dynamic colors from wallpaper
            ColorProviders(
                light = dynamicLightColorScheme(context),
                dark = dynamicDarkColorScheme(context)
            )
        } else {
            // Use default app color scheme
            colors
        }
    }

    /**
     * Get ColorProviders with alpha applied to surface colors.
     * This creates new color schemes with the specified alpha for backgrounds.
     * 
     * @param context Android context
     * @param useDynamicColors Whether to use Material You dynamic colors
     * @param alpha Alpha transparency for surface colors (0.0 - 1.0)
     * @param appThemeMode App's theme preference: "System", "Light", or "Dark" (only used when not using dynamic colors)
     */
    fun getColorProvidersWithAlpha(
        context: Context, 
        useDynamicColors: Boolean, 
        alpha: Float,
        appThemeMode: String = "System"
    ): ColorProviders {
        // Get base schemes
        val lightScheme = if (useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicLightColorScheme(context)
        } else {
            WidgetLightColorScheme
        }
        
        val darkScheme = if (useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(context)
        } else {
            WidgetDarkColorScheme
        }
        
        // Create new schemes with alpha applied to surface
        val lightWithAlpha = lightScheme.copy(
            surface = lightScheme.surface.copy(alpha = alpha),
            surfaceVariant = lightScheme.surfaceVariant.copy(alpha = alpha)
        )
        
        val darkWithAlpha = darkScheme.copy(
            surface = darkScheme.surface.copy(alpha = alpha),
            surfaceVariant = darkScheme.surfaceVariant.copy(alpha = alpha)
        )
        
        // For FOLLOW_APP_THEME with forced light/dark, use same scheme for both
        return when (appThemeMode) {
            "Light" -> ColorProviders(light = lightWithAlpha, dark = lightWithAlpha)
            "Dark" -> ColorProviders(light = darkWithAlpha, dark = darkWithAlpha)
            else -> ColorProviders(light = lightWithAlpha, dark = darkWithAlpha) // "System" - respects OS
        }
    }
}

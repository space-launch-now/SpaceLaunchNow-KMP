package me.calebjones.spacelaunchnow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicMaterialThemeState
import me.calebjones.spacelaunchnow.Primary
import me.calebjones.spacelaunchnow.data.storage.ThemePreferences
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption
import org.koin.compose.koinInject

@Composable
fun SpaceLaunchNowTheme(
    themeOption: ThemeOption = ThemeOption.System,
    content: @Composable () -> Unit,
) {
    val themePreferences = koinInject<ThemePreferences>()
    
    // Collect custom theme preferences (premium feature)
    val customPrimaryColor by themePreferences.customPrimaryColorFlow.collectAsState(initial = null)
    val customSecondaryColor by themePreferences.customSecondaryColorFlow.collectAsState(initial = null)
    val paletteStyleName by themePreferences.paletteStyleFlow.collectAsState(initial = null)
    
    val isDarkTheme = when (themeOption) {
        ThemeOption.System -> isSystemInDarkTheme()
        ThemeOption.Light -> false
        ThemeOption.Dark -> true
    }

    // Use custom colors if set (premium feature), otherwise use defaults
    val seedColor = customPrimaryColor?.let { Color(it.toULong()) } ?: Primary
    
    // Convert palette style name to enum
    val paletteStyle = when (paletteStyleName) {
        "TonalSpot" -> PaletteStyle.TonalSpot
        "Neutral" -> PaletteStyle.Neutral
        "Vibrant" -> PaletteStyle.Vibrant
        "Expressive" -> PaletteStyle.Expressive
        "Rainbow" -> PaletteStyle.Rainbow
        "FruitSalad" -> PaletteStyle.FruitSalad
        "Monochrome" -> PaletteStyle.Monochrome
        "Fidelity" -> PaletteStyle.Fidelity
        "Content" -> PaletteStyle.Content
        else -> if (isDarkTheme) PaletteStyle.Vibrant else PaletteStyle.Fidelity
    }

    val dynamicThemeState = rememberDynamicMaterialThemeState(
        isDark = isDarkTheme,
        style = paletteStyle,
        contrastLevel = 0.3,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        seedColor = seedColor
    )

    DynamicMaterialTheme(
        state = dynamicThemeState,
        animate = true,
        content = content,
    )
}

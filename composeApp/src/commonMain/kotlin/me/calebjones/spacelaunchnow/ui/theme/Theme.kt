package me.calebjones.spacelaunchnow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicMaterialThemeState
import me.calebjones.spacelaunchnow.Primary
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption

@Composable
fun SpaceLaunchNowTheme(
    themeOption: ThemeOption = ThemeOption.System,
    content: @Composable () -> Unit,
) {
    val isDarkTheme = when (themeOption) {
        ThemeOption.System -> isSystemInDarkTheme()
        ThemeOption.Light -> false
        ThemeOption.Dark -> true
    }

    val dynamicThemeState = rememberDynamicMaterialThemeState(
        isDark = isDarkTheme,
        style = if (isDarkTheme) PaletteStyle.Vibrant else PaletteStyle.Fidelity,
        contrastLevel = 0.3,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        seedColor = Primary
    )

    DynamicMaterialTheme(
        state = dynamicThemeState,
        animate = true,
        content = content,
    )
}

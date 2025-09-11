package me.calebjones.spacelaunchnow.ui.theme

import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicMaterialThemeState
import me.calebjones.spacelaunchnow.Primary
import me.calebjones.spacelaunchnow.Secondary
import androidx.compose.runtime.Composable
import com.materialkolor.dynamiccolor.ColorSpec

@Composable
fun SpaceLaunchNowTheme(
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dynamicThemeState = rememberDynamicMaterialThemeState(
        isDark = isDarkTheme,
        style = PaletteStyle.Vibrant,
        contrastLevel = 0.1,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        primary = Primary,
        secondary = Secondary,
    )
    
    DynamicMaterialTheme(
        state = dynamicThemeState,
        animate = true,
        content = content,
    )
}

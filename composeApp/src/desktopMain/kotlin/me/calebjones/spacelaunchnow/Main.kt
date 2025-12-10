package me.calebjones.spacelaunchnow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.di.koinConfig
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption
import me.calebjones.spacelaunchnow.util.initializeBuildConfig
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform.getKoin

fun main() {
    // Initialize BuildConfig to set DEBUG flag
    initializeBuildConfig()

    // Initialize Koin before starting the app (similar to Android)
    startKoin(koinConfig)
    
    application {
        Window(
            title = "Space Launch Now",
            onCloseRequest = ::exitApplication
        ) {
            DesktopApp()
        }
    }
}

@Composable
private fun DesktopApp() {
    val appPreferences = getKoin().get<AppPreferences>()
    val themeOption by appPreferences.themeFlow.collectAsState(initial = ThemeOption.System)
    val useUtc by appPreferences.useUtcFlow.collectAsState(initial = false)
    
    SpaceLaunchNowApp(
        contextFactory = me.calebjones.spacelaunchnow.platform.ContextFactory(null),
        themeOption = themeOption,
        useUtc = useUtc
    )
}
package me.calebjones.spacelaunchnow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import java.awt.Dimension
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
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

    // Initialize SpaceLogger with LoggingPreferences to enable dynamic severity updates
    try {
        val loggingPrefs =
            getKoin().get<me.calebjones.spacelaunchnow.util.logging.LoggingPreferences>()
        me.calebjones.spacelaunchnow.util.logging.SpaceLogger.initialize(loggingPreferences = loggingPrefs)
    } catch (e: Exception) {
        // Logging not critical for Desktop
    }

    application {
        val windowState = rememberWindowState(
            size = DpSize(1280.dp, 900.dp)
        )
        Window(
            title = "Space Launch Now",
            onCloseRequest = ::exitApplication,
            state = windowState
        ) {
            LaunchedEffect(Unit) {
                window.minimumSize = Dimension(800, 600)
            }
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
        contextFactory = me.calebjones.spacelaunchnow.platform.ContextFactory(),
        themeOption = themeOption,
        useUtc = useUtc
    )
}
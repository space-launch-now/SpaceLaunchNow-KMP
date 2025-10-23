package me.calebjones.spacelaunchnow

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin
import me.calebjones.spacelaunchnow.di.koinConfig
import me.calebjones.spacelaunchnow.util.initializeBuildConfig

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
            SpaceLaunchNowApp(
                contextFactory = me.calebjones.spacelaunchnow.platform.ContextFactory(null)
            )
        }
    }
}
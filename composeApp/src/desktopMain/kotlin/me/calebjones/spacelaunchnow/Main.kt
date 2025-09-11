package me.calebjones.spacelaunchnow

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin
import me.calebjones.spacelaunchnow.di.koinConfig

fun main() {
    // Initialize Koin before starting the app (similar to Android)
    startKoin(koinConfig)
    
    application {
        Window(
            title = "Space Launch Now",
            onCloseRequest = ::exitApplication
        ) {
            SpaceLaunchNowApp()
        }
    }
}
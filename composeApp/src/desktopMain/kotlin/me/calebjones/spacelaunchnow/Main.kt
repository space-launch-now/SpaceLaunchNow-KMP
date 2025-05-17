package me.calebjones.spacelaunchnow

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() {
    application {
        Window(
            title = "Space Launch Now",
            onCloseRequest = ::exitApplication
        ) {
            SpaceLaunchNowApp()
        }
    }
}
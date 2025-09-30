package me.calebjones.spacelaunchnow

import androidx.compose.ui.window.ComposeUIViewController
import me.calebjones.spacelaunchnow.util.initializeBuildConfig

fun MainViewController() = ComposeUIViewController {
    // Initialize BuildConfig to set DEBUG flag
    initializeBuildConfig()

    SpaceLaunchNowApp()
}
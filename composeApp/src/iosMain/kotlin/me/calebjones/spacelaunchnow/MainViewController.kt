package me.calebjones.spacelaunchnow

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.ComposeUIViewController
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.di.koinConfig
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption
import me.calebjones.spacelaunchnow.util.initializeBuildConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

private var koinInitialized = false

// Shared state for navigation from iOS
private val navigationDestinationState = mutableStateOf<String?>(null)

// Public function for iOS to trigger navigation
fun setNavigationDestination(destination: String?) {
    navigationDestinationState.value = destination
}

fun MainViewController() = ComposeUIViewController { 
    // Initialize BuildConfig and Koin once before the app starts
    if (!koinInitialized) {
        initializeBuildConfig()
        startKoin(koinConfig)
        koinInitialized = true
    }
    
    val navigationDestination by navigationDestinationState
    
    SpaceLaunchNowApp(
        navigationDestination = navigationDestination,
        onNavigationDestinationConsumed = {
            navigationDestinationState.value = null
        }
    ) 
}
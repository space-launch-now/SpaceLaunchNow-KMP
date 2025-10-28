package me.calebjones.spacelaunchnow

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.billing.RevenueCatManager
import me.calebjones.spacelaunchnow.di.koinConfig
import me.calebjones.spacelaunchnow.util.initializeBuildConfig
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform.getKoin

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
        
        // Initialize RevenueCat immediately after Koin is ready (similar to Android)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val revenueCatManager = getKoin().get<RevenueCatManager>()
                revenueCatManager.initialize(appUserId = null)
                println("iOS: RevenueCat initialized successfully")
            } catch (e: Exception) {
                println("iOS: Failed to initialize RevenueCat - ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    val navigationDestination by navigationDestinationState
    
    SpaceLaunchNowApp(
        contextFactory = me.calebjones.spacelaunchnow.platform.ContextFactory(),
        navigationDestination = navigationDestination,
        onNavigationDestinationConsumed = {
            navigationDestinationState.value = null
        }
    ) 
}

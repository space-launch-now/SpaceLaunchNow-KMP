package me.calebjones.spacelaunchnow

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.billing.BillingManager
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.subscription.SubscriptionSyncer
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
        
        // Initialize Billing and Subscription system on background thread
        CoroutineScope(Dispatchers.Default).launch {
            try {
                println("iOS: 🚀 Starting billing and subscription initialization...")
                
                // Step 1: Initialize BillingManager (RevenueCat)
                val billingManager = getKoin().get<BillingManager>()
                billingManager.initialize(appUserId = null)
                println("iOS: ✅ BillingManager initialized successfully")
                
                // Step 2: Initialize and start SubscriptionSyncer
                // This listens to billing state changes and persists to LocalSubscriptionStorage
                val syncer = getKoin().get<SubscriptionSyncer>()
                syncer.startSyncing()
                println("iOS: ✅ SubscriptionSyncer started successfully")
                
                // Step 3: Initialize SubscriptionRepository (loads cached state)
                val repository = getKoin().get<SubscriptionRepository>()
                repository.initialize()
                println("iOS: ✅ SubscriptionRepository initialized successfully")
                
                // Step 4: Force initial sync to ensure purchase state is persisted
                syncer.syncNow()
                println("iOS: ✅ Initial subscription sync complete")
                
                println("iOS: 🎉 All billing and subscription systems initialized")
            } catch (e: Exception) {
                println("iOS: ❌ Failed to initialize billing/subscription system - ${e.message}")
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

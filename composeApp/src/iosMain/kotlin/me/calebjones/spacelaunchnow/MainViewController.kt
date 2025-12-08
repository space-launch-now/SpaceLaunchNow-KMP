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
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import me.calebjones.spacelaunchnow.util.initializeBuildConfig
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform.getKoin

private val log by lazy { SpaceLogger.getLogger("MainViewController") }
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
        // Initialize SpaceLogger before any logging calls
        SpaceLogger.initialize()
        startKoin(koinConfig)
        koinInitialized = true
        
        // Initialize Billing and Subscription system on background thread
        CoroutineScope(Dispatchers.Default).launch {
            try {
                log.i { "iOS: 🚀 Starting billing and subscription initialization..." }
                
                // Step 1: Initialize BillingManager (RevenueCat)
                val billingManager = getKoin().get<BillingManager>()
                billingManager.initialize(appUserId = null)
                log.i { "iOS: ✅ BillingManager initialized successfully" }
                
                // Step 2: Initialize and start SubscriptionSyncer
                // This listens to billing state changes and persists to LocalSubscriptionStorage
                val syncer = getKoin().get<SubscriptionSyncer>()
                syncer.startSyncing()
                log.i { "iOS: ✅ SubscriptionSyncer started successfully" }
                
                // Step 3: Initialize SubscriptionRepository (loads cached state)
                val repository = getKoin().get<SubscriptionRepository>()
                repository.initialize()
                log.i { "iOS: ✅ SubscriptionRepository initialized successfully" }
                
                // Step 4: Force initial sync to ensure purchase state is persisted
                syncer.syncNow()
                log.i { "iOS: ✅ Initial subscription sync complete" }
                
                log.i { "iOS: 🎉 All billing and subscription systems initialized" }
            } catch (e: Exception) {
                log.e(e) { "iOS: ❌ Failed to initialize billing/subscription system" }
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

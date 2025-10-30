package me.calebjones.spacelaunchnow

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowWidthSizeClass
import me.calebjones.spacelaunchnow.data.billing.RevenueCatManager
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.navigation.LaunchDetail
import me.calebjones.spacelaunchnow.navigation.SupportUs
import me.calebjones.spacelaunchnow.platform.ContextFactory
import me.calebjones.spacelaunchnow.ui.ads.AdConsentPopup
import me.calebjones.spacelaunchnow.ui.ads.AdInitializer
import me.calebjones.spacelaunchnow.ui.ads.WithPreloadedAds
import me.calebjones.spacelaunchnow.ui.layout.desktop.TabletDesktopLayout
import me.calebjones.spacelaunchnow.ui.layout.phone.PhoneLayout
import me.calebjones.spacelaunchnow.ui.viewmodel.AppSettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption
import me.calebjones.spacelaunchnow.util.BuildConfig
import org.koin.compose.koinInject

/**
 * CompositionLocal to provide the useUtc setting throughout the app
 */
val LocalUseUtc = compositionLocalOf { false }

/**
 * CompositionLocal to provide the ContextFactory throughout the app
 */
val LocalContextFactory =
    compositionLocalOf<ContextFactory?> { null }

@Composable
fun isTabletOrDesktop(): Boolean {
    // Use WindowSizeClass for proper adaptive layout detection
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val isLargeScreen = (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED)
    println("WindowSizeClass width: ${windowSizeClass.windowWidthSizeClass}, isLargeScreen: $isLargeScreen")
    return isLargeScreen || getPlatform().type.isDesktop
}

@Composable
fun SpaceLaunchNowApp(
    contextFactory: ContextFactory,
    notificationLaunchId: String? = null,
    onNotificationLaunchIdConsumed: () -> Unit = {},
    navigationDestination: String? = null,
    onNavigationDestinationConsumed: () -> Unit = {}
) {
    // Initialize notifications and subscription on app start
    val notificationRepository = koinInject<NotificationRepository>()
    val subscriptionRepository = koinInject<SubscriptionRepository>()
    val revenueCatManager = koinInject<RevenueCatManager>()
    val pushMessaging = koinInject<PushMessaging>()
    val appPreferences = koinInject<AppPreferences>()

    // Warm up settings ViewModel to preload preferences (eagerly loads DataStore values)
    // This singleton creation triggers all StateFlows to start collecting immediately,
    // ensuring switches show correct state with no animation when settings screen loads
    val appSettingsViewModel = koinInject<AppSettingsViewModel>()

    // Trigger StateFlow collection by accessing a flow
    LaunchedEffect(Unit) {
        // Access the ViewModel to ensure it's fully initialized
        appSettingsViewModel.themeFlow.value
        println("✅ Settings ViewModels warmed up - preferences preloaded")
    }

    // Observe the theme setting
    val themeOption by appPreferences.themeFlow.collectAsState(initial = ThemeOption.System)

    // Observe the useUtc setting
    val useUtc by appPreferences.useUtcFlow.collectAsState(initial = false)

    val navController = rememberNavController()

    // Handle notification-based navigation
    LaunchedEffect(notificationLaunchId) {
        if (notificationLaunchId != null) {
            println("Navigating to launch detail for ID: $notificationLaunchId")
            navController.navigate(
                LaunchDetail(
                    notificationLaunchId
                )
            )
            // Clear the notification launch ID after navigation
            onNotificationLaunchIdConsumed()
        }
    }

    // Handle navigation destination (e.g., from widget)
    LaunchedEffect(navigationDestination) {
        when (navigationDestination) {
            "subscription" -> {
                println("Navigating to SupportUs screen from widget")
                navController.navigate(SupportUs)
                onNavigationDestinationConsumed()
            }

            null -> {} // No navigation destination
            else -> {
                println("Unknown navigation destination: $navigationDestination")
                onNavigationDestinationConsumed()
            }
        }
    }

    LaunchedEffect(Unit) {
        println("=== APP START DEBUG INFO ===")

        // Initialize ads using platform-specific abstraction
        val testDeviceIds = if (BuildConfig.IS_DEBUG) {
            listOf(
                "0BF9377651BCA3F62260F25FFC54F6A8",
            )
        } else {
            emptyList()
        }
        
        val adInitSuccess = AdInitializer.initialize(context = contextFactory.getActivity())
        
        if (adInitSuccess) {
            AdInitializer.configure(BuildConfig.IS_DEBUG, testDeviceIds)
        }

        try {
            // Get and print FCM token
            val token = pushMessaging.getToken()
            println("FCM Token: $token")
        } catch (e: Exception) {
            println("Failed to get FCM token: ${e.message}")
        }

        try {
            // Initialize notifications
            notificationRepository.initialize()

            // Get and print current state (using the new state flow)
            val currentState = notificationRepository.state.value
            println("Current state:")
            println("  - Notifications enabled: ${currentState.enableNotifications}")
            println("  - Follow all launches: ${currentState.followAllLaunches}")
            println("  - Use strict matching: ${currentState.useStrictMatching}")
            println("  - Subscribed agencies: ${currentState.subscribedAgencies.size}")
            println("  - Subscribed locations: ${currentState.subscribedLocations.size}")
            println("  - Topic settings: ${currentState.topicSettings}")
            println("  - Subscribed FCM topics: ${currentState.subscribedTopics.size}")

            println("Settings loaded - state management handled by repository")
        } catch (e: Exception) {
            println("Failed to initialize notifications: ${e.message}")
            e.printStackTrace()
        }

        try {
            // Initialize subscription billing
            subscriptionRepository.initialize()
            println("Subscription repository initialized successfully")
        } catch (e: Exception) {
            println("Failed to initialize subscription repository: ${e.message}")
            e.printStackTrace()
        }

        try {
            // Initialize RevenueCat
            revenueCatManager.initialize()
            println("RevenueCat manager initialized successfully")
        } catch (e: Exception) {
            println("Failed to initialize RevenueCat: ${e.message}")
            e.printStackTrace()
        }

        println("=== END APP START DEBUG INFO ===")
    }

    // Determine layout type and update on window size changes (rotation, window resize)
    // Don't cache this - let it recalculate on every recomposition when screen rotates
    val useTabletLayout = isTabletOrDesktop()
    println("Device layout type: ${if (useTabletLayout) "Tablet/Desktop" else "Phone"}")

    // Provide the useUtc setting and contextFactory throughout the app
    // Ad-related CompositionLocals are provided by WithPreloadedAds wrapper
    CompositionLocalProvider(
        LocalUseUtc provides useUtc,
        LocalContextFactory provides contextFactory
    ) {
        // Show consent popup (platform-specific implementation)
        // Must be inside CompositionLocalProvider to access LocalContextFactory
        AdConsentPopup(
            onFailure = { println("Consent popup failure: ${it.message}") }
        )
        
        // Wrap content with preloaded ads (platform-specific: Android/iOS preloads, Desktop no-op)
        WithPreloadedAds(
            context = contextFactory.getActivity()
        ) {
            if (useTabletLayout) {
                TabletDesktopLayout(navController = navController, themeOption = themeOption)
            } else {
                PhoneLayout(navController = navController, themeOption = themeOption)
            }
        }
    }
}
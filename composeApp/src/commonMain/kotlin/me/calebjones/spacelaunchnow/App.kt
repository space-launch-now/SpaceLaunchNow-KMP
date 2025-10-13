package me.calebjones.spacelaunchnow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.billing.RevenueCatManager
import me.calebjones.spacelaunchnow.ui.layout.desktop.TabletDesktopLayout
import me.calebjones.spacelaunchnow.ui.layout.phone.PhoneLayout
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption
import org.koin.compose.koinInject

/**
 * CompositionLocal to provide the useUtc setting throughout the app
 */
val LocalUseUtc = compositionLocalOf { false }

@Composable
fun isTabletOrDesktop(): Boolean {
    val screenWidthDp = getScreenWidth()
    val isLargeScreen = screenWidthDp >= 600.dp // Example threshold for tablets
    return isLargeScreen
//    return getPlatform().name == "Desktop"
}

@Composable
fun SpaceLaunchNowApp(
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

    // Observe the theme setting
    val themeOption by appPreferences.themeFlow.collectAsState(initial = me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption.System)

    // Observe the useUtc setting
    val useUtc by appPreferences.useUtcFlow.collectAsState(initial = false)

    val navController = rememberNavController()

    // Handle notification-based navigation
    LaunchedEffect(notificationLaunchId) {
        if (notificationLaunchId != null) {
            println("Navigating to launch detail for ID: $notificationLaunchId")
            navController.navigate(
                me.calebjones.spacelaunchnow.navigation.LaunchDetail(
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
                navController.navigate(me.calebjones.spacelaunchnow.navigation.SupportUs)
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

    // Determine initial layout type and keep it stable - don't switch between layouts on rotation
    // This preserves navigation state across configuration changes
    val isTabletOrDesktopValue = isTabletOrDesktop()
    val useTabletLayout = remember(navController) { isTabletOrDesktopValue }

    // Provide the useUtc setting throughout the app
    CompositionLocalProvider(LocalUseUtc provides useUtc) {
        if (useTabletLayout) {
            TabletDesktopLayout(navController = navController, themeOption = themeOption)
        } else {
            PhoneLayout(navController = navController, themeOption = themeOption)
        }
    }
}
package me.calebjones.spacelaunchnow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import me.calebjones.spacelaunchnow.di.koinConfig
import me.calebjones.spacelaunchnow.ui.layout.desktop.TabletDesktopLayout
import me.calebjones.spacelaunchnow.ui.layout.phone.PhoneLayout
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun isTabletOrDesktop(): Boolean {
    val screenWidthDp = getScreenWidth()
    val isLargeScreen = screenWidthDp >= 600.dp // Example threshold for tablets
    return isLargeScreen
//    return getPlatform().name == "Desktop"
}

@Composable
fun SpaceLaunchNowApp() {
    KoinApplication(
        application = { koinConfig() }
    ){
        // Initialize notifications on app start and print debug info
        val notificationRepository = koinInject<NotificationRepository>()
        val pushMessaging = koinInject<PushMessaging>()
        val appPreferences = koinInject<AppPreferences>()
        
        // Observe the theme setting
        val themeOption by appPreferences.themeFlow.collectAsState(initial = me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption.System)

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
                notificationRepository.initializeNotifications()

                // Get and print current subscribed topics
                val settings = notificationRepository.getNotificationSettings()
                println("Current subscribed topics: ${settings.subscribedTopics}")
                println("Subscribed agencies: ${settings.subscribedAgencies}")
                println("Subscribed locations: ${settings.subscribedLocations}")
                println("Notifications enabled: ${settings.enableNotifications}")
                println("Follow all launches: ${settings.followAllLaunches}")
                println("Use strict matching: ${settings.useStrictMatching}")
                println("--- Additional Notification Topics ---")
                println("Event notifications: ${settings.eventNotifications}")
                println("Netstamp changed: ${settings.netstampChanged}")
                println("Webcast only: ${settings.webcastOnly}")
                println("24 hour: ${settings.twentyFourHour}")
                println("1 hour: ${settings.oneHour}")
                println("10 minutes: ${settings.tenMinutes}")
                println("1 minute: ${settings.oneMinute}")
                println("In flight: ${settings.inFlight}")
                println("Success: ${settings.success}")
            } catch (e: Exception) {
                println("Failed to get notification settings: ${e.message}")
                e.printStackTrace()
            }

            println("=== END APP START DEBUG INFO ===")
        }

        val navController = rememberNavController()
        val isTabletOrDesktop = isTabletOrDesktop()

        if (isTabletOrDesktop) {
            TabletDesktopLayout(navController = navController, themeOption = themeOption)
        } else {
            PhoneLayout(navController = navController, themeOption = themeOption)
        }
    }
}
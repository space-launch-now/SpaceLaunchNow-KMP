package me.calebjones.spacelaunchnow

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.window.core.layout.WindowWidthSizeClass
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.navigation.AboutLibraries
import me.calebjones.spacelaunchnow.navigation.Agencies
import me.calebjones.spacelaunchnow.navigation.AgencyDetail
import me.calebjones.spacelaunchnow.navigation.CalendarSync
import me.calebjones.spacelaunchnow.navigation.DebugSettings
import me.calebjones.spacelaunchnow.navigation.EventDetail
import me.calebjones.spacelaunchnow.navigation.FullscreenVideo
import me.calebjones.spacelaunchnow.navigation.Home
import me.calebjones.spacelaunchnow.navigation.LaunchDetail
import me.calebjones.spacelaunchnow.navigation.NotificationSettings
import me.calebjones.spacelaunchnow.navigation.Roadmap
import me.calebjones.spacelaunchnow.navigation.RocketDetail
import me.calebjones.spacelaunchnow.navigation.Rockets
import me.calebjones.spacelaunchnow.navigation.Schedule
import me.calebjones.spacelaunchnow.navigation.Settings
import me.calebjones.spacelaunchnow.navigation.Starship
import me.calebjones.spacelaunchnow.navigation.SupportUs
import me.calebjones.spacelaunchnow.navigation.ThemeCustomization
import me.calebjones.spacelaunchnow.platform.ContextFactory
import me.calebjones.spacelaunchnow.ui.about.AboutLibrariesScreen
import me.calebjones.spacelaunchnow.ui.ads.AdConsentPopup
import me.calebjones.spacelaunchnow.ui.ads.AdInitializer
import me.calebjones.spacelaunchnow.ui.ads.WithPreloadedAds
import me.calebjones.spacelaunchnow.ui.agencies.AgencyDetailScreen
import me.calebjones.spacelaunchnow.ui.agencies.AgencyListScreen
import me.calebjones.spacelaunchnow.ui.compose.BetaWarningDialog
import me.calebjones.spacelaunchnow.ui.detail.LaunchDetailScreen
import me.calebjones.spacelaunchnow.ui.event.EventDetailScreen
import me.calebjones.spacelaunchnow.ui.home.HomeScreen
import me.calebjones.spacelaunchnow.ui.layout.desktop.TabletDesktopLayout
import me.calebjones.spacelaunchnow.ui.layout.phone.PhoneLayout
import me.calebjones.spacelaunchnow.ui.layout.phone.composableWithCompositionLocal
import me.calebjones.spacelaunchnow.ui.roadmap.RoadmapScreen
import me.calebjones.spacelaunchnow.ui.rockets.RocketDetailScreen
import me.calebjones.spacelaunchnow.ui.rockets.RocketListScreen
import me.calebjones.spacelaunchnow.ui.schedule.ScheduleScreen
import me.calebjones.spacelaunchnow.ui.settings.CalendarSyncScreen
import me.calebjones.spacelaunchnow.ui.settings.DebugSettingsScreen
import me.calebjones.spacelaunchnow.ui.settings.NotificationSettingsScreen
import me.calebjones.spacelaunchnow.ui.settings.SettingsScreen
import me.calebjones.spacelaunchnow.ui.settings.ThemeCustomizationScreen
import me.calebjones.spacelaunchnow.ui.starship.StarshipScreen
import me.calebjones.spacelaunchnow.ui.subscription.SupportUsScreen
import me.calebjones.spacelaunchnow.ui.video.FullscreenVideoScreen
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption
import me.calebjones.spacelaunchnow.util.BuildConfig

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
    // Desktop platform always uses tablet layout
    if (getPlatform().type.isDesktop) {
        println("🔄 ROTATION_DEBUG: Platform is Desktop - using tablet layout")
        return true
    }

    // For mobile devices (Android/iOS), check BOTH width and height classes
    // A true tablet should have EXPANDED in at least one dimension even when rotated
    // This prevents phones in landscape from being treated as tablets
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val widthClass = windowSizeClass.windowWidthSizeClass
    val heightClass = windowSizeClass.windowHeightSizeClass

    // Only treat as tablet if width is EXPANDED (>= 840dp) 
    // AND height is at least MEDIUM (>= 480dp) to ensure it's not just a phone rotated
    val isTablet = widthClass == WindowWidthSizeClass.EXPANDED

    println("🔄 ROTATION_DEBUG: Width: $widthClass, Height: $heightClass, isTablet: $isTablet")

    return isTablet
}

@Composable
fun SpaceLaunchNowApp(
    contextFactory: ContextFactory,
    themeOption: ThemeOption = ThemeOption.System,
    notificationLaunchId: String? = null,
    onNotificationLaunchIdConsumed: () -> Unit = {},
    navigationDestination: String? = null,
    onNavigationDestinationConsumed: () -> Unit = {}
) {
    // Use default for useUtc to avoid Koin injection during composition on iOS
    // Theme is now passed as parameter from platform-specific code
    val useUtc = false

    val navController = rememberNavController()

    // Determine current window size for layout decisions - now dynamic
    val currentIsTabletSize = isTabletOrDesktop()
    println("🔄 ROTATION_DEBUG: Dynamic layout detection: ${if (currentIsTabletSize) "Tablet/Desktop" else "Phone"}")

    println("🔄 ROTATION_DEBUG: SpaceLaunchNowApp recomposing - NavController: ${navController.hashCode()}, using dynamic layout: ${if (currentIsTabletSize) "Tablet/Desktop" else "Phone"}")

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
        // Run all initialization on background thread to avoid blocking UI on iOS
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            println("=== APP START DEBUG INFO ===")

            try {
                // Lazy inject repositories only when needed (on background thread)
                val koin = org.koin.mp.KoinPlatform.getKoin()
                val notificationRepository = koin.get<NotificationRepository>()
                val subscriptionRepository = koin.get<SubscriptionRepository>()
                val pushMessaging = koin.get<PushMessaging>()

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
            } catch (e: Exception) {
                println("Failed during app initialization: ${e.message}")
                e.printStackTrace()
            }

            println("=== END APP START DEBUG INFO ===")
        }
    }

    // Provide the useUtc setting and contextFactory throughout the app
    // Ad-related CompositionLocals are provided by WithPreloadedAds wrapper
    CompositionLocalProvider(
        LocalUseUtc provides useUtc,
        LocalContextFactory provides contextFactory
    ) {
        BetaWarningDialog()

        // Show consent popup (platform-specific implementation)
        // Must be inside CompositionLocalProvider to access LocalContextFactory
        AdConsentPopup(
            onFailure = { println("Consent popup failure: ${it.message}") }
        )

        // Wrap content with preloaded ads (platform-specific: Android/iOS preloads, Desktop no-op)
        WithPreloadedAds(
            context = contextFactory.getActivity()
        ) {
            // Hoisted NavHost - preserved across layout switches to maintain navigation state
            val navHostContent: @Composable () -> Unit = {
                NavHost(
                    navController = navController,
                    startDestination = Home,
                ) {
                    composableWithCompositionLocal<Home> {
                        HomeScreen(navController = navController)
                    }
                    composableWithCompositionLocal<Schedule> {
                        ScheduleScreen(
                            onLaunchClick = { id -> navController.navigate(LaunchDetail(id)) }
                        )
                    }
                    composableWithCompositionLocal<Settings> {
                        SettingsScreen(
                            navController = navController,
                            onOpenNotificationSettings = {
                                navController.navigate(NotificationSettings)
                            },
                            onOpenDebugSettings = {
                                navController.navigate(DebugSettings)
                            },
                            onOpenAboutLibraries = {
                                navController.navigate(AboutLibraries)
                            }
                        )
                    }
                    composableWithCompositionLocal<LaunchDetail> { backStackEntry ->
                        val launchDetail = backStackEntry.toRoute<LaunchDetail>()
                        LaunchDetailScreen(
                            launchId = launchDetail.launchId,
                            onNavigateBack = { navController.popBackStack() },
                            navController = navController
                        )
                    }
                    composableWithCompositionLocal<EventDetail> { backStackEntry ->
                        val eventDetail = backStackEntry.toRoute<EventDetail>()
                        EventDetailScreen(
                            eventId = eventDetail.eventId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composableWithCompositionLocal<AgencyDetail> { backStackEntry ->
                        val agencyDetail = backStackEntry.toRoute<AgencyDetail>()
                        AgencyDetailScreen(
                            agencyId = agencyDetail.agencyId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composableWithCompositionLocal<FullscreenVideo> { backStackEntry ->
                        val fullscreenVideo = backStackEntry.toRoute<FullscreenVideo>()
                        FullscreenVideoScreen(
                            videoUrl = fullscreenVideo.videoUrl,
                            launchName = fullscreenVideo.launchName,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composableWithCompositionLocal<NotificationSettings> {
                        NotificationSettingsScreen(
                            navController = navController,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composableWithCompositionLocal<DebugSettings> {
                        DebugSettingsScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composableWithCompositionLocal<AboutLibraries> {
                        AboutLibrariesScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composableWithCompositionLocal<SupportUs> {
                        SupportUsScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composableWithCompositionLocal<ThemeCustomization> {
                        ThemeCustomizationScreen(
                            navController = navController
                        )
                    }
                    composableWithCompositionLocal<CalendarSync> {
                        CalendarSyncScreen(
                            navController = navController
                        )
                    }
                    composableWithCompositionLocal<Roadmap> {
                        RoadmapScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composableWithCompositionLocal<Rockets> {
                        RocketListScreen(
                            onNavigateToRocketDetail = { id ->
                                navController.navigate(RocketDetail(id))
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composableWithCompositionLocal<RocketDetail> { backStackEntry ->
                        val rocketDetail = backStackEntry.toRoute<RocketDetail>()
                        RocketDetailScreen(
                            rocketId = rocketDetail.rocketId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composableWithCompositionLocal<Agencies> {
                        AgencyListScreen(
                            onNavigateToAgencyDetail = { id ->
                                navController.navigate(AgencyDetail(id))
                            },
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composableWithCompositionLocal<AgencyDetail> { backStackEntry ->
                        val agencyDetail = backStackEntry.toRoute<AgencyDetail>()
                        AgencyDetailScreen(
                            agencyId = agencyDetail.agencyId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composableWithCompositionLocal<Starship> {
                        StarshipScreen(navController = navController)
                    }
                }
            }

            // Dynamic layout switching while preserving navigation state
            if (currentIsTabletSize) {
                TabletDesktopLayout(
                    navController = navController,
                    themeOption = themeOption,
                    content = navHostContent
                )
            } else {
                PhoneLayout(
                    navController = navController,
                    themeOption = themeOption,
                    content = navHostContent
                )
            }
        }
    }
}
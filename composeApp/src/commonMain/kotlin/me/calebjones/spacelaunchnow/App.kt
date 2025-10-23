package me.calebjones.spacelaunchnow

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import app.lexilabs.basic.ads.AdSize
import app.lexilabs.basic.ads.BasicAds
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.DependsOnGoogleUserMessagingPlatform
import app.lexilabs.basic.ads.ExperimentalBasicAds
import app.lexilabs.basic.ads.RequestConfiguration
import app.lexilabs.basic.ads.composable.ConsentPopup
import app.lexilabs.basic.ads.composable.rememberBannerAd
import app.lexilabs.basic.ads.composable.rememberConsent
import app.lexilabs.basic.ads.composable.rememberInterstitialAd
import app.lexilabs.basic.ads.composable.rememberRewardedAd
import me.calebjones.spacelaunchnow.data.billing.RevenueCatManager
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.ui.ads.GlobalAdManager
import me.calebjones.spacelaunchnow.ui.layout.desktop.TabletDesktopLayout
import me.calebjones.spacelaunchnow.ui.layout.phone.PhoneLayout
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
    compositionLocalOf<me.calebjones.spacelaunchnow.platform.ContextFactory?> { null }

/**
 * CompositionLocal to provide preloaded banner ads throughout the app
 */
val LocalPreloadedBannerAd = compositionLocalOf<app.lexilabs.basic.ads.BannerAdHandler?> { null }
val LocalPreloadedLargeBannerAd =
    compositionLocalOf<app.lexilabs.basic.ads.BannerAdHandler?> { null }
val LocalPreloadedMediumRectangleAd =
    compositionLocalOf<app.lexilabs.basic.ads.BannerAdHandler?> { null }

/**
 * CompositionLocal to provide dedicated navigation banner ad that doesn't conflict with content ads
 */
val LocalPreloadedNavigationBannerAd =
    compositionLocalOf<app.lexilabs.basic.ads.BannerAdHandler?> { null }
val LocalPreloadedNavigationLargeBannerAd =
    compositionLocalOf<app.lexilabs.basic.ads.BannerAdHandler?> { null }
val LocalPreloadedNavigationLeaderboardAd =
    compositionLocalOf<app.lexilabs.basic.ads.BannerAdHandler?> { null }

/**
 * CompositionLocal to provide preloaded tablet-specific ads throughout the app
 */
val LocalPreloadedLeaderboardAd =
    compositionLocalOf<app.lexilabs.basic.ads.BannerAdHandler?> { null }
val LocalPreloadedFullBannerAd =
    compositionLocalOf<app.lexilabs.basic.ads.BannerAdHandler?> { null }
val LocalPreloadedFluidAd =
    compositionLocalOf<app.lexilabs.basic.ads.BannerAdHandler?> { null }

/**
 * CompositionLocal to provide preloaded interstitial and rewarded ads throughout the app
 */
val LocalPreloadedInterstitialAd =
    compositionLocalOf<app.lexilabs.basic.ads.InterstitialAdHandler?> { null }
val LocalPreloadedRewardedAd =
    compositionLocalOf<app.lexilabs.basic.ads.RewardedAdHandler?> { null }

@Composable
fun isTabletOrDesktop(): Boolean {
    // Use WindowSizeClass for proper adaptive layout detection
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    // Check if width is MEDIUM or EXPANDED (not COMPACT)
    // COMPACT: < 600dp (phones in portrait)
    // MEDIUM: 600-839dp (tablets, foldables, phones in landscape) 
    // EXPANDED: >= 840dp (large tablets, desktops)
    val isLargeScreen = windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT
    
    println("WindowSizeClass width: ${windowSizeClass.windowWidthSizeClass}, isLargeScreen: $isLargeScreen")
    return isLargeScreen || getPlatform().type.isDesktop
}

@OptIn(
    DependsOnGoogleUserMessagingPlatform::class, ExperimentalBasicAds::class,
    DependsOnGoogleMobileAds::class
)
@Composable
fun SpaceLaunchNowApp(
    contextFactory: me.calebjones.spacelaunchnow.platform.ContextFactory,
    notificationLaunchId: String? = null,
    onNotificationLaunchIdConsumed: () -> Unit = {},
    navigationDestination: String? = null,
    onNavigationDestinationConsumed: () -> Unit = {}
) {
    // Initialize notifications and subscription on app start
    val consent by rememberConsent(activity = contextFactory.getActivity())
    val notificationRepository = koinInject<NotificationRepository>()
    val subscriptionRepository = koinInject<SubscriptionRepository>()
    val revenueCatManager = koinInject<RevenueCatManager>()
    val pushMessaging = koinInject<PushMessaging>()
    val appPreferences = koinInject<AppPreferences>()
    val globalAdManager = koinInject<GlobalAdManager>()

    // Determine activity parameter based on platform (iOS needs null, Android needs activity)
    val activityOrNull = if (getPlatform().type == PlatformType.IOS) null else contextFactory.getActivity()

    // 🚀 PRELOAD BANNER ADS: Following KMP-Google-AdMob example pattern
    // These ads will be preloaded and kept alive throughout the app lifecycle for instant rendering
    val preloadedBannerAd by rememberBannerAd(
        activity = activityOrNull,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.BANNER
    )
    val preloadedLargeBannerAd by rememberBannerAd(
        activity = activityOrNull,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.LARGE_BANNER
    )
    val preloadedMediumRectangleAd by rememberBannerAd(
        activity = activityOrNull,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.MEDIUM_RECTANGLE
    )

    // 🧭 DEDICATED NAVIGATION ADS: Separate instances to avoid conflicts with content ads
    val preloadedNavigationBannerAd by rememberBannerAd(
        activity = activityOrNull,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.BANNER  // Standard banner for navigation (320x50)
    )
    val preloadedNavigationLargeBannerAd by rememberBannerAd(
        activity = activityOrNull,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.LARGE_BANNER  // Large banner for navigation (320x100)
    )
    val preloadedNavigationLeaderboardAd by rememberBannerAd(
        activity = activityOrNull,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.LEADERBOARD  // Leaderboard for navigation (728x90)
    )

    // 📱 TABLET-SPECIFIC ADS: Wider ads for tablets
    val preloadedLeaderboardAd by rememberBannerAd(
        activity = activityOrNull,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.LEADERBOARD  // 728x90 for tablets
    )
    val preloadedFullBannerAd by rememberBannerAd(
        activity = activityOrNull,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.FULL_BANNER  // 468x60 for medium screens
    )
    
    // 🌊 FLUID ADS: Adaptive/responsive ads that adjust to content
    val preloadedFluidAd by rememberBannerAd(
        activity = activityOrNull,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.BANNER),
        adSize = AdSize.FLUID  // Dynamic size that adapts to content
    )

    // 🚀 PRELOAD INTERSTITIAL & REWARDED ADS: For instant showing when needed
    val preloadedInterstitialAd by rememberInterstitialAd(
        activity = activityOrNull,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.INTERSTITIAL)
    )
    val preloadedRewardedAd by rememberRewardedAd(
        activity = activityOrNull,
        adUnitId = GlobalAdManager.getPlatformAdUnitId(GlobalAdManager.Companion.AdType.REWARDED)
    )

    // Warm up settings ViewModel to preload preferences (eagerly loads DataStore values)
    // This singleton creation triggers all StateFlows to start collecting immediately,
    // ensuring switches show correct state with no animation when settings screen loads
    val appSettingsViewModel =
        koinInject<me.calebjones.spacelaunchnow.ui.viewmodel.AppSettingsViewModel>()

    // Trigger StateFlow collection by accessing a flow
    LaunchedEffect(Unit) {
        // Access the ViewModel to ensure it's fully initialized
        appSettingsViewModel.themeFlow.value
        println("✅ Settings ViewModels warmed up - preferences preloaded")
    }

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
            // Initialize Basic-Ads (iOS doesn't require context, only Android does)
            when (getPlatform().type) {
                PlatformType.ANDROID -> {
                    val activity = contextFactory.getActivity()
                    println("🎯 Initializing Basic-Ads on Android with activity: $activity")
                    BasicAds.initialize(activity)
                }
                PlatformType.IOS -> {
                    println("🎯 Initializing Basic-Ads on iOS (no context required)")
                    BasicAds.initialize(null)
                }
                PlatformType.DESKTOP -> {
                    println("🎯 Basic-Ads not supported on Desktop")
                    return@LaunchedEffect
                }
            }

            // Configure for better ad loading (especially in development)
            val testDeviceIds = if (BuildConfig.IS_DEBUG) {
                listOf(
                    "0BF9377651BCA3F62260F25FFC54F6A8",
                )
            } else {
                emptyList()
            }

            BasicAds.configuration = RequestConfiguration(
                maxAdContentRating = null,
                publisherPrivacyPersonalizationState = RequestConfiguration.PublisherPrivacyPersonalizationState.DEFAULT,
                tagForChildDirectedTreatment = 0,
                tagForUnderAgeOfConsent = 0,
                testDeviceIds = testDeviceIds
            )

            println("✅ Basic-Ads initialized successfully")
            println("🧪 Test device IDs configured: $testDeviceIds")
        } catch (e: Exception) {
            println("❌ Failed to initialize Basic-Ads: ${e.message}")
            e.printStackTrace()
        }

        // Log GlobalAdManager initialization (already injected via Koin at composable level)
        println("🚀 GlobalAdManager injected via Koin - optimizing ad performance...")

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
    println("Device layout type: ${if (isTabletOrDesktopValue) "Tablet/Desktop" else "Phone"}")
    val useTabletLayout = remember(navController) { isTabletOrDesktopValue }

    // Try to show a consent popup
    ConsentPopup(
        consent = consent,
        onFailure = { println("failure:${it.message}") }
    )

    // Provide the useUtc setting, contextFactory, and preloaded ads throughout the app
    CompositionLocalProvider(
        LocalUseUtc provides useUtc,
        LocalContextFactory provides contextFactory,
        LocalPreloadedBannerAd provides preloadedBannerAd,
        LocalPreloadedLargeBannerAd provides preloadedLargeBannerAd,
        LocalPreloadedMediumRectangleAd provides preloadedMediumRectangleAd,
        LocalPreloadedNavigationBannerAd provides preloadedNavigationBannerAd,
        LocalPreloadedNavigationLargeBannerAd provides preloadedNavigationLargeBannerAd,
        LocalPreloadedNavigationLeaderboardAd provides preloadedNavigationLeaderboardAd,
        LocalPreloadedLeaderboardAd provides preloadedLeaderboardAd,
        LocalPreloadedFullBannerAd provides preloadedFullBannerAd,
        LocalPreloadedFluidAd provides preloadedFluidAd,
        LocalPreloadedInterstitialAd provides preloadedInterstitialAd,
        LocalPreloadedRewardedAd provides preloadedRewardedAd
    ) {
        if (useTabletLayout) {
            TabletDesktopLayout(navController = navController, themeOption = themeOption)
        } else {
            PhoneLayout(navController = navController, themeOption = themeOption)
        }
    }
}
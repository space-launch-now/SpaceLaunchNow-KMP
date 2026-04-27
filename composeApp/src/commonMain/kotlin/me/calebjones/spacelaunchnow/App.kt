package me.calebjones.spacelaunchnow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.window.core.layout.WindowWidthSizeClass
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.analytics.navigation.AnalyticsScreenTracker
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.navigation.AboutLibraries
import me.calebjones.spacelaunchnow.navigation.Agencies
import me.calebjones.spacelaunchnow.navigation.AgencyDetail
import me.calebjones.spacelaunchnow.navigation.AstronautDetail
import me.calebjones.spacelaunchnow.navigation.Astronauts
import me.calebjones.spacelaunchnow.navigation.CalendarSync
import me.calebjones.spacelaunchnow.navigation.DebugSettings
import me.calebjones.spacelaunchnow.navigation.EventDetail
import me.calebjones.spacelaunchnow.navigation.Explore
import me.calebjones.spacelaunchnow.navigation.FullscreenVideo
import me.calebjones.spacelaunchnow.navigation.Home
import me.calebjones.spacelaunchnow.navigation.LaunchDetail
import me.calebjones.spacelaunchnow.navigation.LiveOnboarding
import me.calebjones.spacelaunchnow.navigation.NewsEvents
import me.calebjones.spacelaunchnow.navigation.NotificationSettings
import me.calebjones.spacelaunchnow.navigation.Onboarding
import me.calebjones.spacelaunchnow.navigation.Preload
import me.calebjones.spacelaunchnow.navigation.Roadmap
import me.calebjones.spacelaunchnow.navigation.RocketDetail
import me.calebjones.spacelaunchnow.navigation.Rockets
import me.calebjones.spacelaunchnow.navigation.Schedule
import me.calebjones.spacelaunchnow.navigation.Settings
import me.calebjones.spacelaunchnow.navigation.SpaceStationDetail
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
import me.calebjones.spacelaunchnow.ui.astronaut.AstronautDetailView
import me.calebjones.spacelaunchnow.ui.astronaut.AstronautListScreen
import me.calebjones.spacelaunchnow.ui.detail.LaunchDetailScreen
import me.calebjones.spacelaunchnow.ui.event.EventDetailScreen
import me.calebjones.spacelaunchnow.ui.explore.ExploreScreen
import me.calebjones.spacelaunchnow.ui.home.HomeScreen
import me.calebjones.spacelaunchnow.ui.layout.AdaptiveAppScaffold
import me.calebjones.spacelaunchnow.ui.newsevents.NewsEventsScreen
import me.calebjones.spacelaunchnow.ui.layout.phone.composableWithCompositionLocal
import me.calebjones.spacelaunchnow.ui.onboarding.LiveOnboardingScreen
import me.calebjones.spacelaunchnow.ui.onboarding.OnboardingPaywallScreen
import me.calebjones.spacelaunchnow.ui.preload.PreloadScreen
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
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowTheme
import me.calebjones.spacelaunchnow.ui.video.FullscreenVideoScreen
import me.calebjones.spacelaunchnow.ui.viewmodel.AppRatingViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import org.koin.compose.koinInject


private val log by lazy { SpaceLogger.getLogger("App") }

/**
 * CompositionLocal to provide the useUtc setting throughout the app
 */
val LocalUseUtc = compositionLocalOf { false }

/**
 * CompositionLocal to provide the ContextFactory throughout the app
 */
val LocalContextFactory =
    compositionLocalOf<ContextFactory?> { null }

@Deprecated(
    message = "Use rememberAdaptiveLayoutState() from ui.layout instead",
    replaceWith = ReplaceWith(
        "rememberAdaptiveLayoutState()",
        "me.calebjones.spacelaunchnow.ui.layout.rememberAdaptiveLayoutState"
    )
)
@Composable
fun isTabletOrDesktop(): Boolean {
    // Desktop platform always uses tablet layout
    if (getPlatform().type.isDesktop) {
        log.v { "Platform is Desktop - using tablet layout" }
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

    log.v { "Width: $widthClass, Height: $heightClass, isTablet: $isTablet" }

    return isTablet
}

@Composable
fun SpaceLaunchNowApp(
    contextFactory: ContextFactory,
    themeOption: ThemeOption = ThemeOption.System,
    useUtc: Boolean = false,
    notificationLaunchId: String? = null,
    onNotificationLaunchIdConsumed: () -> Unit = {},
    notificationEventId: Int? = null,
    onNotificationEventIdConsumed: () -> Unit = {},
    navigationDestination: String? = null,
    onNavigationDestinationConsumed: () -> Unit = {}
) {
    // useUtc is now passed as parameter from platform-specific code
    // Theme is now passed as parameter from platform-specific code

    val navController = rememberNavController()

    log.v { "SpaceLaunchNowApp recomposing - NavController: ${navController.hashCode()}" }

    LaunchedEffect(Unit) {
        // Run all initialization on background thread to avoid blocking UI on iOS
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {

            log.i { "=== APP START DEBUG INFO ===" }

            try {
                // Lazy inject repositories only when needed (on background thread)
                val koin = org.koin.mp.KoinPlatform.getKoin()
                val notificationRepository = koin.get<NotificationRepository>()
                val subscriptionRepository = koin.get<SubscriptionRepository>()
                val pushMessaging = koin.get<PushMessaging>()

                // Track app opened
                val analyticsManager = koin.get<AnalyticsManager>()
                analyticsManager.track(AnalyticsEvent.AppOpened())

                // Initialize ads using platform-specific abstraction
                val testDeviceIds = if (BuildConfig.IS_DEBUG) {
                    listOf(
                        "0BF9377651BCA3F62260F25FFC54F6A8",
                    )
                } else {
                    emptyList()
                }

                // CR-3: Ad SDK must initialize on Main thread per Google AdMob threading contract
                val adInitSuccess =
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        AdInitializer.initialize(context = contextFactory.getActivity())
                    }

                if (adInitSuccess) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        AdInitializer.configure(BuildConfig.IS_DEBUG, testDeviceIds)
                    }
                    
                    // Pre-warm ad requests to reduce time-to-first-ad
                    val koinForAds = org.koin.mp.KoinPlatform.getKoin()
                    val globalAdManager = koinForAds.get<me.calebjones.spacelaunchnow.ui.ads.GlobalAdManager>()
                    globalAdManager.initializeAndPreload()
                    globalAdManager.preWarmAdRequests()
                }

                try {
                    // Get and print FCM token
                    val token = pushMessaging.getToken()
                    log.d { "FCM Token: $token" }
                } catch (e: Exception) {
                    log.w(e) { "Failed to get FCM token" }
                }

                try {
                    // Initialize notifications
                    notificationRepository.initialize()

                    // Get and print current state (using the new state flow)
                    val currentState = notificationRepository.state.value
                    log.d { "Current state:" }
                    log.d { "  - Notifications enabled: ${currentState.enableNotifications}" }
                    log.d { "  - Follow all launches: ${currentState.followAllLaunches}" }
                    log.d { "  - Use strict matching: ${currentState.useStrictMatching}" }
                    log.d { "  - Subscribed agencies: ${currentState.subscribedAgencies.size}" }
                    log.d { "  - Subscribed locations: ${currentState.subscribedLocations.size}" }
                    log.d { "  - Topic settings: ${currentState.topicSettings}" }
                    log.d { "  - Subscribed FCM topics: ${currentState.subscribedTopics.size}" }

                    log.i { "Settings loaded - state management handled by repository" }
                } catch (e: Exception) {
                    log.e(e) { "Failed to initialize notifications" }
                }

                try {
                    // Initialize subscription billing
                    subscriptionRepository.initialize()
                    log.i { "Subscription repository initialized successfully" }
                } catch (e: Exception) {
                    log.e(e) { "Failed to initialize subscription repository" }
                }
            } catch (e: Exception) {
                log.e(e) { "Failed during app initialization" }
            }

            log.i { "=== END APP START DEBUG INFO ===" }
        }
    }

    // Provide the useUtc setting and contextFactory throughout the app
    // Ad-related CompositionLocals are provided by WithPreloadedAds wrapper
    // Wrap everything in theme so dialogs get proper theming
    SpaceLaunchNowTheme(themeOption = themeOption) {
        CompositionLocalProvider(
            LocalUseUtc provides useUtc,
            LocalContextFactory provides contextFactory
        ) {
            // Migrate existing users: if they've seen BetaWarningDialog, mark onboarding complete
            val appPreferences: AppPreferences = koinInject()
            LaunchedEffect(Unit) {
                if (appPreferences.isBetaWarningShown()) {
                    appPreferences.setOnboardingCompleted(true)
                }
            }

            // Onboarding / preload gate — PreloadScreen determines the next destination
            // internally based on liveOnboardingCompleted and onboardingPaywallShown.
            // null = DataStore still initializing; defer NavHost until resolved.
            val liveOnboardingCompleted by appPreferences.liveOnboardingCompletedFlow.collectAsState(initial = null)
            val onboardingPaywallShown by appPreferences.onboardingPaywallV1ShownFlow.collectAsState(initial = null)
            val subscriptionViewModel: SubscriptionViewModel = koinInject()
            val subscriptionState by subscriptionViewModel.subscriptionState.collectAsState()
            // Lock startRoute once DataStore values resolve — prevents NavHost from
            // getting a different startDestination on recomposition after the deep link
            // is consumed (which would reset the navigation back-stack).
            val startRoute: Any? = remember(liveOnboardingCompleted, onboardingPaywallShown) {
                when {
                    liveOnboardingCompleted == null || onboardingPaywallShown == null -> null
                    notificationLaunchId != null || notificationEventId != null || navigationDestination != null -> Home
                    else -> Preload
                }
            }

            // Show consent popup (platform-specific implementation)
            // Must be inside CompositionLocalProvider to access LocalContextFactory
            // CR-2: Track consent resolution to gate ad preloading
            var isConsentResolved by remember { mutableStateOf(false) }

            AdConsentPopup(
                onFailure = { throwable ->
                    log.w(throwable) { "Consent popup failure" }
                    isConsentResolved = true
                },
                onConsentResolved = { isConsentResolved = true }
            )

            // App rating integration - shows enjoyment dialog first, then native review or feedback
            val appRatingViewModel: AppRatingViewModel = koinInject()
            // recordAppLaunch must be called on the same instance whose StateFlow the UI observes
            LaunchedEffect(Unit) {
                appRatingViewModel.recordAppLaunch()
            }
            val shouldShowEnjoymentDialog by appRatingViewModel.shouldShowEnjoymentDialog.collectAsState()
            val shouldShowFeedbackDialog by appRatingViewModel.shouldShowFeedbackDialog.collectAsState()
            val shouldShowNativeReview by appRatingViewModel.shouldShowNativeReview.collectAsState()

            // Delay showing the dialog until user has been in the app for a bit
            var showDelayedDialog by remember { mutableStateOf(false) }
            LaunchedEffect(shouldShowEnjoymentDialog) {
                log.i { "LaunchedEffect: shouldShowEnjoymentDialog=$shouldShowEnjoymentDialog, showDelayedDialog=$showDelayedDialog" }
                if (shouldShowEnjoymentDialog && !showDelayedDialog) {
                    log.i { "⏱️ Rating dialog conditions met, delaying 5 seconds before showing..." }
                    kotlinx.coroutines.delay(2_000) // 2 seconds
                    showDelayedDialog = true
                    log.i { "✅ Delay complete, setting showDelayedDialog=true" }
                } else if (!shouldShowEnjoymentDialog) {
                    log.d { "Resetting showDelayedDialog to false" }
                    showDelayedDialog = false // Reset when conditions no longer met
                }
            }

            // Show enjoyment dialog after delay
            log.i { "Checking if should render dialog: showDelayedDialog=$showDelayedDialog" }
            if (showDelayedDialog) {
                log.i { "🎨 Rendering AppRatingDialog now" }
                me.calebjones.spacelaunchnow.ui.components.AppRatingDialog(
                    onYesEnjoyingApp = { appRatingViewModel.onUserEnjoyingApp() },
                    onNoNotEnjoying = { appRatingViewModel.onUserNotEnjoyingApp() },
                    onNotNow = { appRatingViewModel.onNotNow() },
                    onDismiss = { appRatingViewModel.dismissEnjoymentDialog() }
                )
            }

            // Show feedback dialog if user said they're not enjoying
            if (shouldShowFeedbackDialog) {
                me.calebjones.spacelaunchnow.ui.components.FeedbackDialog(
                    onSendEmail = {
                        me.calebjones.spacelaunchnow.util.ExternalLinkHandler.openEmail(
                            recipient = "support@spacelaunchnow.me",
                            subject = "Space Launch Now Feedback",
                            body = "Hi, I'd like to share some feedback about the app:\n\n"
                        )
                        appRatingViewModel.onFeedbackSent() // Track that they sent feedback
                    },
                    onOpenGitHub = {
                        me.calebjones.spacelaunchnow.util.ExternalLinkHandler.openUrl(
                            "https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/new"
                        )
                        appRatingViewModel.onFeedbackSent() // Track that they sent feedback
                    },
                    onOpenDiscord = {
                        me.calebjones.spacelaunchnow.util.ExternalLinkHandler.openUrl(
                            "https://discord.gg/WVfzEDW"
                        )
                        appRatingViewModel.onFeedbackSent() // Track that they sent feedback
                    },
                    onDismiss = { appRatingViewModel.dismissFeedbackDialog() }
                )
            }

            // Trigger native review if user confirmed they're enjoying the app
            LaunchedEffect(shouldShowNativeReview) {
                if (shouldShowNativeReview) {
                    val activity = contextFactory.getActivity()
                    appRatingViewModel.requestReview(activity)
                }
            }

            // CR-2: Gate ad preloading on consent resolution; content renders immediately either way
            WithPreloadedAds(
                context = contextFactory.getActivity(),
                shouldPreloadAds = isConsentResolved
            ) {
                if (startRoute == null) {
                    // DataStore still loading — blank surface prevents flash to Home screen
                    androidx.compose.material3.Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {}
                } else {
                    val analyticsManager: AnalyticsManager = koinInject()

                    // Hoisted NavHost - preserved across layout switches to maintain navigation state
                    val navHostContent: @Composable () -> Unit = {
                        NavHost(
                            navController = navController,
                            startDestination = startRoute,
                        ) {
                            composableWithCompositionLocal<Preload> {
                                PreloadScreen(
                                    onPreloadComplete = { nextDestination ->
                                        navController.navigate(nextDestination) {
                                            popUpTo<Preload> { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composableWithCompositionLocal<LiveOnboarding> {
                                LiveOnboardingScreen(
                                    onComplete = {
                                        navController.navigate(Onboarding) {
                                            popUpTo<LiveOnboarding> { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composableWithCompositionLocal<Onboarding> {
                                OnboardingPaywallScreen(
                                    onComplete = {
                                        navController.navigate(Home) {
                                            popUpTo<Onboarding> { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composableWithCompositionLocal<Home> {
                                HomeScreen(navController = navController)
                            }
                            composableWithCompositionLocal<Schedule> {
                                ScheduleScreen(
                                    onLaunchClick = { id -> navController.navigate(LaunchDetail(id)) }
                                )
                            }
                            composableWithCompositionLocal<Explore> {
                                ExploreScreen(navController = navController)
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
                                    onNavigateBack = { navController.popBackStack() },
                                    navController = navController
                                )
                            }
                            composableWithCompositionLocal<AgencyDetail> { backStackEntry ->
                                val agencyDetail = backStackEntry.toRoute<AgencyDetail>()
                                val scope = rememberCoroutineScope()
                                AgencyDetailScreen(
                                    agencyId = agencyDetail.agencyId,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToSchedule = { agencyId ->
                                        scope.launch {
                                            // Apply agency filter and wait for it to complete
                                            val scheduleViewModel =
                                                org.koin.mp.KoinPlatform.getKoin()
                                                    .get<me.calebjones.spacelaunchnow.ui.viewmodel.ScheduleViewModel>()
                                            scheduleViewModel.filterByAgencyAndWait(agencyId)
                                            navController.navigate(Schedule)
                                        }
                                    }
                                )
                            }
                            composableWithCompositionLocal<SpaceStationDetail> { backStackEntry ->
                                val stationDetail = backStackEntry.toRoute<SpaceStationDetail>()
                                me.calebjones.spacelaunchnow.ui.spacestation.SpaceStationDetailScreen(
                                    stationId = stationDetail.stationId,
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
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToLiveOnboarding = { navController.navigate(LiveOnboarding) }
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
                                val scope = rememberCoroutineScope()
                                AgencyDetailScreen(
                                    agencyId = agencyDetail.agencyId,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToSchedule = { agencyId ->
                                        scope.launch {
                                            // Apply agency filter and wait for it to complete
                                            val scheduleViewModel =
                                                org.koin.mp.KoinPlatform.getKoin()
                                                    .get<me.calebjones.spacelaunchnow.ui.viewmodel.ScheduleViewModel>()
                                            scheduleViewModel.filterByAgencyAndWait(agencyId)
                                            navController.navigate(Schedule)
                                        }
                                    }
                                )
                            }
                            composableWithCompositionLocal<Astronauts> {
                                AstronautListScreen(
                                    onNavigateToAstronautDetail = { id ->
                                        navController.navigate(AstronautDetail(id))
                                    },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composableWithCompositionLocal<AstronautDetail> { backStackEntry ->
                                val astronautDetail = backStackEntry.toRoute<AstronautDetail>()
                                AstronautDetailView(
                                    astronautId = astronautDetail.astronautId,
                                    onLaunchClick = { launchId ->
                                        navController.navigate(LaunchDetail(launchId))
                                    },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composableWithCompositionLocal<SpaceStationDetail> { backStackEntry ->
                                val stationDetail = backStackEntry.toRoute<SpaceStationDetail>()
                                me.calebjones.spacelaunchnow.ui.spacestation.SpaceStationDetailScreen(
                                    stationId = stationDetail.stationId,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composableWithCompositionLocal<Starship> {
                                StarshipScreen(navController = navController)
                            }
                            composableWithCompositionLocal<NewsEvents> {
                                NewsEventsScreen(
                                    onEventClick = { eventId ->
                                        navController.navigate(EventDetail(eventId))
                                    }
                                )
                            }
                        }

                        // Analytics screen tracking — fires a screen-view event on each navigation change.
                        AnalyticsScreenTracker(
                            navController = navController,
                            analyticsManager = analyticsManager
                        )

                        // Deep-link / notification navigation.
                        // Placed after NavHost so the graph is guaranteed to be set.
                        // Handles both cold start and warm start (onNewIntent).
                        LaunchedEffect(notificationLaunchId) {
                            if (notificationLaunchId != null) {
                                log.d { "Navigating to launch detail for ID: $notificationLaunchId" }
                                navController.navigate(LaunchDetail(notificationLaunchId))
                                onNotificationLaunchIdConsumed()
                            }
                        }
                        LaunchedEffect(notificationEventId) {
                            if (notificationEventId != null) {
                                log.d { "Navigating to event detail for ID: $notificationEventId" }
                                navController.navigate(EventDetail(notificationEventId))
                                onNotificationEventIdConsumed()
                            }
                        }
                        LaunchedEffect(navigationDestination) {
                            when (navigationDestination) {
                                "subscription" -> {
                                    log.d { "Navigating to SupportUs from widget" }
                                    navController.navigate(SupportUs)
                                    onNavigationDestinationConsumed()
                                }
                                null -> {} // No navigation destination
                                else -> {
                                    log.w { "Unknown navigation destination: $navigationDestination" }
                                    onNavigationDestinationConsumed()
                                }
                            }
                        }
                    }

                    // Adaptive scaffold — auto-selects NavigationBar/Rail/Drawer by window size
                    AdaptiveAppScaffold(
                        navController = navController,
                        themeOption = themeOption,
                        onTabSelected = { tab ->
                            analyticsManager.track(AnalyticsEvent.TabSelected(tab = tab))
                        },
                        content = navHostContent
                    )
                } // end else — onboarding state resolved
            }
        }
    }
}

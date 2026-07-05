package me.calebjones.spacelaunchnow

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.calebjones.spacelaunchnow.analytics.initializeDatadog
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.di.koinConfig
import me.calebjones.spacelaunchnow.util.initializeBuildConfig
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import me.calebjones.spacelaunchnow.util.logging.setupCrashlyticsExceptionHook
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform.getKoin

private val log by lazy { SpaceLogger.getLogger("MainViewController") }
private var koinInitialized = false

// Shared state for navigation from iOS
private val navigationDestinationState = mutableStateOf<String?>(null)

// Shared state for notification-based navigation
private val notificationLaunchIdState = mutableStateOf<String?>(null)

// Shared state for notification-based event navigation (events + custom event targets)
private val notificationEventIdState = mutableStateOf<Int?>(null)

// Shared state for news-notification navigation (opens the article in-app via NewsDetail)
private val notificationNewsUrlState = mutableStateOf<String?>(null)
private val notificationNewsTitleState = mutableStateOf<String?>(null)

// Callback for theme changes — set from Swift to receive updates
private var themeChangeListener: ((Int) -> Unit)? = null

/**
 * Register a listener to be called when the app theme changes.
 * The Int parameter is the UIUserInterfaceStyle raw value:
 * 0 = Unspecified (follow system), 1 = Light, 2 = Dark.
 */
fun setThemeChangeListener(listener: ((Int) -> Unit)?) {
    themeChangeListener = listener
}

// Public function for iOS to trigger navigation
fun setNavigationDestination(destination: String?) {
    try {
        log.i { "iOS: Setting navigation destination: $destination" }
        navigationDestinationState.value = destination
    } catch (t: Throwable) {
        println("iOS: Setting navigation destination: $destination (failed: ${t.message})")
    }
}

// Public function for iOS to trigger navigation from notification
fun setNotificationLaunchId(launchId: String?) {
    try {
        log.i { "iOS: Setting notification launch ID: $launchId" }
        notificationLaunchIdState.value = launchId
    } catch (t: Throwable) {
        println("iOS: Setting notification launch ID: $launchId (failed: ${t.message})")
    }
}

// Public function for iOS to trigger event-detail navigation from a notification tap.
// Accepts a String to keep the Swift call site simple; non-numeric values are ignored.
fun setNotificationEventId(eventId: String?) {
    try {
        val id = eventId?.toIntOrNull()
        if (id == null) {
            log.w { "iOS: Ignoring notification event ID (not an Int): $eventId" }
            return
        }
        log.i { "iOS: Setting notification event ID: $id" }
        notificationEventIdState.value = id
    } catch (t: Throwable) {
        println("iOS: Setting notification event ID: $eventId (failed: ${t.message})")
    }
}

// Public function for iOS to open a news article in-app from a notification tap.
// Mirrors the Android news_url/news_title deep link: drives the in-app NewsDetail screen
// (internal WKWebView) instead of opening the URL in Safari. Blank/missing url is ignored.
fun setNotificationNews(url: String?, title: String?) {
    try {
        if (url.isNullOrBlank()) {
            log.w { "iOS: Ignoring notification news (blank url)" }
            return
        }
        log.i { "iOS: Setting notification news url: $url" }
        notificationNewsUrlState.value = url
        notificationNewsTitleState.value = title
    } catch (t: Throwable) {
        println("iOS: Setting notification news url: $url (failed: ${t.message})")
    }
}

fun MainViewController() = ComposeUIViewController {
    // Initialize BuildConfig and Koin once before the app starts
    if (!koinInitialized) {
        initializeBuildConfig()
        // Register CrashKiOS unhandled exception hook so Kotlin crashes on iOS
        // produce readable stack traces in Crashlytics
        setupCrashlyticsExceptionHook()
        // Initialize diagnostics file store before SpaceLogger so the writer is included
        me.calebjones.spacelaunchnow.util.logging.DiagnosticsLog.initialize(
            me.calebjones.spacelaunchnow.util.logging.IosDiagnosticsFileStore()
        )
        // Initialize SpaceLogger before any logging calls
        SpaceLogger.initialize()
        startKoin(koinConfig)
        koinInitialized = true

        // Re-initialize SpaceLogger with LoggingPreferences to enable dynamic severity updates
        try {
            val loggingPrefs =
                getKoin().get<me.calebjones.spacelaunchnow.util.logging.LoggingPreferences>()
            SpaceLogger.initialize(loggingPreferences = loggingPrefs)
            log.d { "✅ SpaceLogger preferences observer configured" }
        } catch (e: Exception) {
            log.w(e) { "Failed to configure SpaceLogger preferences observer" }
        }

        // Always initialize Datadog; whether logs UPLOAD is governed by TrackingConsent,
        // which DiagnosticLevelController derives from the user's Diagnostic Logging setting.
        try {
            val loggingPrefs =
                getKoin().get<me.calebjones.spacelaunchnow.util.logging.LoggingPreferences>()
            val debugPrefs =
                getKoin().get<me.calebjones.spacelaunchnow.data.storage.DebugPreferences>()

            val sampleRate = runBlocking {
                debugPrefs.debugSettingsFlow.first().datadogSampleRate
            }
            log.d { "Initializing Datadog (consent-based) with ${sampleRate.toInt()}% sample rate..." }
            initializeDatadog(
                context = null,
                sampleRate = sampleRate,
                debugPreferences = debugPrefs
            )
            me.calebjones.spacelaunchnow.util.logging.DiagnosticLevelController.start(loggingPrefs)
            log.d { "✅ Datadog initialized; consent controller started" }
        } catch (e: Exception) {
            log.e(e) { "❌ Failed to initialize Datadog" }
        }

        // Initialize Billing and Subscription system on background thread
        // NOTE: repository.initialize() handles billingClient.initialize() and syncer.startSyncing()
        // internally — no need to call them separately here.
        CoroutineScope(Dispatchers.Default).launch {
            try {
                log.i { "iOS: 🚀 Starting billing and subscription initialization..." }

                val repository = getKoin().get<SubscriptionRepository>()
                repository.initialize()

                log.i { "iOS: 🎉 Billing and subscription systems initialized" }
            } catch (e: Exception) {
                log.e(e) { "iOS: ❌ Failed to initialize billing/subscription system" }
            }
        }

        // Sync notification filter prefs to the App Group on every launch so the Notification
        // Service Extension always filters against current settings — even when the app is
        // killed and the bridge would otherwise never have been written. Then log the live
        // state alongside the synced NSE prefs for confirmation.
        CoroutineScope(Dispatchers.Default).launch {
            try {
                me.calebjones.spacelaunchnow.data.notifications.IosNotificationBridge.refreshState()
                me.calebjones.spacelaunchnow.data.notifications.IosNotificationBridge.logStartupState()
            } catch (e: Exception) {
                log.e(e) { "iOS: ❌ Failed to sync/log notification startup state" }
            }
        }
    }

    val navigationDestination by navigationDestinationState
    val notificationLaunchId by notificationLaunchIdState
    val notificationEventId by notificationEventIdState
    val notificationNewsUrl by notificationNewsUrlState
    val notificationNewsTitle by notificationNewsTitleState

    // Collect useUtc and theme preferences for reactive updates
    val appPreferences = getKoin().get<AppPreferences>()
    val useUtc by appPreferences.useUtcFlow.collectAsState(initial = false)
    val themeOption by appPreferences.themeFlow.collectAsState(initial = me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption.System)

    // Notify Swift when theme changes so it can set overrideUserInterfaceStyle
    LaunchedEffect(themeOption) {
        val style = when (themeOption) {
            me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption.Light -> 1
            me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption.Dark -> 2
            else -> 0
        }
        themeChangeListener?.invoke(style)
    }

    SpaceLaunchNowApp(
        contextFactory = me.calebjones.spacelaunchnow.platform.ContextFactory(),
        themeOption = themeOption,
        useUtc = useUtc,
        navigationDestination = navigationDestination,
        onNavigationDestinationConsumed = {
            navigationDestinationState.value = null
        },
        notificationLaunchId = notificationLaunchId,
        onNotificationLaunchIdConsumed = {
            notificationLaunchIdState.value = null
        },
        notificationEventId = notificationEventId,
        onNotificationEventIdConsumed = {
            notificationEventIdState.value = null
        },
        notificationNewsUrl = notificationNewsUrl,
        notificationNewsTitle = notificationNewsTitle,
        onNotificationNewsConsumed = {
            notificationNewsUrlState.value = null
            notificationNewsTitleState.value = null
        }
    )
}

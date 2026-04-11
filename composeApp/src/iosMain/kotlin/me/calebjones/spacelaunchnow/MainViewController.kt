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
import co.touchlab.kermit.Severity
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
    } catch (e: Exception) {
        // Fallback: set state even if logging fails (e.g. SpaceLogger not yet initialized)
        println("iOS: Setting navigation destination: $destination (logger unavailable: ${e.message})")
        navigationDestinationState.value = destination
    }
}

// Public function for iOS to trigger navigation from notification
fun setNotificationLaunchId(launchId: String?) {
    try {
        log.i { "iOS: Setting notification launch ID: $launchId" }
        notificationLaunchIdState.value = launchId
    } catch (e: Exception) {
        // Fallback: set state even if logging fails (e.g. SpaceLogger not yet initialized
        // during cold-start from notification tap before MainViewController composable runs)
        println("iOS: Setting notification launch ID: $launchId (logger unavailable: ${e.message})")
        notificationLaunchIdState.value = launchId
    }
}

fun MainViewController() = ComposeUIViewController {
    // Initialize BuildConfig and Koin once before the app starts
    if (!koinInitialized) {
        initializeBuildConfig()
        // Register CrashKiOS unhandled exception hook so Kotlin crashes on iOS
        // produce readable stack traces in Crashlytics
        setupCrashlyticsExceptionHook()
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

        // Initialize Datadog analytics (matching Android pattern)
        try {
            val loggingPrefs =
                getKoin().get<me.calebjones.spacelaunchnow.util.logging.LoggingPreferences>()
            val debugPrefs =
                getKoin().get<me.calebjones.spacelaunchnow.data.storage.DebugPreferences>()

            val consoleSeverity = runBlocking {
                loggingPrefs.getConsoleSeverity().first()
            }
            val sampleRate = runBlocking {
                debugPrefs.debugSettingsFlow.first().datadogSampleRate
            }

            if (consoleSeverity <= Severity.Debug || me.calebjones.spacelaunchnow.util.BuildConfig.IS_DEBUG) {
                log.d { "Initializing Datadog (diagnostic logging enabled) with ${sampleRate.toInt()}% sample rate..." }
                initializeDatadog(
                    context = null,
                    sampleRate = sampleRate,
                    debugPreferences = debugPrefs
                )
                log.d { "✅ Datadog initialized successfully" }
            } else {
                log.i { "⏭️ Datadog initialization skipped (diagnostic logging disabled)" }
            }
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
    }

    val navigationDestination by navigationDestinationState
    val notificationLaunchId by notificationLaunchIdState

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
        }
    )
}

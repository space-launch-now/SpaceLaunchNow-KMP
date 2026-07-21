package me.calebjones.spacelaunchnow

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.analytics.initializeDatadog
import me.calebjones.spacelaunchnow.data.billing.BillingManager
import me.calebjones.spacelaunchnow.data.notifications.NotificationDisplayHelper
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.di.koinConfig
import me.calebjones.spacelaunchnow.util.initializeBuildConfig
import me.calebjones.spacelaunchnow.util.logging.LoggingPreferences
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import me.calebjones.spacelaunchnow.util.logging.logger
import me.calebjones.spacelaunchnow.workers.WidgetUpdateWorker
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.includes
import java.util.concurrent.TimeUnit

class MainApplication : Application() {

    // Inject dependencies for initialization
    private val billingManager: BillingManager by inject()

    private val log by lazy { logger() }

    override fun onCreate() {
        super.onCreate()
        me.calebjones.spacelaunchnow.util.ShareContextHolder.appContext = applicationContext
        instance = this

        // Initialize logging FIRST - before any other initialization
        me.calebjones.spacelaunchnow.util.logging.DiagnosticsLog.initialize(
            me.calebjones.spacelaunchnow.util.logging.AndroidDiagnosticsFileStore(this)
        )
        SpaceLogger.initialize()
        log.i { "=== Starting Application onCreate ===" }

        // Explicitly enable Crashlytics collection and verify initialization
        try {
            val crashlytics = com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
            crashlytics.setCrashlyticsCollectionEnabled(true)
            crashlytics.sendUnsentReports()
            val variant = if (me.calebjones.spacelaunchnow.BuildConfig.IS_DEBUG) "debug" else "release"
            crashlytics.setCustomKey("app_variant", variant)
            crashlytics.log("Crashlytics initialized in MainApplication.onCreate ($variant)")
            log.i { "\u2705 Firebase Crashlytics initialized and collection enabled ($variant)" }
        } catch (e: Exception) {
            log.e(e) { "\u274C Firebase Crashlytics initialization failed" }
        }

        // Initialize BuildConfig FIRST before Koin to set DEBUG flag
        initializeBuildConfig()
        log.d { "BuildConfig initialized, IS_DEBUG = ${me.calebjones.spacelaunchnow.util.BuildConfig.IS_DEBUG}" }

        // Initialize AppDirectories for KStore file storage
        log.d { "Initializing AppDirectories..." }
        me.calebjones.spacelaunchnow.data.subscription.AppDirectories.initialize(this)
        log.d { "✅ AppDirectories initialized" }

        // Initialize ExternalLinkHandler for feedback/support
        log.d { "Initializing ExternalLinkHandler..." }
        me.calebjones.spacelaunchnow.util.ExternalLinkHandler.initialize(this)
        log.d { "✅ ExternalLinkHandler initialized" }

        // Now start Koin - BuildConfig.IS_DEBUG is now set
        log.d { "Starting Koin..." }
        try {
            val koin = startKoin {
                androidLogger(Level.DEBUG)
                androidContext(this@MainApplication)
                includes(koinConfig)
            }

            log.d { "Koin started successfully" }

            // Configure Coil ImageLoader with memory-aware settings for low-RAM devices
            val customImageLoader = koin.koin.getOrNull<coil3.ImageLoader>()
            if (customImageLoader != null) {
                coil3.SingletonImageLoader.setSafe { customImageLoader }
                log.d { "✅ Custom Coil ImageLoader configured (memory-optimized)" }
            } else {
                log.d { "Using default Coil ImageLoader" }
            }

            // Test if NotificationRepository is registered
            val notificationRepo = koin.koin.getOrNull<NotificationRepository>()
            if (notificationRepo != null) {
                log.d { "✅ NotificationRepository is registered" }
            } else {
                log.e { "❌ NotificationRepository is NOT registered!" }
            }

        } catch (e: Exception) {
            log.e(e) { "Failed to start Koin" }
            throw e
        }

        // Always initialize Datadog; upload is governed by TrackingConsent applied by
        // DiagnosticLevelController from the user's Diagnostic Logging setting.
        log.d { "Initializing Datadog (consent-based)..." }
        val loggingPrefsForController = try {
            val loggingPrefs = getKoin().get<LoggingPreferences>()
            val debugPrefs =
                getKoin().get<me.calebjones.spacelaunchnow.data.storage.DebugPreferences>()

            initializeDatadog(
                context = this,
                sampleRate = null, // resolves to 100f default inside; debug slider overrides via observer
                debugPreferences = debugPrefs
            )
            log.d { "✅ Datadog initialized (consent-based)" }
            loggingPrefs
        } catch (e: Exception) {
            log.e(e) { "❌ Failed to initialize Datadog" }
            // Don't crash the app if Datadog fails
            null
        }
        try {
            val prefs = loggingPrefsForController ?: getKoin().get<LoggingPreferences>()
            me.calebjones.spacelaunchnow.util.logging.DiagnosticLevelController.start(prefs)
        } catch (e: Exception) {
            log.e(e) { "❌ Failed to start DiagnosticLevelController" }
        }

        // Initialize Billing and Subscription system after Koin is ready
        log.d { "Initializing Billing and Subscription system..." }

        @Suppress("OPT_IN_USAGE")
        kotlinx.coroutines.GlobalScope.launch {
            try {
                log.d { "🚀 Starting billing and subscription initialization..." }

                // Step 1: Initialize BillingManager (RevenueCat)
                billingManager.initialize(appUserId = null)
                log.d { "✅ BillingManager initialized successfully" }

                // Step 2: Initialize and start SubscriptionSyncer
                // This listens to billing state changes and persists to LocalSubscriptionStorage
                val syncer =
                    getKoin().get<me.calebjones.spacelaunchnow.data.subscription.SubscriptionSyncer>()
                syncer.startSyncing()
                log.d { "✅ SubscriptionSyncer started successfully" }

                // Step 3: Initialize SubscriptionRepository (loads cached state)
                val repository =
                    getKoin().get<me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository>()
                repository.initialize()
                log.d { "✅ SubscriptionRepository initialized successfully" }

                // Step 4: Force initial sync to ensure purchase state is persisted
                syncer.syncNow()
                log.d { "✅ Initial subscription sync complete" }

                // Step 4b: Start RevenueCat attributes syncer
                val rcSyncer =
                    getKoin().get<me.calebjones.spacelaunchnow.data.billing.RevenueCatAttributesSyncer>()
                val tempAccess =
                    getKoin().get<me.calebjones.spacelaunchnow.data.storage.TemporaryPremiumAccess>()
                val appPrefs =
                    getKoin().get<me.calebjones.spacelaunchnow.data.storage.AppPreferences>()
                val themePrefs =
                    getKoin().get<me.calebjones.spacelaunchnow.data.storage.ThemePreferences>()

                rcSyncer.start(
                    scope = kotlinx.coroutines.GlobalScope,
                    subscriptionStateFlow = kotlinx.coroutines.flow.flow {
                        repository.state.collect {
                            emit(it.subscriptionType.name.lowercase())
                        }
                    },
                    themeModeFlow = kotlinx.coroutines.flow.flow {
                        appPrefs.themeFlow.collect { emit(it.name.lowercase()) }
                    },
                    hasCustomThemeFlow = kotlinx.coroutines.flow.flow {
                        themePrefs.customPrimaryColorFlow.collect { emit(it != null) }
                    },
                    grantsTotalFlow = tempAccess.grantsTotalFlow,
                    adsShownTotalFlow = tempAccess.adsShownTotalFlow,
                    tempAccessActiveFlow = kotlinx.coroutines.flow.flow {
                        tempAccess.accessChangeTrigger.collect {
                            val active =
                                tempAccess.hasTemporaryAccess(
                                    me.calebjones.spacelaunchnow.data.model.PremiumFeature.CUSTOM_THEMES
                                ) || tempAccess.hasTemporaryAccess(
                                    me.calebjones.spacelaunchnow.data.model.PremiumFeature.ADVANCED_WIDGETS
                                ) || tempAccess.hasTemporaryAccess(
                                    me.calebjones.spacelaunchnow.data.model.PremiumFeature.WIDGETS_CUSTOMIZATION
                                )
                            emit(active)
                        }
                    },
                )
                log.d { "✅ RevenueCatAttributesSyncer started" }

                // Step 4c: Forward FCM token to RevenueCat for re-engagement campaigns.
                try {
                    val pushMessaging =
                        getKoin().get<me.calebjones.spacelaunchnow.data.notifications.PushMessaging>()
                    val fcmToken = pushMessaging.getToken().getOrNull()
                    if (!fcmToken.isNullOrBlank()) {
                        val rcAttrs =
                            getKoin().get<me.calebjones.spacelaunchnow.data.billing.RevenueCatAttributes>()
                        rcAttrs.setPushToken(fcmToken)
                        me.calebjones.spacelaunchnow.util.logging.PushDiagnostics.recordForwardedToRc()
                        log.i { "✅ FCM token forwarded to RevenueCat" }
                    } else {
                        me.calebjones.spacelaunchnow.util.logging.PushDiagnostics
                            .recordForwardSkipped("token_unavailable")
                        log.w { "FCM token not available yet; skipping RC push token set" }
                    }
                } catch (e: Exception) {
                    me.calebjones.spacelaunchnow.util.logging.PushDiagnostics
                        .recordForwardSkipped("exception: ${e.message}")
                    log.w(e) { "Failed to forward FCM token to RevenueCat" }
                }

                // Step 5: Start Wear OS entitlement pusher (observes state changes)
                val wearPusher = getKoin().get<me.calebjones.spacelaunchnow.sync.WearEntitlementPusher>()
                wearPusher.start()
                log.d { "✅ WearEntitlementPusher started" }

                // Step 6: Start Wear OS filter sync pusher (re-syncs launches when user changes filters)
                val wearFilterPusher = getKoin().get<me.calebjones.spacelaunchnow.sync.WearFilterSyncPusher>()
                wearFilterPusher.start()
                log.d { "✅ WearFilterSyncPusher started" }

                log.i { "🎉 All billing and subscription systems initialized" }
            } catch (e: Exception) {
                log.e(e) { "❌ Failed to initialize Billing/Subscription system" }
                // Don't crash the app if billing fails
            }
        }

        log.d { "Creating notification channels..." }

        // Create notification channels for Android O+
        NotificationDisplayHelper.createNotificationChannels(this)

        log.d { "Scheduling widget updates..." }

        // Schedule widget updates
        scheduleWidgetUpdates()

        log.i { "=== Application onCreate complete ===" }
    }

    private fun scheduleWidgetUpdates() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val widgetUpdateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            15, TimeUnit.MINUTES // Update every 15 minutes
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "widget_update_work",
            ExistingPeriodicWorkPolicy.KEEP,
            widgetUpdateRequest
        )
    }

    companion object {
        var instance: MainApplication? = null
            private set
    }
}
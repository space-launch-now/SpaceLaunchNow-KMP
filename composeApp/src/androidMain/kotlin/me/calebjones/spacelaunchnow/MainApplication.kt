package me.calebjones.spacelaunchnow

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.first
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
        instance = this

        // Initialize logging FIRST - before any other initialization
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

        // Re-initialize SpaceLogger with LoggingPreferences to enable dynamic severity updates
        log.d { "Re-initializing SpaceLogger with LoggingPreferences..." }
        try {
            val loggingPrefs = getKoin().get<LoggingPreferences>()
            SpaceLogger.initialize(loggingPreferences = loggingPrefs)
            log.d { "✅ SpaceLogger preferences observer configured" }
        } catch (e: Exception) {
            log.w(e) { "Failed to configure SpaceLogger preferences observer" }
        }

        // Initialize Datadog analytics using KMP SDK
        // Use safe defaults during startup to avoid blocking main thread, then reconfigure in background
        log.d { "Initializing Datadog with safe startup defaults..." }
        try {
            val loggingPrefs = getKoin().get<LoggingPreferences>()
            val debugPrefs =
                getKoin().get<me.calebjones.spacelaunchnow.data.storage.DebugPreferences>()

            // Use safe defaults during startup to avoid runBlocking calls
            // These will be reconfigured in background if user preferences differ
            val defaultSeverity = co.touchlab.kermit.Severity.Warn
            val defaultSampleRate = 75f

            // Initialize Datadog with conservative defaults (always initialize in debug builds)
            if (BuildConfig.IS_DEBUG) {
                log.d { "Debug build - initializing Datadog with ${defaultSampleRate.toInt()}% sample rate..." }
                initializeDatadog(
                    context = this,
                    sampleRate = defaultSampleRate,
                    debugPreferences = debugPrefs
                )
                log.d { "✅ Datadog initialized (debug mode)" }
            } else {
                // For release builds: defer Datadog initialization to background to avoid startup blocking
                // This checks actual user preferences asynchronously
                @Suppress("OPT_IN_USAGE")
                kotlinx.coroutines.GlobalScope.launch {
                    try {
                        val consoleSeverity = loggingPrefs.getConsoleSeverity().first()
                        val sampleRate = debugPrefs.debugSettingsFlow.first().datadogSampleRate
                        
                        if (consoleSeverity <= co.touchlab.kermit.Severity.Debug) {
                            log.d { "User has diagnostic logging enabled - initializing Datadog deferred with ${sampleRate.toInt()}% sample rate" }
                            initializeDatadog(
                                context = this@MainApplication,
                                sampleRate = sampleRate,
                                debugPreferences = debugPrefs
                            )
                            log.d { "✅ Datadog initialized (deferred)" }
                        } else {
                            log.i { "⏭️ Datadog skipped (diagnostic logging disabled - saves costs)" }
                        }
                    } catch (e: Exception) {
                        log.w(e) { "Failed to check Datadog initialization preferences" }
                    }
                }
            }
        } catch (e: Exception) {
            log.e(e) { "❌ Failed to initialize Datadog" }
            // Don't crash the app if Datadog fails
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
package me.calebjones.spacelaunchnow

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
        // IMPORTANT: Only initialize if diagnostic logging is enabled to prevent excessive costs
        log.d { "Checking Datadog initialization requirements..." }
        try {
            val loggingPrefs = getKoin().get<LoggingPreferences>()
            val debugPrefs =
                getKoin().get<me.calebjones.spacelaunchnow.data.storage.DebugPreferences>()

            val consoleSeverity = runBlocking {
                loggingPrefs.getConsoleSeverity().first()
            }
            val sampleRate = runBlocking {
                debugPrefs.debugSettingsFlow.first().datadogSampleRate
            }

            // Initialize Datadog if console logging is more verbose than production default (Warn)
            if (consoleSeverity <= co.touchlab.kermit.Severity.Debug || BuildConfig.IS_DEBUG) {
                log.d { "Initializing Datadog (diagnostic logging enabled) with ${sampleRate.toInt()}% sample rate..." }
                initializeDatadog(
                    context = this,
                    sampleRate = sampleRate,
                    debugPreferences = debugPrefs
                )
                log.d { "✅ Datadog initialized successfully" }
            } else {
                log.i { "⏭️ Datadog initialization skipped (diagnostic logging disabled - saves costs)" }
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
package me.calebjones.spacelaunchnow

import android.app.Application
import android.util.Log
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

    private val log = logger()

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize logging FIRST - before any other initialization
        SpaceLogger.initialize()
        log.i { "=== Starting Application onCreate ===" }

        // Initialize BuildConfig FIRST before Koin to set DEBUG flag
        initializeBuildConfig()
        log.d { "BuildConfig initialized, IS_DEBUG = ${me.calebjones.spacelaunchnow.util.BuildConfig.IS_DEBUG}" }

        // Initialize AppDirectories for KStore file storage
        log.d { "Initializing AppDirectories..." }
        me.calebjones.spacelaunchnow.data.subscription.AppDirectories.initialize(this)
        log.d { "✅ AppDirectories initialized" }

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

        // Initialize Datadog analytics using KMP SDK
        log.d { "Initializing Datadog..." }
        try {
            // initializeDatadog reads from .env file via EnvironmentManager
            initializeDatadog(context = this)
            log.d { "✅ Datadog initialized successfully" }
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
                val syncer = getKoin().get<me.calebjones.spacelaunchnow.data.subscription.SubscriptionSyncer>()
                syncer.startSyncing()
                log.d { "✅ SubscriptionSyncer started successfully" }

                // Step 3: Initialize SubscriptionRepository (loads cached state)
                val repository = getKoin().get<me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository>()
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
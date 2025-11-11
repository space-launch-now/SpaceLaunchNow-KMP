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
import me.calebjones.spacelaunchnow.workers.WidgetUpdateWorker
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

    override fun onCreate() {
        super.onCreate()
        instance = this

        Log.d("MainApplication", "=== Starting Application onCreate ===")

        // Initialize BuildConfig FIRST before Koin to set DEBUG flag
        initializeBuildConfig()
        Log.d(
            "MainApplication",
            "BuildConfig initialized, IS_DEBUG = ${me.calebjones.spacelaunchnow.util.BuildConfig.IS_DEBUG}"
        )

        // Initialize AppDirectories for KStore file storage
        Log.d("MainApplication", "Initializing AppDirectories...")
        me.calebjones.spacelaunchnow.data.subscription.AppDirectories.initialize(this)
        Log.d("MainApplication", "✅ AppDirectories initialized")

        // Now start Koin - BuildConfig.IS_DEBUG is now set
        Log.d("MainApplication", "Starting Koin...")
        try {
            val koin = startKoin {
                androidLogger(Level.DEBUG)
                androidContext(this@MainApplication)
                includes(koinConfig)
            }

            Log.d("MainApplication", "Koin started successfully")

            // Test if NotificationRepository is registered
            val notificationRepo = koin.koin.getOrNull<NotificationRepository>()
            if (notificationRepo != null) {
                Log.d("MainApplication", "✅ NotificationRepository is registered")
            } else {
                Log.e("MainApplication", "❌ NotificationRepository is NOT registered!")
            }

        } catch (e: Exception) {
            Log.e("MainApplication", "Failed to start Koin", e)
            throw e
        }

        // Initialize Datadog analytics using KMP SDK
        Log.d("MainApplication", "Initializing Datadog...")
        try {
            // initializeDatadog reads from .env file via EnvironmentManager
            initializeDatadog(context = this)
            Log.d("MainApplication", "✅ Datadog initialized successfully")
        } catch (e: Exception) {
            Log.e("MainApplication", "❌ Failed to initialize Datadog", e)
            // Don't crash the app if Datadog fails
        }

        // Initialize Billing after Koin is ready
        Log.d("MainApplication", "Initializing Billing...")

        @Suppress("OPT_IN_USAGE")
        kotlinx.coroutines.GlobalScope.launch {
            try {
                // Initialize with null appUserId for anonymous user
                billingManager.initialize(appUserId = null)
                Log.d("MainApplication", "✅ Billing initialized successfully")
            } catch (e: Exception) {
                Log.e("MainApplication", "❌ Failed to initialize Billing", e)
                // Don't crash the app if billing fails
            }
        }

        Log.d("MainApplication", "Creating notification channels...")

        // Create notification channels for Android O+
        NotificationDisplayHelper.createNotificationChannels(this)

        Log.d("MainApplication", "Scheduling widget updates...")

        // Schedule widget updates
        scheduleWidgetUpdates()

        Log.d("MainApplication", "=== Application onCreate complete ===")
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
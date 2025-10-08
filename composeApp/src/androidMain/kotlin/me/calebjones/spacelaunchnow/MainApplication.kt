package me.calebjones.spacelaunchnow

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.di.koinConfig
import me.calebjones.spacelaunchnow.workers.WidgetUpdateWorker
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.util.concurrent.TimeUnit
import me.calebjones.spacelaunchnow.util.initializeBuildConfig
import org.koin.dsl.includes

class MainApplication : Application() {

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
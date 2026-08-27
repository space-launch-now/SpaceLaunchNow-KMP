package me.calebjones.spacelaunchnow.wear

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import co.touchlab.kermit.Logger
import me.calebjones.spacelaunchnow.wear.di.wearModule
import me.calebjones.spacelaunchnow.wear.worker.WatchDataRefreshWorker
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.util.concurrent.TimeUnit

class WearApplication : Application(), Configuration.Provider {

    /**
     * WorkManager's androidx.startup auto-initializer is removed in AndroidManifest.xml
     * (issue #181), so WorkManager initializes on demand from this configuration the first
     * time [WorkManager.getInstance] is called. This override is mandatory once auto-init
     * is gone: without it every getInstance() call throws IllegalStateException on every
     * watch, not just the firmware the manifest change is protecting against.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    override fun onCreate() {
        super.onCreate()
        initKoin()
        initWorkManager()
        Logger.i { "WearApplication initialized" }
    }

    private fun initKoin() {
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@WearApplication)
            modules(wearModule)
        }
    }

    private fun initWorkManager() {
        val refreshRequest = PeriodicWorkRequestBuilder<WatchDataRefreshWorker>(30, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()
        // WorkManager initializes lazily here rather than in InitializationProvider, so a
        // platform-level failure surfaces on this call instead of killing the process at
        // startup. runCatching is deliberate: the failure mode seen on the phone (issue
        // #181) is a NoSuchMethodError, an Error, which catch (e: Exception) would miss.
        runCatching {
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "watch_data_refresh",
                ExistingPeriodicWorkPolicy.KEEP,
                refreshRequest
            )
        }.onSuccess {
            Logger.i { "WorkManager periodic refresh scheduled" }
        }.onFailure { throwable ->
            Logger.e(throwable) { "WorkManager unavailable; periodic watch refresh not scheduled" }
        }
    }
}

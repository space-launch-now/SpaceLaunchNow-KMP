package me.calebjones.spacelaunchnow.wear

import android.app.Application
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

class WearApplication : Application() {
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
        val workManager = WorkManager.getInstance(this)
        val refreshRequest = PeriodicWorkRequestBuilder<WatchDataRefreshWorker>(30, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()
        workManager.enqueueUniquePeriodicWork(
            "watch_data_refresh",
            ExistingPeriodicWorkPolicy.KEEP,
            refreshRequest
        )
        Logger.i { "WorkManager periodic refresh scheduled" }
    }
}

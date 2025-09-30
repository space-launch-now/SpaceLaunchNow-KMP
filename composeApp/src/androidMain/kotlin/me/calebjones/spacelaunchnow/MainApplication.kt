package me.calebjones.spacelaunchnow

import android.app.Application
import org.koin.core.context.startKoin
import me.calebjones.spacelaunchnow.di.koinConfig
import me.calebjones.spacelaunchnow.util.initializeBuildConfig

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize BuildConfig first to set DEBUG flag
        initializeBuildConfig()

        // Initialize Koin with the shared configuration
        startKoin(koinConfig)
    }

    companion object {
        var instance : Application? = null
    }
}
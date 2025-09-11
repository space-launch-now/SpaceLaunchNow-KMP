package me.calebjones.spacelaunchnow

import android.app.Application
import org.koin.core.context.startKoin
import me.calebjones.spacelaunchnow.di.koinConfig

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize Koin with the shared configuration
        startKoin(koinConfig)
    }

    companion object {
        var instance : Application? = null
    }
}
package me.calebjones.spacelaunchnow

import android.app.Application
import me.calebjones.spacelaunchnow.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import me.calebjones.spacelaunchnow.di.nativeConfig

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        var instance : Application? = null
    }
}
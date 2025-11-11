package me.calebjones.spacelaunchnow.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import me.calebjones.spacelaunchnow.data.storage.createDataStore
import me.calebjones.spacelaunchnow.data.storage.createDebugDataStore
import me.calebjones.spacelaunchnow.data.storage.createAppSettingsDataStore
import me.calebjones.spacelaunchnow.data.storage.createSubscriptionDataStore
import me.calebjones.spacelaunchnow.database.DatabaseDriverFactory
import me.calebjones.spacelaunchnow.widgets.PlatformWidgetUpdater
import me.calebjones.spacelaunchnow.util.LaunchSharingService
import me.calebjones.spacelaunchnow.util.AndroidSharingService

val androidModule = module {
    single { createDataStore(androidContext()) }
    single(named("DebugDataStore")) { createDebugDataStore(androidContext()) }
    single(named("AppSettingsDataStore")) { createAppSettingsDataStore(androidContext()) }
    single(named("SubscriptionDataStore")) { createSubscriptionDataStore(androidContext()) }
    
    // Database driver factory
    single { DatabaseDriverFactory(androidContext()) }
    
    // Platform-specific widget updater (Android only)
    single { PlatformWidgetUpdater(context = androidContext()) }
    
    // Sharing functionality
    single<LaunchSharingService> { AndroidSharingService(androidContext()) }
}

actual fun nativeConfig(): KoinAppDeclaration = {
    // Android context is set in MainApplication.onCreate via androidContext()
    // Just return the androidModule here
    modules(androidModule)
}
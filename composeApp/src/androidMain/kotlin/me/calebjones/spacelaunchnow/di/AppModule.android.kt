package me.calebjones.spacelaunchnow.di

import me.calebjones.spacelaunchnow.data.billing.BillingManager
import me.calebjones.spacelaunchnow.data.billing.createBillingManager
import me.calebjones.spacelaunchnow.data.storage.createAppSettingsDataStore
import me.calebjones.spacelaunchnow.data.storage.createDataStore
import me.calebjones.spacelaunchnow.data.storage.createDebugDataStore
import me.calebjones.spacelaunchnow.database.DatabaseDriverFactory
import me.calebjones.spacelaunchnow.util.AndroidSharingService
import me.calebjones.spacelaunchnow.util.LaunchSharingService
import me.calebjones.spacelaunchnow.widgets.PlatformWidgetUpdater
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val androidModule = module {
    single { createDataStore(androidContext()) }
    single(named("DebugDataStore")) { createDebugDataStore(androidContext()) }
    single(named("AppSettingsDataStore")) { createAppSettingsDataStore(androidContext()) }

    // Platform-specific BillingManager
    single<BillingManager> {
        createBillingManager(androidContext())
    }
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
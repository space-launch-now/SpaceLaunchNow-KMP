package me.calebjones.spacelaunchnow.di

import me.calebjones.spacelaunchnow.data.billing.BillingManager
import me.calebjones.spacelaunchnow.data.billing.createBillingManager
import me.calebjones.spacelaunchnow.data.storage.createDataStore
import me.calebjones.spacelaunchnow.data.storage.createDebugDataStore
import me.calebjones.spacelaunchnow.data.storage.createNotificationHistoryDataStore
import me.calebjones.spacelaunchnow.database.DatabaseDriverFactory
import me.calebjones.spacelaunchnow.rating.AppRatingManager
import me.calebjones.spacelaunchnow.util.LaunchSharingService
import me.calebjones.spacelaunchnow.util.createPlatformSharingService
import me.calebjones.spacelaunchnow.widgets.PlatformWidgetUpdater
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val iosModule = module {
    single { createDataStore("notification_settings") }
    single(named("DebugDataStore")) { createDebugDataStore() }
    single(named("AppSettingsDataStore")) { createDataStore("app_settings") }
    single(named("NotificationHistoryDataStore")) { createNotificationHistoryDataStore() }

    // Billing
    single<BillingManager> { createBillingManager() }
    // Database driver factory
    single { DatabaseDriverFactory() }

    // Sharing functionality
    single<LaunchSharingService> { createPlatformSharingService() }

    // App rating manager
    single { AppRatingManager() }

    // Widget timeline reloader — triggers WidgetCenter.shared.reloadAllTimelines() via Swift bridge
    single { PlatformWidgetUpdater(context = null) }
}

actual fun nativeConfig(): KoinAppDeclaration = {
    modules(iosModule)
}
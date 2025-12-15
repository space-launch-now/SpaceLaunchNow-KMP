package me.calebjones.spacelaunchnow.di

import me.calebjones.spacelaunchnow.data.billing.BillingManager
import me.calebjones.spacelaunchnow.data.billing.createBillingManager
import me.calebjones.spacelaunchnow.data.storage.createDataStore
import me.calebjones.spacelaunchnow.data.storage.createDebugDataStore
import me.calebjones.spacelaunchnow.database.DatabaseDriverFactory
import me.calebjones.spacelaunchnow.rating.AppRatingManager
import me.calebjones.spacelaunchnow.util.LaunchSharingService
import me.calebjones.spacelaunchnow.util.createPlatformSharingService
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.logger.slf4jLogger

val desktopModule = module {
    single { createDataStore("notification_settings") }
    single(named("DebugDataStore")) { createDebugDataStore() }
    single(named("AppSettingsDataStore")) { createDataStore("app_settings") }

    // Billing (no-op for Desktop)
    single<BillingManager> { createBillingManager() }
    // Database driver factory
    single { DatabaseDriverFactory() }

    // Desktop sharing functionality
    single<LaunchSharingService> { createPlatformSharingService() }

    // App rating manager (no-op for Desktop)
    single { AppRatingManager() }
}

actual fun nativeConfig(): KoinAppDeclaration = {
    slf4jLogger()
    modules(desktopModule)
}
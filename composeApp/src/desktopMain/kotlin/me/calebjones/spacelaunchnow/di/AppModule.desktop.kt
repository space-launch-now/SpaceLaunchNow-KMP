package me.calebjones.spacelaunchnow.di

import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.logger.slf4jLogger
import me.calebjones.spacelaunchnow.data.billing.BillingManager
import me.calebjones.spacelaunchnow.data.billing.createBillingManager
import me.calebjones.spacelaunchnow.data.storage.createDataStore
import me.calebjones.spacelaunchnow.data.storage.createDebugDataStore
import me.calebjones.spacelaunchnow.database.DatabaseDriverFactory
import me.calebjones.spacelaunchnow.util.LaunchSharingService
import me.calebjones.spacelaunchnow.util.createPlatformSharingService

val desktopModule = module {
    single { createDataStore("notification_settings") }
    single(named("DebugDataStore")) { createDebugDataStore() }
    single(named("AppSettingsDataStore")) { createDataStore("app_settings") }
    single(named("SubscriptionDataStore")) { createDataStore("subscription_settings") }
    
    // Billing (no-op for Desktop)
    single<BillingManager> { createBillingManager() }
    // Database driver factory
    single { DatabaseDriverFactory() }
    
    // Desktop sharing functionality
    single<LaunchSharingService> { createPlatformSharingService() }
}

actual fun nativeConfig(): KoinAppDeclaration = {
    slf4jLogger()
    modules(desktopModule)
}
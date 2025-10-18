package me.calebjones.spacelaunchnow.di

import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import me.calebjones.spacelaunchnow.data.storage.createDataStore
import me.calebjones.spacelaunchnow.data.storage.createDebugDataStore
import me.calebjones.spacelaunchnow.util.LaunchSharingService
import me.calebjones.spacelaunchnow.util.createPlatformSharingService

val iosModule = module {
    single { createDataStore("notification_settings") }
    single(named("DebugDataStore")) { createDebugDataStore() }
    single(named("AppSettingsDataStore")) { createDataStore("app_settings") }
    single(named("SubscriptionDataStore")) { createDataStore("subscription_settings") }
    
    // Sharing functionality
    single<LaunchSharingService> { createPlatformSharingService() }
}

actual fun nativeConfig(): KoinAppDeclaration = {
    modules(iosModule)
}
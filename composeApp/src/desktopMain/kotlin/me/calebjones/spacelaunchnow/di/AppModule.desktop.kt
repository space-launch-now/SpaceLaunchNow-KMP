package me.calebjones.spacelaunchnow.di

import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.logger.slf4jLogger
import me.calebjones.spacelaunchnow.data.storage.createDataStore
import me.calebjones.spacelaunchnow.data.storage.createDebugDataStore

val desktopModule = module {
    single { createDataStore("notification_settings") }
    single(named("DebugDataStore")) { createDebugDataStore() }
    single(named("AppSettingsDataStore")) { createDataStore("app_settings") }
    single(named("SubscriptionDataStore")) { createDataStore("subscription_settings") }
}

actual fun nativeConfig(): KoinAppDeclaration = {
    slf4jLogger()
    modules(desktopModule)
}
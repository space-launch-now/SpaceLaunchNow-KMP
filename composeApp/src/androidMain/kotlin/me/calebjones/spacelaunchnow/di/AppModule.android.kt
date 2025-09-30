package me.calebjones.spacelaunchnow.di

import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import me.calebjones.spacelaunchnow.MainApplication
import me.calebjones.spacelaunchnow.data.storage.createDataStore
import me.calebjones.spacelaunchnow.data.storage.createDebugDataStore
import me.calebjones.spacelaunchnow.data.storage.createAppSettingsDataStore

val androidModule = module {
    single { createDataStore(androidContext()) }
    single(named("DebugDataStore")) { createDebugDataStore(androidContext()) }
    single(named("AppSettingsDataStore")) { createAppSettingsDataStore(androidContext()) }
}

actual fun nativeConfig(): KoinAppDeclaration = {
    androidLogger()
    androidContext(MainApplication.instance ?: error("No Android application context set"))
    modules(androidModule)
}
package me.calebjones.spacelaunchnow.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import me.calebjones.spacelaunchnow.data.storage.createDataStore
import me.calebjones.spacelaunchnow.data.storage.createDebugDataStore
import me.calebjones.spacelaunchnow.data.storage.createAppSettingsDataStore

val androidModule = module {
    single { createDataStore(androidContext()) }
    single(named("DebugDataStore")) { createDebugDataStore(androidContext()) }
    single(named("AppSettingsDataStore")) { createAppSettingsDataStore(androidContext()) }
}

actual fun nativeConfig(): KoinAppDeclaration = {
    // Android context is set in MainApplication.onCreate via androidContext()
    // Just return the androidModule here
    modules(androidModule)
}
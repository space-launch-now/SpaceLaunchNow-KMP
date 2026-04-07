package me.calebjones.spacelaunchnow.di

import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManagerImpl
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsPreferences
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsProvider
import me.calebjones.spacelaunchnow.analytics.providers.ConsoleAnalyticsProvider
import me.calebjones.spacelaunchnow.getPlatform
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Koin module for the analytics pipeline.
 *
 * - [ConsoleAnalyticsProvider] is registered on all platforms but disabled by default
 *   on mobile (Android/iOS) to avoid verbose console analytics overhead in production.
 *   It is enabled automatically on Desktop where Firebase is unavailable.
 * - [FirebaseAnalyticsProvider] is registered per-platform in the platform AppModule
 *   (Android: [AppModule.android.kt], iOS: [AppModule.ios.kt]).
 * - [AnalyticsManagerImpl] collects ALL [AnalyticsProvider] bindings via [getAll].
 */
val analyticsModule = module {
    single<AnalyticsProvider>(named("console")) {
        ConsoleAnalyticsProvider(isEnabled = getPlatform().type.isDesktop)
    }

    single { AnalyticsPreferences(get(named("AppSettingsDataStore"))) }

    single<AnalyticsManager> {
        AnalyticsManagerImpl(
            providers = getAll<AnalyticsProvider>(),
            preferences = getOrNull<AnalyticsPreferences>()
        )
    }
}

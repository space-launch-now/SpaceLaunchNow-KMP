package me.calebjones.spacelaunchnow.di

import me.calebjones.spacelaunchnow.data.billing.BillingManager
import me.calebjones.spacelaunchnow.data.billing.createBillingManager
import me.calebjones.spacelaunchnow.data.storage.createAppSettingsDataStore
import me.calebjones.spacelaunchnow.data.storage.createDataStore
import me.calebjones.spacelaunchnow.data.storage.createDebugDataStore
import me.calebjones.spacelaunchnow.data.storage.createNotificationHistoryDataStore
import me.calebjones.spacelaunchnow.database.DatabaseDriverFactory
import me.calebjones.spacelaunchnow.rating.AppRatingManager
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsProvider
import me.calebjones.spacelaunchnow.analytics.providers.FirebaseAnalyticsProvider
import me.calebjones.spacelaunchnow.sync.PhoneDataLayerService
import me.calebjones.spacelaunchnow.sync.PhoneDataLayerSync
import me.calebjones.spacelaunchnow.sync.WearEntitlementPusher
import me.calebjones.spacelaunchnow.sync.WearFilterSyncPusher
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
    single(named("NotificationHistoryDataStore")) { createNotificationHistoryDataStore(androidContext()) }

    // Phone-to-Watch DataLayer sync
    single<PhoneDataLayerSync> { PhoneDataLayerService(androidContext(), get(), get(), get()) }

    // Observe subscription state and push entitlement changes to watch
    single { WearEntitlementPusher(get(), get(), kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default + kotlinx.coroutines.SupervisorJob())) }

    // Observe filter preference changes and push updated launch list to watch
    single { WearFilterSyncPusher(get(), get(), kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default + kotlinx.coroutines.SupervisorJob())) }

    // Platform-specific BillingManager
    single<BillingManager> {
        createBillingManager(androidContext(), getOrNull())
    }
    // Database driver factory
    single { DatabaseDriverFactory(androidContext()) }

    // Platform-specific widget updater (Android only)
    single { PlatformWidgetUpdater(context = androidContext()) }

    // Sharing functionality
    single<LaunchSharingService> { AndroidSharingService(androidContext()) }

    // App rating manager (Activity will be passed at call time)
    single { AppRatingManager() }

    // Firebase analytics provider (Android platform)
    single<AnalyticsProvider>(named("firebase")) { FirebaseAnalyticsProvider() }
}

actual fun nativeConfig(): KoinAppDeclaration = {
    // Android context is set in MainApplication.onCreate via androidContext()
    // Just return the androidModule here
    modules(androidModule)
}
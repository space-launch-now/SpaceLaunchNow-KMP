package me.calebjones.spacelaunchnow.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.revenuecat.purchases.kmp.Purchases
import me.calebjones.spacelaunchnow.UserViewModel
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.data.UserRepository
import me.calebjones.spacelaunchnow.data.UserRepositoryImpl
import me.calebjones.spacelaunchnow.data.billing.BillingClient
import me.calebjones.spacelaunchnow.data.billing.RevenueCatBillingClient
import me.calebjones.spacelaunchnow.data.billing.RevenueCatManager
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.data.preferences.WidgetPreferences
import me.calebjones.spacelaunchnow.data.repository.AgencyRepository
import me.calebjones.spacelaunchnow.data.repository.AgencyRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepository
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.data.repository.EventsRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.LaunchRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.data.repository.NotificationRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.RocketRepository
import me.calebjones.spacelaunchnow.data.repository.RocketRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.repository.SimpleSubscriptionRepository
import me.calebjones.spacelaunchnow.data.subscription.LocalSubscriptionStorage
import me.calebjones.spacelaunchnow.data.subscription.SubscriptionSyncer
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepository
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepositoryImpl
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.data.storage.NotificationPreferences
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.data.storage.SubscriptionStorage
import me.calebjones.spacelaunchnow.data.storage.TemporaryPremiumAccess
import me.calebjones.spacelaunchnow.data.storage.ThemePreferences
import me.calebjones.spacelaunchnow.database.DatabaseDriverFactory
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource
import me.calebjones.spacelaunchnow.database.EventLocalDataSource
import me.calebjones.spacelaunchnow.database.ArticleLocalDataSource
import me.calebjones.spacelaunchnow.database.UpdateLocalDataSource
import me.calebjones.spacelaunchnow.platform.ContextFactory
import me.calebjones.spacelaunchnow.ui.ads.GlobalAdManager
import me.calebjones.spacelaunchnow.ui.settings.ThemeCustomizationViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.AgencyViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.AppSettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.DebugSettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.EventViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.HomeViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.NextUpViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.RocketViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.ScheduleViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.SettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.UpdatesViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

expect fun nativeConfig(): KoinAppDeclaration

val koinConfig = koinConfiguration {
    includes(nativeConfig())
    modules(networkModule, apiModule, appModule, debugModule)
}

val appModule = module {
    singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
    viewModelOf(::UserViewModel)
    
    // Database and local data sources
    single { 
        val driver = get<DatabaseDriverFactory>().createDriver()
        SpaceLaunchDatabase(driver)
    }
    single { LaunchLocalDataSource(get()) }
    single { EventLocalDataSource(get()) }
    single { ArticleLocalDataSource(get()) }
    single { UpdateLocalDataSource(get()) }
    
    single<LaunchRepository> {
        LaunchRepositoryImpl(
            launchesApi = get(),
            agenciesApi = get(),
            appPreferences = get()
        )
    }
    viewModelOf(::LaunchViewModel)
    viewModelOf(::NextUpViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::ScheduleViewModel)
    singleOf(::UpdatesRepositoryImpl) { bind<UpdatesRepository>() }
    viewModelOf(::UpdatesViewModel)
    viewModelOf(::EventViewModel)
    viewModelOf(::AgencyViewModel)
    singleOf(::AgencyRepositoryImpl) { bind<AgencyRepository>() }
    singleOf(::ArticlesRepositoryImpl) { bind<ArticlesRepository>() }
    singleOf(::EventsRepositoryImpl) { bind<EventsRepository>() }
    singleOf(::RocketRepositoryImpl) { bind<RocketRepository>() }
    viewModelOf(::RocketViewModel)
    singleOf(::LaunchCache)

    // Global Ad Manager - Singleton managed by Koin
    single {
        GlobalAdManager(contextFactory = getOrNull<ContextFactory>()).also {
            it.initializeAndPreload()
        }
    }

    // Notification dependencies
    singleOf(::PushMessaging)
    singleOf(::NotificationPreferences)

    // New notification state storage
    single {
        val dataStore = get<DataStore<Preferences>>()
        NotificationStateStorage(dataStore)
    }

    // App settings dependencies
    single {
        val appDataStore = get<DataStore<Preferences>>(named("AppSettingsDataStore"))
        AppPreferences(appDataStore)
    }

    // Premium theme customization preferences
    single {
        val appDataStore = get<DataStore<Preferences>>(named("AppSettingsDataStore"))
        ThemePreferences(appDataStore)
    }

    // Widget appearance preferences
    single {
        val appDataStore = get<DataStore<Preferences>>(named("AppSettingsDataStore"))
        WidgetPreferences(appDataStore)
    }

    // NotificationRepository - new clean architecture
    single<NotificationRepository> {
        NotificationRepositoryImpl(
            pushMessaging = get(),
            storage = get<NotificationStateStorage>(),
            debugPreferences = getOrNull<DebugPreferences>()
        )
    }

    // Subscription dependencies
    single {
        val subscriptionDataStore = get<DataStore<Preferences>>(named("SubscriptionDataStore"))
        SubscriptionStorage(subscriptionDataStore)
    }

    // Temporary premium access for rewarded ads
    single {
        val appDataStore = get<DataStore<Preferences>>(named("AppSettingsDataStore"))
        TemporaryPremiumAccess(
            dataStore = appDataStore,
            themePreferences = get<ThemePreferences>(),
            widgetPreferences = get<WidgetPreferences>()
        )
    }

    // RevenueCat dependencies - initialize first
    singleOf(::RevenueCatManager)

    // BillingClient now uses RevenueCat instead of platform-specific implementations
    // Use lazy factory to avoid accessing Purchases.sharedInstance during Koin initialization
    single<BillingClient> {
        val revenueCatManager = get<RevenueCatManager>()
        BillingClient(
            revenueCatClient = RevenueCatBillingClient(
                revenueCatManager = revenueCatManager
            )
        )
    }

    // Local subscription storage (KStore)
    single { LocalSubscriptionStorage() }
    
    // Subscription syncer (handles RevenueCat sync)
    single {
        SubscriptionSyncer(
            localStorage = get(),
            revenueCatManager = get()
        )
    }

    // Simple subscription repository
    single<SubscriptionRepository> {
        SimpleSubscriptionRepository(
            localStorage = get(),
            syncer = get(),
            billingClient = get(),
            widgetPreferences = get(),
            platformWidgetUpdater = getOrNull(),
            temporaryPremiumAccess = get()
        )
    }

    // SubscriptionViewModel with RevenueCatManager
    single {
        SubscriptionViewModel(
            repository = get(),
            revenueCatManager = get()
        )
    }

    single { AppSettingsViewModel(appPreferences = get()) }
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ThemeCustomizationViewModel)
}

// Debug-only module - dependencies are always provided but only used when BuildConfig.isDebug is true
val debugModule = module {
    single {
        // Use a separate qualifier for debug DataStore
        val debugDataStore = get<DataStore<Preferences>>(named("DebugDataStore"))
        DebugPreferences(debugDataStore)
    }
    single {
        DebugSettingsViewModel(
            debugPreferences = get(),
            revenueCatManager = get(),
            launchRepository = get(),
            notificationRepository = get()
        )
    }
}
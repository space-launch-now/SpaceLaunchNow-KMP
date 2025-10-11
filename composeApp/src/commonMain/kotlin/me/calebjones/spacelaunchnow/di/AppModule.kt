package me.calebjones.spacelaunchnow.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import me.calebjones.spacelaunchnow.UserViewModel
import me.calebjones.spacelaunchnow.data.UserRepository
import me.calebjones.spacelaunchnow.data.UserRepositoryImpl
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.NextUpViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.HomeViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.UpdatesViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.EventViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.SettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.DebugSettingsViewModel
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.LaunchRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepository
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepository
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.data.repository.EventsRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.data.repository.NotificationRepositoryImpl
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.data.storage.NotificationPreferences
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.data.storage.SubscriptionStorage
import me.calebjones.spacelaunchnow.data.billing.BillingClient
import me.calebjones.spacelaunchnow.data.billing.createBillingClient
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepositoryImpl
import me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModel
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.ui.viewmodel.AppSettingsViewModel

expect fun nativeConfig() : KoinAppDeclaration

val koinConfig = koinConfiguration {
    includes(nativeConfig())
    modules(networkModule, apiModule, appModule, debugModule)
}

val appModule = module {
    singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
    viewModelOf(::UserViewModel)
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
    singleOf(::UpdatesRepositoryImpl) { bind<UpdatesRepository>() }
    viewModelOf(::UpdatesViewModel)
    viewModelOf(::EventViewModel)
    singleOf(::ArticlesRepositoryImpl) { bind<ArticlesRepository>() }
    singleOf(::EventsRepositoryImpl) { bind<EventsRepository>() }
    singleOf(::LaunchCache)

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

    single { createBillingClient() }

    single<SubscriptionRepository> {
        SubscriptionRepositoryImpl(
            billingClient = get(),
            storage = get<SubscriptionStorage>(),
            debugPreferences = get<DebugPreferences>()
        )
    }
    viewModelOf(::SubscriptionViewModel)
    
    single { AppSettingsViewModel(appPreferences = get()) }
    viewModelOf(::SettingsViewModel)
}

// Debug-only module - dependencies are always provided but only used when BuildConfig.isDebug is true
val debugModule = module {
    single {
        // Use a separate qualifier for debug DataStore
        val debugDataStore = get<DataStore<Preferences>>(named("DebugDataStore"))
        DebugPreferences(debugDataStore)
    }
    single { DebugSettingsViewModel(debugPreferences = get()) }
}
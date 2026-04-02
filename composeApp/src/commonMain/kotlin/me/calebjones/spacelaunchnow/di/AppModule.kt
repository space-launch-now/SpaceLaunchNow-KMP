package me.calebjones.spacelaunchnow.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import me.calebjones.spacelaunchnow.UserViewModel
import me.calebjones.spacelaunchnow.api.iss.IssTrackingRepository
import me.calebjones.spacelaunchnow.api.iss.IssTrackingRepositoryImpl
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.data.UserRepository
import me.calebjones.spacelaunchnow.data.UserRepositoryImpl
import me.calebjones.spacelaunchnow.data.billing.BillingClient
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.data.preferences.WidgetPreferences
import me.calebjones.spacelaunchnow.data.repository.AgencyRepository
import me.calebjones.spacelaunchnow.data.repository.AgencyRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepository
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.AstronautFilterRepository
import me.calebjones.spacelaunchnow.data.repository.AstronautFilterRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.AstronautRepository
import me.calebjones.spacelaunchnow.data.repository.AstronautRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.data.repository.EventsRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.InfoRepository
import me.calebjones.spacelaunchnow.data.repository.InfoRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.LaunchRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.LauncherConfigRepository
import me.calebjones.spacelaunchnow.data.repository.LauncherConfigRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.LauncherRepository
import me.calebjones.spacelaunchnow.data.repository.LauncherRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.data.repository.NotificationRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ProgramRepository
import me.calebjones.spacelaunchnow.data.repository.ProgramRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.RemoteConfigRepository
import me.calebjones.spacelaunchnow.data.repository.RemoteConfigRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.RocketFilterRepository
import me.calebjones.spacelaunchnow.data.repository.RocketFilterRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.RocketRepository
import me.calebjones.spacelaunchnow.data.repository.RocketRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ScheduleFilterRepository
import me.calebjones.spacelaunchnow.data.repository.ScheduleFilterRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.SimpleSubscriptionRepository
import me.calebjones.spacelaunchnow.data.repository.SpacecraftConfigRepository
import me.calebjones.spacelaunchnow.data.repository.SpacecraftConfigRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.SpacecraftRepository
import me.calebjones.spacelaunchnow.data.repository.SpacecraftRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.SpaceStationRepository
import me.calebjones.spacelaunchnow.data.repository.SpaceStationRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepository
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepositoryImpl
import me.calebjones.spacelaunchnow.data.services.LaunchFilterService
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.data.storage.NotificationHistoryStorage
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.data.storage.PinnedContentPreferences
import me.calebjones.spacelaunchnow.data.storage.TemporaryPremiumAccess
import me.calebjones.spacelaunchnow.data.storage.ThemePreferences
import me.calebjones.spacelaunchnow.data.subscription.LocalSubscriptionStorage
import me.calebjones.spacelaunchnow.data.subscription.SubscriptionSyncer
import me.calebjones.spacelaunchnow.database.ArticleLocalDataSource
import me.calebjones.spacelaunchnow.database.CacheCleanupService
import me.calebjones.spacelaunchnow.database.DatabaseDriverFactory
import me.calebjones.spacelaunchnow.database.EventLocalDataSource
import me.calebjones.spacelaunchnow.database.FilterOptionsLocalDataSource
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource
import me.calebjones.spacelaunchnow.database.ProgramLocalDataSource
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import me.calebjones.spacelaunchnow.database.SpacecraftLocalDataSource
import me.calebjones.spacelaunchnow.database.SpaceStationLocalDataSource
import me.calebjones.spacelaunchnow.database.UpdateLocalDataSource
import me.calebjones.spacelaunchnow.platform.ContextFactory
import me.calebjones.spacelaunchnow.ui.ads.GlobalAdManager
import me.calebjones.spacelaunchnow.ui.roadmap.RoadmapViewModel
import me.calebjones.spacelaunchnow.ui.settings.ThemeCustomizationViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.AgencyListViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.AgencyViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.AppRatingViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.AppSettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.AstronautDetailViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.AstronautListViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.DebugSettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.EventViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.EventsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.FeaturedLaunchViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.FeedViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.HistoryViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchCarouselViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchesViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.NewsEventsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.NextUpViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.OnboardingViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.PreloadViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.RocketViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.ScheduleViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.SettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.SpaceStationViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.StarshipViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.StatsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.UpdatesViewModel
import me.calebjones.spacelaunchnow.util.logging.LoggingPreferences
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

expect fun nativeConfig(): KoinAppDeclaration

val koinConfig = koinConfiguration {
    includes(nativeConfig())
    modules(networkModule, apiModule, appModule, debugModule, imageLoaderModule)
}

val appModule = module {
    singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
    viewModelOf(::UserViewModel)

    // Database and local data sources
    single {
        val driver = get<DatabaseDriverFactory>().createDriver()
        SpaceLaunchDatabase(driver)
    }
    single { LaunchLocalDataSource(get(), get()) }
    single { EventLocalDataSource(get(), get()) }
    single { ArticleLocalDataSource(get(), get()) }
    single { UpdateLocalDataSource(get(), get()) }
    single { ProgramLocalDataSource(get(), get()) }
    single { SpacecraftLocalDataSource(get(), get()) }
    single { SpaceStationLocalDataSource(get(), get()) }
    single { FilterOptionsLocalDataSource(get(), get()) }

    single<LaunchRepository> {
        LaunchRepositoryImpl(
            launchesApi = get(),
            agenciesApi = get(),
            appPreferences = get(),
            localDataSource = get()
        )
    }
    single<ArticlesRepository> {
        ArticlesRepositoryImpl(
            articlesApi = get(),
            localDataSource = get()
        )
    }
    single<InfoRepository> {
        InfoRepositoryImpl(
            infoApi = get()
        )
    }
    viewModelOf(::LaunchViewModel)
    viewModelOf(::NextUpViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::PreloadViewModel)

    // App rating ViewModel - using standard viewModel DSL
    viewModel {
        AppRatingViewModel(
            appRatingManager = get(),
            appPreferences = get()
        )
    }

    // Domain-specific ViewModels
    viewModelOf(::LaunchesViewModel)
    viewModelOf(::FeaturedLaunchViewModel)
    viewModelOf(::LaunchCarouselViewModel)
    viewModelOf(::FeedViewModel)
    viewModelOf(::EventsViewModel)
    viewModelOf(::NewsEventsViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::StatsViewModel)
    viewModelOf(::StarshipViewModel)

    viewModelOf(::ScheduleViewModel)
    singleOf(::UpdatesRepositoryImpl) { bind<UpdatesRepository>() }
    viewModelOf(::UpdatesViewModel)
    viewModelOf(::EventViewModel)
    viewModelOf(::AgencyViewModel)
    viewModelOf(::AgencyListViewModel)
    singleOf(::AgencyRepositoryImpl) { bind<AgencyRepository>() }
    singleOf(::AstronautRepositoryImpl) { bind<AstronautRepository>() }
    singleOf(::AstronautFilterRepositoryImpl) { bind<AstronautFilterRepository>() }
    viewModelOf(::AstronautListViewModel)
    viewModel { (astronautId: Int) -> AstronautDetailViewModel(get(), astronautId) }

    // Space Station tracking
    singleOf(::IssTrackingRepositoryImpl) {
        bind<IssTrackingRepository>()
    }
    singleOf(::SpaceStationRepositoryImpl) { bind<SpaceStationRepository>() }
    viewModelOf(::SpaceStationViewModel)
    singleOf(::RocketRepositoryImpl) { bind<RocketRepository>() }
    singleOf(::RocketFilterRepositoryImpl) { bind<RocketFilterRepository>() }
    singleOf(::SpacecraftRepositoryImpl) { bind<SpacecraftRepository>() }
    singleOf(::LauncherRepositoryImpl) { bind<LauncherRepository>() }
    singleOf(::ProgramRepositoryImpl) { bind<ProgramRepository>() }
    singleOf(::LauncherConfigRepositoryImpl) { bind<LauncherConfigRepository>() }
    singleOf(::SpacecraftConfigRepositoryImpl) { bind<SpacecraftConfigRepository>() }
    single<ScheduleFilterRepository> {
        ScheduleFilterRepositoryImpl(
            agenciesApi = get(),
            programsApi = get(),
            launcherConfigurationsApi = get(),
            launcherConfigurationFamiliesApi = get(),
            locationsApi = get(),
            configApi = get(),
            localDataSource = get()
        )
    }
    single<EventsRepository> {
        EventsRepositoryImpl(
            eventsApi = get(),
            localDataSource = get()
        )
    }
    single<UpdatesRepository> {
        UpdatesRepositoryImpl(
            updatesApi = get(),
            localDataSource = get()
        )
    }
    viewModelOf(::RocketViewModel)
    singleOf(::LaunchCache)

    // Background cleanup task for expired cache entries
    single {
        CacheCleanupService(
            launchDataSource = get(),
            eventDataSource = get(),
            articleDataSource = get(),
            updateDataSource = get(),
            programDataSource = get(),
            spacecraftDataSource = get(),
            spaceStationDataSource = get()
        )
    }

    // Global Ad Manager - Singleton managed by Koin
    single {
        GlobalAdManager(contextFactory = getOrNull<ContextFactory>()).also {
            it.initializeAndPreload()
        }
    }

    // Notification dependencies
    singleOf(::PushMessaging)

    // Launch filter service - converts filter settings to API parameters
    singleOf(::LaunchFilterService)

    // Notification state storage
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

    // Logging preferences for user-controlled diagnostic logging
    single {
        val appDataStore = get<DataStore<Preferences>>(named("AppSettingsDataStore"))
        LoggingPreferences(appDataStore)
    }

    // Pinned content preferences for storing dismissed pinned content IDs
    single {
        val appDataStore = get<DataStore<Preferences>>(named("AppSettingsDataStore"))
        PinnedContentPreferences(appDataStore)
    }

    // NotificationRepository - new clean architecture
    single<NotificationRepository> {
        NotificationRepositoryImpl(
            pushMessaging = get(),
            storage = get<NotificationStateStorage>(),
            debugPreferences = getOrNull<DebugPreferences>()
        )
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

    // Billing dependencies - platform-specific BillingManager is provided by platform modules
    // BillingClient wrapper for backward compatibility
    single<BillingClient> {
        BillingClient(billingManager = get())
    }

    // Local subscription storage (KStore)
    single { LocalSubscriptionStorage() }

    // Subscription syncer (uses BillingManager)
    single {
        SubscriptionSyncer(
            localStorage = get(),
            billingManager = get()
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

    // SubscriptionViewModel - now uses BillingManager (Phase 7 complete!)
    viewModelOf(::SubscriptionViewModel)

    single { AppSettingsViewModel(appPreferences = get()) }
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ThemeCustomizationViewModel)

    // Remote Config Repository for Firebase Remote Config
    single<RemoteConfigRepository> { RemoteConfigRepositoryImpl() }

    viewModelOf(::RoadmapViewModel)
}

// Debug-only module - dependencies are always provided but only used when BuildConfig.isDebug is true
val debugModule = module {
    single {
        // Use a separate qualifier for debug DataStore
        val debugDataStore = get<DataStore<Preferences>>(named("DebugDataStore"))
        DebugPreferences(debugDataStore)
    }

    // Notification history storage for debugging
    single {
        val historyDataStore = get<DataStore<Preferences>>(named("NotificationHistoryDataStore"))
        NotificationHistoryStorage(historyDataStore)
    }

    // DebugSettingsViewModel - now uses BillingManager (Phase 7 complete!)
    viewModel {
        DebugSettingsViewModel(
            debugPreferences = getOrNull(),
            billingManager = getOrNull(),
            launchRepository = getOrNull(),
            notificationRepository = getOrNull(),
            pushMessaging = getOrNull(),
            notificationHistoryStorage = get()  // NOT optional - we need this!
        )
    }
}
package me.calebjones.spacelaunchnow.ui.screens

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManagerImpl
import me.calebjones.spacelaunchnow.data.repository.MockRemoteConfigRepository
import me.calebjones.spacelaunchnow.data.repository.RemoteConfigRepository
import me.calebjones.spacelaunchnow.data.repository.MockSubscriptionRepository
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.storage.HomeUpgradeRowPreferences
import me.calebjones.spacelaunchnow.data.storage.TemporaryPremiumAccess
import me.calebjones.spacelaunchnow.data.repository.FakeNotificationRepository
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.rating.AppRatingManager
import me.calebjones.spacelaunchnow.ui.viewmodel.AppRatingViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.AppSettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.SettingsViewModel
import me.calebjones.spacelaunchnow.util.logging.LoggingPreferences
import me.calebjones.spacelaunchnow.data.billing.BillingManager
import me.calebjones.spacelaunchnow.data.billing.MockBillingManager
import me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModel
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.data.repository.FakeLaunchRepository
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.ScheduleFilterRepository
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.ui.viewmodel.ScheduleViewModel
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import java.nio.file.Files

/** Fakes that every dense-screen layout test needs; a test adds its own module for the rest. */
object ScreenKoin {
    fun newDataStore(): DataStore<Preferences> {
        val dir = Files.createTempDirectory("layout-test")
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = { dir.resolve("test.preferences_pb").toString().toPath() },
        )
    }

    val common: Module get() = module {
        single<AnalyticsManager> { AnalyticsManagerImpl(emptyList()) }
        single<RemoteConfigRepository> { MockRemoteConfigRepository() }
        single<SubscriptionRepository> { MockSubscriptionRepository() }
        single { HomeUpgradeRowPreferences(newDataStore()) }
        single { TemporaryPremiumAccess(newDataStore()) }
    }

    /** What `SettingsScreen` and `NotificationSettingsScreen` inject. */
    val settings: Module get() = module {
        single<NotificationRepository> { FakeNotificationRepository() }
        single { AppPreferences(newDataStore()) }
        single { LoggingPreferences(newDataStore()) }
        single { AppRatingManager() }
        single { AppSettingsViewModel(appPreferences = get()) }
        viewModelOf(::SettingsViewModel)
        viewModel { AppRatingViewModel(get(), get()) }
    }

    /** What `SupportUsScreen` injects; the billing fake serves two products. */
    val paywall: Module get() = module {
        single<BillingManager> { MockBillingManager() }
        single { SubscriptionViewModel(get(), get(), get()) }
    }

    /** What `ScheduleScreen` injects; every list query returns [ScreenFixtures.upcomingLaunches]. */
    val schedule: Module get() = module {
        single<LaunchRepository> { ScheduleLaunchRepository(ScreenFixtures.upcomingLaunches) }
        single<ScheduleFilterRepository> { EmptyScheduleFilters }
        single { AppPreferences(newDataStore()) }
        viewModelOf(::ScheduleViewModel)
    }
}

/** The shared fake with the filtered-launch query answered from a fixed list. */
private class ScheduleLaunchRepository(
    private val launches: List<Launch>,
    base: LaunchRepository = FakeLaunchRepository(),
) : LaunchRepository by base {
    override suspend fun getFilteredLaunchesDomain(
        limit: Int,
        offset: Int,
        upcoming: Boolean?,
        previous: Boolean?,
        ordering: String?,
        search: String?,
        lspIds: List<Int>?,
        locationIds: List<Int>?,
        programIds: List<Int>?,
        rocketConfigurationId: Int?,
        isCrewed: Boolean?,
        includeSuborbital: Boolean?,
        statusIds: List<Int>?,
        orbitIds: List<Int>?,
        missionTypeIds: List<Int>?,
        launcherConfigFamilyIds: List<Int>?,
        forceRefresh: Boolean,
    ): Result<PaginatedResult<Launch>> =
        Result.success(PaginatedResult(count = launches.size, next = null, previous = null, results = launches))
}

private object EmptyScheduleFilters : ScheduleFilterRepository {
    private val none = Result.success(emptyList<FilterOption>())
    override suspend fun getAgencies(forceRefresh: Boolean) = none
    override suspend fun getPrograms(forceRefresh: Boolean) = none
    override suspend fun getRockets(forceRefresh: Boolean) = none
    override suspend fun getLocations(forceRefresh: Boolean) = none
    override suspend fun getStatuses(forceRefresh: Boolean) = none
    override suspend fun getOrbits(forceRefresh: Boolean) = none
    override suspend fun getMissionTypes(forceRefresh: Boolean) = none
    override suspend fun getLauncherConfigFamilies(forceRefresh: Boolean) = none
}

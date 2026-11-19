package me.calebjones.spacelaunchnow.di

import me.calebjones.spacelaunchnow.data.model.DataBackend
import me.calebjones.spacelaunchnow.data.repository.AstronautFilterRepository
import me.calebjones.spacelaunchnow.data.repository.AstronautFilterRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.RocketFilterRepository
import me.calebjones.spacelaunchnow.data.repository.RocketFilterRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ScheduleFilterRepository
import me.calebjones.spacelaunchnow.data.repository.ScheduleFilterRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.terminus.SqlFilterOptionStore
import me.calebjones.spacelaunchnow.data.repository.terminus.TerminusAstronautFilterRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.terminus.TerminusFilterFetcher
import me.calebjones.spacelaunchnow.data.repository.terminus.TerminusRocketFilterRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.terminus.TerminusScheduleFilterRepositoryImpl
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Picks the schedule, rocket and astronaut filter repositories by [DataBackend], as
 * [eventsUpdatesRepositoryModule] does for events and updates. The branch sits inside each
 * factory and Koin singles are lazy, so the LL path never builds a Terminus client and the
 * Terminus path never builds an LL one. A function, not a `val`, so each Koin application
 * gets its own singletons.
 */
fun filterRepositoryModule(): Module = module {
    // One fetcher for the three Terminus filter repositories: they share the memory cache and the disk copy.
    single { TerminusFilterFetcher(SqlFilterOptionStore(get()), get<AppPreferences>()) }
    single<ScheduleFilterRepository> {
        when (get<DataBackend>()) {
            DataBackend.LL -> ScheduleFilterRepositoryImpl(
                agenciesApi = get(),
                programsApi = get(),
                launcherConfigurationsApi = get(),
                launcherConfigurationFamiliesApi = get(),
                locationsApi = get(),
                configApi = get(),
                localDataSource = get()
            )
            DataBackend.TERMINUS -> TerminusScheduleFilterRepositoryImpl(
                agenciesApi = get(),
                programsApi = get(),
                launcherConfigurationsApi = get(),
                launcherConfigurationFamiliesApi = get(),
                locationsApi = get(),
                configApi = get(),
                fetcher = get()
            )
        }
    }
    single<RocketFilterRepository> {
        when (get<DataBackend>()) {
            DataBackend.LL -> RocketFilterRepositoryImpl(
                programsApi = get(),
                launcherConfigurationFamiliesApi = get()
            )
            DataBackend.TERMINUS -> TerminusRocketFilterRepositoryImpl(
                programsApi = get(),
                launcherConfigurationFamiliesApi = get(),
                fetcher = get()
            )
        }
    }
    single<AstronautFilterRepository> {
        when (get<DataBackend>()) {
            DataBackend.LL -> AstronautFilterRepositoryImpl(configApi = get())
            DataBackend.TERMINUS -> TerminusAstronautFilterRepositoryImpl(configApi = get(), fetcher = get())
        }
    }
}

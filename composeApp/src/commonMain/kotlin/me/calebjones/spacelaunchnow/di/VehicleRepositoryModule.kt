package me.calebjones.spacelaunchnow.di

import me.calebjones.spacelaunchnow.api.terminus.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LaunchersApi
import me.calebjones.spacelaunchnow.data.model.DataBackend
import me.calebjones.spacelaunchnow.data.repository.AgencyRepository
import me.calebjones.spacelaunchnow.data.repository.LauncherConfigRepository
import me.calebjones.spacelaunchnow.data.repository.LauncherRepository
import me.calebjones.spacelaunchnow.data.repository.RocketRepository
import me.calebjones.spacelaunchnow.data.repository.ll.LLAgencyRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ll.LLLauncherConfigRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ll.LLLauncherRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ll.LLRocketRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.terminus.TerminusAgencyRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.terminus.TerminusLauncherConfigRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.terminus.TerminusLauncherRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.terminus.TerminusRocketRepositoryImpl
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Picks the agency, launcher configuration, launcher and rocket repositories by [DataBackend].
 * The branch sits inside each factory, and Koin singles are lazy, so the Terminus repositories
 * and the Terminus clients behind them are only built on [DataBackend.TERMINUS]; the LL path
 * never resolves them. Terminus serves every method of the four interfaces, so none delegates
 * to LL. A function, not a `val`, for the same reason as [eventsUpdatesRepositoryModule].
 */
fun vehicleRepositoryModule(): Module = module {
    single<AgencyRepository> {
        when (get<DataBackend>()) {
            DataBackend.LL -> LLAgencyRepositoryImpl(get())
            DataBackend.TERMINUS -> TerminusAgencyRepositoryImpl(get<AgenciesApi>())
        }
    }
    single<LauncherConfigRepository> {
        when (get<DataBackend>()) {
            DataBackend.LL -> LLLauncherConfigRepositoryImpl(get())
            DataBackend.TERMINUS -> TerminusLauncherConfigRepositoryImpl(get<LauncherConfigurationsApi>())
        }
    }
    single<LauncherRepository> {
        when (get<DataBackend>()) {
            DataBackend.LL -> LLLauncherRepositoryImpl(get())
            DataBackend.TERMINUS -> TerminusLauncherRepositoryImpl(get<LaunchersApi>())
        }
    }
    single<RocketRepository> {
        when (get<DataBackend>()) {
            DataBackend.LL -> LLRocketRepositoryImpl(get())
            DataBackend.TERMINUS -> TerminusRocketRepositoryImpl(get<LauncherConfigurationsApi>())
        }
    }
}

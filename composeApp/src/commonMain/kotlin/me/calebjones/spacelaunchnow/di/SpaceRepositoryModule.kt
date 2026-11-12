package me.calebjones.spacelaunchnow.di

import me.calebjones.spacelaunchnow.data.model.DataBackend
import me.calebjones.spacelaunchnow.data.repository.AstronautRepository
import me.calebjones.spacelaunchnow.data.repository.ProgramRepository
import me.calebjones.spacelaunchnow.data.repository.SpaceStationRepository
import me.calebjones.spacelaunchnow.data.repository.ll.LLAstronautRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ll.LLProgramRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ll.LLSpaceStationRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.terminus.TerminusAstronautRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.terminus.TerminusProgramRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.terminus.TerminusSpaceStationRepositoryImpl
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Picks the [SpaceStationRepository], [AstronautRepository] and [ProgramRepository] by
 * [DataBackend] (Tasks#1012). The branch sits inside each factory, and Koin singles are lazy, so
 * the Terminus repositories and the Terminus clients behind them are only built on
 * [DataBackend.TERMINUS]; the LL path never resolves them.
 *
 * On Terminus the station repository still holds an LL instance, for the ISS TLE only: the TLE
 * comes from wheretheiss.at, not from Launch Library, and its cache lives in that instance.
 *
 * A function, not a `val`, so each Koin application gets its own singletons.
 */
fun spaceRepositoryModule(): Module = module {
    single<SpaceStationRepository> {
        val ll = LLSpaceStationRepositoryImpl(
            spaceStationsApi = get(),
            expeditionsApi = get(),
            issTrackingRepository = get(),
            localDataSource = getOrNull()
        )
        when (get<DataBackend>()) {
            DataBackend.LL -> ll
            DataBackend.TERMINUS -> TerminusSpaceStationRepositoryImpl(
                spaceStationsApi = get(),
                expeditionsApi = get(),
                tle = ll
            )
        }
    }
    single<AstronautRepository> {
        when (get<DataBackend>()) {
            DataBackend.LL -> LLAstronautRepositoryImpl(astronautsApi = get())
            DataBackend.TERMINUS -> TerminusAstronautRepositoryImpl(astronautsApi = get())
        }
    }
    single<ProgramRepository> {
        when (get<DataBackend>()) {
            DataBackend.LL -> LLProgramRepositoryImpl(programsApi = get(), localDataSource = getOrNull())
            DataBackend.TERMINUS -> TerminusProgramRepositoryImpl(programsApi = get())
        }
    }
}

package me.calebjones.spacelaunchnow.di

import me.calebjones.spacelaunchnow.data.model.DataBackend
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepository
import me.calebjones.spacelaunchnow.data.repository.ll.LLEventsRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ll.LLUpdatesRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.terminus.TerminusEventsRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.terminus.TerminusUpdatesRepositoryImpl
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Picks the [EventsRepository] and [UpdatesRepository] by [DataBackend]. The branch sits inside
 * each factory, and Koin singles are lazy, so the Terminus repositories and the Terminus clients
 * behind them are only built on [DataBackend.TERMINUS]; the LL path never resolves them. Neither
 * Terminus repository delegates to LL: Terminus serves every method of both interfaces.
 *
 * A function, not a `val`: a Koin module caches its singletons, so a shared instance would leak
 * them between Koin applications (the tests build one per case).
 */
fun eventsUpdatesRepositoryModule(): Module = module {
    single<EventsRepository> {
        when (get<DataBackend>()) {
            DataBackend.LL -> LLEventsRepositoryImpl(
                eventsApi = get(),
                configApi = get(),
                localDataSource = get()
            )
            DataBackend.TERMINUS -> TerminusEventsRepositoryImpl(
                eventsApi = get(),
                configApi = get()
            )
        }
    }
    single<UpdatesRepository> {
        when (get<DataBackend>()) {
            DataBackend.LL -> LLUpdatesRepositoryImpl(
                updatesApi = get(),
                localDataSource = get()
            )
            DataBackend.TERMINUS -> TerminusUpdatesRepositoryImpl(updatesApi = get())
        }
    }
}

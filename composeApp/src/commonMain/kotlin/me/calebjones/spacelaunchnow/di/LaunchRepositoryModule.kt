package me.calebjones.spacelaunchnow.di

import me.calebjones.spacelaunchnow.data.model.DataBackend
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.ll.LLLaunchRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.terminus.TerminusLaunchRepositoryImpl
import org.koin.core.qualifier.named
import org.koin.core.module.Module
import org.koin.dsl.module

/** Qualifiers: both impls are LaunchRepository, so an unqualified lookup would be ambiguous. */
const val LL_REPOSITORY = "LLLaunchRepository"
const val TERMINUS_REPOSITORY = "TerminusLaunchRepository"

/**
 * Picks the [LaunchRepository] by [DataBackend]. Koin singles are lazy, so the Terminus
 * repository, and the Terminus client behind it, are only built when the backend is
 * [DataBackend.TERMINUS]; the LL path never resolves them. The Terminus repository serves
 * every method of the interface and does not delegate to LL.
 *
 * A function, not a `val`: a Koin module caches its singletons, so a shared instance would leak
 * them between Koin applications (the tests build one per case).
 */
fun launchRepositoryModule(): Module = module {
    single(named(LL_REPOSITORY)) {
        LLLaunchRepositoryImpl(
            launchesApi = get(),
            appPreferences = get(),
            localDataSource = get(),
            statsLocalDataSource = get()
        )
    }
    single(named(TERMINUS_REPOSITORY)) {
        TerminusLaunchRepositoryImpl(
            launchesApi = get(),
            localDataSource = get(),
            statsLocalDataSource = get(),
            appPreferences = get()
        )
    }
    single<LaunchRepository> {
        when (get<DataBackend>()) {
            DataBackend.LL -> get<LLLaunchRepositoryImpl>(named(LL_REPOSITORY))
            DataBackend.TERMINUS -> get<TerminusLaunchRepositoryImpl>(named(TERMINUS_REPOSITORY))
        }
    }
}

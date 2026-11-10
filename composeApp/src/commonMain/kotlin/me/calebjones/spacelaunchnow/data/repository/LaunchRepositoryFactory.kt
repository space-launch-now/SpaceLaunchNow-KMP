package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.terminus.apis.LaunchesApi as TerminusLaunchesApi
import me.calebjones.spacelaunchnow.data.model.DataBackend
import me.calebjones.spacelaunchnow.data.repository.terminus.TerminusLaunchRepositoryImpl
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource

/**
 * Picks the launch repository for [backend]. [terminusLaunchesApi] is a provider so the
 * Terminus client is only built on [DataBackend.TERMINUS]: the default LL path never touches it.
 */
fun createLaunchRepository(
    backend: DataBackend,
    llRepository: LaunchRepository,
    terminusLaunchesApi: () -> TerminusLaunchesApi,
    localDataSource: LaunchLocalDataSource?
): LaunchRepository = when (backend) {
    DataBackend.LL -> llRepository
    DataBackend.TERMINUS -> TerminusLaunchRepositoryImpl(
        delegate = llRepository,
        launchesApi = terminusLaunchesApi(),
        localDataSource = localDataSource
    )
}

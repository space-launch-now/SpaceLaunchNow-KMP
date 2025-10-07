package me.calebjones.spacelaunchnow

import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.di.koinConfig
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform.getKoin

// Global holder for Koin instance
private var koinInstance: Koin? = null

/**
 * Initialize Koin for iOS
 * This should be called before the UI is created to ensure all dependencies are available
 */
fun initKoin() {
    if (koinInstance == null) {
        val app = startKoin(koinConfig)
        koinInstance = app.koin
    }
}

/**
 * Helper class to access Koin dependencies from Swift
 * This avoids complex ObjC interop issues
 */
class KoinHelper : KoinComponent {
    val launchRepository: LaunchRepository by inject()

    /**
     * Fetch upcoming launches and unwrap the Result type
     * Throws an exception if the API call fails
     */
    @Throws(Exception::class)
    suspend fun fetchUpcomingLaunches(limit: Int): PaginatedLaunchNormalList {
        return launchRepository.getUpcomingLaunchesNormal(limit).getOrThrow()
    }

    /**
     * Fetch upcoming launches and unwrap the Result type
     * Returns the PaginatedLaunchNormalList directly or null if failed
     */
    suspend fun fetchUpcomingLaunchesOrNull(limit: Int): PaginatedLaunchNormalList? {
        val result = launchRepository.getUpcomingLaunchesNormal(limit)
        return result.getOrNull()
    }

    companion object {
        fun instance(): KoinHelper = KoinHelper()
    }
}

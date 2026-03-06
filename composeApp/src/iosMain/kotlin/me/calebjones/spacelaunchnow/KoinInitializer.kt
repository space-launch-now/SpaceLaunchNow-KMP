package me.calebjones.spacelaunchnow

import kotlinx.coroutines.flow.first
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.notifications.IosPushMessagingBridge
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.services.LaunchFilterService
import me.calebjones.spacelaunchnow.data.storage.NotificationHistoryStorage
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.util.logging.logger
import me.calebjones.spacelaunchnow.di.koinConfig
import me.calebjones.spacelaunchnow.util.initializeBuildConfig
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform.getKoin

// Global holder for Koin instance
private var koinInstance: Koin? = null

/**
 * Initialize Koin for iOS (also used by widget extensions).
 * SpaceLogger must be initialized before Koin because KoinHelper's constructor
 * calls logger(), which requires SpaceLogger to be ready.
 */
fun initKoin() {
    if (koinInstance == null) {
        // SpaceLogger must be initialized before any Kotlin code that calls logger()
        initializeBuildConfig()
        SpaceLogger.initialize()

        val app = startKoin(koinConfig)
        koinInstance = app.koin

        // Note: IosPushMessagingBridge now uses KoinComponent with by inject()
        // so it automatically gets NotificationHistoryStorage when Koin starts
    }
}

/**
 * Helper class to access Koin dependencies from Swift
 * This avoids complex ObjC interop issues
 */
class KoinHelper : KoinComponent {
    private val log = logger()
    
    val launchRepository: LaunchRepository by inject()
    val subscriptionRepository: SubscriptionRepository by inject()

    /**
     * Fetch upcoming launches and unwrap the Result type
     * Throws an exception if the API call fails
     */
    @Throws(Exception::class)
    suspend fun fetchUpcomingLaunches(limit: Int): PaginatedLaunchNormalList {
        return launchRepository.getUpcomingLaunchesNormal(limit).getOrThrow().data
    }

    /**
     * Fetch upcoming launches and unwrap the Result type
     * Returns the PaginatedLaunchNormalList directly or null if failed.
     * Applies user filter preferences (agency/location) from NotificationState.
     */
    suspend fun fetchUpcomingLaunchesOrNull(limit: Int): PaginatedLaunchNormalList? {
        val notificationStateStorage = getKoin().get<NotificationStateStorage>()
        val launchFilterService = getKoin().get<LaunchFilterService>()

        val state = notificationStateStorage.stateFlow.first()
        val agencyIds = launchFilterService.getAgencyIds(state)
        val locationIds = launchFilterService.getLocationIds(state)

        val result = launchRepository.getUpcomingLaunchesNormal(
            limit,
            forceRefresh = true,
            agencyIds = agencyIds,
            locationIds = locationIds
        )
        return result.getOrNull()?.data
    }
    
    /**
     * Check if the user has access to the ADVANCED_WIDGETS premium feature
     * This is used by iOS as a general subscription check (widget access gating
     * uses shared UserDefaults via WidgetAccessSharer instead)
     */
    suspend fun hasWidgetAccess(): Boolean {
        return try {
            subscriptionRepository.hasFeature(PremiumFeature.ADVANCED_WIDGETS)
        } catch (e: Exception) {
            false // Default to locked if check fails
        }
    }
    
    companion object {
        fun instance(): KoinHelper = KoinHelper()
    }
}

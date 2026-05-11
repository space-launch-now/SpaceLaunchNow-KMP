package me.calebjones.spacelaunchnow

import kotlinx.coroutines.flow.first
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.notifications.IosPushMessagingBridge
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.services.LaunchFilterService
import me.calebjones.spacelaunchnow.data.storage.NotificationHistoryStorage
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
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
    suspend fun fetchUpcomingLaunches(limit: Int): PaginatedResult<Launch> {
        return launchRepository.getUpcomingLaunchesNormalDomain(limit).getOrThrow().data
    }

    /**
     * Fetch upcoming launches and unwrap the Result type
     * Returns paginated domain Launch data directly or null if failed.
     * Applies user filter preferences (agency/location) from NotificationState.
     */
    suspend fun fetchUpcomingLaunchesOrNull(limit: Int): PaginatedResult<Launch>? {
        val notificationStateStorage = getKoin().get<NotificationStateStorage>()
        val launchFilterService = getKoin().get<LaunchFilterService>()

        val state = notificationStateStorage.stateFlow.first()
        val agencyIds = launchFilterService.getAgencyIds(state)
        val locationIds = launchFilterService.getLocationIds(state)

        val result = launchRepository.getUpcomingLaunchesNormalDomain(
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

/**
 * Called from Swift (e.g. `AppDelegate.application(_:didFinishLaunchingWithOptions:)`)
 * after `initKoin()` and after RevenueCat has been configured by `IosBillingManager`.
 */
fun startRevenueCatAttributesSyncer() {
    val koin = getKoin()
    val rcSyncer =
        koin.get<me.calebjones.spacelaunchnow.data.billing.RevenueCatAttributesSyncer>()
    val tempAccess =
        koin.get<me.calebjones.spacelaunchnow.data.storage.TemporaryPremiumAccess>()
    val appPrefs =
        koin.get<me.calebjones.spacelaunchnow.data.storage.AppPreferences>()
    val themePrefs =
        koin.get<me.calebjones.spacelaunchnow.data.storage.ThemePreferences>()
    val repository =
        koin.get<me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository>()

    @Suppress("OPT_IN_USAGE")
    val scope = kotlinx.coroutines.GlobalScope

    rcSyncer.start(
        scope = scope,
        subscriptionStateFlow = kotlinx.coroutines.flow.flow {
            repository.state.collect { emit(it.subscriptionType.name.lowercase()) }
        },
        themeModeFlow = kotlinx.coroutines.flow.flow {
            appPrefs.themeFlow.collect { emit(it.name.lowercase()) }
        },
        hasCustomThemeFlow = kotlinx.coroutines.flow.flow {
            themePrefs.customPrimaryColorFlow.collect { emit(it != null) }
        },
        grantsTotalFlow = tempAccess.grantsTotalFlow,
        adsShownTotalFlow = tempAccess.adsShownTotalFlow,
        tempAccessActiveFlow = kotlinx.coroutines.flow.flow {
            tempAccess.accessChangeTrigger.collect {
                val active = tempAccess.hasTemporaryAccess(
                    me.calebjones.spacelaunchnow.data.model.PremiumFeature.CUSTOM_THEMES
                ) || tempAccess.hasTemporaryAccess(
                    me.calebjones.spacelaunchnow.data.model.PremiumFeature.ADVANCED_WIDGETS
                ) || tempAccess.hasTemporaryAccess(
                    me.calebjones.spacelaunchnow.data.model.PremiumFeature.WIDGETS_CUSTOMIZATION
                )
                emit(active)
            }
        },
    )
}

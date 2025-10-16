package me.calebjones.spacelaunchnow

import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.preferences.WidgetPreferences
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.di.koinConfig
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform.getKoin
import kotlinx.coroutines.flow.first

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
    val subscriptionRepository: SubscriptionRepository by inject()
    val widgetPreferences: WidgetPreferences by inject()

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
    
    /**
     * Check if the user has access to the ADVANCED_WIDGETS premium feature
     * This is used by iOS widgets to gate content behind subscription
     */
    suspend fun hasWidgetAccess(): Boolean {
        return try {
            subscriptionRepository.hasFeature(PremiumFeature.ADVANCED_WIDGETS)
        } catch (e: Exception) {
            println("KoinHelper: Failed to check widget access: ${e.message}")
            false // Default to locked if check fails
        }
    }
    
    /**
     * Get widget background alpha (0.0 to 1.0)
     * Used by iOS widgets to set background transparency
     */
    suspend fun getWidgetBackgroundAlpha(): Float {
        return try {
            widgetPreferences.widgetBackgroundAlphaFlow.first()
        } catch (e: Exception) {
            println("KoinHelper: Failed to get widget background alpha: ${e.message}")
            0.75f // Default value
        }
    }
    
    /**
     * Get widget corner radius in dp (0 to 40)
     * Used by iOS widgets to set corner radius
     */
    suspend fun getWidgetCornerRadius(): Float {
        return try {
            widgetPreferences.widgetCornerRadiusFlow.first()
        } catch (e: Exception) {
            println("KoinHelper: Failed to get widget corner radius: ${e.message}")
            16f // Default value
        }
    }

    companion object {
        fun instance(): KoinHelper = KoinHelper()
    }
}

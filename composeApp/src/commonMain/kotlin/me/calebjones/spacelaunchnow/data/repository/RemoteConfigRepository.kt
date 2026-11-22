package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.data.model.DataBackend
import me.calebjones.spacelaunchnow.data.model.OnboardingVariant
import me.calebjones.spacelaunchnow.data.model.PinnedContent
import me.calebjones.spacelaunchnow.data.model.RoadmapData

/**
 * Repository for Firebase Remote Config operations
 * 
 * Provides access to remotely-configured app values with built-in
 * caching and offline fallback support.
 */
interface RemoteConfigRepository {
    
    /**
     * Fetch remote config values from Firebase and activate them
     * 
     * Uses Firebase Remote Config's built-in caching:
     * - Default: 1-hour minimum fetch interval
     * - forceRefresh: Bypasses cache for immediate fetch
     * 
     * @param forceRefresh When true, ignores cache and fetches from network
     * @return Result.success if fetch and activate succeeded
     * @return Result.failure with exception if operation failed
     */
    suspend fun fetchAndActivate(forceRefresh: Boolean = false): Result<Unit>
    
    /**
     * Get roadmap data from remote config
     * 
     * Reads the 'roadmap_data' parameter and deserializes to RoadmapData.
     * Falls back to default values if:
     * - Remote value not yet fetched
     * - Remote value is invalid JSON
     * - Network unavailable and no cached value
     * 
     * @return Result.success with parsed RoadmapData
     * @return Result.failure with exception if parsing failed
     */
    suspend fun getRoadmapData(): Result<RoadmapData>
    
    /**
     * Get pinned/featured content from remote config
     * 
     * Reads the 'pinned_content' parameter and deserializes to PinnedContent.
     * Use this to feature specific launches or events at the top of the home screen.
     * 
     * @return Result.success with PinnedContent (null if not configured or disabled)
     * @return Result.failure with exception if parsing failed
     */
    suspend fun getPinnedContent(): Result<PinnedContent?>
    
    /**
     * Set default values for remote config parameters
     *
     * Should be called during app initialization to ensure
     * the app functions before first remote fetch completes.
     *
     * Defaults are embedded JSON matching the RoadmapData structure.
     */
    suspend fun setDefaults()

    /**
     * Raw JSON of the 'diagnostics_config' parameter (remote diagnostics control),
     * or null when unset/blank/unavailable. Parsing lives in
     * util/logging/RemoteDiagnostics.kt so it stays pure and unit-testable.
     */
    suspend fun getDiagnosticsConfigJson(): String?

    /**
     * The onboarding A/B variant from the 'onboarding_variant' parameter.
     * CONTROL when unset, unknown, or Firebase is unavailable.
     */
    suspend fun getOnboardingVariant(): OnboardingVariant

    /**
     * The data source from the boolean 'use_legacy_backend' parameter: `true` gives
     * [DataBackend.LL]. [DataBackend.DEFAULT] (Terminus) when unset, unrecognized, or Firebase
     * is unavailable: the fail-safe side. The old 'data_backend' parameter is not read.
     */
    suspend fun getDataBackend(): DataBackend

    /**
     * Detail-view visits between interstitials, from 'ads_interstitial_visits'.
     * Default 4; a value of 0 or less turns interstitials off.
     */
    suspend fun getInterstitialVisits(): Int

    /**
     * Whether the review prompt after opening a launch from a notification is on,
     * from 'rating_prompt_after_notification_enabled'. Default false; false on read failure.
     */
    suspend fun getNotificationRatingPromptEnabled(): Boolean

    /**
     * Whether the day-7 win-back sheet is on, from 'winback_sheet_enabled'.
     * Default false; false on read failure.
     */
    suspend fun getWinbackSheetEnabled(): Boolean

    /**
     * Minimum seconds between interstitials, from 'ads_interstitial_min_interval_s'.
     * Default 120.
     */
    suspend fun getInterstitialMinIntervalSeconds(): Long

    /**
     * Whether the dismissible upgrade row on Home is on, from 'home_upgrade_row_enabled'.
     * Default false; false on read failure.
     */
    suspend fun getHomeUpgradeRowEnabled(): Boolean

    /**
     * Whether a tap on a locked notification control opens the paywall, from
     * 'paywall_locked_notification_tap_enabled'. Default false; false on read failure.
     */
    suspend fun getLockedNotificationTapPaywallEnabled(): Boolean
}

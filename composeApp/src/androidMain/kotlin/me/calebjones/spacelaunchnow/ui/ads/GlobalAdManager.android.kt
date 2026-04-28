package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.lexilabs.basic.ads.AdSize
import app.lexilabs.basic.ads.AdUnitId
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.time.Clock.System
import me.calebjones.spacelaunchnow.PlatformType
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.platform.ContextFactory
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.data.config.AdMobConfig
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

private val log by lazy { SpaceLogger.getLogger("GlobalAdManager") }

/**
 * Android implementation of GlobalAdManager with BasicAds integration.
 *
 * Provides optimized ad configuration and preloading strategies:
 * - Optimal ad configuration caching
 * - Ad request optimization
 * - Loading state management
 * - Performance monitoring
 */
@OptIn(DependsOnGoogleMobileAds::class)
actual class GlobalAdManager actual constructor(
    private val contextFactory: ContextFactory?
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Initialization and configuration state
    var isInitialized by mutableStateOf(false)
        private set
    var isOptimizationReady by mutableStateOf(false)
        private set

    // Interstitial ad tracking for detailed views
    private var detailViewVisitCount = 0
    private var lastInterstitialShownAt = 0L
    private val minInterstitialInterval = 120_000L // 2 minutes minimum between interstitials
    private val visitsBeforeInterstitial = 6 // Show interstitial every 6th visit

    // Ad configuration cache for faster setup
    private val adConfigurations = mutableMapOf<AdSize, AdConfig>()

    // Performance tracking
    private var adLoadStartTimes = mutableMapOf<String, Long>()
    private var adLoadMetrics = mutableListOf<AdLoadMetric>()

    data class AdConfig(
        val adUnitId: String,
        val adSize: AdSize,
        val isOptimized: Boolean = true,
        val priority: AdPriority = AdPriority.NORMAL
    )

    enum class AdPriority {
        HIGH,    // Critical ads (home page, detail pages)
        NORMAL,  // Standard ads (lists, navigation)
        LOW      // Background ads (settings, etc.)
    }

    data class AdLoadMetric(
        val adSize: AdSize,
        val loadTimeMs: Long,
        val success: Boolean,
        val timestamp: Long = System.now().toEpochMilliseconds()
    )

    /**
     * Initialize the ad manager and prepare optimizations
     */
    actual fun initializeAndPreload() {
        // Note: iOS doesn't require a context factory, only Android does
        if (contextFactory == null && getPlatform().type == PlatformType.ANDROID) {
            return
        }

        // Configuration happens synchronously now instead of in coroutine
        isInitialized = true
    
        // Setup configurations synchronously for instant availability
        setupAdConfigurations()
    }
    
    /**
     * Pre-warm ad requests to reduce time-to-first-ad.
     * Call this immediately after SDK initialization to signal the system is ready.
     */
    actual fun preWarmAdRequests() {
        if (!isInitialized) {
            log.w { "🚨 Cannot pre-warm ads: GlobalAdManager not initialized" }
            return
        }
        
        log.d { "🚀 Pre-warming ad requests - SDK ready for ad loading" }
        
        // Signal that optimization is ready for ad requests
        // The actual preloading happens via WithPreloadedAds CompositionLocal
        isOptimizationReady = true
        
        // Log configuration summary
        log.d { "📊 Ad configurations ready: ${adConfigurations.size} sizes configured" }
    }

    private fun setupAdConfigurations() {
        // 🚀 PERFORMANCE: Only configure the 3 essential ad sizes we actually preload
        // This reduces memory footprint and initialization time
        
        // Configure banner ads (most common - used everywhere)
        adConfigurations[AdSize.BANNER] = AdConfig(
            adUnitId = getPlatformAdUnitId(AdType.BANNER),
            adSize = AdSize.BANNER,
            isOptimized = true,
            priority = AdPriority.HIGH
        )

        // Configure large banner ads (detail views, featured content)
        adConfigurations[AdSize.LARGE_BANNER] = AdConfig(
            adUnitId = getPlatformAdUnitId(AdType.BANNER),
            adSize = AdSize.LARGE_BANNER,
            isOptimized = true,
            priority = AdPriority.HIGH
        )

        // Configure medium rectangle ads (inline list ads)
        adConfigurations[AdSize.MEDIUM_RECTANGLE] = AdConfig(
            adUnitId = getPlatformAdUnitId(AdType.BANNER),
            adSize = AdSize.MEDIUM_RECTANGLE,
            isOptimized = true,
            priority = AdPriority.HIGH  // Upgraded to HIGH for inline list ads
        )
    }

    /**
     * Get optimal ad configuration for a given size
     */
    fun getOptimalAdConfig(adSize: AdSize): AdConfig {
        return adConfigurations[adSize] ?: AdConfig(
            adUnitId = getPlatformAdUnitId(AdType.BANNER),
            adSize = adSize,
            isOptimized = false,
            priority = AdPriority.LOW
        )
    }

    /**
     * Start tracking ad load performance
     */
    fun trackAdLoadStart(adSize: AdSize): String {
        val trackingId = "${adSize}_${System.now().toEpochMilliseconds()}"
        adLoadStartTimes[trackingId] = System.now().toEpochMilliseconds()
        return trackingId
    }

    /**
     * Complete ad load tracking and record metrics
     */
    fun completeAdLoadTracking(trackingId: String, adSize: AdSize, success: Boolean) {
        val startTime = adLoadStartTimes.remove(trackingId)
        if (startTime != null) {
            val loadTime = System.now().toEpochMilliseconds() - startTime
            val metric = AdLoadMetric(adSize, loadTime, success)
            adLoadMetrics.add(metric)

            // Keep only recent metrics (last 50)
            if (adLoadMetrics.size > 50) {
                adLoadMetrics.removeAt(0)
            }
        }
    }

    /**
     * Get average load time for an ad size
     */
    fun getAverageLoadTime(adSize: AdSize): Long {
        val relevantMetrics = adLoadMetrics.filter { it.adSize == adSize && it.success }
        return if (relevantMetrics.isNotEmpty()) {
            relevantMetrics.map { it.loadTimeMs }.average().toLong()
        } else {
            0L
        }
    }

    /**
     * Check if optimizations are ready for faster loading
     */
    fun isOptimizationComplete(): Boolean = isOptimizationReady

    /**
     * Check if configuration is ready
     */
    fun isConfigurationReady(): Boolean = isInitialized

    /**
     * Get recommended ad size based on performance metrics
     */
    fun getRecommendedAdSize(): AdSize {
        // Return the ad size with best performance, fallback to BANNER
        val bestPerformingSize = adLoadMetrics
            .filter { it.success }
            .groupBy { it.adSize }
            .mapValues { (_, metrics) -> metrics.map { it.loadTimeMs }.average() }
            .minByOrNull { it.value }
            ?.key

        return bestPerformingSize ?: AdSize.BANNER
    }

    /**
     * Get performance statistics
     */
    fun getPerformanceStats(): String {
        val totalLoads = adLoadMetrics.size
        val successfulLoads = adLoadMetrics.count { it.success }
        val successRate = if (totalLoads > 0) (successfulLoads * 100 / totalLoads) else 0
        val avgLoadTime = if (successfulLoads > 0) {
            adLoadMetrics.filter { it.success }.map { it.loadTimeMs }.average().toLong()
        } else 0L

        return """
            📊 GlobalAdManager Performance Stats:
            Total Ad Loads: $totalLoads
            Success Rate: $successRate% ($successfulLoads/$totalLoads)
            Average Load Time: ${avgLoadTime}ms
            Configured Ad Sizes: ${adConfigurations.size}
            Optimization Ready: $isOptimizationReady
        """.trimIndent()
    }

    /**
     * Get comprehensive status information
     */
    fun getStatusInfo(): String {
        return """
            🚀 GlobalAdManager Status:
            Initialized: $isInitialized
            Optimization Ready: $isOptimizationReady
            Cached Configurations: ${adConfigurations.size}
            Context Available: ${contextFactory != null}
            Performance Metrics: ${adLoadMetrics.size} records
            Active Tracking: ${adLoadStartTimes.size} loads
        """.trimIndent()
    }

    /**
     * Should show interstitial ad when entering detail view?
     * Shows every Nth visit (configurable via visitsBeforeInterstitial) with minimum time interval
     */
    actual fun shouldShowInterstitialOnDetailView(): Boolean {
        detailViewVisitCount++

        val shouldShowByCount = detailViewVisitCount % visitsBeforeInterstitial == 0
        val currentTime = System.now().toEpochMilliseconds()
        val enoughTimeElapsed = (currentTime - lastInterstitialShownAt) >= minInterstitialInterval

        val shouldShow = shouldShowByCount && enoughTimeElapsed

        log.d("🎯 InterstitialAd: Visit #$detailViewVisitCount, ShouldShow: $shouldShow (Count: $shouldShowByCount, Time: $enoughTimeElapsed)")

        if (shouldShow) {
            lastInterstitialShownAt = currentTime
        }

        return shouldShow
    }

    /**
     * Reset detail view counter (useful for testing or app restart)
     */
    actual fun resetDetailViewCounter() {
        detailViewVisitCount = 0
        lastInterstitialShownAt = 0L
        log.d("🔄 InterstitialAd: Counter reset")
    }

    /**
     * Get current detail view visit count (for debugging)
     */
    actual fun getDetailViewVisitCount(): Int = detailViewVisitCount

    /**
     * Get minutes since last interstitial ad was shown (for debugging)
     */
    actual fun getMinutesSinceLastInterstitial(): Long {
        val currentTime = System.now().toEpochMilliseconds()
        return if (lastInterstitialShownAt > 0) {
            (currentTime - lastInterstitialShownAt) / 60_000L
        } else {
            999L // No ad shown yet
        }
    }

    actual companion object {
        /**
         * Get the correct Ad Unit ID for the current platform and ad type
         * Uses test Ad Unit IDs in debug builds, production IDs from AdMobConfig in release builds
         */
        actual fun getPlatformAdUnitId(adType: AdType): String {
            // Use test Ad Unit IDs in debug builds
            if (BuildConfig.IS_DEBUG) {
                return when (adType) {
                    AdType.BANNER -> AdUnitId.BANNER_DEFAULT
                    AdType.INTERSTITIAL -> AdUnitId.INTERSTITIAL_DEFAULT
                    AdType.REWARDED -> AdUnitId.REWARDED_DEFAULT
                }
            }

            // Use production Ad Unit IDs from AdMobConfig in release builds
            return when (adType) {
                AdType.BANNER -> AdMobConfig.bannerAdUnitId
                AdType.INTERSTITIAL -> AdMobConfig.interstitialAdUnitId
                AdType.REWARDED -> AdMobConfig.rewardedAdUnitId
            }
        }
    }
}
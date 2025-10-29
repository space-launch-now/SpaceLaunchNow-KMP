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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.calebjones.spacelaunchnow.PlatformType
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.platform.ContextFactory
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.data.config.AdMobConfig

/**
 * Global ad manager that provides optimized ad configuration and preloading strategies.
 *
 * Since BannerAdHandler instances can only be created within Composable contexts using rememberBannerAd,
 * this manager focuses on:
 * - Optimal ad configuration caching
 * - Ad request optimization
 * - Loading state management
 * - Performance monitoring
 */
@OptIn(DependsOnGoogleMobileAds::class)
class GlobalAdManager(
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
    private val minInterstitialInterval = 300_000L // 5 minutes minimum between interstitials

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
        val timestamp: Long = Clock.System.now().toEpochMilliseconds()
    )

    /**
     * Initialize the ad manager and prepare optimizations
     */
    fun initializeAndPreload() {
        // Note: iOS doesn't require a context factory, only Android does
        if (contextFactory == null && getPlatform().type == PlatformType.ANDROID) {
            println("🚫 GlobalAdManager: No context factory on Android, skipping initialization")
            return
        }

        println("🚀 GlobalAdManager: Initializing ad optimization system on ${getPlatform().type}...")

        scope.launch {
            // Initialize ad configurations for optimal performance
            setupAdConfigurations()

            isInitialized = true
            isOptimizationReady = true

            println("✅ GlobalAdManager: Initialization complete - ads will load optimally!")
        }
    }

    private suspend fun setupAdConfigurations() {
        // Configure banner ads (most common)
        adConfigurations[AdSize.BANNER] = AdConfig(
            adUnitId = getPlatformAdUnitId(AdType.BANNER),
            adSize = AdSize.BANNER,
            isOptimized = true,
            priority = AdPriority.HIGH
        )

        // Configure large banner ads (home page, detail views)
        adConfigurations[AdSize.LARGE_BANNER] = AdConfig(
            adUnitId = getPlatformAdUnitId(AdType.BANNER),
            adSize = AdSize.LARGE_BANNER,
            isOptimized = true,
            priority = AdPriority.HIGH
        )

        // Configure medium rectangle ads (content areas)
        adConfigurations[AdSize.MEDIUM_RECTANGLE] = AdConfig(
            adUnitId = getPlatformAdUnitId(AdType.BANNER),
            adSize = AdSize.MEDIUM_RECTANGLE,
            isOptimized = true,
            priority = AdPriority.NORMAL
        )

        // Configure leaderboard ads (tablets)
        adConfigurations[AdSize.LEADERBOARD] = AdConfig(
            adUnitId = getPlatformAdUnitId(AdType.BANNER),
            adSize = AdSize.LEADERBOARD,
            isOptimized = true,
            priority = AdPriority.NORMAL
        )

        // Configure fluid ads (responsive)
        adConfigurations[AdSize.FLUID] = AdConfig(
            adUnitId = getPlatformAdUnitId(AdType.BANNER),
            adSize = AdSize.FLUID,
            isOptimized = true,
            priority = AdPriority.LOW
        )

        println("📝 GlobalAdManager: Configured ${adConfigurations.size} ad sizes for optimization")
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
        val trackingId = "${adSize}_${Clock.System.now().toEpochMilliseconds()}"
        adLoadStartTimes[trackingId] = Clock.System.now().toEpochMilliseconds()
        println("🏁 GlobalAdManager: Started tracking ad load for $adSize (ID: $trackingId)")
        return trackingId
    }

    /**
     * Complete ad load tracking and record metrics
     */
    fun completeAdLoadTracking(trackingId: String, adSize: AdSize, success: Boolean) {
        val startTime = adLoadStartTimes.remove(trackingId)
        if (startTime != null) {
            val loadTime = Clock.System.now().toEpochMilliseconds() - startTime
            val metric = AdLoadMetric(adSize, loadTime, success)
            adLoadMetrics.add(metric)

            // Keep only recent metrics (last 50)
            if (adLoadMetrics.size > 50) {
                adLoadMetrics.removeAt(0)
            }

            val status = if (success) "✅" else "❌"
            println("📊 GlobalAdManager: $status Ad load completed in ${loadTime}ms for $adSize")
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
     * Shows every 4th visit with minimum 30-second interval
     */
    fun shouldShowInterstitialOnDetailView(): Boolean {
        detailViewVisitCount++

        val shouldShowByCount = detailViewVisitCount % 4 == 0
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val enoughTimeElapsed = (currentTime - lastInterstitialShownAt) >= minInterstitialInterval

        val shouldShow = shouldShowByCount && enoughTimeElapsed

        println("🎯 InterstitialAd: Visit #$detailViewVisitCount, ShouldShow: $shouldShow (Count: $shouldShowByCount, Time: $enoughTimeElapsed)")

        if (shouldShow) {
            lastInterstitialShownAt = currentTime
            println("📅 InterstitialAd: Timestamp updated to $currentTime")
        }

        return shouldShow
    }

    /**
     * Reset detail view counter (useful for testing or app restart)
     */
    fun resetDetailViewCounter() {
        detailViewVisitCount = 0
        lastInterstitialShownAt = 0L
        println("🔄 InterstitialAd: Counter reset")
    }

    /**
     * Get current detail view visit count (for debugging)
     */
    fun getDetailViewVisitCount(): Int = detailViewVisitCount

    /**
     * Get minutes since last interstitial ad was shown (for debugging)
     */
    fun getMinutesSinceLastInterstitial(): Long {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        return if (lastInterstitialShownAt > 0) {
            (currentTime - lastInterstitialShownAt) / 60_000L
        } else {
            999L // No ad shown yet
        }
    }

    companion object {
        /**
         * Ad types supported by the app
         */
        enum class AdType {
            BANNER,
            INTERSTITIAL,
            REWARDED
        }

        /**
         * Get the correct Ad Unit ID for the current platform and ad type
         * Uses test Ad Unit IDs in debug builds, production IDs from AdMobConfig in release builds
         */
        fun getPlatformAdUnitId(adType: AdType = AdType.BANNER): String {
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
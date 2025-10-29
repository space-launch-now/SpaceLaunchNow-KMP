package me.calebjones.spacelaunchnow.ui.ads

import me.calebjones.spacelaunchnow.platform.ContextFactory

/**
 * Global ad manager that provides optimized ad configuration and preloading strategies.
 * 
 * Platform-specific implementations:
 * - Android/iOS: Full ad management with BasicAds integration
 * - Desktop: No-op stub implementation
 */
expect class GlobalAdManager(contextFactory: ContextFactory?) {
    /**
     * Initialize the ad manager and prepare optimizations
     */
    fun initializeAndPreload()
    
    /**
     * Should show interstitial ad when entering detail view?
     * Shows every Nth visit with minimum interval
     */
    fun shouldShowInterstitialOnDetailView(): Boolean
    
    /**
     * Reset detail view counter (useful for testing or app restart)
     */
    fun resetDetailViewCounter()
    
    /**
     * Get current detail view visit count (for debugging)
     */
    fun getDetailViewVisitCount(): Int
    
    /**
     * Get minutes since last interstitial ad was shown (for debugging)
     */
    fun getMinutesSinceLastInterstitial(): Long
    
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
         */
        fun getPlatformAdUnitId(adType: AdType = AdType.BANNER): String
    }
}

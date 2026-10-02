package me.calebjones.spacelaunchnow.ui.ads

/**
 * Records which screen visit each shared preloaded banner was last shown for.
 *
 * Banners come from a small app-wide pool (see WithPreloadedAds), so without this every
 * launch detail screen shows the same creative. A placement that passes a refresh key asks
 * for a new ad when its banner was last shown for a different token. The first keyed use of
 * a banner keeps the preloaded ad, so the first screen still renders at once.
 *
 * Main-thread only: called from composition effects.
 */
internal object BannerRefreshTracker {
    private val lastTokens = mutableMapOf<Any, Any>()

    /** Returns true when [banner] was last shown for a token other than [token]. */
    fun shouldReload(banner: Any, token: Any): Boolean {
        val previous = lastTokens.put(banner, token)
        return previous != null && previous != token
    }
}

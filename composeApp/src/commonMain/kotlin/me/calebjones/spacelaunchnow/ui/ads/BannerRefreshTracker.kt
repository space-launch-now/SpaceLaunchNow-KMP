package me.calebjones.spacelaunchnow.ui.ads

/**
 * Records which screen visit each shared preloaded banner was last shown for.
 *
 * Banners come from a small app-wide pool (see WithPreloadedAds), so without this every
 * launch detail screen shows the same creative. A placement that passes a refresh key asks
 * for a new ad when its banner was last shown for a different token. The first keyed use of
 * a banner keeps the preloaded ad, so the first screen still renders at once.
 *
 * Reloads are rate limited per banner: a new token inside [minIntervalMs] of the last load keeps
 * the current ad, so fast navigation does not burn ad requests.
 *
 * Main-thread only: called from composition effects.
 */
internal object BannerRefreshTracker {
    /** Default minimum gap between two loads of the same banner. */
    const val DEFAULT_MIN_INTERVAL_MS = 45_000L

    private val lastTokens = mutableMapOf<Any, Any>()
    private val lastLoadMs = mutableMapOf<Any, Long>()

    /**
     * Returns true when [banner] was last shown for a token other than [token] and at least
     * [minIntervalMs] have passed since its last load. A first use records [nowMs] as the load time.
     */
    fun shouldReload(
        banner: Any,
        token: Any,
        nowMs: Long,
        minIntervalMs: Long = DEFAULT_MIN_INTERVAL_MS
    ): Boolean {
        val previous = lastTokens.put(banner, token)
        val lastLoad = lastLoadMs[banner]
        if (previous == null || lastLoad == null) {
            lastLoadMs[banner] = nowMs
            return false
        }
        if (previous == token || nowMs - lastLoad < minIntervalMs) return false
        lastLoadMs[banner] = nowMs
        return true
    }

    /** Drops the state kept for [banner]; call when its handler is destroyed. */
    fun forget(banner: Any) {
        lastTokens.remove(banner)
        lastLoadMs.remove(banner)
    }
}

package me.calebjones.spacelaunchnow.ui.ads

/** Decides whether a banner's real frame is worth a field report. Pure, no platform types. */
internal object AdFrameCheck {

    /**
     * True when the banner's real size [realPx] differs from the size it loaded at [loadedPx],
     * or its visible part [clippedPx] (null when not measured) is smaller than the real size.
     * Sizes are (width, height) in px. Equal sizes and no clipping give false.
     */
    fun mismatch(
        loadedPx: Pair<Int, Int>,
        realPx: Pair<Int, Int>,
        clippedPx: Pair<Int, Int>? = null,
    ): Boolean {
        if (loadedPx.first <= 0 || loadedPx.second <= 0) return false
        if (realPx != loadedPx) return true
        return clippedPx != null && (clippedPx.first < realPx.first || clippedPx.second < realPx.second)
    }
}

/** Lets each placement report a frame mismatch once per session (process). */
internal object FrameMismatchLimiter {
    private val seen = mutableSetOf<String>()

    /** True the first time [placement] asks, false after that. */
    fun shouldReport(placement: String): Boolean = seen.add(placement)

    /** For tests. */
    fun reset() = seen.clear()
}

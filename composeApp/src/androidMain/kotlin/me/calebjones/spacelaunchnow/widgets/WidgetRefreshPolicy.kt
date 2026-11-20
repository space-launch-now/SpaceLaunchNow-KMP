package me.calebjones.spacelaunchnow.widgets

import me.calebjones.spacelaunchnow.domain.model.Launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * When the widgets refresh their launch list from the network (Tasks#1223). A redraw never does:
 * it reads the cache. The worker asks this policy at each run, and the answer depends on how
 * near the next launch is, because a launch that is near changes often.
 *
 * Both functions take the clock as an argument, so a test sets the time.
 */
object WidgetRefreshPolicy {

    /** A launch more than a day away. */
    val FAR_MAX_AGE: Duration = 6.hours

    /** A launch from 1 hour to 24 hours away. */
    val NEAR_MAX_AGE: Duration = 1.hours

    /** A launch less than an hour away or less than an hour past, or one on Hold or in flight. */
    val IMMINENT_MAX_AGE: Duration = 15.minutes

    private val DAY = 24.hours
    private val HOUR = 1.hours
    private const val STATUS_HOLD = 5
    private const val STATUS_IN_FLIGHT = 6

    /**
     * How old the cached list may be before a refresh is due. Each launch sets a limit, and the
     * shortest limit wins, so the nearest launch decides:
     * - status Hold or In Flight, or NET less than 1 hour away or less than 1 hour past: 15 minutes;
     * - NET from 1 hour to 24 hours away, or more than 1 hour past: 1 hour;
     * - NET more than 24 hours away: 6 hours.
     * A list with no NET and no such status (or no launch) has no near launch: 6 hours.
     */
    fun maxCacheAge(launches: List<Launch>, now: Instant): Duration =
        launches.minOfOrNull { maxCacheAge(it, now) } ?: FAR_MAX_AGE

    /**
     * True when the list stored at [cachedAt] is older than [maxCacheAge]. No stored list
     * ([cachedAt] null) is due. A list stored in the future (the clock moved back) is due too.
     */
    fun isRefreshDue(launches: List<Launch>, cachedAt: Instant?, now: Instant): Boolean {
        if (cachedAt == null) return true
        val age = now - cachedAt
        return age.isNegative() || age > maxCacheAge(launches, now)
    }

    /**
     * True when a placed widget needs the launch list. The next-up widget always does. The
     * launch list widget does only with access: a locked one draws no launches, so it needs none.
     */
    fun widgetsNeedList(nextUpPlaced: Boolean, launchListPlaced: Boolean, hasAccess: Boolean): Boolean =
        nextUpPlaced || (launchListPlaced && hasAccess)

    private fun maxCacheAge(launch: Launch, now: Instant): Duration {
        if (launch.status?.id == STATUS_HOLD || launch.status?.id == STATUS_IN_FLIGHT) return IMMINENT_MAX_AGE
        val net = launch.net ?: return FAR_MAX_AGE
        val untilNet = net - now
        return when {
            untilNet > DAY -> FAR_MAX_AGE
            untilNet >= HOUR -> NEAR_MAX_AGE
            untilNet > -HOUR -> IMMINENT_MAX_AGE
            else -> NEAR_MAX_AGE
        }
    }
}

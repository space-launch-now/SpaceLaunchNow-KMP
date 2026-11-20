package me.calebjones.spacelaunchnow.widgets

import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.util.logging.logger
import me.calebjones.spacelaunchnow.util.rethrowIfCancellation
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * The one list of upcoming launches that both widgets draw (Tasks#1223). The next-up widget
 * shows the first row of it.
 *
 * A redraw ([launchesForRender]) reads the cache and never refreshes it. The network is read in
 * two places only: the first draw with nothing cached, and [refreshIfDue] from the update worker.
 */
class WidgetLaunchSource(
    private val repository: LaunchRepository,
    private val clock: Clock = Clock.System
) {
    private val log = logger()

    /**
     * The launches to draw. A cached list is used whatever its age, and may come from the home
     * screen (it is shorter when the home list holds fewer rows). With nothing cached, one read
     * fills the cache so the widget is not blank. [hasAccess] false (a locked widget) reads nothing.
     */
    suspend fun launchesForRender(
        agencyIds: List<Int>?,
        locationIds: List<Int>?,
        hasAccess: Boolean = true
    ): List<Launch> {
        if (!hasAccess) return emptyList()
        repository.getCachedUpcomingLaunchesForWidget(LIST_LIMIT, agencyIds, locationIds)
            ?.let { return it.data.results }
        return fetch(agencyIds, locationIds, forceRefresh = false)?.results.orEmpty()
    }

    /**
     * Reads the network when [WidgetRefreshPolicy] says the cached list is too old for how near
     * its first launch is. A failed read keeps the cached list. Returns true when it read.
     */
    suspend fun refreshIfDue(agencyIds: List<Int>?, locationIds: List<Int>?): Boolean {
        val cached = repository.getCachedUpcomingLaunchesForWidget(LIST_LIMIT, agencyIds, locationIds)
        val cachedAt = cached?.timestamp?.let(Instant::fromEpochMilliseconds)
        val due = WidgetRefreshPolicy.isRefreshDue(cached?.data?.results.orEmpty(), cachedAt, clock.now())
        if (!due) {
            log.d { "Widget list is fresh enough, no refresh" }
            return false
        }
        fetch(agencyIds, locationIds, forceRefresh = true)
        return true
    }

    private suspend fun fetch(
        agencyIds: List<Int>?,
        locationIds: List<Int>?,
        forceRefresh: Boolean
    ) = try {
        repository.getUpcomingLaunchesForWidget(LIST_LIMIT, forceRefresh, agencyIds, locationIds)
            .onFailure { log.w(it) { "Widget launch read failed" } }
            .getOrNull()?.data
    } catch (e: Exception) {
        e.rethrowIfCancellation()
        log.e(e) { "Failed to read widget launches" }
        null
    }

    companion object {
        /** Rows read for the list widget. */
        const val LIST_LIMIT = 10
    }
}

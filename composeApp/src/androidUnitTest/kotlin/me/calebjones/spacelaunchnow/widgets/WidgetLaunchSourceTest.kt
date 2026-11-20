package me.calebjones.spacelaunchnow.widgets

import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.FakeLaunchRepository
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class WidgetLaunchSourceTest {

    private val now = Instant.parse("2026-11-20T12:00:00Z")

    private val clock = object : Clock {
        override fun now(): Instant = now
    }

    /** Counts the widget reads; the cache and the network answers are set by the test. */
    private class WidgetRepository(
        var cached: DataResult<PaginatedResult<Launch>>? = null,
        var network: Result<DataResult<PaginatedResult<Launch>>> = Result.failure(IllegalStateException("no network")),
        val delegate: FakeLaunchRepository = FakeLaunchRepository()
    ) : LaunchRepository by delegate {
        val cacheReads = mutableListOf<Triple<Int, List<Int>?, List<Int>?>>()
        val networkReads = mutableListOf<NetworkRead>()

        class NetworkRead(
            val limit: Int,
            val forceRefresh: Boolean,
            val agencyIds: List<Int>?,
            val locationIds: List<Int>?
        )

        override suspend fun getCachedUpcomingLaunchesForWidget(
            limit: Int, agencyIds: List<Int>?, locationIds: List<Int>?
        ): DataResult<PaginatedResult<Launch>>? {
            cacheReads += Triple(limit, agencyIds, locationIds)
            return cached
        }

        override suspend fun getUpcomingLaunchesForWidget(
            limit: Int, forceRefresh: Boolean, agencyIds: List<Int>?, locationIds: List<Int>?
        ): Result<DataResult<PaginatedResult<Launch>>> {
            networkReads += NetworkRead(limit, forceRefresh, agencyIds, locationIds)
            return network
        }

        /** True when a widget read went to the normal list (the old, network-reading path). */
        val readNormalList get() = delegate.getUpcomingLaunchesNormalDomainCalled
    }

    private fun page(launches: List<Launch>) = PaginatedResult(launches.size, null, null, launches)

    private fun result(
        vararg launches: Launch,
        cachedAgo: Duration = Duration.ZERO,
        source: DataSource = DataSource.STALE_CACHE
    ) = DataResult(page(launches.toList()), source, (now - cachedAgo).toEpochMilliseconds())

    private fun source(repository: LaunchRepository) = WidgetLaunchSource(repository, clock)

    private val soon = widgetLaunch("soon", now + 2.hours)

    // -- Render ----------------------------------------------------------------

    @Test
    fun renderWithACachedListMakesNoNetworkCall() = runTest {
        // Even a very old list: a redraw never refreshes.
        val repository = WidgetRepository(cached = result(soon, cachedAgo = 40.hours))

        val launches = source(repository).launchesForRender(agencyIds = listOf(44), locationIds = listOf(12))

        assertEquals(listOf(soon), launches)
        assertTrue(repository.networkReads.isEmpty())
        assertFalse(repository.readNormalList)
        assertEquals(1, repository.cacheReads.size)
        assertEquals(Triple<Int, List<Int>?, List<Int>?>(10, listOf(44), listOf(12)), repository.cacheReads.single())
    }

    @Test
    fun renderWithAnEmptyCacheMakesOneNetworkCall() = runTest {
        val repository = WidgetRepository(cached = null, network = Result.success(result(soon, source = DataSource.NETWORK)))

        val launches = source(repository).launchesForRender(agencyIds = listOf(44), locationIds = null)

        assertEquals(listOf(soon), launches)
        val read = repository.networkReads.single()
        assertEquals(10, read.limit)
        assertEquals(listOf(44), read.agencyIds)
        assertNull(read.locationIds)
        assertFalse(read.forceRefresh)
        assertFalse(repository.readNormalList)
    }

    @Test
    fun renderWithAnEmptyCacheAndNoNetworkDrawsAnEmptyList() = runTest {
        val repository = WidgetRepository()

        assertTrue(source(repository).launchesForRender(null, null).isEmpty())
        assertEquals(1, repository.networkReads.size)
    }

    @Test
    fun cachedEmptyListIsNotRefetchedOnRender() = runTest {
        val repository = WidgetRepository(cached = result())

        assertTrue(source(repository).launchesForRender(null, null).isEmpty())
        assertTrue(repository.networkReads.isEmpty())
    }

    @Test
    fun lockedLaunchListWidgetMakesNoCallAtAll() = runTest {
        val repository = WidgetRepository(cached = null, network = Result.success(result(soon)))

        val launches = source(repository).launchesForRender(null, null, hasAccess = false)

        assertTrue(launches.isEmpty())
        assertTrue(repository.networkReads.isEmpty())
        assertTrue(repository.cacheReads.isEmpty())
        assertFalse(repository.readNormalList)
    }

    // -- 5a: the home list's entry ---------------------------------------------

    @Test
    fun renderDrawsTheShorterHomeEntryWithoutANetworkCall() = runTest {
        // The repository returns the home list entry (8 rows, 5 minutes old): the widget draws
        // those rows and makes no call.
        val eight = (1..8).map { widgetLaunch("h$it", now + it.hours) }
        val repository = WidgetRepository(cached = result(*eight.toTypedArray(), cachedAgo = 5.minutes))

        val launches = source(repository).launchesForRender(null, null)

        assertEquals(eight, launches)
        assertTrue(repository.networkReads.isEmpty())
    }

    @Test
    fun refreshSkipsWhenTheHomeEntryIsNewEnough() = runTest {
        val repository = WidgetRepository(cached = result(soon, cachedAgo = 5.minutes))

        assertFalse(source(repository).refreshIfDue(null, null))
        assertTrue(repository.networkReads.isEmpty())
    }

    // -- Worker refresh --------------------------------------------------------

    @Test
    fun refreshReadsTheNetworkWhenTheListIsTooOldForTheNextLaunch() = runTest {
        // Next launch 2 h away: 1 hour limit. The list is 61 minutes old.
        val repository = WidgetRepository(
            cached = result(soon, cachedAgo = 61.minutes),
            network = Result.success(result(soon, source = DataSource.NETWORK))
        )

        assertTrue(source(repository).refreshIfDue(listOf(44), listOf(12)))

        val network = repository.networkReads.single()
        assertTrue(network.forceRefresh)
        assertEquals(10, network.limit)
        assertEquals(listOf(44), network.agencyIds)
        assertEquals(listOf(12), network.locationIds)
    }

    @Test
    fun refreshSkipsWhenTheListIsNewEnough() = runTest {
        val repository = WidgetRepository(cached = result(soon, cachedAgo = 59.minutes))

        assertFalse(source(repository).refreshIfDue(null, null))
        assertTrue(repository.networkReads.isEmpty())
    }

    @Test
    fun refreshWithAnEmptyCacheReadsOnce() = runTest {
        val repository = WidgetRepository(cached = null, network = Result.success(result(soon)))

        assertTrue(source(repository).refreshIfDue(null, null))
        assertEquals(1, repository.networkReads.size)
    }

    @Test
    fun failedRefreshKeepsTheCachedListAndDoesNotThrow() = runTest {
        val repository = WidgetRepository(cached = result(soon, cachedAgo = 3.hours))

        assertTrue(source(repository).refreshIfDue(null, null))
        assertEquals(listOf(soon), source(repository).launchesForRender(null, null))
        assertEquals(1, repository.networkReads.size, "the render after the failed refresh reads no network")
    }
}

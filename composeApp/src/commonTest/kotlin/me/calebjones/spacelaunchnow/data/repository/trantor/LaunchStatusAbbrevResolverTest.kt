package me.calebjones.spacelaunchnow.data.repository.trantor

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.api.trantor.models.LookupItem
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.LaunchStatus
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

class LaunchStatusAbbrevResolverTest {

    private val statuses = listOf(
        LookupItem(id = 3, name = "Success", abbrev = "SUCCESS"),
        LookupItem(id = 1, name = "Go for Launch", abbrev = "GO"),
        LookupItem(id = 99, name = "No Abbrev", abbrev = null)
    )

    private fun launch(statusId: Int, name: String = "Launch Successful", abbrev: String? = null): Launch {
        val base = me.calebjones.spacelaunchnow.domain.model.Provider(
            id = 1, name = "SpaceX", abbrev = "SpX", type = null, countryCode = null,
            logoUrl = null, socialLogo = null, imageUrl = null
        )
        return Launch(
            id = "id-$statusId", name = "Falcon 9", slug = "f9", net = null, windowStart = null,
            windowEnd = null, lastUpdated = null,
            status = LaunchStatus(id = statusId, name = name, abbrev = abbrev, description = null),
            provider = base, imageUrl = null, thumbnailUrl = null, infographic = null, netPrecision = null
        )
    }

    @Test
    fun fillsAbbrevByStatusId() = runTest {
        val resolver = LaunchStatusAbbrevResolver({ statuses })
        assertEquals("SUCCESS", resolver.resolve(launch(3)).status?.abbrev)
        assertEquals("GO", resolver.resolve(launch(1)).status?.abbrev)
    }

    @Test
    fun unknownStatusIdOrBlankAbbrevStaysNull() = runTest {
        val resolver = LaunchStatusAbbrevResolver({ statuses })
        assertNull(resolver.resolve(launch(42)).status?.abbrev)
        assertNull(resolver.resolve(launch(99)).status?.abbrev)
    }

    @Test
    fun resolvesWholePageAndKeepsPaging() = runTest {
        val resolver = LaunchStatusAbbrevResolver({ statuses })
        val page = PaginatedResult(count = 7, next = "n", previous = null, results = listOf(launch(3), launch(1)))
        val out = resolver.resolve(page)
        assertEquals(listOf("SUCCESS", "GO"), out.results.map { it.status?.abbrev })
        assertEquals(7, out.count)
        assertEquals("n", out.next)
    }

    @Test
    fun lookupsFetchedOnlyOnceAcrossCalls() = runTest {
        var calls = 0
        val resolver = LaunchStatusAbbrevResolver({ calls++; statuses })
        resolver.resolve(launch(3))
        resolver.resolve(launch(1))
        resolver.resolve(PaginatedResult(1, null, null, listOf(launch(3))))
        assertEquals(1, calls)
    }

    @Test
    fun lookupsFailureLeavesAbbrevNullAndDoesNotThrow() = runTest {
        val resolver = LaunchStatusAbbrevResolver({ throw RuntimeException("offline") })
        val out = resolver.resolve(launch(3))
        assertNull(out.status?.abbrev)
        assertEquals("Launch Successful", out.status?.name)
    }

    @Test
    fun failureIsNotRetriedImmediately() = runTest {
        var calls = 0
        val resolver = LaunchStatusAbbrevResolver({ calls++; throw RuntimeException("offline") }, retryAfterFailure = 1.hours)
        resolver.resolve(launch(3))
        resolver.resolve(launch(3))
        assertEquals(1, calls)
    }

    @Test
    fun slowLookupsTimeOutInsteadOfBlockingTheList() = runTest {
        val resolver = LaunchStatusAbbrevResolver({ delay(10_000); statuses }, fetchTimeout = 100.milliseconds)
        val out = resolver.resolve(launch(3))
        assertNull(out.status?.abbrev)
    }

    @Test
    fun existingAbbrevIsNotOverwritten() = runTest {
        val resolver = LaunchStatusAbbrevResolver({ statuses })
        assertEquals("OK", resolver.resolve(launch(3, abbrev = "OK")).status?.abbrev)
    }
}

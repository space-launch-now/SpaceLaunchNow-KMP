package me.calebjones.spacelaunchnow.data.repository.terminus

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.terminus.apis.ConfigApi
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.database.FilterOptionsLocalDataSource
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource.CachedValue
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import me.calebjones.spacelaunchnow.ui.viewmodel.InMemoryPreferencesDataStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/** In-memory [FilterOptionStore] for tests. */
internal class FakeFilterOptionStore : FilterOptionStore {
    val rows = mutableMapOf<String, CachedValue<List<FilterOption>>>()
    override suspend fun read(key: String) = rows[key]
    override suspend fun write(key: String, options: List<FilterOption>, cachedAt: Long) {
        rows[key] = CachedValue(options, cachedAt)
    }
}

/** The Terminus filter fetcher with its disk store (Tasks#1207). */
class TerminusFilterFetcherTest {

    private val base = "https://terminus.test"
    private val json = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    private val body = """{"count":1,"next":null,"previous":null,"results":[{"id":2,"name":"Active"}]}"""
    private val active = listOf(FilterOption(2, "Active"))
    private val key = "filter:astronaut-statuses"

    private enum class Mode { OK, DOWN, IO }

    private class Env(val requests: MutableList<Url>, var mode: Mode, var nowMs: Long) {
        lateinit var api: ConfigApi
    }

    private fun env(): Env {
        val env = Env(mutableListOf(), Mode.OK, 1_000_000L)
        val engine = MockEngine { request ->
            env.requests += request.url
            when (env.mode) {
                Mode.OK -> respond(body, HttpStatusCode.OK, json)
                Mode.DOWN -> respond("down", HttpStatusCode.ServiceUnavailable, json)
                Mode.IO -> throw IOException("offline")
            }
        }
        env.api = ConfigApi(base, engine)
        return env
    }

    /** A new fetcher with a new memory cache: a cold start over the same [store]. */
    private fun Env.fetcher(store: FilterOptionStore, ttl: Duration = 7.days) =
        TerminusFilterFetcher(store, { ttl }, TerminusMemoryCache(ttl = 1.hours, now = { nowMs }), { nowMs })

    private suspend fun Env.read(fetcher: TerminusFilterFetcher, force: Boolean = false) = fetcher.options(
        "astronaut-statuses", force,
        { limit, offset -> api.configAstronautStatusesList(limit = limit, offset = offset) }
    ) { page -> FilterPage(page.results.map { FilterOption(it.id, it.name) }, page.count) }

    @Test
    fun firstReadFetchesAndStores() = runTest {
        val env = env()
        val store = FakeFilterOptionStore()

        assertEquals(active, env.read(env.fetcher(store)).getOrThrow())

        assertEquals(1, env.requests.size)
        assertEquals(active, store.rows[key]?.value)
        assertEquals(env.nowMs, store.rows[key]?.cachedAt)
    }

    @Test
    fun aColdStartInsideSevenDaysMakesNoRequest() = runTest {
        val env = env()
        val store = FakeFilterOptionStore()
        env.read(env.fetcher(store)).getOrThrow()

        env.nowMs += 6.days.inWholeMilliseconds
        assertEquals(active, env.read(env.fetcher(store)).getOrThrow())

        assertEquals(1, env.requests.size)
    }

    @Test
    fun afterSevenDaysItFetchesAgain() = runTest {
        val env = env()
        val store = FakeFilterOptionStore()
        env.read(env.fetcher(store)).getOrThrow()

        env.nowMs += 7.days.inWholeMilliseconds + 1
        env.read(env.fetcher(store)).getOrThrow()

        assertEquals(2, env.requests.size)
        assertEquals(env.nowMs, store.rows[key]?.cachedAt)
    }

    @Test
    fun aServerErrorOrIoFailureServesTheStaleDiskCopy() = runTest {
        for (mode in listOf(Mode.DOWN, Mode.IO)) {
            val env = env()
            val store = FakeFilterOptionStore()
            env.read(env.fetcher(store)).getOrThrow()

            env.nowMs += 30.days.inWholeMilliseconds
            env.mode = mode
            assertEquals(active, env.read(env.fetcher(store)).getOrThrow(), "mode $mode")
            assertEquals(2, env.requests.size, "mode $mode")
        }
    }

    @Test
    fun noCopyAndAFailedFetchIsAFailure() = runTest {
        val env = env()
        env.mode = Mode.DOWN

        assertTrue(env.read(env.fetcher(FakeFilterOptionStore())).isFailure)
    }

    @Test
    fun forceRefreshFetchesAndReplacesTheStoredCopy() = runTest {
        val env = env()
        val store = FakeFilterOptionStore()
        store.write(key, listOf(FilterOption(9, "Old")), env.nowMs)

        assertEquals(listOf(FilterOption(9, "Old")), env.read(env.fetcher(store)).getOrThrow())
        assertEquals(0, env.requests.size)

        env.nowMs += 1000
        assertEquals(active, env.read(env.fetcher(store), force = true).getOrThrow())
        assertEquals(1, env.requests.size)
        assertEquals(active, store.rows[key]?.value)
        assertEquals(env.nowMs, store.rows[key]?.cachedAt)
    }

    @Test
    fun withTheDebugTtlACopyOlderThanAnHourIsFetchedAgain() = runTest {
        val env = env()
        val store = FakeFilterOptionStore()
        val debug = TerminusFilterFetcher.DEBUG_DISK_TTL
        env.read(env.fetcher(store, debug)).getOrThrow()

        env.nowMs += 30 * 60 * 1000L
        env.read(env.fetcher(store, debug)).getOrThrow()
        assertEquals(1, env.requests.size)

        env.nowMs += 60 * 60 * 1000L
        env.read(env.fetcher(store, debug)).getOrThrow()
        assertEquals(2, env.requests.size)
    }

    @Test
    fun ttlsAreSevenDaysAndOneHour() {
        assertEquals(7.days, TerminusFilterFetcher.FILTER_DISK_TTL)
        assertEquals(1.hours, TerminusFilterFetcher.DEBUG_DISK_TTL)
    }

    // -- SQL store ------------------------------------------------------------------------------

    private fun database(): SpaceLaunchDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SpaceLaunchDatabase.Schema.create(driver)
        return SpaceLaunchDatabase(driver)
    }

    @Test
    fun theSqlStoreRoundTripsListsIncludingAnEmptyOne() = runTest {
        val store = SqlFilterOptionStore(database())
        val options = listOf(FilterOption(1, "Go", "Go"), FilterOption(2, "Active"))

        assertNull(store.read("filter:a"))
        store.write("filter:a", options, 42L)
        store.write("filter:empty", emptyList(), 43L)

        assertEquals(options, store.read("filter:a")?.value)
        assertEquals(42L, store.read("filter:a")?.cachedAt)
        assertEquals(emptyList(), store.read("filter:empty")?.value)
        assertEquals(43L, store.read("filter:empty")?.cachedAt)

        store.write("filter:a", emptyList(), 50L)
        assertEquals(emptyList(), store.read("filter:a")?.value)
    }

    @Test
    fun anUnreadableStoredListIsAMiss() = runTest {
        val db = database()
        db.terminusFilterListQueries.insertOrReplaceList("filter:bad", "not json", 1L)

        assertNull(SqlFilterOptionStore(db).read("filter:bad"))
    }

    @Test
    fun llAndTerminusRowsDoNotMix() = runTest {
        val db = database()
        val ll = FilterOptionsLocalDataSource(db, AppPreferences(InMemoryPreferencesDataStore()))
        ll.cacheAgency(1, "LL Agency", "LLA")
        val before = ll.getAllAgencies().map { Triple(it.id, it.name, it.abbreviation) }
        val store = SqlFilterOptionStore(db)

        assertNull(store.read("filter:agencies"))
        store.write("filter:agencies", listOf(FilterOption(1, "Terminus Agency")), 1L)

        assertEquals(before, ll.getAllAgencies().map { Triple(it.id, it.name, it.abbreviation) })
        assertEquals(listOf(FilterOption(1, "Terminus Agency")), store.read("filter:agencies")?.value)
    }
}

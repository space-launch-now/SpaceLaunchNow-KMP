package me.calebjones.spacelaunchnow.data.repository.terminus

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi as LLLaunchesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LaunchesApi as TerminusLaunchesApi
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.data.model.DataBackend
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.ll.LLLaunchRepositoryImpl
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import me.calebjones.spacelaunchnow.database.StatsLocalDataSource
import me.calebjones.spacelaunchnow.di.LL_REPOSITORY
import me.calebjones.spacelaunchnow.di.launchRepositoryModule
import me.calebjones.spacelaunchnow.ui.viewmodel.InMemoryPreferencesDataStore
import me.calebjones.spacelaunchnow.util.logging.LogConfig
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Launch lists on Terminus behind [DataBackend] (Tasks#1008 S2). The fixtures are real
 * `GET /api/v1/launches/mini/` and `GET /api/v1/launches/` responses from Terminus staging
 * (`limit=3&upcoming=true&ordering=net`).
 */
@OptIn(ExperimentalTime::class)
class TerminusLaunchListRepositoryTest {

    private fun resource(name: String): String = checkNotNull(
        TerminusLaunchListRepositoryTest::class.java.getResourceAsStream("/terminus/$name")
    ) { "missing fixture $name" }.bufferedReader().use { it.readText() }

    private val netField = Regex("\"net\":\"[^\"]*\"")

    /** Moves every NET of a fixture, so the test does not depend on the day it runs. */
    private fun withNet(raw: String, net: Instant): String =
        netField.replace(raw) { "\"net\":\"$net\"" }

    private val farNet get() = Clock.System.now() + 30.days
    private val nearNet get() = Clock.System.now() + 2.hours

    private val miniFixture: String get() = withNet(resource("launches_mini_list.json"), farNet)
    private val normalFixture: String get() = withNet(resource("launches_normal_list.json"), farNet)

    private val jsonHeaders: Headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private class Reply(val body: String, val status: HttpStatusCode = HttpStatusCode.OK)

    private class Env(
        val terminusRequests: MutableList<Url>,
        val llRequests: MutableList<Url>,
        val driver: JdbcSqliteDriver,
        val database: SpaceLaunchDatabase,
        val local: LaunchLocalDataSource,
        val preferences: AppPreferences,
        val llRepository: LLLaunchRepositoryImpl,
        val repository: LaunchRepository,
        private val terminusClientBuilt: () -> Boolean
    ) {
        val terminusClientWasBuilt: Boolean get() = terminusClientBuilt()

        /** Forces every cached row to count as expired, so only the stale read can serve it. */
        fun expireCache() {
            driver.execute(null, "UPDATE LaunchDetailedCache SET expires_at = 0", 0)
        }

        fun cacheRow(id: String) = database.launchQueries.getDetailedByIdStale(id).executeAsOneOrNull()
    }

    private fun env(
        backend: DataBackend = DataBackend.TERMINUS,
        llBody: String = "",
        terminusHandler: (Url) -> Reply
    ): Env {
        val terminusRequests = mutableListOf<Url>()
        val llRequests = mutableListOf<Url>()
        val terminusEngine = MockEngine { request ->
            terminusRequests += request.url
            val reply = terminusHandler(request.url)
            respond(content = reply.body, status = reply.status, headers = jsonHeaders)
        }
        val llEngine = MockEngine { request ->
            llRequests += request.url
            respond(content = llBody, status = HttpStatusCode.OK, headers = jsonHeaders)
        }
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SpaceLaunchDatabase.Schema.create(driver)
        val database = SpaceLaunchDatabase(driver)
        val preferences = AppPreferences(InMemoryPreferencesDataStore())
        val local = LaunchLocalDataSource(database, preferences)
        var terminusClientBuilt = false
        val koin = koinApplication {
            modules(
                module {
                    single { preferences }
                    single { local }
                    single { StatsLocalDataSource(database, preferences) }
                    single { LLLaunchesApi(baseUrl = "https://ll.test", httpClientEngine = llEngine) }
                    single { AgenciesApi(baseUrl = "https://ll.test", httpClientEngine = llEngine) }
                    single<TerminusLaunchesApi> {
                        terminusClientBuilt = true
                        TerminusLaunchesApi(baseUrl = "https://terminus.test", httpClientEngine = terminusEngine)
                    }
                    single<DataBackend> { backend }
                },
                launchRepositoryModule()
            )
        }.koin
        return Env(
            terminusRequests, llRequests, driver, database, local, preferences,
            koin.get<LLLaunchRepositoryImpl>(named(LL_REPOSITORY)), koin.get<LaunchRepository>(),
            { terminusClientBuilt }
        )
    }

    private fun Url.param(name: String): String? = parameters[name]

    @Test
    fun llBackendMakesZeroTerminusCalls() = runTest {
        val env = env(DataBackend.LL, llBody = miniFixture) { error("Terminus must not be called") }
        val repository = env.repository

        assertTrue(repository.getUpcomingLaunchesDomain(3).isSuccess)
        assertTrue(repository.getPreviousLaunchesDomain(3).isSuccess)
        assertTrue(repository.getFilteredLaunchesDomain(limit = 3, upcoming = true).isSuccess)
        assertEquals(3, env.llRequests.size)

        val normal = env(DataBackend.LL, llBody = normalFixture) { error("Terminus must not be called") }
        assertTrue(normal.repository.getUpcomingLaunchesNormalDomain(3, forceRefresh = true).isSuccess)
        assertTrue(normal.repository.getPreviousLaunchesNormalDomain(3, forceRefresh = true).isSuccess)
        assertEquals(2, normal.llRequests.size)

        for (e in listOf(env, normal)) {
            assertFalse(e.terminusClientWasBuilt)
            assertTrue(e.terminusRequests.isEmpty(), "Terminus got ${e.terminusRequests}")
        }
    }

    @Test
    fun upcomingListRequestsMiniEndpointAndMapsLikeLl() = runTest {
        val fixture = miniFixture
        val env = env(llBody = fixture) { Reply(fixture) }

        val viaTerminus = env.repository.getUpcomingLaunchesDomain(limit = 3).getOrThrow()
        val viaLl = env.llRepository.getUpcomingLaunchesDomain(limit = 3).getOrThrow()

        assertEquals(1, env.terminusRequests.size)
        val url = env.terminusRequests.single()
        assertEquals("/api/v1/launches/mini/", url.encodedPath)
        assertEquals("true", url.param("upcoming"))
        assertEquals("net", url.param("ordering"))
        assertEquals("3", url.param("limit"))
        assertNull(url.param("offset"), "offset 0 is left out")
        assertEquals(1, env.llRequests.size, "only the LL comparison call reaches LL")
        assertEquals(3, viaTerminus.results.size)
        assertEquals(viaLl, viaTerminus)
    }

    @Test
    fun previousListRequestsMiniEndpointNewestFirst() = runTest {
        val fixture = miniFixture
        val env = env { Reply(fixture) }

        env.repository.getPreviousLaunchesDomain(limit = 5, offset = 10).getOrThrow()

        val url = env.terminusRequests.single()
        assertEquals("/api/v1/launches/mini/", url.encodedPath)
        assertEquals("true", url.param("previous"))
        assertEquals("-net", url.param("ordering"))
        assertEquals("10", url.param("offset"))
        assertNull(url.param("upcoming"))
    }

    @Test
    fun timeFiltersAreRoundedOnTheTerminusPath() = runTest {
        val fixture = miniFixture
        val env = env { Reply(fixture) }

        env.repository.getUpcomingLaunchesDomain(
            limit = 3,
            netGt = Instant.parse("2026-11-10T12:00:30Z"),
            netLt = Instant.parse("2026-11-12T12:00:30Z")
        ).getOrThrow()

        val url = env.terminusRequests.single()
        assertEquals("2026-11-10T12:00:00Z", url.param("net__gt"))
        assertEquals("2026-11-12T12:01:00Z", url.param("net__lt"))
        assertEquals("net", url.param("ordering"))
        assertNull(url.param("upcoming"), "a time window replaces the upcoming flag, as on LL")
    }

    @Test
    fun filteredListMapsEveryFilterToItsWireName() = runTest {
        val fixture = miniFixture
        val env = env { Reply(fixture) }

        env.repository.getFilteredLaunchesDomain(
            limit = 20,
            offset = 40,
            upcoming = true,
            previous = false,
            ordering = "-net",
            search = "starlink",
            lspIds = listOf(121, 44, 121),
            locationIds = listOf(12, 27),
            programIds = listOf(1),
            rocketConfigurationId = 164,
            isCrewed = true,
            includeSuborbital = false,
            statusIds = listOf(3, 1),
            orbitIds = listOf(8),
            missionTypeIds = listOf(10),
            launcherConfigFamilyIds = listOf(5, 6)
        ).getOrThrow()

        val url = env.terminusRequests.single()
        assertEquals("/api/v1/launches/mini/", url.encodedPath)
        val expected = mapOf(
            "limit" to "20", "offset" to "40", "upcoming" to "true", "previous" to "false",
            "ordering" to "-net", "search" to "starlink", "lsp__id" to "44,121",
            "location__ids" to "12,27", "program" to "1", "rocket__configuration__id" to "164",
            "is_crewed" to "true", "include_suborbital" to "false", "status__ids" to "1,3",
            "orbit__ids" to "8", "mission_type__ids" to "10", "launcher_config_family_ids" to "5,6"
        )
        for ((name, value) in expected) assertEquals(value, url.param(name), "query $name in ${url.encodedQuery}")
    }

    @Test
    fun normalListsRequestFullEndpointAndMapLikeLl() = runTest {
        val fixture = normalFixture
        val env = env(llBody = fixture) { Reply(fixture) }

        val upcoming = env.repository.getUpcomingLaunchesNormalDomain(
            limit = 3, forceRefresh = false, agencyIds = listOf(44, 121), locationIds = listOf(12)
        ).getOrThrow()
        val previous = env.repository.getPreviousLaunchesNormalDomain(
            limit = 3, forceRefresh = false, agencyIds = null, locationIds = null
        ).getOrThrow()

        val (up, prev) = env.terminusRequests
        assertEquals("/api/v1/launches/", up.encodedPath)
        assertEquals("true", up.param("upcoming"))
        assertEquals("net", up.param("ordering"))
        assertEquals("44,121", up.param("lsp__id"))
        assertEquals("12", up.param("location__ids"))
        assertNull(up.param("status__ids"), "hide-TBD is off by default")
        assertEquals("/api/v1/launches/", prev.encodedPath)
        assertEquals("true", prev.param("previous"))
        assertEquals("-net", prev.param("ordering"))
        assertEquals(DataSource.NETWORK, upcoming.source)
        assertEquals(DataSource.NETWORK, previous.source)

        val viaLl = env.llRepository.getUpcomingLaunchesNormalDomain(
            limit = 3, forceRefresh = true, agencyIds = null, locationIds = null
        ).getOrThrow()
        assertEquals(viaLl.data, upcoming.data)
    }

    @Test
    fun hideTbdSendsTheStatusFilterLlSends() = runTest {
        val fixture = normalFixture
        val env = env { Reply(fixture) }
        env.preferences.updateHideTbdLaunches(true)

        env.repository.getUpcomingLaunchesNormalDomain(limit = 3, forceRefresh = true).getOrThrow()

        assertEquals("1,3,4,5,6,7,9", env.terminusRequests.single().param("status__ids"))
    }

    @Test
    fun launchOutsideADayCachesPastThirtySeconds() = runTest {
        val fixture = miniFixture
        val env = env { Reply(fixture) }

        env.repository.getUpcomingLaunchesDomain(limit = 3).getOrThrow()
        env.repository.getUpcomingLaunchesDomain(limit = 3).getOrThrow()

        assertEquals(1, env.terminusRequests.size, "the second read is a fresh cache hit")
        val row = assertNotNull(env.database.launchQueries.getDetailedByIdStale(cacheId("mini")).executeAsOneOrNull())
        assertTrue(row.expires_at - row.cached_at > 30_000, "ttl ${row.expires_at - row.cached_at} ms")
    }

    @Test
    fun listWithinADayOfNetCachesForAtMost30Seconds() = runTest {
        val near = withNet(resource("launches_mini_list.json"), nearNet)
        val env = env { Reply(near) }
        env.repository.getUpcomingLaunchesDomain(limit = 3).getOrThrow()

        val row = assertNotNull(env.cacheRow(cacheId("mini")))
        assertTrue(row.expires_at - row.cached_at <= 30_000, "ttl ${row.expires_at - row.cached_at} ms")

        val normalNear = withNet(resource("launches_normal_list.json"), nearNet)
        val normalEnv = env { Reply(normalNear) }
        normalEnv.repository.getUpcomingLaunchesNormalDomain(limit = 3).getOrThrow()
        val normalRow = assertNotNull(normalEnv.cacheRow(cacheId("normal")))
        assertTrue(normalRow.expires_at - normalRow.cached_at <= 30_000)
    }

    @Test
    fun serverErrorServesTheStaleList() = runTest {
        var status = HttpStatusCode.OK
        val fixture = miniFixture
        val env = env { Reply(if (status == HttpStatusCode.OK) fixture else "oops", status) }

        val first = env.repository.getUpcomingLaunchesDomain(limit = 3).getOrThrow()
        env.expireCache()
        status = HttpStatusCode.ServiceUnavailable
        val second = env.repository.getUpcomingLaunchesDomain(limit = 3)

        assertEquals(2, env.terminusRequests.size)
        assertEquals(first, second.getOrThrow())
    }

    @Test
    fun serverErrorWithoutAStaleEntryFails() = runTest {
        val env = env { Reply("oops", HttpStatusCode.InternalServerError) }

        val result = env.repository.getUpcomingLaunchesDomain(limit = 3)

        assertTrue(result.exceptionOrNull()?.message.orEmpty().startsWith("API Error 500:"), "${result.exceptionOrNull()}")
    }

    @Test
    fun networkFailureServesTheStaleNormalListWithItsSource() = runTest {
        var fail = false
        val fixture = normalFixture
        val env = env {
            if (fail) throw IOException("offline")
            Reply(fixture)
        }

        val first = env.repository.getUpcomingLaunchesNormalDomain(limit = 3).getOrThrow()
        fail = true
        val second = env.repository.getUpcomingLaunchesNormalDomain(limit = 3, forceRefresh = true).getOrThrow()

        assertEquals(DataSource.STALE_CACHE, second.source)
        assertEquals(first.data, second.data)
        val cached = env.repository.getUpcomingLaunchesNormalDomain(limit = 3).getOrThrow()
        assertEquals(DataSource.CACHE, cached.source)
    }

    @Test
    fun clientErrorDoesNotServeTheStaleList() = runTest {
        var status = HttpStatusCode.OK
        val fixture = miniFixture
        val env = env { Reply(if (status == HttpStatusCode.OK) fixture else "bad", status) }
        env.repository.getUpcomingLaunchesDomain(limit = 3).getOrThrow()
        env.expireCache()
        status = HttpStatusCode.BadRequest

        assertTrue(env.repository.getUpcomingLaunchesDomain(limit = 3).isFailure)
    }

    @Test
    fun llAndUndecodableRowsAreACacheMissForTheTerminusList() = runTest {
        val fixture = miniFixture
        val env = env { Reply(fixture) }
        val id = cacheId("mini")
        val now = Clock.System.now().toEpochMilliseconds()
        // An LL-style blob (a bare page, no backend envelope) planted under the list's row id.
        env.database.launchQueries.insertOrReplaceDetailed(
            id = id, name = id, status_id = null, status_name = null, net = null, window_end = null,
            window_start = null, launch_service_provider_id = null, launch_service_provider_name = null,
            rocket_configuration_id = null, rocket_configuration_name = null, pad_name = null,
            location_name = null, image_url = null, mission_name = null, mission_description = null,
            json_data = fixture, cached_at = now, expires_at = now + 600_000
        )

        assertTrue(env.repository.getUpcomingLaunchesDomain(limit = 3).isSuccess)
        assertEquals(1, env.terminusRequests.size, "an LL row must not count as a cache hit")

        env.database.launchQueries.insertOrReplaceDetailed(
            id = id, name = id, status_id = null, status_name = null, net = null, window_end = null,
            window_start = null, launch_service_provider_id = null, launch_service_provider_name = null,
            rocket_configuration_id = null, rocket_configuration_name = null, pad_name = null,
            location_name = null, image_url = null, mission_name = null, mission_description = null,
            json_data = "{not json", cached_at = now, expires_at = now + 600_000
        )
        assertTrue(env.repository.getUpcomingLaunchesDomain(limit = 3).isSuccess)
        assertEquals(2, env.terminusRequests.size)
    }

    @Test
    fun terminusListRowsAreNotLaunchRowsForLl() = runTest {
        val fixture = miniFixture
        val env = env { Reply(fixture) }
        env.repository.getUpcomingLaunchesDomain(limit = 3).getOrThrow()

        // The LL repository reads this table by launch id only; a list row has no launch id.
        assertNull(env.local.getDetailedLaunchApi("terminus-list"))
        assertNull(env.llRepository.getStaleDetailedLaunch(cacheId("mini")))
    }

    @Test
    fun otherCallsStayOnLl() = runTest {
        val fixture = miniFixture
        val env = env(llBody = fixture) { Reply(fixture) }

        env.repository.getFeaturedLaunchDomain(forceRefresh = true)
        env.repository.getInFlightLaunchesDomain(forceRefresh = true)
        env.repository.getStarshipLaunchesDomain(limit = 3, forceRefresh = true)

        assertTrue(env.terminusRequests.isEmpty(), "Terminus got ${env.terminusRequests}")
        assertTrue(env.llRequests.isNotEmpty())
    }

    @Test
    fun enumFieldsDecodeInBothFixtures() {
        val json = Json { ignoreUnknownKeys = true }
        val mini = json.decodeFromString<PaginatedLaunchBasicList>(resource("launches_mini_list.json"))
        val normal = json.decodeFromString<PaginatedLaunchNormalList>(resource("launches_normal_list.json"))

        assertEquals(3, mini.results.size)
        assertEquals(3, normal.results.size)
        assertTrue(mini.count >= 3)
        for (launch in mini.results) {
            assertNotNull(launch.status.name)
            // The net_precision enum (day, minute, ...) must decode; a new value would throw above.
            launch.netPrecision?.let { assertNotNull(it.name) }
        }
        for (launch in normal.results) {
            assertNotNull(launch.status.name)
            launch.netPrecision?.let { assertNotNull(it.name) }
        }
    }

    /** The cache row id of the list the `limit=3` upcoming request made. */
    private fun cacheId(kind: String): String =
        "terminus-list:$kind:" + LaunchListQuery.of(limit = 3, upcoming = true, ordering = "net").key

    @Test
    fun logLabelNeverContainsSearchText() {
        val query = LaunchListQuery.of(limit = 3, search = "secret rocket", lspId = listOf(4))
        assertFalse(query.logLabel.contains("secret"))
        assertFalse(query.logLabel.contains("rocket"))
        assertTrue(query.logLabel.contains("search=<set>"))
        assertTrue(query.logLabel.contains("lsp__id=4"))
        assertTrue(query.key.contains("secret"))
    }

    @Test
    fun keyDiffersWhenSearchForgesAnotherFilter() {
        val forged = LaunchListQuery.of(search = "x&lsp__id=1")
        val real = LaunchListQuery.of(search = "x", lspId = listOf(1))
        assertTrue(forged.key != real.key)
        assertTrue(LaunchListQuery.of(search = "a=b").key != LaunchListQuery.of(search = "a%3Db").key)
    }

    @Test
    fun keyStaysShortForLongSearch() {
        val a = LaunchListQuery.of(search = "a".repeat(5000))
        val b = LaunchListQuery.of(search = "a".repeat(4999) + "b")
        assertTrue(a.key.length < 100)
        assertTrue(a.key != b.key)
    }

    @Test
    fun undecodableListEntryLogDoesNotContainSearchText() = runTest {
        val lines = mutableListOf<String>()
        val writer = object : LogWriter() {
            override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
                lines += message
            }
        }
        SpaceLogger.initialize(LogConfig(Severity.Verbose, listOf(writer)))
        try {
            val env = env { error("Terminus must not be called") }
            val query = LaunchListQuery.of(limit = 3, search = "secret rocket")
            val now = Clock.System.now().toEpochMilliseconds()
            env.database.launchQueries.insertOrReplaceDetailed(
                id = "terminus-list:mini:" + query.key, name = "x", status_id = null, status_name = null,
                net = null, window_end = null, window_start = null, launch_service_provider_id = null,
                launch_service_provider_name = null, rocket_configuration_id = null,
                rocket_configuration_name = null, pad_name = null, location_name = null, image_url = null,
                mission_name = null, mission_description = null, json_data = "not json",
                cached_at = now, expires_at = now + 60_000
            )

            assertNull(env.local.getTerminusMiniList(query.key))

            val undecodable = lines.filter { it.contains("Undecodable") }
            assertTrue(undecodable.isNotEmpty(), "expected an Undecodable log line, got $lines")
            for (line in lines) {
                assertFalse(line.contains("secret"), line)
                assertFalse(line.contains("rocket"), line)
            }
        } finally {
            SpaceLogger.initialize(LogConfig(Severity.Verbose, emptyList()))
        }
    }
}

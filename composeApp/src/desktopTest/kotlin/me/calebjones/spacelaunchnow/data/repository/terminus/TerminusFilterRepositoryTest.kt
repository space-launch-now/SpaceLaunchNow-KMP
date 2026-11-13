package me.calebjones.spacelaunchnow.data.repository.terminus

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AgenciesApi as LLAgenciesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ConfigApi as LLConfigApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LauncherConfigurationFamiliesApi as LLFamiliesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LauncherConfigurationsApi as LLConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LocationsApi as LLLocationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ProgramsApi as LLProgramsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.AgenciesApi as TerminusAgenciesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.ConfigApi as TerminusConfigApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LauncherConfigurationFamiliesApi as TerminusFamiliesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LauncherConfigurationsApi as TerminusConfigurationsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LocationsApi as TerminusLocationsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.ProgramsApi as TerminusProgramsApi
import me.calebjones.spacelaunchnow.data.model.DataBackend
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.data.repository.AstronautFilterRepository
import me.calebjones.spacelaunchnow.data.repository.AstronautFilterRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.RocketFilterRepository
import me.calebjones.spacelaunchnow.data.repository.RocketFilterRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ScheduleFilterRepository
import me.calebjones.spacelaunchnow.data.repository.ScheduleFilterRepositoryImpl
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.database.FilterOptionsLocalDataSource
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import me.calebjones.spacelaunchnow.di.filterRepositoryModule
import me.calebjones.spacelaunchnow.ui.viewmodel.InMemoryPreferencesDataStore
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Filter options on Terminus behind [DataBackend] (Tasks#1010 S4). The payloads follow the
 * generated Terminus models (`GET /api/v1/{agencies,programs,launcher_configurations,...}/mini/`
 * and `/api/v1/config/...`).
 */
class TerminusFilterRepositoryTest {

    private val jsonHeaders: Headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    private val base = "https://terminus.test"

    private fun page(count: Int, vararg results: String) =
        """{"count":$count,"next":null,"previous":null,"results":[${results.joinToString(",")}]}"""

    private val emptyPage = page(0)

    private fun agency(id: Int, name: String, abbrev: String?) =
        """{"response_mode":"list","id":$id,"url":"$base/agencies/$id/","name":"$name","type":null,"abbrev":${abbrev?.let { "\"$it\"" }}}"""

    private fun program(id: Int, name: String) =
        """{"response_mode":"list","id":$id,"url":"$base/programs/$id/","name":"$name","image":null,"info_url":null,"wiki_url":null}"""

    private fun rocket(id: Int, name: String, fullName: String?, manufacturer: String?) =
        """{"response_mode":"normal","id":$id,"url":"$base/c/$id/","name":"$name","full_name":${fullName?.let { "\"$it\"" }},"families":[],""" +
            """"manufacturer":${manufacturer?.let { """{"response_mode":"list","id":9,"url":"$base/agencies/9/","name":"Maker","type":null,"abbrev":"$it"}""" }},""" +
            """"program":[],"image":null,"info_url":null,"wiki_url":null}"""

    private fun location(id: Int, name: String?) =
        """{"response_mode":"detailed","id":$id,"url":"$base/l/$id/","name":${name?.let { "\"$it\"" }},"celestial_body":{"response_mode":"normal","id":1,"name":"Earth","type":null,""" +
            """"diameter":null,"mass":null,"gravity":null,"length_of_day":null,"atmosphere":null,"image":null,""" +
            """"description":null,"wiki_url":null},"country":null,"image":null}"""

    private fun status(id: Int, name: String, abbrev: String) = """{"id":$id,"name":"$name","abbrev":"$abbrev"}"""

    private fun orbit(id: Int, name: String, abbrev: String) =
        """{"id":$id,"name":"$name","abbrev":"$abbrev","celestial_body":{"response_mode":"list","id":1,"name":"Earth"}}"""

    private fun named(id: Int, name: String?) = """{"id":$id,"name":${name?.let { "\"$it\"" }}}"""

    private fun family(id: Int, name: String) =
        """{"response_mode":"normal","id":$id,"name":"$name","manufacturer":[],"parent":null}"""

    /** Answers each Terminus filter endpoint with one row; a path in [big] answers 150 rows over two pages. */
    private fun byPath(url: Url): String {
        val path = url.encodedPath
        val offset = url.parameters["offset"]?.toInt() ?: 0
        return when {
            path.startsWith("/api/v1/agencies/mini") -> page(150, *rows(offset, 150) { agency(it, "Agency $it", "A$it") })
            path.startsWith("/api/v1/programs/mini") -> page(1, program(7, "Artemis"))
            path.startsWith("/api/v1/launcher_configurations") ->
                page(2, rocket(1, "Falcon 9", "Falcon 9 Block 5", "SpX"), rocket(2, "Vulcan", null, null))
            path.startsWith("/api/v1/locations") -> page(2, location(3, "Kennedy"), location(4, null))
            path.startsWith("/api/v1/config/launch_statuses") -> page(1, status(1, "Go", "Go"))
            path.startsWith("/api/v1/config/orbits") -> page(1, orbit(8, "Low Earth Orbit", "LEO"))
            path.startsWith("/api/v1/config/mission_types") -> page(2, named(5, "Resupply"), named(6, null))
            path.startsWith("/api/v1/launcher_configuration_families") -> page(1, family(11, "Falcon"))
            path.startsWith("/api/v1/config/astronaut_statuses") -> page(1, named(2, "Active"))
            else -> emptyPage
        }
    }

    private fun rows(offset: Int, total: Int, make: (Int) -> String): Array<String> =
        (offset until minOf(offset + TerminusFilterFetcher.PAGE_SIZE, total)).map(make).toTypedArray()

    private class Clock(var nowMs: Long = 1_000_000L)

    private class Direct(
        val requests: MutableList<Url>,
        val schedule: TerminusScheduleFilterRepositoryImpl,
        val rocket: TerminusRocketFilterRepositoryImpl,
        val astronaut: TerminusAstronautFilterRepositoryImpl,
        val clock: Clock,
        val fail: (Boolean) -> Unit
    )

    /** Terminus-only repositories whose cache reads a fake clock. [fail] makes every call answer 503. */
    private fun direct(): Direct {
        val requests = mutableListOf<Url>()
        var failing = false
        val engine = MockEngine { request ->
            requests += request.url
            if (failing) respond("down", HttpStatusCode.ServiceUnavailable, jsonHeaders)
            else respond(byPath(request.url), HttpStatusCode.OK, jsonHeaders)
        }
        val clock = Clock()
        fun fetcher() = TerminusFilterFetcher(TerminusMemoryCache(now = { clock.nowMs }, maxEntries = 128))
        val agencies = TerminusAgenciesApi(base, engine)
        val programs = TerminusProgramsApi(base, engine)
        val configurations = TerminusConfigurationsApi(base, engine)
        val families = TerminusFamiliesApi(base, engine)
        val locations = TerminusLocationsApi(base, engine)
        val config = TerminusConfigApi(base, engine)
        return Direct(
            requests,
            TerminusScheduleFilterRepositoryImpl(agencies, programs, configurations, families, locations, config, fetcher()),
            TerminusRocketFilterRepositoryImpl(programs, families, fetcher()),
            TerminusAstronautFilterRepositoryImpl(config, fetcher()),
            clock
        ) { failing = it }
    }

    // -- Mapping and request shape --------------------------------------------------------------

    @Test
    fun scheduleFiltersMapToFilterOptionsWithTheLlLabels() = runTest {
        val env = direct()
        val repo = env.schedule

        assertEquals(listOf(FilterOption(7, "Artemis")), repo.getPrograms().getOrThrow())
        assertEquals(
            listOf(FilterOption(1, "Falcon 9 Block 5", "SpX"), FilterOption(2, "Vulcan", null)),
            repo.getRockets().getOrThrow()
        )
        assertEquals(
            listOf(FilterOption(3, "Kennedy"), FilterOption(4, "Unknown Location")),
            repo.getLocations().getOrThrow()
        )
        assertEquals(listOf(FilterOption(1, "Go", "Go")), repo.getStatuses().getOrThrow())
        assertEquals(listOf(FilterOption(8, "Low Earth Orbit", "LEO")), repo.getOrbits().getOrThrow())
        assertEquals(
            listOf(FilterOption(5, "Resupply"), FilterOption(6, "Unknown Mission Type")),
            repo.getMissionTypes().getOrThrow()
        )
        assertEquals(listOf(FilterOption(11, "Falcon")), repo.getLauncherConfigFamilies().getOrThrow())
    }

    @Test
    fun scheduleFiltersAskTerminusForTheSameSubsetAsLl() = runTest {
        val env = direct()

        env.schedule.getAgencies().getOrThrow()
        env.schedule.getRockets().getOrThrow()
        env.schedule.getLocations().getOrThrow()

        val agencies = env.requests.first { it.encodedPath.startsWith("/api/v1/agencies/mini") }
        assertEquals("true", agencies.parameters["featured"])
        assertEquals("name", agencies.parameters["ordering"])
        assertEquals("25", agencies.parameters["limit"])
        val rockets = env.requests.first { it.encodedPath.startsWith("/api/v1/launcher_configurations") }
        assertEquals("false", rockets.parameters["is_placeholder"])
        val locations = env.requests.first { it.encodedPath.startsWith("/api/v1/locations") }
        assertEquals("true", locations.parameters["active"])
    }

    @Test
    fun rocketAndAstronautFiltersMap() = runTest {
        val env = direct()

        assertEquals(listOf(FilterOption(7, "Artemis")), env.rocket.getPrograms().getOrThrow())
        assertEquals(listOf(FilterOption(11, "Falcon")), env.rocket.getFamilies().getOrThrow())
        assertEquals(listOf(FilterOption(2, "Active")), env.astronaut.getStatuses().getOrThrow())
    }

    // -- Paging and cache -----------------------------------------------------------------------

    @Test
    fun aListLargerThanOnePageIsReadToTheEnd() = runTest {
        val env = direct()

        val agencies = env.schedule.getAgencies().getOrThrow()

        assertEquals(150, agencies.size)
        assertEquals(FilterOption(149, "Agency 149", "A149"), agencies.last())
        assertEquals(listOf("0", "25", "50", "75", "100", "125"), env.requests.map { it.parameters["offset"] })
    }

    @Test
    fun anEndlessListStopsAtThePageCap() = runTest {
        val requests = mutableListOf<Url>()
        val engine = MockEngine { request ->
            requests += request.url
            val offset = request.url.parameters["offset"]?.toInt() ?: 0
            val rows = rows(offset, Int.MAX_VALUE) { program(it, "Program $it") }
            respond(page(Int.MAX_VALUE, *rows), HttpStatusCode.OK, jsonHeaders)
        }
        val repo = TerminusRocketFilterRepositoryImpl(
            TerminusProgramsApi(base, engine), TerminusFamiliesApi(base, engine), TerminusFilterFetcher()
        )

        val programs = repo.getPrograms().getOrThrow()

        assertEquals(TerminusFilterFetcher.MAX_PAGES, requests.size)
        assertEquals(TerminusFilterFetcher.MAX_PAGES * TerminusFilterFetcher.PAGE_SIZE, programs.size)
    }

    @Test
    fun aSecondReadIsServedFromMemoryAndForceRefreshAsksAgain() = runTest {
        val env = direct()

        env.rocket.getPrograms().getOrThrow()
        env.rocket.getPrograms().getOrThrow()
        assertEquals(1, env.requests.size)

        env.rocket.getPrograms(forceRefresh = true).getOrThrow()
        assertEquals(2, env.requests.size)
    }

    @Test
    fun anExpiredEntryIsReadAgainAndTheStaleOneCoversAServerError() = runTest {
        val env = direct()
        env.astronaut.getStatuses().getOrThrow()

        env.clock.nowMs += 2 * 60 * 60 * 1000L
        env.fail(true)
        val stale = env.astronaut.getStatuses().getOrThrow()

        assertEquals(listOf(FilterOption(2, "Active")), stale)
        assertEquals(2, env.requests.size)
    }

    @Test
    fun aServerErrorWithNothingCachedIsAFailure() = runTest {
        val env = direct()
        env.fail(true)

        assertTrue(env.astronaut.getStatuses().isFailure)
    }

    // -- Backend switch -------------------------------------------------------------------------

    private class Wired(
        val terminusRequests: MutableList<Url>,
        val llRequests: MutableList<Url>,
        val schedule: ScheduleFilterRepository,
        val rocket: RocketFilterRepository,
        val astronaut: AstronautFilterRepository,
        private val terminusClientBuilt: () -> Boolean
    ) {
        val terminusClientWasBuilt get() = terminusClientBuilt()
    }

    private fun wired(backend: DataBackend): Wired {
        val terminusRequests = mutableListOf<Url>()
        val llRequests = mutableListOf<Url>()
        val terminusEngine = MockEngine { request ->
            terminusRequests += request.url
            respond(byPath(request.url), HttpStatusCode.OK, jsonHeaders)
        }
        val llEngine = MockEngine { request ->
            llRequests += request.url
            respond(emptyPage, HttpStatusCode.OK, jsonHeaders)
        }
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SpaceLaunchDatabase.Schema.create(driver)
        val database = SpaceLaunchDatabase(driver)
        val preferences = AppPreferences(InMemoryPreferencesDataStore())
        var terminusClientBuilt = false
        fun <T> built(block: () -> T): T {
            terminusClientBuilt = true
            return block()
        }
        val koin = koinApplication {
            modules(
                module {
                    single { FilterOptionsLocalDataSource(database, preferences) }
                    single { LLAgenciesApi("https://ll.test", llEngine) }
                    single { LLProgramsApi("https://ll.test", llEngine) }
                    single { LLConfigurationsApi("https://ll.test", llEngine) }
                    single { LLFamiliesApi("https://ll.test", llEngine) }
                    single { LLLocationsApi("https://ll.test", llEngine) }
                    single { LLConfigApi("https://ll.test", llEngine) }
                    single { built { TerminusAgenciesApi(base, terminusEngine) } }
                    single { built { TerminusProgramsApi(base, terminusEngine) } }
                    single { built { TerminusConfigurationsApi(base, terminusEngine) } }
                    single { built { TerminusFamiliesApi(base, terminusEngine) } }
                    single { built { TerminusLocationsApi(base, terminusEngine) } }
                    single { built { TerminusConfigApi(base, terminusEngine) } }
                    single<DataBackend> { backend }
                },
                filterRepositoryModule()
            )
        }.koin
        return Wired(
            terminusRequests, llRequests,
            koin.get<ScheduleFilterRepository>(), koin.get<RocketFilterRepository>(), koin.get<AstronautFilterRepository>(),
            { terminusClientBuilt }
        )
    }

    @Test
    fun llBackendMakesZeroTerminusCalls() = runTest {
        val env = wired(DataBackend.LL)

        assertIs<ScheduleFilterRepositoryImpl>(env.schedule)
        assertIs<RocketFilterRepositoryImpl>(env.rocket)
        assertIs<AstronautFilterRepositoryImpl>(env.astronaut)
        env.schedule.getAgencies(forceRefresh = true)
        env.schedule.getStatuses(forceRefresh = true)
        env.rocket.getFamilies(forceRefresh = true)
        env.astronaut.getStatuses(forceRefresh = true)

        assertTrue(env.llRequests.isNotEmpty())
        assertFalse(env.terminusClientWasBuilt, "the Terminus clients must not be built on LL")
        assertTrue(env.terminusRequests.isEmpty(), "Terminus got ${env.terminusRequests}")
    }

    @Test
    fun terminusBackendMakesZeroLlCalls() = runTest {
        val env = wired(DataBackend.TERMINUS)

        assertIs<TerminusScheduleFilterRepositoryImpl>(env.schedule)
        assertIs<TerminusRocketFilterRepositoryImpl>(env.rocket)
        assertIs<TerminusAstronautFilterRepositoryImpl>(env.astronaut)
        assertTrue(env.schedule.getStatuses().isSuccess)
        assertTrue(env.rocket.getFamilies().isSuccess)
        assertTrue(env.astronaut.getStatuses().isSuccess)

        assertTrue(env.llRequests.isEmpty(), "LL got ${env.llRequests}")
        assertEquals(3, env.terminusRequests.size)
    }
}

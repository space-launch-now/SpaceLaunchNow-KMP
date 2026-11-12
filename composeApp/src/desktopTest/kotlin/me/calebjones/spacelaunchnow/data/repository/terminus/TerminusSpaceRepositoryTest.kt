package me.calebjones.spacelaunchnow.data.repository.terminus

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.api.iss.IssPosition
import me.calebjones.spacelaunchnow.api.iss.IssTle
import me.calebjones.spacelaunchnow.api.iss.IssTrackingRepository
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AstronautsApi as LLAstronautsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ExpeditionsApi as LLExpeditionsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ProgramsApi as LLProgramsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpaceStationsApi as LLSpaceStationsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.AstronautsApi as TerminusAstronautsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.ExpeditionsApi as TerminusExpeditionsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.ProgramsApi as TerminusProgramsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.SpaceStationsApi as TerminusSpaceStationsApi
import me.calebjones.spacelaunchnow.data.model.DataBackend
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.AstronautRepository
import me.calebjones.spacelaunchnow.data.repository.ProgramRepository
import me.calebjones.spacelaunchnow.data.repository.SpaceStationRepository
import me.calebjones.spacelaunchnow.data.repository.ll.LLAstronautRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ll.LLProgramRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ll.LLSpaceStationRepositoryImpl
import me.calebjones.spacelaunchnow.di.spaceRepositoryModule
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Stations, expeditions, astronauts and programs on Terminus behind [DataBackend] (Tasks#1012).
 * Each fixture in `resources/terminus` is a Terminus payload that is also valid LL2 JSON, so the
 * same body fed to the LL repository must give the same domain value as the Terminus repository.
 */
class TerminusSpaceRepositoryTest {

    private fun resource(name: String): String = checkNotNull(
        TerminusSpaceRepositoryTest::class.java.getResourceAsStream("/terminus/$name")
    ) { "missing fixture $name" }.bufferedReader().use { it.readText() }

    private val jsonHeaders: Headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private class Reply(val body: String, val status: HttpStatusCode = HttpStatusCode.OK)

    private class Backend(val requests: MutableList<Url>, val engine: MockEngine)

    private fun backend(handler: (Url) -> Reply): Backend {
        val requests = mutableListOf<Url>()
        val engine = MockEngine { request ->
            requests += request.url
            val reply = handler(request.url)
            respond(content = reply.body, status = reply.status, headers = jsonHeaders)
        }
        return Backend(requests, engine)
    }

    private fun byPath(url: Url): String = when {
        url.encodedPath.contains("/space_stations/") -> resource("space_station_detail.json")
        url.encodedPath.contains("/expeditions/") -> resource("expedition_detail.json")
        url.encodedPath.matches(Regex(".*/astronauts/\\d+/")) -> resource("astronaut_detail.json")
        url.encodedPath.contains("/astronauts/") -> resource("astronauts_list.json")
        url.encodedPath.contains("/programs/") -> resource("program_detail.json")
        else -> error("unexpected ${url.encodedPath}")
    }

    private val tle = IssTle(1L, 2L, "25544", "ISS", "ISS (ZARYA)", "1 25544U", "2 25544")

    private val fakeIss = object : IssTrackingRepository {
        override suspend fun getCurrentPosition(): Result<IssPosition> = Result.failure(UnsupportedOperationException())
        override suspend fun getTleData(): Result<IssTle> = Result.success(tle)
        override suspend fun getPositionsAtTimestamps(timestamps: List<Long>): Result<List<IssPosition>> =
            Result.failure(UnsupportedOperationException())
    }

    private val base = "https://terminus.test"
    private val llBase = "https://ll.test"

    private fun terminusStation(b: Backend, tleSource: SpaceStationRepository = llStation(backend { Reply("{}") })) =
        TerminusSpaceStationRepositoryImpl(
            TerminusSpaceStationsApi(baseUrl = base, httpClientEngine = b.engine),
            TerminusExpeditionsApi(baseUrl = base, httpClientEngine = b.engine),
            tleSource
        )

    private fun llStation(b: Backend) = LLSpaceStationRepositoryImpl(
        LLSpaceStationsApi(baseUrl = llBase, httpClientEngine = b.engine),
        LLExpeditionsApi(baseUrl = llBase, httpClientEngine = b.engine),
        fakeIss
    )

    // -- Mapping: Terminus result equals LL result for the same body ---------------------------------

    @Test
    fun stationMapsLikeLl() = runTest {
        val t = backend { Reply(resource("space_station_detail.json")) }
        val l = backend { Reply(resource("space_station_detail.json")) }

        val terminus = terminusStation(t).getSpaceStationDetails(4).getOrThrow()
        val ll = llStation(l).getSpaceStationDetails(4).getOrThrow()

        assertEquals("/api/v1/space_stations/4/", t.requests.single().encodedPath)
        assertEquals(DataSource.NETWORK, terminus.source)
        assertEquals(ll.data, terminus.data)
        assertEquals("International Space Station", terminus.data.name)
        assertEquals(listOf(300), terminus.data.activeExpeditions.map { it.id })
    }

    @Test
    fun expeditionsMapLikeLlWithCrew() = runTest {
        val t = backend { Reply(resource("expedition_detail.json")) }
        val l = backend { Reply(resource("expedition_detail.json")) }

        val terminus = terminusStation(t).getExpeditionDetails(listOf(300), 4).getOrThrow()
        val ll = llStation(l).getExpeditionDetails(listOf(300), 4).getOrThrow()

        assertEquals("/api/v1/expeditions/300/", t.requests.single().encodedPath)
        assertEquals(ll.data, terminus.data)
        val crew = terminus.data.single().crew
        assertEquals(1, crew.size)
        assertEquals("Commander", crew.single().role)
        assertEquals("Jane Doe", crew.single().astronaut.name)
        assertEquals(1, terminus.data.single().spacewalks.size)
    }

    @Test
    fun astronautListMapsLikeLl() = runTest {
        val t = backend { Reply(resource("astronauts_list.json")) }
        val l = backend { Reply(resource("astronauts_list.json")) }

        val terminus = TerminusAstronautRepositoryImpl(TerminusAstronautsApi(baseUrl = base, httpClientEngine = t.engine))
            .getAstronauts(limit = 2, statusIds = listOf(1), agencyIds = listOf(44), inSpace = true).getOrThrow()
        val ll = LLAstronautRepositoryImpl(LLAstronautsApi(baseUrl = llBase, httpClientEngine = l.engine))
            .getAstronauts(limit = 2, statusIds = listOf(1), agencyIds = listOf(44), inSpace = true).getOrThrow()

        val request = t.requests.single()
        assertEquals("/api/v1/astronauts/", request.encodedPath)
        assertEquals("2", request.parameters["limit"])
        assertEquals("1.0", request.parameters["status_ids"])
        assertEquals("44.0", request.parameters["agency_ids"])
        assertEquals("true", request.parameters["in_space"])
        assertEquals(ll, terminus)
        assertEquals(listOf(1, 2), terminus.results.map { it.id })
    }

    @Test
    fun astronautDetailMapsLikeLl() = runTest {
        val t = backend { Reply(resource("astronaut_detail.json")) }
        val l = backend { Reply(resource("astronaut_detail.json")) }

        val terminus = TerminusAstronautRepositoryImpl(TerminusAstronautsApi(baseUrl = base, httpClientEngine = t.engine))
            .getAstronautDetail(1).getOrThrow()
        val ll = LLAstronautRepositoryImpl(LLAstronautsApi(baseUrl = llBase, httpClientEngine = l.engine))
            .getAstronautDetail(1).getOrThrow()

        assertEquals("/api/v1/astronauts/1/", t.requests.single().encodedPath)
        assertEquals(ll, terminus)
        assertEquals("Jane Doe", terminus.name)
        assertEquals(1, terminus.spacewalks.size)
        assertEquals(1, terminus.socialMediaLinks.size)
    }

    @Test
    fun programMapsLikeLl() = runTest {
        val t = backend { Reply(resource("program_detail.json")) }
        val l = backend { Reply(resource("program_detail.json")) }

        val terminus = TerminusProgramRepositoryImpl(TerminusProgramsApi(baseUrl = base, httpClientEngine = t.engine))
            .getProgramDomain(17).getOrThrow()
        val ll = LLProgramRepositoryImpl(LLProgramsApi(baseUrl = llBase, httpClientEngine = l.engine))
            .getProgramDomain(17).getOrThrow()

        assertEquals("/api/v1/programs/17/", t.requests.single().encodedPath)
        assertEquals(ll.data, terminus.data)
        assertEquals("Artemis", terminus.data.name)
        assertEquals(DataSource.NETWORK, terminus.source)
    }

    // -- Cache --------------------------------------------------------------------------------------

    @Test
    fun secondProgramReadIsCachedAndForceRefreshSkipsIt() = runTest {
        val t = backend { Reply(resource("program_detail.json")) }
        val repo = TerminusProgramRepositoryImpl(TerminusProgramsApi(baseUrl = base, httpClientEngine = t.engine))

        repo.getProgramDomain(17).getOrThrow()
        val cached = repo.getProgramDomain(17).getOrThrow()
        repo.getProgramDomain(17, forceRefresh = true).getOrThrow()

        assertEquals(DataSource.CACHE, cached.source)
        assertEquals(2, t.requests.size)
    }

    @Test
    fun stationFallsBackToStaleOnServerError() = runTest {
        var fail = false
        val t = backend { if (fail) Reply("{}", HttpStatusCode.InternalServerError) else Reply(resource("space_station_detail.json")) }
        val repo = terminusStation(t)

        repo.getSpaceStationDetails(4).getOrThrow()
        fail = true
        val stale = repo.getSpaceStationDetails(4, forceRefresh = true).getOrThrow()

        assertEquals(DataSource.STALE_CACHE, stale.source)
        assertEquals("International Space Station", stale.data.name)
    }

    @Test
    fun notFoundIsAFailure() = runTest {
        val t = backend { Reply("{}", HttpStatusCode.NotFound) }
        val repo = TerminusAstronautRepositoryImpl(TerminusAstronautsApi(baseUrl = base, httpClientEngine = t.engine))

        assertTrue(repo.getAstronautDetail(404).exceptionOrNull() is TerminusNotFoundException)
    }

    @Test
    fun terminusTleStaysOnTheLlInstance() = runTest {
        val t = backend { Reply(resource("space_station_detail.json")) }

        val result = terminusStation(t, llStation(backend { Reply("{}") })).getIssTle().getOrThrow()

        assertEquals(tle, result.data)
        assertTrue(t.requests.isEmpty(), "the TLE read must not call Terminus")
    }

    // -- Backend switch -----------------------------------------------------------------------------

    private class Wired(
        val terminus: Backend,
        val ll: Backend,
        val station: SpaceStationRepository,
        val astronauts: AstronautRepository,
        val programs: ProgramRepository,
        private val terminusClientBuilt: () -> Boolean
    ) {
        val terminusClientWasBuilt get() = terminusClientBuilt()
    }

    private fun wired(which: DataBackend): Wired {
        val terminus = backend { Reply(byPath(it)) }
        val ll = backend { Reply(byPath(it)) }
        var built = false
        val koin = koinApplication {
            modules(
                module {
                    single { LLSpaceStationsApi(baseUrl = llBase, httpClientEngine = ll.engine) }
                    single { LLExpeditionsApi(baseUrl = llBase, httpClientEngine = ll.engine) }
                    single { LLAstronautsApi(baseUrl = llBase, httpClientEngine = ll.engine) }
                    single { LLProgramsApi(baseUrl = llBase, httpClientEngine = ll.engine) }
                    single<IssTrackingRepository> { fakeIss }
                    single<TerminusSpaceStationsApi> {
                        built = true
                        TerminusSpaceStationsApi(baseUrl = base, httpClientEngine = terminus.engine)
                    }
                    single<TerminusExpeditionsApi> {
                        built = true
                        TerminusExpeditionsApi(baseUrl = base, httpClientEngine = terminus.engine)
                    }
                    single<TerminusAstronautsApi> {
                        built = true
                        TerminusAstronautsApi(baseUrl = base, httpClientEngine = terminus.engine)
                    }
                    single<TerminusProgramsApi> {
                        built = true
                        TerminusProgramsApi(baseUrl = base, httpClientEngine = terminus.engine)
                    }
                    single<DataBackend> { which }
                },
                spaceRepositoryModule()
            )
        }.koin
        return Wired(
            terminus, ll, koin.get(), koin.get(), koin.get(), { built }
        )
    }

    @Test
    fun llBackendMakesZeroTerminusCalls() = runTest {
        val env = wired(DataBackend.LL)

        assertIs<LLSpaceStationRepositoryImpl>(env.station)
        assertIs<LLAstronautRepositoryImpl>(env.astronauts)
        assertIs<LLProgramRepositoryImpl>(env.programs)
        assertTrue(env.station.getSpaceStationDetails(4, forceRefresh = true).isSuccess)
        assertTrue(env.station.getExpeditionDetails(listOf(300), 4, forceRefresh = true).isSuccess)
        assertTrue(env.astronauts.getAstronauts().isSuccess)
        assertTrue(env.astronauts.getAstronautDetail(1).isSuccess)
        assertTrue(env.programs.getProgramDomain(17, forceRefresh = true).isSuccess)
        env.station.prewarmIssCache()

        assertTrue(env.ll.requests.isNotEmpty())
        assertFalse(env.terminusClientWasBuilt, "the Terminus clients must not be built on LL")
        assertTrue(env.terminus.requests.isEmpty(), "Terminus got ${env.terminus.requests}")
    }

    @Test
    fun terminusBackendMakesZeroLlCalls() = runTest {
        val env = wired(DataBackend.TERMINUS)

        assertIs<TerminusSpaceStationRepositoryImpl>(env.station)
        assertIs<TerminusAstronautRepositoryImpl>(env.astronauts)
        assertIs<TerminusProgramRepositoryImpl>(env.programs)
        assertTrue(env.station.getSpaceStationDetails(4).isSuccess)
        assertTrue(env.station.getExpeditionDetails(listOf(300), 4).isSuccess)
        assertTrue(env.astronauts.getAstronauts().isSuccess)
        assertTrue(env.astronauts.getAstronautDetail(1).isSuccess)
        assertTrue(env.programs.getProgramDomain(17).isSuccess)

        assertTrue(env.ll.requests.isEmpty(), "LL got ${env.ll.requests}")
        assertEquals(5, env.terminus.requests.size)
    }
}

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
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ConfigApi as LLConfigApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.EventsApi as LLEventsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.UpdatesApi as LLUpdatesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.ConfigApi as TerminusConfigApi
import me.calebjones.spacelaunchnow.api.terminus.apis.EventsApi as TerminusEventsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.UpdatesApi as TerminusUpdatesApi
import me.calebjones.spacelaunchnow.data.model.DataBackend
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepository
import me.calebjones.spacelaunchnow.data.repository.ll.LLEventsRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ll.LLUpdatesRepositoryImpl
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.database.EventLocalDataSource
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import me.calebjones.spacelaunchnow.database.UpdateLocalDataSource
import me.calebjones.spacelaunchnow.di.eventsUpdatesRepositoryModule
import me.calebjones.spacelaunchnow.domain.model.LaunchRef
import me.calebjones.spacelaunchnow.ui.viewmodel.InMemoryPreferencesDataStore
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Events and updates on Terminus behind [DataBackend] (Tasks#1009 S3). The fixtures in
 * `resources/terminus` follow the generated Terminus models (`GET /api/v1/events/`,
 * `/events/{id}/`, `/config/event_types/`, `/updates/`); the launch inside the first update is a
 * launch from the real staging launch fixture.
 */
class TerminusEventsUpdatesRepositoryTest {

    private fun resource(name: String): String = checkNotNull(
        TerminusEventsUpdatesRepositoryTest::class.java.getResourceAsStream("/terminus/$name")
    ) { "missing fixture $name" }.bufferedReader().use { it.readText() }

    private val jsonHeaders: Headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    private val emptyPage = """{"count":0,"next":null,"previous":null,"results":[]}"""

    private class Reply(val body: String, val status: HttpStatusCode = HttpStatusCode.OK)

    private fun Url.param(name: String): String? = parameters[name]

    // -- Direct repositories with a fake clock ---------------------------------------------------

    private class Direct(
        val requests: MutableList<Url>,
        val events: TerminusEventsRepositoryImpl,
        val updates: TerminusUpdatesRepositoryImpl,
        val clock: Clock
    )

    private class Clock(var nowMs: Long = 1_000_000L)

    /** Terminus-only repositories whose cache reads [Clock]. [handler] answers by URL. */
    private fun direct(handler: (Url) -> Reply): Direct {
        val requests = mutableListOf<Url>()
        val engine = MockEngine { request ->
            requests += request.url
            val reply = handler(request.url)
            respond(content = reply.body, status = reply.status, headers = jsonHeaders)
        }
        val clock = Clock()
        val cache = TerminusMemoryCache(now = { clock.nowMs })
        val events = TerminusEventsRepositoryImpl(
            TerminusEventsApi(baseUrl = "https://terminus.test", httpClientEngine = engine),
            TerminusConfigApi(baseUrl = "https://terminus.test", httpClientEngine = engine),
            cache
        )
        val updates = TerminusUpdatesRepositoryImpl(
            TerminusUpdatesApi(baseUrl = "https://terminus.test", httpClientEngine = engine),
            cache
        )
        return Direct(requests, events, updates, clock)
    }

    private fun byPath(url: Url): String = when {
        url.encodedPath.startsWith("/api/v1/updates") -> resource("updates_list.json")
        url.encodedPath.startsWith("/api/v1/config/event_types") -> resource("event_types.json")
        url.encodedPath == "/api/v1/events/1201/" -> resource("event_detail.json")
        else -> resource("events_list.json")
    }

    // -- Backend switch ---------------------------------------------------------------------------

    private class Wired(
        val terminusRequests: MutableList<Url>,
        val llRequests: MutableList<Url>,
        val events: EventsRepository,
        val updates: UpdatesRepository,
        private val terminusClientBuilt: () -> Boolean
    ) {
        val terminusClientWasBuilt get() = terminusClientBuilt()
    }

    private fun wired(backend: DataBackend): Wired {
        val terminusRequests = mutableListOf<Url>()
        val llRequests = mutableListOf<Url>()
        val terminusEngine = MockEngine { request ->
            terminusRequests += request.url
            respond(content = byPath(request.url), status = HttpStatusCode.OK, headers = jsonHeaders)
        }
        val llEngine = MockEngine { request ->
            llRequests += request.url
            respond(content = emptyPage, status = HttpStatusCode.OK, headers = jsonHeaders)
        }
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SpaceLaunchDatabase.Schema.create(driver)
        val database = SpaceLaunchDatabase(driver)
        val preferences = AppPreferences(InMemoryPreferencesDataStore())
        var terminusClientBuilt = false
        val koin = koinApplication {
            modules(
                module {
                    single { EventLocalDataSource(database, preferences) }
                    single { UpdateLocalDataSource(database, preferences) }
                    single { LLEventsApi(baseUrl = "https://ll.test", httpClientEngine = llEngine) }
                    single { LLConfigApi(baseUrl = "https://ll.test", httpClientEngine = llEngine) }
                    single { LLUpdatesApi(baseUrl = "https://ll.test", httpClientEngine = llEngine) }
                    single<TerminusEventsApi> {
                        terminusClientBuilt = true
                        TerminusEventsApi(baseUrl = "https://terminus.test", httpClientEngine = terminusEngine)
                    }
                    single<TerminusConfigApi> {
                        terminusClientBuilt = true
                        TerminusConfigApi(baseUrl = "https://terminus.test", httpClientEngine = terminusEngine)
                    }
                    single<TerminusUpdatesApi> {
                        terminusClientBuilt = true
                        TerminusUpdatesApi(baseUrl = "https://terminus.test", httpClientEngine = terminusEngine)
                    }
                    single<DataBackend> { backend }
                },
                eventsUpdatesRepositoryModule()
            )
        }.koin
        return Wired(
            terminusRequests, llRequests, koin.get<EventsRepository>(), koin.get<UpdatesRepository>(),
            { terminusClientBuilt }
        )
    }

    @Test
    fun llBackendMakesZeroTerminusCalls() = runTest {
        val env = wired(DataBackend.LL)

        assertIs<LLEventsRepositoryImpl>(env.events)
        assertIs<LLUpdatesRepositoryImpl>(env.updates)
        assertTrue(env.events.getUpcomingEventsDomain(5, forceRefresh = true).isSuccess)
        assertTrue(env.events.getEventsPaginatedDomain(limit = 5, search = "walk").isSuccess)
        assertTrue(env.events.getEventsByTypeDomain(listOf(5)).isSuccess)
        assertTrue(env.events.getEventsByLaunchIdDomain("abc").isSuccess)
        assertTrue(env.events.getEventsByProgram(1).isSuccess)
        assertTrue(env.events.getEvents().isSuccess)
        assertTrue(env.events.getEventTypesDomain().isSuccess)
        assertTrue(env.updates.getLatestUpdates(5, forceRefresh = true).isSuccess)
        assertTrue(env.updates.getUpdatesByProgram(1).isSuccess)

        assertTrue(env.llRequests.isNotEmpty())
        assertFalse(env.terminusClientWasBuilt, "the Terminus clients must not be built on LL")
        assertTrue(env.terminusRequests.isEmpty(), "Terminus got ${env.terminusRequests}")
    }

    @Test
    fun terminusBackendMakesZeroLlCalls() = runTest {
        val env = wired(DataBackend.TERMINUS)

        assertIs<TerminusEventsRepositoryImpl>(env.events)
        assertIs<TerminusUpdatesRepositoryImpl>(env.updates)
        assertEquals(2, env.events.getUpcomingEventsDomain(2).getOrThrow().data.results.size)
        assertEquals(3, env.updates.getLatestUpdates(3).getOrThrow().data.results.size)
        assertTrue(env.llRequests.isEmpty(), "LL got ${env.llRequests}")
        assertEquals(2, env.terminusRequests.size)
    }

    // -- Mapping ------------------------------------------------------------------------------------

    @Test
    fun eventsListMapsToDomain() = runTest {
        val env = direct { Reply(resource("events_list.json")) }

        val result = env.events.getUpcomingEventsDomain(limit = 2).getOrThrow()

        assertEquals(DataSource.NETWORK, result.source)
        val page = result.data
        assertEquals(42, page.count)
        assertEquals(listOf(1201, 1202), page.results.map { it.id })
        val first = page.results.first()
        assertEquals("ISS US Spacewalk", first.name)
        assertEquals("iss-us-spacewalk", first.slug)
        assertEquals("Spacewalk", first.type.name)
        assertEquals("https://example.test/media/events/spacewalk.jpg", first.imageUrl)
        assertEquals(1, first.updates.size)
        assertEquals("Mission Ops", first.updates.single().createdBy)
        assertTrue(page.results[1].updates.isEmpty())
    }

    @Test
    fun llPageMethodsReturnTheLlTypeFromTheTerminusPayload() = runTest {
        val env = direct { Reply(resource("events_list.json")) }

        val byProgram = env.events.getEventsByProgram(programId = 1, limit = 2).getOrThrow()
        val plain = env.events.getEvents(limit = 2, upcoming = true, typeIds = listOf(5)).getOrThrow()

        assertEquals(listOf(1201, 1202), byProgram.data.results.map { it.id })
        assertEquals(DataSource.NETWORK, byProgram.source)
        assertEquals(listOf(1201, 1202), plain.results.map { it.id })
    }

    @Test
    fun eventDetailMapsToDomain() = runTest {
        val env = direct { Reply(resource("event_detail.json")) }

        val event = env.events.getEventDetailDomain(1201).getOrThrow()

        assertEquals("/api/v1/events/1201/", env.requests.single().encodedPath)
        assertEquals(1201, event.id)
        assertEquals("ISS US Spacewalk", event.name)
        assertEquals(1, event.launches.size)
        assertEquals("Vulcan VC6L | Amazon Leo (LV-01)", event.launches.single().name)
        assertEquals("06:30:00", event.duration)
    }

    @Test
    fun eventTypesMapToDomain() = runTest {
        val env = direct { Reply(resource("event_types.json")) }

        val types = env.events.getEventTypesDomain().getOrThrow()

        assertEquals("/api/v1/config/event_types/", env.requests.single().encodedPath)
        assertEquals(listOf(5 to "Spacewalk", 10 to "Press Event"), types.map { it.id to it.name })
    }

    @Test
    fun updatesMapLaunchToLaunchRefAndKeepCreatedByAsText() = runTest {
        val env = direct { Reply(resource("updates_list.json")) }

        val result = env.updates.getLatestUpdates(limit = 3).getOrThrow()

        val (withLaunch, withEvent, bare) = result.data.results
        assertEquals(LaunchRef("ef9850ad-1096-425b-9d27-f7bbd186e61a", "Vulcan VC6L | Amazon Leo (LV-01)"), withLaunch.launch)
        assertEquals("Mission Ops", withLaunch.createdBy)
        assertNull(withLaunch.event)

        assertNull(withEvent.launch, "no launch maps to null")
        assertEquals("Jane Q. Reporter (NASA)", withEvent.createdBy)
        assertEquals(1201, withEvent.event?.id)
        assertEquals("ISS US Spacewalk", withEvent.event?.name)

        assertNull(bare.launch)
        assertNull(bare.createdBy)
        assertNull(bare.comment)
        assertEquals(3, result.data.count)
    }

    // -- Request URLs and params ----------------------------------------------------------------------

    @Test
    fun upcomingEventsSendUpcomingOrderingAndLimit() = runTest {
        val env = direct { Reply(resource("events_list.json")) }

        env.events.getUpcomingEventsDomain(limit = 7).getOrThrow()

        val url = env.requests.single()
        assertEquals("/api/v1/events/", url.encodedPath)
        assertEquals("true", url.param("upcoming"))
        assertEquals("date", url.param("ordering"))
        assertEquals("7", url.param("limit"))
        assertNull(url.param("offset"), "offset 0 is left out")
    }

    @Test
    fun paginatedEventsSendOffsetSearchAndTypeIds() = runTest {
        val env = direct { Reply(resource("events_list.json")) }

        env.events.getEventsPaginatedDomain(
            limit = 20, offset = 40, search = "space walk & more", typeIds = listOf(10, 5, 10), upcoming = false
        ).getOrThrow()

        val url = env.requests.single()
        assertEquals("20", url.param("limit"))
        assertEquals("40", url.param("offset"))
        assertEquals("space walk & more", url.param("search"))
        assertEquals("5,10", url.param("type__ids"))
        assertEquals("false", url.param("upcoming"))
        assertEquals("date", url.param("ordering"))
    }

    @Test
    fun eventsByLaunchProgramAndTypeSendTheirFilters() = runTest {
        val env = direct { Reply(resource("events_list.json")) }

        env.events.getEventsByLaunchIdDomain("ef9850ad-1096-425b-9d27-f7bbd186e61a", limit = 4).getOrThrow()
        env.events.getEventsByProgram(programId = 1, limit = 6, upcoming = true).getOrThrow()
        env.events.getEventsByTypeDomain(listOf(5), limit = 3).getOrThrow()

        val (byLaunch, byProgram, byType) = env.requests
        assertEquals("ef9850ad-1096-425b-9d27-f7bbd186e61a", byLaunch.param("launch__ids"))
        assertEquals("-date", byLaunch.param("ordering"))
        assertEquals("4", byLaunch.param("limit"))
        assertEquals(listOf("1"), byProgram.parameters.getAll("program"))
        assertEquals("true", byProgram.param("upcoming"))
        assertEquals("date", byProgram.param("ordering"))
        assertEquals("5", byType.param("type__ids"))
        assertEquals("3", byType.param("limit"))
    }

    @Test
    fun updatesSendNewestFirstAndProgramFilter() = runTest {
        val env = direct { Reply(resource("updates_list.json")) }

        env.updates.getLatestUpdates(limit = 10).getOrThrow()
        env.updates.getUpdatesByProgram(allProgram = 1, limit = 20).getOrThrow()

        val (latest, byProgram) = env.requests
        assertEquals("/api/v1/updates/", latest.encodedPath)
        assertEquals("-created_on", latest.param("ordering"))
        assertEquals("10", latest.param("limit"))
        assertNull(latest.param("all__program"))
        assertEquals("1", byProgram.param("all__program"))
        assertEquals("-created_on", byProgram.param("ordering"))
        assertEquals("20", byProgram.param("limit"))
    }

    // -- Cache ----------------------------------------------------------------------------------------

    @Test
    fun twoReadsInsideTheTtlMakeOneRequest() = runTest {
        val env = direct { Reply(byPath(it)) }

        val first = env.events.getUpcomingEventsDomain(limit = 2).getOrThrow()
        env.clock.nowMs += 29_000
        val second = env.events.getUpcomingEventsDomain(limit = 2).getOrThrow()

        assertEquals(1, env.requests.size)
        assertEquals(DataSource.NETWORK, first.source)
        assertEquals(DataSource.CACHE, second.source)
        assertEquals(first.data, second.data)

        env.updates.getLatestUpdates(3).getOrThrow()
        env.updates.getLatestUpdates(3).getOrThrow()
        assertEquals(2, env.requests.size, "updates have their own entry: one more request, not two")
    }

    @Test
    fun aReadAfterTheTtlRefetches() = runTest {
        val env = direct { Reply(byPath(it)) }

        env.events.getUpcomingEventsDomain(limit = 2).getOrThrow()
        env.clock.nowMs += 31_000
        val again = env.events.getUpcomingEventsDomain(limit = 2).getOrThrow()

        assertEquals(2, env.requests.size)
        assertEquals(DataSource.NETWORK, again.source)
    }

    @Test
    fun forceRefreshSkipsTheFreshEntry() = runTest {
        val env = direct { Reply(byPath(it)) }

        env.events.getUpcomingEventsDomain(limit = 2).getOrThrow()
        env.events.getUpcomingEventsDomain(limit = 2, forceRefresh = true).getOrThrow()

        assertEquals(2, env.requests.size)
    }

    @Test
    fun differentRequestsDoNotShareAnEntry() = runTest {
        val env = direct { Reply(byPath(it)) }

        env.events.getEventsPaginatedDomain(limit = 5, search = "a&b").getOrThrow()
        env.events.getEventsPaginatedDomain(limit = 5, search = "a").getOrThrow()
        env.events.getEventsPaginatedDomain(limit = 5, search = "a&b").getOrThrow()

        assertEquals(2, env.requests.size)
    }

    @Test
    fun serverErrorServesTheStaleEntry() = runTest {
        var fail = false
        val env = direct { if (fail) Reply("oops", HttpStatusCode.BadGateway) else Reply(byPath(it)) }

        val first = env.updates.getLatestUpdates(limit = 3).getOrThrow()
        env.clock.nowMs += 120_000
        fail = true
        val second = env.updates.getLatestUpdates(limit = 3).getOrThrow()

        assertEquals(2, env.requests.size)
        assertEquals(DataSource.STALE_CACHE, second.source)
        assertEquals(first.data, second.data)
    }

    @Test
    fun networkFailureServesTheStaleEntry() = runTest {
        var fail = false
        val env = direct {
            if (fail) throw IOException("offline")
            Reply(byPath(it))
        }

        val first = env.events.getEventsPaginatedDomain(limit = 2).getOrThrow()
        env.clock.nowMs += 120_000
        fail = true
        val second = env.events.getEventsPaginatedDomain(limit = 2).getOrThrow()

        assertEquals(DataSource.STALE_CACHE, second.source)
        assertEquals(first.data, second.data)
    }

    @Test
    fun serverErrorWithoutAStaleEntryFails() = runTest {
        val env = direct { Reply("oops", HttpStatusCode.InternalServerError) }

        assertTrue(env.events.getUpcomingEventsDomain(limit = 2).isFailure)
        assertTrue(env.updates.getLatestUpdates(limit = 2).isFailure)
    }

    @Test
    fun clientErrorDoesNotServeTheStaleEntry() = runTest {
        var status = HttpStatusCode.OK
        val env = direct { Reply(if (status == HttpStatusCode.OK) byPath(it) else "nope", status) }

        env.events.getEventDetailDomain(1201).getOrThrow()
        env.clock.nowMs += 120_000
        status = HttpStatusCode.NotFound

        assertTrue(env.events.getEventDetailDomain(1201).isFailure)
    }

    @Test
    fun logLabelNeverContainsSearchText() {
        val query = EventListQuery.of(limit = 5, search = "secret term")

        assertFalse(query.logLabel.contains("secret"))
        assertTrue(query.logLabel.contains("search=<set>"))
        assertTrue(query.key.contains("search=secret%20term"))
    }

    @Test
    fun keyDiffersWhenSearchForgesAnotherFilter() {
        val forged = EventListQuery.of(limit = 5, search = "x&upcoming=true")
        val real = EventListQuery.of(limit = 5, search = "x", upcoming = true)

        assertTrue(forged.key != real.key)
    }

    @Test
    fun memoryCacheDropsTheOldestEntryAtItsBound() = runTest {
        var now = 0L
        val cache = TerminusMemoryCache(maxEntries = 2, now = { now })

        cache.put("a", "1")
        cache.put("b", "2")
        cache.put("c", "3")

        assertNull(cache.stale<String>("a"))
        assertEquals("2", cache.stale<String>("b")?.value)
        assertEquals("3", cache.fresh<String>("c")?.value)
        now += 30_000
        assertNull(cache.fresh<String>("c"), "an entry is stale at 30 s")
        assertEquals("3", cache.stale<String>("c")?.value)
    }
}

package me.calebjones.spacelaunchnow.data.repository.terminus

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi as LLLaunchesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed as LLLaunchDetailed
import me.calebjones.spacelaunchnow.api.terminus.apis.LaunchesApi as TerminusLaunchesApi
import me.calebjones.spacelaunchnow.api.terminus.models.LaunchDetailed as TerminusLaunchDetailed
import me.calebjones.spacelaunchnow.data.model.DataBackend
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.ll.LLLaunchRepositoryImpl
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import me.calebjones.spacelaunchnow.database.StatsLocalDataSource
import me.calebjones.spacelaunchnow.di.LL_REPOSITORY
import me.calebjones.spacelaunchnow.di.launchRepositoryModule
import me.calebjones.spacelaunchnow.ui.viewmodel.InMemoryPreferencesDataStore
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/**
 * Launch detail on Terminus behind [DataBackend] (Tasks#1006 S1). The fixture is a real
 * `GET /api/v1/launches/{id}/` response from Terminus staging.
 */
class TerminusLaunchRepositoryTest {

    private val fixture: String = checkNotNull(
        TerminusLaunchRepositoryTest::class.java.getResourceAsStream("/terminus/launch_detail.json")
    ) { "missing fixture" }.bufferedReader().use { it.readText() }

    private val fixtureId = Json.parseToJsonElement(fixture).jsonObject.getValue("id").jsonPrimitive.content
    private val json = Json { ignoreUnknownKeys = true }

    private class Reply(val body: String, val status: HttpStatusCode = HttpStatusCode.OK)

    private class Env(
        val terminusRequests: MutableList<String>,
        val llRequests: MutableList<String>,
        val database: SpaceLaunchDatabase,
        val local: LaunchLocalDataSource,
        val llRepository: LLLaunchRepositoryImpl,
        val repository: LaunchRepository,
        private val terminusClientBuilt: () -> Boolean,
    ) {
        /** True once Koin has resolved the Terminus client (the provider ran). */
        val terminusClientWasBuilt: Boolean get() = terminusClientBuilt()
    }

    private val jsonHeaders: Headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun env(terminusHandler: (String) -> Reply): Env = envFor(DataBackend.TERMINUS, terminusHandler)

    private fun envFor(backend: DataBackend, terminusHandler: (String) -> Reply): Env {
        val terminusRequests = mutableListOf<String>()
        val llRequests = mutableListOf<String>()
        val terminusEngine = MockEngine { request ->
            terminusRequests += request.url.encodedPath
            val reply = terminusHandler(request.url.encodedPath)
            respond(content = reply.body, status = reply.status, headers = jsonHeaders)
        }
        val llEngine = MockEngine { request ->
            llRequests += request.url.encodedPath
            respond(content = fixture, status = HttpStatusCode.OK, headers = jsonHeaders)
        }
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SpaceLaunchDatabase.Schema.create(driver)
        val database = SpaceLaunchDatabase(driver)
        val preferences = AppPreferences(InMemoryPreferencesDataStore())
        val local = LaunchLocalDataSource(database, preferences)
        var terminusClientBuilt = false
        // The production module picks the repository by backend; the test supplies its inputs.
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
            terminusRequests, llRequests, database, local,
            koin.get<LLLaunchRepositoryImpl>(named(LL_REPOSITORY)), koin.get<LaunchRepository>(),
            { terminusClientBuilt }
        )
    }

    private fun Env.terminusRepository(): LaunchRepository =
        repository

    private fun okWith(body: String = fixture): (String) -> Reply = { Reply(body) }

    private fun withNet(raw: String, net: String): String {
        val obj = Json.parseToJsonElement(raw).jsonObject
        return JsonObject(obj + ("net" to JsonPrimitive(net))).toString()
    }

    @Test
    fun llBackendMakesZeroTerminusCalls() = runTest {
        val env = envFor(DataBackend.LL, okWith())
        val repository = env.repository

        assertSame(env.llRepository, repository)
        val result = repository.getLaunchDetailDomain(fixtureId)

        assertTrue(result.isSuccess, "LL detail failed: ${result.exceptionOrNull()}")
        assertFalse(env.terminusClientWasBuilt)
        assertTrue(env.terminusRequests.isEmpty(), "Terminus got ${env.terminusRequests}")
        assertEquals(1, env.llRequests.size)
    }

    @Test
    fun terminusBackendRequestsLaunchDetailAndMapsToDomain() = runTest {
        val env = env(okWith())

        val result = env.terminusRepository().getLaunchDetailDomain(fixtureId)

        assertEquals(listOf("/api/v1/launches/$fixtureId/"), env.terminusRequests)
        assertTrue(env.llRequests.isEmpty(), "LL got ${env.llRequests}")
        val launch = result.getOrThrow()
        assertEquals(fixtureId, launch.id)
        assertEquals(Json.parseToJsonElement(fixture).jsonObject.getValue("name").jsonPrimitive.content, launch.name)
        assertTrue(launch.provider.name.isNotBlank())
        assertNotNull(launch.pad)
        assertNotNull(launch.rocketDetail)
    }

    @Test
    fun terminusAndLlMapTheSameResponseToTheSameLaunch() = runTest {
        val env = env(okWith())

        val viaTerminus = env.terminusRepository().getLaunchDetailDomain(fixtureId).getOrThrow()
        val viaLl = env.llRepository.getLaunchDetailDomain(fixtureId, forceRefresh = true).getOrThrow()

        assertEquals(viaLl, viaTerminus)
    }

    @Test
    fun emptyStringAndNullUriFieldsBothDecode() = runTest {
        // Terminus sends "" for empty uri-or-empty fields and null for unset ones (Tasks#1005).
        val uriFields = Regex("\"(link|map_image|wiki_link|info_link|feature_image)\":\"[^\"]*\"")
        val withEmpty = uriFields.replace(fixture) { "\"${it.groupValues[1]}\":\"\"" }
        val withNull = uriFields.replace(fixture) { "\"${it.groupValues[1]}\":null" }
        assertTrue(withEmpty.contains("\"link\":\"\""), "fixture has no link field to blank")

        for (body in listOf(fixture, withEmpty, withNull)) {
            val decoded = json.decodeFromString<TerminusLaunchDetailed>(body)
            assertEquals(fixtureId, decoded.id)
            val env = env(okWith(body))
            assertTrue(env.terminusRepository().getLaunchDetailDomain(fixtureId).isSuccess)
        }
        val blank = json.decodeFromString<TerminusLaunchDetailed>(withEmpty)
        assertEquals("", blank.launchServiceProvider.image?.license?.link)
        val unset = json.decodeFromString<TerminusLaunchDetailed>(withNull)
        assertNull(unset.launchServiceProvider.image?.license?.link)
    }

    @Test
    fun notFoundFailsLikeLlAndGivesNullById() = runTest {
        val env = env { Reply("""{"detail":"No Launch matches the given query."}""", HttpStatusCode.NotFound) }
        val repository = env.terminusRepository()

        val error = assertFailsWith<Exception> { repository.getLaunchDetailDomain(fixtureId).getOrThrow() }
        assertTrue(error.message!!.startsWith("API Error 404:"), error.message)

        val byId = repository.getLaunchByIdDomain(fixtureId)
        assertTrue(byId.isSuccess)
        assertNull(byId.getOrNull())
    }

    @Test
    fun cachedDetailIsServedWhenTheNetworkFails() = runTest {
        var fail = false
        val env = env {
            if (fail) throw IOException("offline")
            Reply(fixture)
        }
        val repository = env.terminusRepository()

        val first = repository.getLaunchDetailDomain(fixtureId).getOrThrow()
        fail = true
        val second = repository.getLaunchDetailDomain(fixtureId, forceRefresh = true)

        assertEquals(2, env.terminusRequests.size)
        assertEquals(first, second.getOrThrow())
        assertEquals(first, repository.getStaleDetailedLaunch(fixtureId))
    }

    @Test
    fun serverErrorServesTheStaleEntry() = runTest {
        var status = HttpStatusCode.OK
        val env = env { Reply(if (status == HttpStatusCode.OK) fixture else "oops", status) }
        val repository = env.terminusRepository()

        val first = repository.getLaunchDetailDomain(fixtureId).getOrThrow()
        status = HttpStatusCode.ServiceUnavailable

        assertEquals(first, repository.getLaunchDetailDomain(fixtureId, forceRefresh = true).getOrThrow())
    }

    @Test
    fun freshCacheEntryIsServedWithoutANetworkCall() = runTest {
        val env = env(okWith())
        val repository = env.terminusRepository()

        repository.getLaunchDetailDomain(fixtureId).getOrThrow()
        repository.getLaunchDetailDomain(fixtureId).getOrThrow()

        assertEquals(1, env.terminusRequests.size)
    }

    @Test
    fun undecodableLegacyBlobIsACacheMissNotACrash() = runTest {
        val env = env(okWith())
        // A legacy LL blob (what the LL repository stores) and plain garbage, one after the other.
        env.local.cacheDetailedLaunch(json.decodeFromString<LLLaunchDetailed>(fixture))

        assertNull(env.terminusRepository().getStaleDetailedLaunch(fixtureId))
        assertNull(env.local.getTerminusDetailedLaunch(fixtureId))
        assertTrue(env.terminusRepository().getLaunchDetailDomain(fixtureId).isSuccess)
        assertEquals(1, env.terminusRequests.size, "legacy blob must not count as a cache hit")

        val now = Clock.System.now().toEpochMilliseconds()
        env.database.launchQueries.insertOrReplaceDetailed(
            id = fixtureId, name = "x", status_id = null, status_name = null, net = null,
            window_end = null, window_start = null, launch_service_provider_id = null,
            launch_service_provider_name = null, rocket_configuration_id = null,
            rocket_configuration_name = null, pad_name = null, location_name = null,
            image_url = null, mission_name = null, mission_description = null,
            json_data = "{not json", cached_at = now, expires_at = now + 600_000
        )
        assertNull(env.terminusRepository().getStaleDetailedLaunch(fixtureId))
        assertTrue(env.terminusRepository().getLaunchDetailDomain(fixtureId).isSuccess)
        assertEquals(2, env.terminusRequests.size)
    }

    @Test
    fun terminusBlobIsACacheMissForTheLlRepository() = runTest {
        val env = env(okWith())
        env.terminusRepository().getLaunchDetailDomain(fixtureId).getOrThrow()

        assertNull(env.local.getDetailedLaunchApi(fixtureId))
        assertNull(env.llRepository.getStaleDetailedLaunch(fixtureId))
    }

    @Test
    fun launchWithinADayOfNetCachesForAtMost30Seconds() = runTest {
        val soon = withNet(fixture, (Clock.System.now() + 2.hours).toString())
        val near = env(okWith(soon))
        near.terminusRepository().getLaunchDetailDomain(fixtureId).getOrThrow()
        val nearRow = near.database.launchQueries.getDetailedByIdStale(fixtureId).executeAsOne()
        assertTrue(nearRow.expires_at - nearRow.cached_at <= 30_000, "ttl ${nearRow.expires_at - nearRow.cached_at} ms")

        val far = env(okWith())
        far.terminusRepository().getLaunchDetailDomain(fixtureId).getOrThrow()
        val farRow = far.database.launchQueries.getDetailedByIdStale(fixtureId).executeAsOne()
        assertTrue(farRow.expires_at - farRow.cached_at > 30_000, "ttl ${farRow.expires_at - farRow.cached_at} ms")
    }
}

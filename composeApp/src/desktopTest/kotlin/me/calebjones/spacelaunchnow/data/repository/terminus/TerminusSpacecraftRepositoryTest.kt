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
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpacecraftApi as LLSpacecraftApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpacecraftConfigurationsApi as LLSpacecraftConfigurationsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.SpacecraftApi as TerminusSpacecraftApi
import me.calebjones.spacelaunchnow.api.terminus.apis.SpacecraftConfigurationsApi as TerminusSpacecraftConfigurationsApi
import me.calebjones.spacelaunchnow.data.model.DataBackend
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.SpacecraftConfigRepository
import me.calebjones.spacelaunchnow.data.repository.SpacecraftRepository
import me.calebjones.spacelaunchnow.data.repository.ll.LLSpacecraftConfigRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ll.LLSpacecraftRepositoryImpl
import me.calebjones.spacelaunchnow.di.spaceRepositoryModule
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Spacecraft and spacecraft configurations on Terminus behind [DataBackend] (Tasks#1012). The
 * fixtures in `resources/terminus` are Terminus payloads that are also valid LL2 JSON, so the same
 * body fed to the LL repository must give the same domain value as the Terminus repository.
 */
class TerminusSpacecraftRepositoryTest {

    private fun resource(name: String): String = checkNotNull(
        TerminusSpacecraftRepositoryTest::class.java.getResourceAsStream("/terminus/$name")
    ) { "missing fixture $name" }.bufferedReader().use { it.readText() }

    private val config: String get() = resource("spacecraft_config_detail.json")
    private val spacecraft: String
        get() = resource("spacecraft_detail.json").replace("\"__SPACECRAFT_CONFIG__\"", config)

    private fun page(vararg results: String) =
        """{"count":${results.size},"next":null,"previous":null,"results":[${results.joinToString(",")}]}"""

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

    private fun byPath(url: Url): String {
        val path = url.encodedPath
        return when {
            path.endsWith("/spacecraft/detailed/") -> page(spacecraft)
            path.endsWith("/spacecraft/301/") -> spacecraft
            path.endsWith("/spacecraft_configurations/detailed/") -> page(config)
            path.endsWith("/spacecraft_configurations/11/") -> config
            else -> error("unexpected $path")
        }
    }

    private val base = "https://terminus.test"
    private val llBase = "https://ll.test"

    private fun terminusSpacecraft(b: Backend) =
        TerminusSpacecraftRepositoryImpl(TerminusSpacecraftApi(baseUrl = base, httpClientEngine = b.engine))

    private fun llSpacecraft(b: Backend) =
        LLSpacecraftRepositoryImpl(LLSpacecraftApi(baseUrl = llBase, httpClientEngine = b.engine))

    private fun terminusConfigs(b: Backend) = TerminusSpacecraftConfigRepositoryImpl(
        TerminusSpacecraftConfigurationsApi(baseUrl = base, httpClientEngine = b.engine)
    )

    private fun llConfigs(b: Backend) = LLSpacecraftConfigRepositoryImpl(
        LLSpacecraftConfigurationsApi(baseUrl = llBase, httpClientEngine = b.engine)
    )

    // -- Mapping: Terminus result equals LL result for the same body ---------------------------------

    @Test
    fun spacecraftByConfigMapsLikeLl() = runTest {
        val t = backend { Reply(byPath(it)) }
        val l = backend { Reply(byPath(it)) }

        val terminus = terminusSpacecraft(t).getSpacecraftByConfigDomain(11, limit = 5, forceRefresh = true, isPlaceholder = false).getOrThrow()
        val ll = llSpacecraft(l).getSpacecraftByConfigDomain(11, limit = 5, forceRefresh = true, isPlaceholder = false).getOrThrow()

        val request = t.requests.single()
        assertEquals("/api/v1/spacecraft/detailed/", request.encodedPath)
        assertEquals("11", request.parameters["spacecraft_config"])
        assertEquals("5", request.parameters["limit"])
        assertEquals("-id", request.parameters["ordering"])
        assertEquals("false", request.parameters["is_placeholder"])
        assertEquals(DataSource.NETWORK, terminus.source)
        assertEquals(ll.data, terminus.data)
        assertEquals(listOf(301), terminus.data.map { it.id })
        assertEquals("Crew Dragon", terminus.data.single().config?.name)
    }

    @Test
    fun spacecraftDetailMapsLikeLl() = runTest {
        val t = backend { Reply(byPath(it)) }
        val l = backend { Reply(byPath(it)) }

        val terminus = terminusSpacecraft(t).getSpacecraftDetailsDomain(301).getOrThrow()
        val ll = llSpacecraft(l).getSpacecraftDetailsDomain(301).getOrThrow()

        assertEquals("/api/v1/spacecraft/301/", t.requests.single().encodedPath)
        assertEquals(ll, terminus)
        assertEquals("Endeavour", terminus.name)
        assertEquals("C206", terminus.serialNumber)
        assertEquals("Active", terminus.status?.name)
    }

    @Test
    fun spacecraftListMapsLikeLl() = runTest {
        val t = backend { Reply(byPath(it)) }
        val l = backend { Reply(byPath(it)) }

        val terminus = terminusSpacecraft(t).getSpacecraftDomain(limit = 10, offset = 20, inSpace = true, search = "dragon").getOrThrow()
        val ll = llSpacecraft(l).getSpacecraftDomain(limit = 10, offset = 20, inSpace = true, search = "dragon").getOrThrow()

        val request = t.requests.single()
        assertEquals("/api/v1/spacecraft/detailed/", request.encodedPath)
        assertEquals("10", request.parameters["limit"])
        assertEquals("20", request.parameters["offset"])
        assertEquals("true", request.parameters["in_space"])
        assertEquals("dragon", request.parameters["search"])
        assertEquals("-flights_count", request.parameters["ordering"])
        assertEquals(ll, terminus)
        assertEquals(1, terminus.count)
    }

    @Test
    fun configsByAgencyMapLikeLl() = runTest {
        val t = backend { Reply(byPath(it)) }
        val l = backend { Reply(byPath(it)) }

        val terminus = terminusConfigs(t).getConfigurationsByAgencyDomain(121, limit = 7, offset = 3).getOrThrow()
        val ll = llConfigs(l).getConfigurationsByAgencyDomain(121, limit = 7, offset = 3).getOrThrow()

        val request = t.requests.single()
        assertEquals("/api/v1/spacecraft_configurations/detailed/", request.encodedPath)
        assertEquals("121", request.parameters["agency"])
        assertEquals("7", request.parameters["limit"])
        assertEquals("3", request.parameters["offset"])
        assertEquals(ll, terminus)
        assertEquals(listOf(11), terminus.results.map { it.id })
    }

    @Test
    fun configsListMapsLikeLl() = runTest {
        val t = backend { Reply(byPath(it)) }
        val l = backend { Reply(byPath(it)) }

        val terminus = terminusConfigs(t).getConfigurationsDomain(limit = 4, offset = 0, search = "dragon").getOrThrow()
        val ll = llConfigs(l).getConfigurationsDomain(limit = 4, offset = 0, search = "dragon").getOrThrow()

        val request = t.requests.single()
        assertEquals("/api/v1/spacecraft_configurations/detailed/", request.encodedPath)
        assertEquals("dragon", request.parameters["search"])
        assertEquals(null, request.parameters["agency"])
        assertEquals(ll, terminus)
    }

    @Test
    fun configDetailMapsLikeLl() = runTest {
        val t = backend { Reply(byPath(it)) }
        val l = backend { Reply(byPath(it)) }

        val terminus = terminusConfigs(t).getConfigurationDetailsDomain(11).getOrThrow()
        val ll = llConfigs(l).getConfigurationDetailsDomain(11).getOrThrow()

        assertEquals("/api/v1/spacecraft_configurations/11/", t.requests.single().encodedPath)
        assertEquals(ll, terminus)
        assertEquals("Crew Dragon", terminus.name)
        assertEquals(4, terminus.crewCapacity)
        assertEquals("SpaceX", terminus.agency?.name)
    }

    // -- Cache --------------------------------------------------------------------------------------

    @Test
    fun secondByConfigReadIsCachedAndForceRefreshSkipsIt() = runTest {
        val t = backend { Reply(byPath(it)) }
        val repo = terminusSpacecraft(t)

        repo.getSpacecraftByConfigDomain(11, forceRefresh = false, isPlaceholder = null).getOrThrow()
        val cached = repo.getSpacecraftByConfigDomain(11, forceRefresh = false, isPlaceholder = null).getOrThrow()
        repo.getSpacecraftByConfigDomain(11, forceRefresh = true, isPlaceholder = null).getOrThrow()

        assertEquals(DataSource.CACHE, cached.source)
        assertEquals(2, t.requests.size)
    }

    @Test
    fun configFallsBackToStaleOnServerError() = runTest {
        var fail = false
        val t = backend { if (fail) Reply("{}", HttpStatusCode.InternalServerError) else Reply(byPath(it)) }
        val repo = terminusSpacecraft(t)

        repo.getSpacecraftByConfigDomain(11, forceRefresh = false, isPlaceholder = null).getOrThrow()
        fail = true
        val stale = repo.getSpacecraftByConfigDomain(11, forceRefresh = true, isPlaceholder = null).getOrThrow()

        assertEquals(DataSource.STALE_CACHE, stale.source)
        assertEquals(listOf(301), stale.data.map { it.id })
    }

    @Test
    fun notFoundIsAFailure() = runTest {
        val t = backend { Reply("{}", HttpStatusCode.NotFound) }

        assertTrue(terminusSpacecraft(t).getSpacecraftDetailsDomain(404).exceptionOrNull() is TerminusNotFoundException)
        assertTrue(terminusConfigs(t).getConfigurationDetailsDomain(404).exceptionOrNull() is TerminusNotFoundException)
    }

    // -- Backend switch -----------------------------------------------------------------------------

    private class Wired(
        val terminus: Backend,
        val ll: Backend,
        val spacecraft: SpacecraftRepository,
        val configs: SpacecraftConfigRepository,
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
                    single { LLSpacecraftApi(baseUrl = llBase, httpClientEngine = ll.engine) }
                    single { LLSpacecraftConfigurationsApi(baseUrl = llBase, httpClientEngine = ll.engine) }
                    single<TerminusSpacecraftApi> {
                        built = true
                        TerminusSpacecraftApi(baseUrl = base, httpClientEngine = terminus.engine)
                    }
                    single<TerminusSpacecraftConfigurationsApi> {
                        built = true
                        TerminusSpacecraftConfigurationsApi(baseUrl = base, httpClientEngine = terminus.engine)
                    }
                    single<DataBackend> { which }
                },
                spaceRepositoryModule()
            )
        }.koin
        return Wired(terminus, ll, koin.get(), koin.get(), { built })
    }

    @Test
    fun llBackendMakesZeroTerminusCalls() = runTest {
        val env = wired(DataBackend.LL)

        assertIs<LLSpacecraftRepositoryImpl>(env.spacecraft)
        assertIs<LLSpacecraftConfigRepositoryImpl>(env.configs)
        assertTrue(env.spacecraft.getSpacecraftByConfigDomain(11, forceRefresh = true).isSuccess)
        assertTrue(env.spacecraft.getSpacecraftDetailsDomain(301).isSuccess)
        assertTrue(env.spacecraft.getSpacecraftDomain().isSuccess)
        assertTrue(env.configs.getConfigurationsByAgencyDomain(121).isSuccess)
        assertTrue(env.configs.getConfigurationsDomain().isSuccess)
        assertTrue(env.configs.getConfigurationDetailsDomain(11).isSuccess)

        assertEquals(6, env.ll.requests.size)
        assertFalse(env.terminusClientWasBuilt, "the Terminus clients must not be built on LL")
        assertTrue(env.terminus.requests.isEmpty(), "Terminus got ${env.terminus.requests}")
    }

    @Test
    fun terminusBackendMakesZeroLlCalls() = runTest {
        val env = wired(DataBackend.TERMINUS)

        assertIs<TerminusSpacecraftRepositoryImpl>(env.spacecraft)
        assertIs<TerminusSpacecraftConfigRepositoryImpl>(env.configs)
        assertTrue(env.spacecraft.getSpacecraftByConfigDomain(11, forceRefresh = true).isSuccess)
        assertTrue(env.spacecraft.getSpacecraftDetailsDomain(301).isSuccess)
        assertTrue(env.spacecraft.getSpacecraftDomain().isSuccess)
        assertTrue(env.configs.getConfigurationsByAgencyDomain(121).isSuccess)
        assertTrue(env.configs.getConfigurationsDomain().isSuccess)
        assertTrue(env.configs.getConfigurationDetailsDomain(11).isSuccess)

        assertTrue(env.ll.requests.isEmpty(), "LL got ${env.ll.requests}")
        assertEquals(6, env.terminus.requests.size)
    }
}

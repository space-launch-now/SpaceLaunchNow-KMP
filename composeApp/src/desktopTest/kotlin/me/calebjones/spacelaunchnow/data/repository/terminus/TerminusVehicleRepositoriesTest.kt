package me.calebjones.spacelaunchnow.data.repository.terminus

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AgenciesApi as LLAgenciesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LauncherConfigurationsApi as LLLauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchersApi as LLLaunchersApi
import me.calebjones.spacelaunchnow.api.terminus.apis.AgenciesApi as TerminusAgenciesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LauncherConfigurationsApi as TerminusLauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LaunchersApi as TerminusLaunchersApi
import me.calebjones.spacelaunchnow.data.model.DataBackend
import me.calebjones.spacelaunchnow.data.repository.AgencyRepository
import me.calebjones.spacelaunchnow.data.repository.LauncherConfigRepository
import me.calebjones.spacelaunchnow.data.repository.LauncherRepository
import me.calebjones.spacelaunchnow.data.repository.RocketRepository
import me.calebjones.spacelaunchnow.data.repository.ll.LLAgencyRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ll.LLLauncherConfigRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ll.LLLauncherRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ll.LLRocketRepositoryImpl
import me.calebjones.spacelaunchnow.di.vehicleRepositoryModule
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Agency, launcher configuration, launcher and rocket repositories on Terminus behind
 * [DataBackend] (Tasks#1011 S5). The fixtures in `resources/terminus` follow the generated
 * Terminus models. The same JSON is served to the LL repository (Terminus serves the LL2 shapes),
 * so each row checks that the Terminus repository returns the same domain value as LL.
 */
class TerminusVehicleRepositoriesTest {

    private fun resource(name: String): String = checkNotNull(
        TerminusVehicleRepositoriesTest::class.java.getResourceAsStream("/terminus/$name")
    ) { "missing fixture $name" }.bufferedReader().use { it.readText() }

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    /** Serves the fixture that matches the endpoint, whatever the base path prefix. */
    private fun byPath(url: Url): String {
        val segments = url.encodedPath.trim('/').split('/')
        val last = segments.last()
        val isId = last.toIntOrNull() != null
        val detailed = last == "detailed"
        return when {
            "agencies" in segments -> if (isId) resource("agency_detail.json") else resource("agencies_list.json")
            "launcher_configurations" in segments -> when {
                isId -> resource("launcher_config_detail.json")
                detailed -> resource("launcher_configs_detailed_list.json")
                else -> resource("launcher_configs_normal_list.json")
            }
            "launchers" in segments -> if (isId) resource("launcher_detail.json") else resource("launchers_list.json")
            else -> error("unexpected request $url")
        }
    }

    private fun engine(requests: MutableList<Url> = mutableListOf()) = MockEngine { request ->
        requests += request.url
        respond(content = byPath(request.url), status = HttpStatusCode.OK, headers = jsonHeaders)
    }

    private class Both<T>(val ll: T, val terminus: T)

    private fun agencies(): Both<AgencyRepository> {
        val e = engine()
        return Both(
            LLAgencyRepositoryImpl(LLAgenciesApi(baseUrl = "https://ll.test", httpClientEngine = e)),
            TerminusAgencyRepositoryImpl(TerminusAgenciesApi(baseUrl = "https://terminus.test", httpClientEngine = e))
        )
    }

    private fun configs(): Both<LauncherConfigRepository> {
        val e = engine()
        return Both(
            LLLauncherConfigRepositoryImpl(LLLauncherConfigurationsApi(baseUrl = "https://ll.test", httpClientEngine = e)),
            TerminusLauncherConfigRepositoryImpl(
                TerminusLauncherConfigurationsApi(baseUrl = "https://terminus.test", httpClientEngine = e)
            )
        )
    }

    private fun launchers(): Both<LauncherRepository> {
        val e = engine()
        return Both(
            LLLauncherRepositoryImpl(LLLaunchersApi(baseUrl = "https://ll.test", httpClientEngine = e)),
            TerminusLauncherRepositoryImpl(TerminusLaunchersApi(baseUrl = "https://terminus.test", httpClientEngine = e))
        )
    }

    private fun rockets(): Both<RocketRepository> {
        val e = engine()
        return Both(
            LLRocketRepositoryImpl(LLLauncherConfigurationsApi(baseUrl = "https://ll.test", httpClientEngine = e)),
            TerminusRocketRepositoryImpl(
                TerminusLauncherConfigurationsApi(baseUrl = "https://terminus.test", httpClientEngine = e)
            )
        )
    }

    // -- Parity per row ---------------------------------------------------------------------------

    @Test
    fun getAgenciesDomainMatchesLl() = runTest {
        val p = agencies()
        val expected = p.ll.getAgenciesDomain(10, 0, null, null, null, null, null).getOrThrow()
        val actual = p.terminus.getAgenciesDomain(10, 0, null, null, null, null, null).getOrThrow()
        assertEquals(2, actual.results.size)
        assertEquals("NASA", actual.results.first().abbrev)
        assertEquals(expected, actual)
    }

    @Test
    fun searchAgenciesDomainMatchesLl() = runTest {
        val p = agencies()
        assertEquals(p.ll.searchAgenciesDomain("nasa", 5).getOrThrow(), p.terminus.searchAgenciesDomain("nasa", 5).getOrThrow())
    }

    @Test
    fun searchAgenciesDomainReadsPagesOf25() = runTest {
        val requests = mutableListOf<Url>()
        val page = resource("agencies_list.json")
        val nextLink = Regex("\"next\"\\s*:\\s*(null|\"[^\"]*\")")
        val e = MockEngine { request ->
            requests += request.url
            val body = if (request.url.parameters["offset"] == "25") {
                page.replace(nextLink, "\"next\": null")
            } else {
                page.replace(nextLink, "\"next\": \"https://terminus.test/api/v1/agencies/?offset=25\"")
            }
            respond(content = body, status = HttpStatusCode.OK, headers = jsonHeaders)
        }
        val terminus = TerminusAgencyRepositoryImpl(TerminusAgenciesApi(baseUrl = "https://terminus.test", httpClientEngine = e))

        val result = terminus.searchAgenciesDomain("nasa", 50).getOrThrow()

        assertEquals(listOf("25", "25"), requests.map { it.parameters["limit"] })
        assertEquals(listOf(null, "25"), requests.map { it.parameters["offset"] })
        assertEquals(4, result.results.size)
        assertEquals(null, result.next)
    }

    @Test
    fun getAgencyDetailDomainMatchesLl() = runTest {
        val p = agencies()
        val actual = p.terminus.getAgencyDetailDomain(44).getOrThrow()
        assertEquals(44, actual.id)
        assertEquals(p.ll.getAgencyDetailDomain(44).getOrThrow(), actual)
    }

    @Test
    fun getConfigurationsByProgramDomainMatchesLl() = runTest {
        val p = configs()
        val actual = p.terminus.getConfigurationsByProgramDomain(1, 10, 0).getOrThrow()
        assertEquals(listOf(164), actual.results.map { it.id })
        assertEquals(p.ll.getConfigurationsByProgramDomain(1, 10, 0).getOrThrow(), actual)
    }

    @Test
    fun getConfigurationsDomainMatchesLl() = runTest {
        val p = configs()
        assertEquals(
            p.ll.getConfigurationsDomain(10, 0, "falcon").getOrThrow(),
            p.terminus.getConfigurationsDomain(10, 0, "falcon").getOrThrow()
        )
    }

    @Test
    fun getConfigurationDetailsDomainMatchesLl() = runTest {
        val p = configs()
        val actual = p.terminus.getConfigurationDetailsDomain(164).getOrThrow()
        assertEquals(164, actual.id)
        assertEquals(p.ll.getConfigurationDetailsDomain(164).getOrThrow(), actual)
    }

    @Test
    fun getLaunchersDomainMatchesLl() = runTest {
        val p = launchers()
        val actual = p.terminus.getLaunchersDomain(10, 0, null, null, null, null).getOrThrow()
        assertEquals(listOf(77), actual.results.map { it.id })
        assertEquals(p.ll.getLaunchersDomain(10, 0, null, null, null, null).getOrThrow(), actual)
    }

    @Test
    fun getLaunchersByConfigDomainMatchesLl() = runTest {
        val p = launchers()
        assertEquals(
            p.ll.getLaunchersByConfigDomain(164, 10, 0).getOrThrow(),
            p.terminus.getLaunchersByConfigDomain(164, 10, 0).getOrThrow()
        )
    }

    @Test
    fun getLauncherDetailsDomainMatchesLl() = runTest {
        val p = launchers()
        val actual = p.terminus.getLauncherDetailsDomain(77).getOrThrow()
        assertEquals(77, actual.id)
        assertEquals(p.ll.getLauncherDetailsDomain(77).getOrThrow(), actual)
    }

    @Test
    fun getRocketsDomainMatchesLl() = runTest {
        val p = rockets()
        val actual = p.terminus.getRocketsDomain(10, 0, null, null, null, null, null, null).getOrThrow()
        assertEquals(listOf(164), actual.results.map { it.id })
        assertEquals(p.ll.getRocketsDomain(10, 0, null, null, null, null, null, null).getOrThrow(), actual)
    }

    @Test
    fun getRocketDetailsDomainMatchesLl() = runTest {
        val p = rockets()
        assertEquals(p.ll.getRocketDetailsDomain(164).getOrThrow(), p.terminus.getRocketDetailsDomain(164).getOrThrow())
    }

    // -- Request shape -------------------------------------------------------------------------------

    @Test
    fun terminusCallsTheDocumentedEndpoints() = runTest {
        val requests = mutableListOf<Url>()
        val e = engine(requests)
        val base = "https://terminus.test"
        val cfgApi = TerminusLauncherConfigurationsApi(baseUrl = base, httpClientEngine = e)
        TerminusLauncherConfigRepositoryImpl(cfgApi).getConfigurationsByProgramDomain(9, 5, 0).getOrThrow()
        TerminusRocketRepositoryImpl(cfgApi).getRocketsDomain(5, 0, null, null, listOf(9), null, true, null).getOrThrow()
        TerminusLauncherRepositoryImpl(TerminusLaunchersApi(baseUrl = base, httpClientEngine = e))
            .getLaunchersByConfigDomain(164, 5, 0).getOrThrow()

        assertEquals("/api/v1/launcher_configurations/detailed/", requests[0].encodedPath)
        assertEquals("9", requests[0].parameters["program"])
        assertEquals("false", requests[0].parameters["is_placeholder"])
        assertEquals("/api/v1/launcher_configurations/", requests[1].encodedPath)
        assertEquals("true", requests[1].parameters["active"])
        assertEquals("/api/v1/launchers/detailed/", requests[2].encodedPath)
        assertEquals("164", requests[2].parameters["launcher_config__ids"])
    }

    // -- Backend switch ------------------------------------------------------------------------------

    private class Wired(
        val terminusRequests: MutableList<Url>,
        val llRequests: MutableList<Url>,
        val agencies: AgencyRepository,
        val configs: LauncherConfigRepository,
        val launchers: LauncherRepository,
        val rockets: RocketRepository,
        val terminusClientBuilt: () -> Boolean
    )

    private fun wired(backend: DataBackend): Wired {
        val terminusRequests = mutableListOf<Url>()
        val llRequests = mutableListOf<Url>()
        val terminusEngine = engine(terminusRequests)
        val llEngine = engine(llRequests)
        var terminusClientBuilt = false
        val koin = koinApplication {
            modules(
                module {
                    single { LLAgenciesApi(baseUrl = "https://ll.test", httpClientEngine = llEngine) }
                    single { LLLauncherConfigurationsApi(baseUrl = "https://ll.test", httpClientEngine = llEngine) }
                    single { LLLaunchersApi(baseUrl = "https://ll.test", httpClientEngine = llEngine) }
                    single<TerminusAgenciesApi> {
                        terminusClientBuilt = true
                        TerminusAgenciesApi(baseUrl = "https://terminus.test", httpClientEngine = terminusEngine)
                    }
                    single<TerminusLauncherConfigurationsApi> {
                        terminusClientBuilt = true
                        TerminusLauncherConfigurationsApi(baseUrl = "https://terminus.test", httpClientEngine = terminusEngine)
                    }
                    single<TerminusLaunchersApi> {
                        terminusClientBuilt = true
                        TerminusLaunchersApi(baseUrl = "https://terminus.test", httpClientEngine = terminusEngine)
                    }
                    single<DataBackend> { backend }
                },
                vehicleRepositoryModule()
            )
        }.koin
        return Wired(
            terminusRequests, llRequests,
            koin.get(), koin.get(), koin.get(), koin.get(),
            { terminusClientBuilt }
        )
    }

    @Test
    fun llBackendMakesZeroTerminusCalls() = runTest {
        val env = wired(DataBackend.LL)

        assertIs<LLAgencyRepositoryImpl>(env.agencies)
        assertIs<LLLauncherConfigRepositoryImpl>(env.configs)
        assertIs<LLLauncherRepositoryImpl>(env.launchers)
        assertIs<LLRocketRepositoryImpl>(env.rockets)
        assertTrue(env.agencies.getAgenciesDomain(5, 0, null, null, null, null, null).isSuccess)
        assertTrue(env.agencies.searchAgenciesDomain("x", 5).isSuccess)
        assertTrue(env.agencies.getAgencyDetailDomain(44).isSuccess)
        assertTrue(env.configs.getConfigurationsByProgramDomain(1, 5, 0).isSuccess)
        assertTrue(env.configs.getConfigurationsDomain(5, 0, null).isSuccess)
        assertTrue(env.configs.getConfigurationDetailsDomain(164).isSuccess)
        assertTrue(env.launchers.getLaunchersDomain(5, 0, null, null, null, null).isSuccess)
        assertTrue(env.launchers.getLaunchersByConfigDomain(164, 5, 0).isSuccess)
        assertTrue(env.launchers.getLauncherDetailsDomain(77).isSuccess)
        assertTrue(env.rockets.getRocketsDomain(5, 0, null, null, null, null, null, null).isSuccess)
        assertTrue(env.rockets.getRocketDetailsDomain(164).isSuccess)

        assertTrue(env.llRequests.isNotEmpty())
        assertFalse(env.terminusClientBuilt(), "the Terminus clients must not be built on LL")
        assertTrue(env.terminusRequests.isEmpty(), "Terminus got ${env.terminusRequests}")
    }

    @Test
    fun terminusBackendMakesZeroLlCalls() = runTest {
        val env = wired(DataBackend.TERMINUS)

        assertIs<TerminusAgencyRepositoryImpl>(env.agencies)
        assertIs<TerminusLauncherConfigRepositoryImpl>(env.configs)
        assertIs<TerminusLauncherRepositoryImpl>(env.launchers)
        assertIs<TerminusRocketRepositoryImpl>(env.rockets)
        assertEquals(44, env.agencies.getAgencyDetailDomain(44).getOrThrow().id)
        assertEquals(164, env.configs.getConfigurationDetailsDomain(164).getOrThrow().id)
        assertEquals(77, env.launchers.getLauncherDetailsDomain(77).getOrThrow().id)
        assertEquals(164, env.rockets.getRocketDetailsDomain(164).getOrThrow().id)
        assertTrue(env.llRequests.isEmpty(), "LL got ${env.llRequests}")
        assertEquals(4, env.terminusRequests.size)
    }
}

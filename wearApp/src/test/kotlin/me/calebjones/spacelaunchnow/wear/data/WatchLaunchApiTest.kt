package me.calebjones.spacelaunchnow.wear.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.wear.data.model.CachedLaunch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WatchLaunchApiTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun api(body: String): WatchLaunchApi {
        val engine = MockEngine { request ->
            requests += request
            respond(
                content = body,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        return WatchLaunchApi(
            HttpClient(engine) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
        )
    }

    private fun terminusRequests() = requests.filter { it.url.host.contains("terminus.spacelaunchnow.app") }

    @Test
    fun terminusBackendCallsTerminusLaunchListWithSortedFilters() = runTest {
        val launches = api(TERMINUS_FIXTURE).fetchUpcoming(
            config = WatchBackendConfig(WatchDataBackend.TERMINUS),
            filters = WatchFilterPreferences(
                followAllLaunches = false,
                agencyIds = listOf(121, 44, 121),
                locationIds = listOf(12, 27),
            ),
            limit = 20,
        )

        val request = requests.single()
        assertEquals("terminus.spacelaunchnow.app", request.url.host)
        assertEquals("/api/v1/launches/", request.url.encodedPath)
        val params = request.url.parameters
        assertEquals("20", params["limit"])
        assertEquals("true", params["upcoming"])
        assertEquals("net", params["ordering"])
        assertEquals("44,121", params["lsp__id"])
        assertEquals("12,27", params["location__ids"])
        assertEquals(listOf(EXPECTED_FROM_TERMINUS), launches)
    }

    @Test
    fun terminusBackendUsesTheStagingHostWhenTheConfigSaysSo() = runTest {
        api(TERMINUS_FIXTURE).fetchUpcoming(
            config = WatchBackendConfig(WatchDataBackend.TERMINUS, WatchBackendConfig.TERMINUS_STAGING_URL),
            filters = WatchFilterPreferences(),
            limit = 5,
        )

        assertEquals("staging-terminus.spacelaunchnow.app", requests.single().url.host)
    }

    @Test
    fun terminusRequestOmitsIdFiltersWhenFollowingAllLaunches() = runTest {
        api(TERMINUS_FIXTURE).fetchUpcoming(
            config = WatchBackendConfig(WatchDataBackend.TERMINUS),
            filters = WatchFilterPreferences(followAllLaunches = true, agencyIds = listOf(44), locationIds = listOf(12)),
            limit = 20,
        )

        val params = requests.single().url.parameters
        assertTrue("lsp__id" !in params && "location__ids" !in params)
    }

    @Test
    fun terminusAndLlFixturesMapToTheSameWatchModelFields() = runTest {
        val fromTerminus = api(TERMINUS_FIXTURE).fetchUpcoming(
            WatchBackendConfig(WatchDataBackend.TERMINUS), WatchFilterPreferences(), 20,
        ).single()
        val fromLl = api(LL_FIXTURE).fetchUpcoming(
            WatchBackendConfig(WatchDataBackend.LL), WatchFilterPreferences(), 20,
        ).single()

        // Fields both list shapes carry map to the same watch values.
        assertEquals(fromLl.copy(rocketConfigName = null), fromTerminus.copy(
            rocketConfigName = null, missionName = null, missionDescription = null,
        ))
        assertEquals(EXPECTED_FROM_LL, fromLl)
    }

    @Test
    fun llBackendNeverCallsTerminus() = runTest {
        val launches = api(LL_FIXTURE).fetchUpcoming(
            config = WatchBackendConfig(WatchDataBackend.LL),
            filters = WatchFilterPreferences(followAllLaunches = false, agencyIds = listOf(121), locationIds = listOf(12)),
            limit = 20,
        )

        assertEquals(0, terminusRequests().size)
        val request = requests.single()
        assertEquals("ll.thespacedevs.com", request.url.host)
        assertEquals("/2.4.0/launches/upcoming/", request.url.encodedPath)
        assertEquals("list", request.url.parameters["mode"])
        assertEquals("121", request.url.parameters["lsp__id"])
        assertEquals(listOf(EXPECTED_FROM_LL), launches)
    }

    @Test
    fun noBackendValueYetReadsLlAndMakesZeroTerminusRequests() = runTest {
        val dataStore = tempDataStore()

        val config = WatchBackendStore.load(dataStore)
        assertEquals(WatchDataBackend.LL, config.backend)

        api(LL_FIXTURE).fetchUpcoming(config, WatchFilterPreferences(), 20)
        assertEquals(0, terminusRequests().size)
        assertEquals("ll.thespacedevs.com", requests.single().url.host)
    }

    @Test
    fun backendStoreRoundTripsTheValueTheListenerSaves() = runTest {
        val dataStore = tempDataStore()

        WatchBackendStore.save(dataStore, "terminus", WatchBackendConfig.TERMINUS_STAGING_URL)
        assertEquals(
            WatchBackendConfig(WatchDataBackend.TERMINUS, WatchBackendConfig.TERMINUS_STAGING_URL),
            WatchBackendStore.load(dataStore),
        )

        // An unknown backend value or host never sticks: LL and the prod host are the fallbacks.
        WatchBackendStore.save(dataStore, "bogus", "https://evil.example.com")
        assertEquals(WatchBackendConfig(), WatchBackendStore.load(dataStore))
    }

    /** In-memory stand-in: the file-backed store cannot rename over an existing file on Windows. */
    private fun tempDataStore(): DataStore<Preferences> = object : DataStore<Preferences> {
        private val state = MutableStateFlow(emptyPreferences())
        override val data: Flow<Preferences> = state
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            transform(state.value).also { state.value = it }
    }

    private companion object {
        val EXPECTED_FROM_TERMINUS = CachedLaunch(
            id = "abc-123",
            name = "Falcon 9 Block 5 | Starlink 10-1",
            net = Instant.parse("2026-11-20T10:30:00Z"),
            statusAbbrev = "Go",
            statusName = "Go for Launch",
            lspName = "SpaceX",
            lspAbbrev = "SpX",
            rocketConfigName = "Falcon 9 Block 5",
            missionName = "Starlink Group 10-1",
            missionDescription = "A batch of Starlink satellites.",
            padLocationName = "Cape Canaveral, FL, USA",
            imageUrl = "https://example.com/image.jpg",
        )

        val EXPECTED_FROM_LL = CachedLaunch(
            id = "abc-123",
            name = "Falcon 9 Block 5 | Starlink 10-1",
            net = Instant.parse("2026-11-20T10:30:00Z"),
            statusAbbrev = "Go",
            statusName = "Go for Launch",
            lspName = "SpaceX",
            lspAbbrev = "SpX",
            rocketConfigName = "10-1",
            padLocationName = "Cape Canaveral, FL, USA",
            imageUrl = "https://example.com/image.jpg",
        )

        const val TERMINUS_FIXTURE = """
        {"count":1,"next":null,"previous":null,"results":[{
          "id":"abc-123","name":"Falcon 9 Block 5 | Starlink 10-1","net":"2026-11-20T10:30:00Z",
          "status":{"id":1,"name":"Go for Launch","abbrev":"Go"},
          "launch_service_provider":{"id":121,"name":"SpaceX","abbrev":"SpX","type":null},
          "rocket":{"id":1,"configuration":{"id":164,"name":"Falcon 9","full_name":"Falcon 9 Block 5"}},
          "mission":{"id":2,"name":"Starlink Group 10-1","description":"A batch of Starlink satellites."},
          "pad":{"id":80,"location":{"id":12,"name":"Cape Canaveral, FL, USA"}},
          "image":{"id":9,"name":"x","image_url":"https://example.com/image.jpg","thumbnail_url":"https://example.com/t.jpg"}
        }]}
        """

        const val LL_FIXTURE = """
        {"count":1,"next":null,"previous":null,"results":[{
          "id":"abc-123","name":"Falcon 9 Block 5 | Starlink 10-1","net":"2026-11-20T10:30:00Z",
          "status":{"id":1,"name":"Go for Launch","abbrev":"Go"},
          "launch_service_provider":{"id":121,"name":"SpaceX","abbrev":"SpX"},
          "launch_designator":"10-1","location_name":"Cape Canaveral, FL, USA",
          "image":{"image_url":"https://example.com/image.jpg","thumbnail_url":"https://example.com/t.jpg"}
        }]}
        """
    }
}

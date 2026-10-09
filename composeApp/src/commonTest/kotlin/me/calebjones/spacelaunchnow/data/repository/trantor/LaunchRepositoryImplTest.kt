package me.calebjones.spacelaunchnow.data.repository.trantor

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.api.trantor.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.trantor.apis.LaunchesApi
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.ui.viewmodel.InMemoryPreferencesDataStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * D3 (2026-09-05 trantor home-screen-parity spec): [LaunchRepositoryImpl.getFeaturedLaunchDomain]
 * fetches `GET /launches/{id}` for the first candidate and merges it (mission description,
 * provider with all three images, pad location, netPrecision, window fields) over the flat
 * list row, and never fails the hero when that detail fetch fails.
 */
class LaunchRepositoryImplTest {

    private val listRowJson = """
        {
          "count": 1,
          "next": null,
          "previous": null,
          "results": [
            {
              "id": "row-1",
              "name": "Falcon 9 | Starlink",
              "net": "2026-01-01T00:00:00Z",
              "slug": "falcon-9-starlink",
              "status": "Go",
              "status_id": 1,
              "provider_id": 44,
              "provider_name": "SpaceX",
              "mission_id": 10,
              "mission_name": "Starlink",
              "pad_id": 20,
              "pad_name": "SLC-40",
              "location_id": 30,
              "location_name": "CCSFS, FL, USA",
              "net_precision": "Second",
              "net_precision_id": 0,
              "rocket_id": 5,
              "configuration_name": "Falcon 9",
              "image_url": "https://example.com/row.jpg",
              "webcast_live": true
            }
          ]
        }
    """.trimIndent()

    private val detailJson = """
        {
          "id": "row-1",
          "name": "Falcon 9 | Starlink",
          "net": "2026-01-01T00:00:00Z",
          "slug": "falcon-9-starlink",
          "status": "Go",
          "status_id": 1,
          "mission": {
            "id": 10,
            "name": "Starlink",
            "description": "A Starlink mission.",
            "type": "Communications"
          },
          "provider": {
            "id": 44,
            "name": "SpaceX",
            "abbrev": "SpX",
            "agency_type": "Commercial",
            "logo_url": "https://example.com/logo.png",
            "social_logo_url": "https://example.com/social.png",
            "image_url": "https://example.com/image.png"
          },
          "pad": {
            "id": 20,
            "name": "SLC-40",
            "latitude": 28.5,
            "longitude": -80.5,
            "location_id": 30,
            "location": "CCSFS, FL, USA"
          },
          "net_precision": "Second",
          "net_precision_id": 0,
          "window_start": "2026-01-01T00:00:00Z",
          "window_end": "2026-01-01T00:05:00Z"
        }
    """.trimIndent()

    private fun repositoryWith(detailStatus: HttpStatusCode, detailBody: String): LaunchRepositoryImpl {
        val engine = MockEngine { request ->
            if (request.url.encodedPath == "/api/v1/launches") {
                respond(
                    content = listRowJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            } else {
                respond(
                    content = detailBody,
                    status = detailStatus,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
        }
        return LaunchRepositoryImpl(
            launchesApi = LaunchesApi(baseUrl = "https://trantor.test", httpClientEngine = engine),
            agenciesApi = AgenciesApi(baseUrl = "https://trantor.test", httpClientEngine = engine),
            appPreferences = AppPreferences(InMemoryPreferencesDataStore()),
            localDataSource = null,
            statsLocalDataSource = null
        )
    }

    @Test
    fun featuredLaunchMergesDetailOverTheListRow() = runTest {
        val repository = repositoryWith(HttpStatusCode.OK, detailJson)

        val result = repository.getFeaturedLaunchDomain(forceRefresh = true)

        val hero = result.getOrThrow().data.results.first()
        assertEquals("A Starlink mission.", hero.mission?.description)
        assertEquals("https://example.com/social.png", hero.provider.socialLogo)
        assertEquals("https://example.com/logo.png", hero.provider.logoUrl)
        assertEquals("CCSFS, FL, USA", hero.pad?.location?.name)
        assertEquals(0, hero.netPrecision?.id)
        assertNotNull(hero.windowStart)
    }

    @Test
    fun featuredLaunchFallsBackToTheListRowWhenDetailFetchFails() = runTest {
        val repository = repositoryWith(HttpStatusCode.NotFound, """{"detail":"Not Found"}""")

        val result = repository.getFeaturedLaunchDomain(forceRefresh = true)

        // A detail 404 must never fail the hero - the row is returned unchanged.
        val hero = result.getOrThrow().data.results.first()
        assertEquals("row-1", hero.id)
        assertEquals("SpaceX", hero.provider.name)
        // The row's own net_precision/location (D1) survive a detail failure; only the
        // detail-only fields (mission description, window) stay absent.
        assertEquals(0, hero.netPrecision?.id)
        assertEquals("CCSFS, FL, USA", hero.pad?.location?.name)
        assertNull(hero.mission?.description)
    }
}

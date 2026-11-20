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
import me.calebjones.spacelaunchnow.api.terminus.apis.LaunchesApi
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import me.calebjones.spacelaunchnow.ui.viewmodel.InMemoryPreferencesDataStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The widget reads of the Terminus launch repository (Tasks#1223): the slim `/launches/mini/`
 * list with the user's filters, and a cache-only read that also finds the home list's entry.
 */
class TerminusWidgetLaunchListTest {

    private fun resource(name: String): String = checkNotNull(
        TerminusWidgetLaunchListTest::class.java.getResourceAsStream("/terminus/$name")
    ) { "missing fixture $name" }.bufferedReader().use { it.readText() }

    private val miniFixture get() = resource("launches_mini_list.json")
    private val normalFixture get() = resource("launches_normal_list.json")

    private class Env(
        val requests: MutableList<Url>,
        val preferences: AppPreferences,
        val repository: TerminusLaunchRepositoryImpl
    )

    private fun env(): Env {
        val requests = mutableListOf<Url>()
        val engine = MockEngine { request ->
            requests += request.url
            val body = if (request.url.encodedPath.endsWith("/mini/")) miniFixture else normalFixture
            respond(
                content = body,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SpaceLaunchDatabase.Schema.create(driver)
        val preferences = AppPreferences(InMemoryPreferencesDataStore())
        val local = LaunchLocalDataSource(SpaceLaunchDatabase(driver), preferences)
        val api = LaunchesApi(baseUrl = "https://terminus.test", httpClientEngine = engine)
        return Env(requests, preferences, TerminusLaunchRepositoryImpl(api, local, appPreferences = preferences))
    }

    private val agencies = listOf(44, 121)
    private val locations = listOf(12)

    @Test
    fun widgetReadUsesTheMiniListWithTheUsersFilters() = runTest {
        val env = env()
        env.preferences.updateHideTbdLaunches(true)

        val result = env.repository.getUpcomingLaunchesForWidget(
            limit = 10, forceRefresh = true, agencyIds = agencies, locationIds = locations
        ).getOrThrow()

        val url = env.requests.single()
        assertEquals("/api/v1/launches/mini/", url.encodedPath)
        assertEquals("10", url.parameters["limit"])
        assertEquals("true", url.parameters["upcoming"])
        assertEquals("net", url.parameters["ordering"])
        assertEquals("44,121", url.parameters["lsp__id"])
        assertEquals("12", url.parameters["location__ids"])
        assertEquals("1,3,4,5,6,7,9", url.parameters["status__ids"], "hide-TBD")
        assertEquals(DataSource.NETWORK, result.source)
        // The widgets draw the location from pad.location.name; mini sends location_name.
        assertEquals("Cape Canaveral SFS, FL, USA", result.data.results.first().pad?.location?.name)
    }

    @Test
    fun cacheOnlyReadMakesNoRequestAndIsNullWhenNothingIsCached() = runTest {
        val env = env()

        assertNull(env.repository.getCachedUpcomingLaunchesForWidget(10, agencies, locations))
        assertTrue(env.requests.isEmpty())
    }

    @Test
    fun cacheOnlyReadReturnsTheWidgetEntryWithNoRequest() = runTest {
        val env = env()
        env.repository.getUpcomingLaunchesForWidget(10, true, agencies, locations).getOrThrow()
        env.requests.clear()

        val cached = assertNotNull(env.repository.getCachedUpcomingLaunchesForWidget(10, agencies, locations))

        assertTrue(env.requests.isEmpty())
        assertEquals(DataSource.STALE_CACHE, cached.source)
        assertTrue(cached.data.results.isNotEmpty())
        assertNotNull(cached.timestamp)
    }

    @Test
    fun cacheOnlyReadDrawsFromTheHomeListEntry() = runTest {
        val env = env()
        // Home reads the normal list with the same filters and 10 rows.
        env.repository.getUpcomingLaunchesNormalDomain(10, true, agencies, locations).getOrThrow()
        assertEquals("/api/v1/launches/", env.requests.single().encodedPath)
        env.requests.clear()

        val cached = assertNotNull(env.repository.getCachedUpcomingLaunchesForWidget(10, agencies, locations))

        assertTrue(env.requests.isEmpty(), "the widget read the home entry with no request")
        assertTrue(cached.data.results.isNotEmpty())
    }

    @Test
    fun cacheOnlyReadDrawsFromThePreloadEntryOfEightRows() = runTest {
        val env = env()
        env.repository.getUpcomingLaunchesNormalDomain(8, true, agencies, locations).getOrThrow()
        env.requests.clear()

        assertNotNull(env.repository.getCachedUpcomingLaunchesForWidget(10, agencies, locations))
        assertTrue(env.requests.isEmpty())
    }

    @Test
    fun cacheOnlyReadIgnoresAnEntryForOtherFilters() = runTest {
        val env = env()
        env.repository.getUpcomingLaunchesNormalDomain(10, true, listOf(999), locations).getOrThrow()
        env.requests.clear()

        assertNull(env.repository.getCachedUpcomingLaunchesForWidget(10, agencies, locations))
    }

    @Test
    fun cacheOnlyReadTakesTheNewerEntry() = runTest {
        val env = env()
        env.repository.getUpcomingLaunchesNormalDomain(10, true, agencies, locations).getOrThrow()
        Thread.sleep(5)
        env.repository.getUpcomingLaunchesForWidget(10, true, agencies, locations).getOrThrow()
        val widgetEntry = assertNotNull(env.repository.getCachedUpcomingLaunchesForWidget(10, agencies, locations))

        Thread.sleep(5)
        env.repository.getUpcomingLaunchesNormalDomain(10, true, agencies, locations).getOrThrow()
        val homeEntry = assertNotNull(env.repository.getCachedUpcomingLaunchesForWidget(10, agencies, locations))

        assertTrue(homeEntry.timestamp!! > widgetEntry.timestamp!!, "the newer home entry wins")
    }
}

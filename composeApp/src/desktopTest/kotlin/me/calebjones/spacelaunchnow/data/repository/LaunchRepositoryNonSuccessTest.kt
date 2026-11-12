package me.calebjones.spacelaunchnow.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.ll.LLLaunchRepositoryImpl
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import me.calebjones.spacelaunchnow.domain.mapper.testAgencyNormal
import me.calebjones.spacelaunchnow.domain.mapper.testImage
import me.calebjones.spacelaunchnow.domain.mapper.testLaunchStatus
import me.calebjones.spacelaunchnow.domain.mapper.testMission
import me.calebjones.spacelaunchnow.domain.mapper.testNetPrecision
import me.calebjones.spacelaunchnow.domain.mapper.testPadDetailed
import me.calebjones.spacelaunchnow.domain.mapper.testRocketNormal
import me.calebjones.spacelaunchnow.ui.viewmodel.InMemoryPreferencesDataStore
import me.calebjones.spacelaunchnow.util.logging.LogConfig
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * LL2 responses that are not 2xx used to reach `HttpResponse.body()` and fail with
 * `NoTransformationFoundException` (the error page is `text/html`, and the generated client only
 * negotiates `application/json`). That exception extends `UnsupportedOperationException`, so it
 * matched neither the `ResponseException` nor the `IOException` arm and the stale cache was never
 * served. See #240.
 */
class LaunchRepositoryNonSuccessTest {

    private class CapturingWriter : LogWriter() {
        data class Entry(val severity: Severity, val message: String, val throwable: Throwable?)

        val entries = mutableListOf<Entry>()

        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            entries += Entry(severity, message, throwable)
        }
    }

    @AfterTest
    fun resetLogger() {
        SpaceLogger.initialize(LogConfig(Severity.Verbose, emptyList()))
    }

    private fun cachedLaunch(id: String, net: Instant, statusId: Int) = LaunchNormal(
        id = id,
        url = "https://ll.thespacedevs.com/2.4.0/launches/$id/",
        slug = "cached-$id",
        launchDesignator = null,
        status = testLaunchStatus(id = statusId, name = "Cached status"),
        netPrecision = testNetPrecision(),
        image = testImage(),
        launchServiceProvider = testAgencyNormal(),
        infographic = null,
        probability = null,
        weatherConcerns = null,
        failreason = null,
        hashtag = null,
        rocket = testRocketNormal(),
        mission = testMission(),
        pad = testPadDetailed(),
        program = emptyList(),
        orbitalLaunchAttemptCount = null,
        locationLaunchAttemptCount = null,
        padLaunchAttemptCount = null,
        agencyLaunchAttemptCount = null,
        orbitalLaunchAttemptCountYear = null,
        locationLaunchAttemptCountYear = null,
        padLaunchAttemptCountYear = null,
        agencyLaunchAttemptCountYear = null,
        name = "Cached | Launch $id",
        net = net,
        webcastLive = false
    )

    private fun newRepository(
        writer: CapturingWriter,
        engine: MockEngine,
    ): LLLaunchRepositoryImpl = run {
        // The repository captures its logger at construction, so install the writer first.
        SpaceLogger.initialize(LogConfig(Severity.Verbose, listOf(writer)))
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SpaceLaunchDatabase.Schema.create(driver)
        val preferences = AppPreferences(InMemoryPreferencesDataStore())
        val local = LaunchLocalDataSource(SpaceLaunchDatabase(driver), preferences)
        kotlinx.coroutines.runBlocking {
            local.cacheNormalLaunches(
                listOf(
                    // Upcoming (net in the future) feeds the featured stale query.
                    cachedLaunch("cached-upcoming", Clock.System.now() + 2.hours, statusId = 1),
                    // status_id 6 is "In Flight" and feeds the in-flight stale query.
                    cachedLaunch("cached-in-flight", Clock.System.now() - 1.hours, statusId = 6)
                )
            )
        }
        LLLaunchRepositoryImpl(
            launchesApi = LaunchesApi(baseUrl = "https://ll.test", httpClientEngine = engine),
            appPreferences = preferences,
            localDataSource = local
        )
    }

    private fun errorPageEngine() = MockEngine {
        respond(
            content = "<html><body>503 Service Unavailable</body></html>",
            status = HttpStatusCode.ServiceUnavailable,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Html.toString())
        )
    }

    /** A 200 whose JSON body is missing the required fields, i.e. the MissingFieldException path. */
    private fun malformedBodyEngine() = MockEngine {
        respond(
            content = """{"detail":"unexpected shape"}""",
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        )
    }

    private fun assertNoTransformationFailure(writer: CapturingWriter) {
        assertFalse(
            writer.entries.any { it.throwable is NoTransformationFoundException },
            "A non-2xx response must never reach body(): ${writer.entries.map { it.throwable }}"
        )
    }

    @Test
    fun featuredLaunchServesStaleCacheWhenLl2ReturnsNonSuccess() = runTest {
        val writer = CapturingWriter()
        val repo = newRepository(writer, errorPageEngine())

        val result = repo.getFeaturedLaunchDomain(
            forceRefresh = true,
            agencyIds = null,
            locationIds = null
        )

        assertTrue(result.isSuccess, "Expected stale cache, got ${result.exceptionOrNull()}")
        assertEquals(DataSource.STALE_CACHE, result.getOrThrow().source)
        assertTrue(result.getOrThrow().data.results.isNotEmpty())
        assertNoTransformationFailure(writer)
    }

    @Test
    fun inFlightLaunchesServeStaleCacheWhenLl2ReturnsNonSuccess() = runTest {
        val writer = CapturingWriter()
        val repo = newRepository(writer, errorPageEngine())

        val result = repo.getInFlightLaunchesDomain(
            forceRefresh = true,
            agencyIds = null,
            locationIds = null
        )

        assertTrue(result.isSuccess, "Expected stale cache, got ${result.exceptionOrNull()}")
        assertEquals(DataSource.STALE_CACHE, result.getOrThrow().source)
        assertTrue(result.getOrThrow().data.results.isNotEmpty())
        assertNoTransformationFailure(writer)
    }

    @Test
    fun featuredLaunchServesStaleCacheWhenA200FailsToDeserialize() = runTest {
        val writer = CapturingWriter()
        val repo = newRepository(writer, malformedBodyEngine())

        val result = repo.getFeaturedLaunchDomain(
            forceRefresh = true,
            agencyIds = null,
            locationIds = null
        )

        assertTrue(result.isSuccess, "Expected stale cache, got ${result.exceptionOrNull()}")
        assertEquals(DataSource.STALE_CACHE, result.getOrThrow().source)
        assertTrue(result.getOrThrow().data.results.isNotEmpty())
    }

    @Test
    fun inFlightLaunchesServeStaleCacheWhenA200FailsToDeserialize() = runTest {
        val writer = CapturingWriter()
        val repo = newRepository(writer, malformedBodyEngine())

        val result = repo.getInFlightLaunchesDomain(
            forceRefresh = true,
            agencyIds = null,
            locationIds = null
        )

        assertTrue(result.isSuccess, "Expected stale cache, got ${result.exceptionOrNull()}")
        assertEquals(DataSource.STALE_CACHE, result.getOrThrow().source)
        assertTrue(result.getOrThrow().data.results.isNotEmpty())
    }
}

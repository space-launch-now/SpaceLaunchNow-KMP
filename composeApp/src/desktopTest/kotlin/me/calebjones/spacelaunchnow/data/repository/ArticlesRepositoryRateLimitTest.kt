package me.calebjones.spacelaunchnow.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.api.snapi.apis.ArticlesApi
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.database.ArticleLocalDataSource
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import me.calebjones.spacelaunchnow.ui.viewmodel.InMemoryPreferencesDataStore
import me.calebjones.spacelaunchnow.util.logging.LogConfig
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ArticlesRepositoryRateLimitTest {

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

    private fun newRepository(
        writer: CapturingWriter,
        seedCache: Boolean,
    ): ArticlesRepositoryImpl = run {
        // The repository captures its logger at construction, so install the writer first.
        SpaceLogger.initialize(LogConfig(Severity.Verbose, listOf(writer)))
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SpaceLaunchDatabase.Schema.create(driver)
        val local = ArticleLocalDataSource(
            SpaceLaunchDatabase(driver),
            AppPreferences(InMemoryPreferencesDataStore())
        )
        if (seedCache) {
            kotlinx.coroutines.runBlocking { local.cacheArticles(articlePage(1, 3, next = null).results) }
        }
        val api = ArticlesApi(
            baseUrl = "https://snapi.test",
            httpClientEngine = MockEngine { respond("", HttpStatusCode.TooManyRequests) }
        )
        ArticlesRepositoryImpl(api, local)
    }

    @Test
    fun rateLimitWithWarmCacheServesStaleCacheWithoutErrorLog() = runTest {
        val writer = CapturingWriter()
        val repo = newRepository(writer, seedCache = true)

        val result = repo.getArticles(limit = 10, forceRefresh = true)

        assertTrue(result.isSuccess)
        assertEquals(DataSource.STALE_CACHE, result.getOrThrow().source)
        assertFalse(
            writer.entries.any { it.severity >= Severity.Error },
            "A cache-rescued 429 must not log at Error: ${writer.entries}"
        )
        val warn = writer.entries.single { it.severity == Severity.Warn }
        assertTrue("429" in warn.message, "Warn breadcrumb should carry the HTTP status: ${warn.message}")
    }

    @Test
    fun rateLimitWithEmptyCacheFailsAndLogsAtError() = runTest {
        val writer = CapturingWriter()
        val repo = newRepository(writer, seedCache = false)

        val result = repo.getArticles(limit = 10, forceRefresh = true)

        assertTrue(result.isFailure)
        assertIs<ResponseException>(result.exceptionOrNull())
        assertTrue(writer.entries.any { it.severity == Severity.Error && it.throwable is ResponseException })
    }
}

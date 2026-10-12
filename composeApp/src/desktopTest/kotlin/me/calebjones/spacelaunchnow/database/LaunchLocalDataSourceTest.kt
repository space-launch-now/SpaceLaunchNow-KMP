package me.calebjones.spacelaunchnow.database

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.LaunchStatus
import me.calebjones.spacelaunchnow.domain.model.Provider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Covers the cache-invalidation contract described in ADR-0004
 * (docs/architecture/adr/0004-cache-schema-versioning.md) and the phase5-launch unit report:
 * a row that predates the domain-model cache migration (LL-shaped JSON), a corrupt blob, or a
 * schema-version mismatch must all be treated as a cache miss (null / filtered out) on read,
 * never thrown from [LaunchLocalDataSource].
 */
class LaunchLocalDataSourceTest {

    private class InMemoryPreferencesDataStore(
        initial: Preferences = emptyPreferences()
    ) : DataStore<Preferences> {
        private val state = MutableStateFlow(initial)
        override val data: Flow<Preferences> = state
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val updated = transform(state.value)
            state.value = updated
            return updated
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    private fun newDataSource(): Pair<LaunchLocalDataSource, SpaceLaunchDatabase> {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SpaceLaunchDatabase.Schema.create(driver)
        val database = SpaceLaunchDatabase(driver)
        return LaunchLocalDataSource(database, AppPreferences(InMemoryPreferencesDataStore())) to database
    }

    private fun sampleLaunch(id: String = "launch-1", name: String = "Falcon 9 Test") = Launch(
        id = id,
        name = name,
        slug = id,
        net = null,
        windowStart = null,
        windowEnd = null,
        lastUpdated = null,
        status = LaunchStatus(id = 1, name = "Go", abbrev = null, description = null),
        provider = Provider(
            id = 121,
            name = "SpaceX",
            abbrev = "SpX",
            type = "Commercial",
            countryCode = "US",
            logoUrl = null,
            socialLogo = null,
            imageUrl = null
        ),
        imageUrl = null,
        thumbnailUrl = null,
        infographic = null,
        netPrecision = null
    )

    /** Directly inserts a raw row, bypassing [LaunchLocalDataSource]'s own encode path. */
    private fun SpaceLaunchDatabase.insertRawDetailedRow(id: String, jsonData: String, now: Long) {
        launchQueries.insertOrReplaceDetailed(
            id = id,
            name = "raw",
            status_id = null,
            status_name = null,
            net = null,
            window_end = null,
            window_start = null,
            launch_service_provider_id = null,
            launch_service_provider_name = null,
            rocket_configuration_id = null,
            rocket_configuration_name = null,
            pad_name = null,
            location_name = null,
            image_url = null,
            mission_name = null,
            mission_description = null,
            json_data = jsonData,
            cached_at = now,
            expires_at = now + 600_000L
        )
    }

    @Test
    fun cachedDomainLaunch_roundTripsThroughDetailedCache() = runTest {
        val (dataSource, _) = newDataSource()
        val launch = sampleLaunch()

        dataSource.cacheDetailedLaunch(launch)

        assertEquals(launch, dataSource.getDetailedLaunch(launch.id))
        assertEquals(launch, dataSource.getDetailedLaunchStale(launch.id))
    }

    /** Real wall-clock "now", so the freshly-inserted row is not already TTL-expired -
     * without this, [LaunchLocalDataSource.getDetailedLaunch]'s TTL filter alone would
     * explain a null result, masking whether the decode-miss path was actually exercised. */
    private fun realNowMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()

    @Test
    fun legacyLlShapedBlob_isTreatedAsCacheMiss() = runTest {
        val (dataSource, database) = newDataSource()
        val now = realNowMillis()
        // Shape of what the pre-phase5-launch cache stored: a flat LL LaunchDetailed-ish
        // object, not the {schemaVersion, launch} envelope this cache now expects. None of
        // these keys line up with CachedLaunchEnvelope, so decoding must fail cleanly.
        val legacyJson = """
            {"id":"legacy-1","name":"Legacy Launch","net":"2020-01-01T00:00:00Z",
             "status":{"id":1,"name":"Go"},"launch_service_provider":{"id":121,"name":"SpaceX"}}
        """.trimIndent()
        database.insertRawDetailedRow("legacy-1", legacyJson, now)

        assertNull(dataSource.getDetailedLaunch("legacy-1"))
        assertNull(dataSource.getDetailedLaunchStale("legacy-1"))
    }

    @Test
    fun corruptBlob_isTreatedAsCacheMiss() = runTest {
        val (dataSource, database) = newDataSource()
        val now = realNowMillis()
        database.insertRawDetailedRow("corrupt-1", "{not valid json at all {{{", now)

        assertNull(dataSource.getDetailedLaunch("corrupt-1"))
        assertNull(dataSource.getDetailedLaunchStale("corrupt-1"))
    }

    @Test
    fun schemaVersionMismatch_isTreatedAsCacheMiss() = runTest {
        val (dataSource, database) = newDataSource()
        val now = realNowMillis()
        val futureVersionJson = json.encodeToString(
            CachedLaunchEnvelope(schemaVersion = CacheSchemaVersion.LAUNCH + 1, launch = sampleLaunch("future-1"))
        )
        database.insertRawDetailedRow("future-1", futureVersionJson, now)

        assertNull(dataSource.getDetailedLaunch("future-1"))
        assertNull(dataSource.getDetailedLaunchStale("future-1"))
    }
}

package me.calebjones.spacelaunchnow.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.calebjones.spacelaunchnow.api.terminus.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.RocketDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.TimelineEvent
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.domain.mapper.terminus.TerminusLaunchDetail
import me.calebjones.spacelaunchnow.ui.viewmodel.InMemoryPreferencesDataStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * The Terminus detail blob in the shared detailed-launch table (Tasks#1098). The blob is flat:
 * `cache_schema_version`, `backend`, `launch`, `rocket`, `timeline`, in that order. The golden
 * file is the blob the writer produced before the envelope class was removed, so the test fails
 * if a refactor changes a single byte of what is stored.
 */
class LaunchLocalDataSourceTerminusEnvelopeTest {

    private fun resource(name: String): String =
        checkNotNull(javaClass.getResourceAsStream(name)) { "missing $name" }
            .bufferedReader().use { it.readText() }

    private val json = Json { ignoreUnknownKeys = true }
    private val launchJson = resource("/terminus/launch_detail.json")
    private val launchObject = Json.parseToJsonElement(launchJson).jsonObject
    private val id = launchObject.getValue("id").jsonPrimitive.content

    private val timeline = json.decodeFromString<List<TimelineEvent>>(
        """[
            {"type":{"id":1,"abbrev":"Prelaunch","description":"Prelaunch"},"relative_time":"-PT10M"},
            {"type":{"id":2,"abbrev":"Liftoff","description":"Liftoff"},"relative_time":"PT0S"}
        ]"""
    )

    private val detail = TerminusLaunchDetail(
        launch = json.decodeFromString<LaunchDetailed>(launchJson),
        rocket = json.decodeFromString<RocketDetailed>(launchObject.getValue("rocket").toString()),
        timeline = timeline
    )

    private val database = SpaceLaunchDatabase(
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { SpaceLaunchDatabase.Schema.create(it) }
    )
    private val local = LaunchLocalDataSource(database, AppPreferences(InMemoryPreferencesDataStore()))

    private fun storedBlob(): String =
        database.launchQueries.getDetailedByIdStale(id).executeAsOne().json_data

    @Test
    fun writerProducesTheGoldenBlob() = runTest {
        local.cacheTerminusDetailedLaunch(detail)
        assertEquals(resource("/terminus/launch_detail_cache_v2_golden.json"), storedBlob())
    }

    @Test
    fun goldenBlobDecodesToTheSameDetail() = runTest {
        local.cacheTerminusDetailedLaunch(detail) // the row and its columns; the blob is replaced below
        replaceBlob(resource("/terminus/launch_detail_cache_v2_golden.json"))
        assertEquals(detail, local.getTerminusDetailedLaunch(id))
        assertEquals(detail, local.getTerminusDetailedLaunchStale(id))
    }

    @Test
    fun schemaOneBlobIsAMiss() = runTest {
        assertMiss(resource("/terminus/launch_detail_cache_v2_golden.json").replaceFirst(
            "\"cache_schema_version\":2", "\"cache_schema_version\":1"
        ))
    }

    @Test
    fun wrongBackendBlobIsAMiss() = runTest {
        assertMiss(resource("/terminus/launch_detail_cache_v2_golden.json").replaceFirst(
            "\"backend\":\"terminus\"", "\"backend\":\"other\""
        ))
    }

    @Test
    fun legacyLaunchLibraryBlobIsAMiss() = runTest {
        assertMiss(launchJson)
    }

    @Test
    fun undecodableBlobIsAMiss() = runTest {
        assertMiss("not json")
    }

    private suspend fun assertMiss(blob: String) {
        local.cacheTerminusDetailedLaunch(detail)
        replaceBlob(blob)
        assertNull(local.getTerminusDetailedLaunch(id))
        assertNull(local.getTerminusDetailedLaunchStale(id))
    }

    private fun replaceBlob(blob: String) {
        val row = database.launchQueries.getDetailedByIdStale(id).executeAsOne()
        assertNotNull(row)
        database.launchQueries.insertOrReplaceDetailed(
            id = row.id, name = row.name, status_id = row.status_id, status_name = row.status_name,
            net = row.net, window_end = row.window_end, window_start = row.window_start,
            launch_service_provider_id = row.launch_service_provider_id,
            launch_service_provider_name = row.launch_service_provider_name,
            rocket_configuration_id = row.rocket_configuration_id,
            rocket_configuration_name = row.rocket_configuration_name,
            pad_name = row.pad_name, location_name = row.location_name, image_url = row.image_url,
            mission_name = row.mission_name, mission_description = row.mission_description,
            json_data = blob, cached_at = row.cached_at, expires_at = row.expires_at
        )
    }
}

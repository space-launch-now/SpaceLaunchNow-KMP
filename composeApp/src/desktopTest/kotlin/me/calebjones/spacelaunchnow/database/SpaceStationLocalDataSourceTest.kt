package me.calebjones.spacelaunchnow.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.runBlocking
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.domain.model.ExpeditionDetailItem
import me.calebjones.spacelaunchnow.domain.model.SpaceStationDetail
import me.calebjones.spacelaunchnow.ui.viewmodel.InMemoryPreferencesDataStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SpaceStationLocalDataSourceTest {

    private fun newDatabase(): SpaceLaunchDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SpaceLaunchDatabase.Schema.create(driver)
        return SpaceLaunchDatabase(driver)
    }

    private fun newDataSource(database: SpaceLaunchDatabase) =
        SpaceStationLocalDataSource(database, AppPreferences(InMemoryPreferencesDataStore()))

    private fun sampleStation() = SpaceStationDetail(
        id = 4,
        name = "ISS",
        imageUrl = "https://example.com/iss.png",
        statusName = "Active",
        statusId = 1,
        founded = null,
        deorbited = null,
        description = "International Space Station",
        orbit = "LEO",
        typeName = "Space Station",
        owners = emptyList(),
        activeExpeditions = emptyList(),
        dockingLocations = emptyList(),
        height = null,
        width = null,
        mass = null,
        volume = null,
        onboardCrew = 7,
        dockedVehicles = 3
    )

    private fun sampleExpedition(id: Int) = ExpeditionDetailItem(
        id = id,
        name = "Expedition $id",
        start = null,
        end = null,
        crew = emptyList(),
        missionPatches = emptyList(),
        spacewalks = emptyList()
    )

    @Test
    fun cacheSpaceStationRoundTripsDomainModel() = runBlocking {
        val database = newDatabase()
        val dataSource = newDataSource(database)
        val station = sampleStation()

        dataSource.cacheSpaceStation(station)

        assertEquals(station, dataSource.getSpaceStation(4))
    }

    @Test
    fun cacheExpeditionsRoundTripsDomainModel() = runBlocking {
        val database = newDatabase()
        val dataSource = newDataSource(database)
        val expeditions = listOf(sampleExpedition(70), sampleExpedition(71))

        dataSource.cacheExpeditions(expeditions, stationId = 4)

        assertEquals(expeditions.toSet(), dataSource.getExpeditionsByStationId(4).toSet())
    }

    @Test
    fun legacyShapedStationBlobIsTreatedAsCacheMissNotCrash() = runBlocking {
        val database = newDatabase()
        val dataSource = newDataSource(database)
        val now = 0L

        // Write a row directly via the generated queries object (bypassing cacheSpaceStation's
        // domain serializer). The json_data blob's "id" is a string, which is the shape a
        // pre-migration LL-typed cache entry could leave behind - it fails to decode as the
        // domain SpaceStationDetail (id: Int), and must be treated as a cache miss rather than
        // crashing.
        database.spaceStationQueries.insertOrReplaceSpaceStation(
            id = 4L,
            name = "Legacy Station",
            description = null,
            orbit = null,
            founded = null,
            image_url = null,
            onboard_crew = null,
            docked_vehicles = null,
            json_data = """{"id":"not-an-int","name":"Legacy Station"}""",
            cached_at = now,
            expires_at = now + 100_000L
        )

        assertNull(dataSource.getSpaceStation(4))
        assertNull(dataSource.getSpaceStationStale(4))
    }

    @Test
    fun legacyShapedExpeditionBlobIsTreatedAsCacheMissNotCrash() = runBlocking {
        val database = newDatabase()
        val dataSource = newDataSource(database)
        val now = 0L

        // Write a row directly via the generated queries object (bypassing cacheExpedition's
        // domain serializer). The json_data blob's "id" is a string, which is the shape a
        // pre-migration LL-typed cache entry could leave behind - it fails to decode as the
        // domain ExpeditionDetailItem (id: Int), and must be treated as a cache miss rather
        // than crashing.
        database.spaceStationQueries.insertOrReplaceExpedition(
            id = 70L,
            station_id = 4L,
            name = "Legacy Expedition",
            start_date = null,
            end_date = null,
            crew_count = 0L,
            json_data = """{"id":"not-an-int","name":"Legacy Expedition"}""",
            cached_at = now,
            expires_at = now + 100_000L
        )

        assertTrue(dataSource.getExpeditionsByStationId(4).isEmpty())
        assertTrue(dataSource.getExpeditionsByStationIdStale(4).isEmpty())
    }
}
